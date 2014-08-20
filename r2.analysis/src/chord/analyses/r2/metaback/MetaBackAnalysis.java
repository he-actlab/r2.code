package chord.analyses.r2.metaback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.Inst;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operand.AConstOperand;
import joeq.Compiler.Quad.Operand.ParamListOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operand.TypeOperand;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Operator.ALength;
import joeq.Compiler.Quad.Operator.ALoad;
import joeq.Compiler.Quad.Operator.AStore;
import joeq.Compiler.Quad.Operator.Binary;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.Getstatic;
import joeq.Compiler.Quad.Operator.IntIfCmp;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.LookupSwitch;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.MultiNewArray;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.Putfield;
import joeq.Compiler.Quad.Operator.Putstatic;
import joeq.Compiler.Quad.Operator.Return;
import joeq.Compiler.Quad.Operator.Unary;
import joeq.Compiler.Quad.Operator.Return.THROW_A;
import joeq.Compiler.Quad.Operator.TableSwitch;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.RegisterFactory;
import joeq.Compiler.Quad.RegisterFactory.Register;
import chord.project.analyses.rhs.BackTraceIterator;
import chord.project.analyses.rhs.IWrappedPE;
import chord.project.analyses.rhs.TimeoutException;
import chord.util.Alarm;
import chord.util.ArraySet;
import chord.util.Timer;
import chord.util.dnf.Clause;
import chord.util.dnf.ClauseSizeCMP;
import chord.util.dnf.DNF;
import chord.util.dnf.Domain;
import chord.util.dnf.Variable;
import chord.util.tuple.object.Pair;

/**
 * The meta-backward analysis of R2
 * 
 * @author xin
 * 
 */
public class MetaBackAnalysis {
	private BackTraceIterator<Edge, Edge> backIter;
	private DNF errSuf;  // The sufficient condition of error
	private DNF nc;  // The necessary condition for proof
	private int retIdx;  // Used to handle return instructions
	private IWrappedPE<Edge, Edge> pre;  // Previous edge when going backward
	private BackQuadVisitor qv;  // The backward visitor, containing backward transfer functions.
	private Stack<IWrappedPE<Edge, Edge>> callStack;
	private ExpAbstraction abs;  // The abstraction for forward analysis
	private DNF preSuf;  // previous error sufficient condition, used for debugging
	private IWrappedPE<Edge, Edge> queryWPE;
	
	private static boolean DEBUG;
	private static boolean optimizeSumms;
	private static int errSufSize;  // Num of disjuncts to keep in error sufficient condition
	private static int timeout;
	private static boolean dnegation=true;
	private static boolean prune=true;
	private Alarm alarm;
	private Timer timer;
	
	public MetaBackAnalysis(DNF errSuf,
			BackTraceIterator<Edge, Edge> backIter, ExpAbstraction abs) {
		this.backIter = backIter;
		this.errSuf = errSuf;
		this.pre = null;
		this.qv = new BackQuadVisitor();
		this.callStack = new Stack<IWrappedPE<Edge, Edge>>();
		this.abs = abs;
		this.queryWPE = backIter.next();
	}

	private void checkTimeout(){
		if (timeout > 0 && alarm.isTimedOut()) {
            System.out.println("TIMED OUT");
            alarm.cancel();
            printRunTime(true);
            throw new TimeoutException();
        }
	}
	
	private void printRunTime(boolean isTimeOut){
        timer.done();
        long inclusiveTime = timer.getInclusiveTime();
        int queryIndex = SharedData.domP.indexOf(queryWPE.getInst());
        System.out.println((isTimeOut?"TIMED OUT ":"")+"BackwardTime: "+queryIndex+" "+inclusiveTime);
		System.out.println(Timer.getTimeStr(inclusiveTime));
	}
	
	public DNF run() throws TimeoutException{
		if (optimizeSumms == true)
			throw new RuntimeException("Currently the metaback analysis doesn't support optimized summaries");
		timer = new Timer("meta-back-timer");
		timer.init();
		if (timeout > 0) {
            alarm = new Alarm(timeout);
        }
		System.out.println("**************");
		System.out.println(queryWPE.getInst().toVerboseStr());
		checkInvoke(queryWPE);
		while (!isFixed(errSuf) && backIter.hasNext()) {
			checkTimeout();
			IWrappedPE<Edge, Edge> wpe = backIter.next();
			Inst inst = wpe.getInst();
			if (inst instanceof BasicBlock) {
				BasicBlock bb = (BasicBlock) inst;
				if (bb.isEntry() || bb.isExit()) {
					if (DEBUG) {
						System.out.println(wpe);
					}
					if (bb.isExit()) {
						preSuf = errSuf;
						errSuf = handleExit(errSuf);
					}
					pre = wpe;
					continue;
				} else
					assert (bb.size() == 0);
			}
			preSuf = errSuf;
			errSuf = backTransfer(errSuf, wpe.getInst());
			if (DEBUG) {
				System.out.println(wpe.getInst());
				System.out.println("After trans: " + errSuf);
			}
			//An optimization, if errSuf remains unchanged, no need to do double negation
			if(dnegation&&!errSuf.equals(preSuf)&&errSuf.size()>errSufSize)
				errSuf = negate(negate(errSuf));
			if(DEBUG){
				System.out.println("After double negation: "+errSuf);
			}
			Clause fwdState = encodePathEdge(wpe, errSuf);
			if (DEBUG) {
				System.out.println("Forward state: " + fwdState);
				System.out.println(wpe.getPE());
			}
			if(prune)
				errSuf = errSuf.prune(errSufSize, fwdState);  // Drop clauses to prevent blowing up
			if (DEBUG)
				System.out.println("After prune:" + errSuf);
			pre = wpe;
			checkInvoke(wpe);
		}
		if (errSuf.isFalse()) {
			dump();
			throw new RuntimeException("Something wrong with meta back!");
			// nc = DNF.getTrue();
		} else if (errSuf.isTrue())
			nc = DNF.getFalse(new ApproxNumCMP());
		else {
			errSuf = chopNonParameter(errSuf);
			nc= negate(errSuf);
		}
		System.out.println("NC: " + nc.toString());
		printRunTime(false);
		if (timeout > 0)
	         alarm.cancel();
		return nc;
	}

	private DNF handleExit(DNF dnf){
		DNF ret = new DNF(new ClauseSizeCMP(), false);
		Quad invoke = (Quad)pre.getWPE().getInst();
		Register tgtRetReg = (Invoke.getDest(invoke) != null) ? Invoke.getDest(invoke)
				.getRegister() : null;
		int tgtRetIdx = SharedData.domU.indexOf(tgtRetReg);
		for(Clause c : dnf.getClauses()){
			Clause nc = new Clause(true);
			for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
				Variable v = predicate.getKey();
				Domain d = predicate.getValue();
				VVariable vv = null;
				if(v instanceof VVariable){
					vv = (VVariable)v;
					if(vv.getIdx() != tgtRetIdx)
						vv = null;
				}
				if(vv != null){
					jq_Method m = Invoke.getMethod(invoke).getMethod();
					// jspark: put APVariable for precise so that the analysis ignores their return values
					if (SharedData.isRelaxMethod(m) ||
						SharedData.isRelaxAllMethod(m) ||
						SharedData.isRestrictMethod(m) ||
						SharedData.isRestrictAllMethod(m))
//					if (SharedData.isPreciseMethod(m) ||
//							SharedData.isPreciseAllMethod(m))
						nc.addLiteral(R2Variable.singleton, d);
					else
						nc.addLiteral(RVariable.singleton, d);
				}else{
					nc.addLiteral(v, d);
				}
			}
			ret.addClause(nc);
		}
		ret = increaseContext(ret);  // Adjust the context level of each variable
		return ret;
	}
	
	private DNF negate(DNF dnf){
		if(dnf.isTrue())
			return DNF.getFalse(dnf.getCMP());
		if(dnf.isFalse())
			return DNF.getTrue(dnf.getCMP());
		DNF nDNF = new DNF(dnf.getCMP(), true);
		for (Clause c : dnf.getClauses()) {
			// create a false DNF, since we're going to do join
			DNF cDNF = new DNF(dnf.getCMP(), false);
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				checkTimeout();
				Domain d = entry.getValue();
				for (Domain nd : d.space()) {
					if (nd.equals(d))
						continue;
					DNF nhDNF = new DNF(dnf.getCMP(), entry.getKey(), nd);
					cDNF = cDNF.join(nhDNF);
				}

			}
			nDNF = nDNF.intersect(cDNF);
		}
		return nDNF;
	}
	
	private void dump() {
		System.out.println("=====================dump out current state======================");
		System.out.println(preSuf.toString());
		System.out.println(pre.getInst());
		System.out.println(pre);
		System.out.println("====dump out the stack====");
		for (int j = callStack.size() - 1; j >= 0; j--) {
			IWrappedPE<Edge, Edge> wpe = callStack.get(j);
			System.out.println(wpe);
		}
		throw new RuntimeException();
	}

	private DNF increaseContext(DNF dnf) {
		DNF nDNF = new DNF(dnf.getCMP(), false);
		for (Clause c : dnf.getClauses()) {
			Clause tnc = new Clause(true);
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				if (entry.getKey() instanceof VVariable) {
					VVariable tv = (VVariable) entry.getKey();
					tnc.addLiteral(tv.getIncreased(), entry.getValue());
				} else
					tnc.addLiteral(entry.getKey(), entry.getValue());
			}
			nDNF.addClause(tnc);
		}
		return nDNF;
	}

	private DNF decreaseContext(DNF dnf) {
		DNF nDNF = new DNF(dnf.getCMP(), false);
		for (Clause c : dnf.getClauses()) {
			Clause tnc = new Clause(true);
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				if (entry.getKey() instanceof VVariable) {
					VVariable tv = (VVariable) entry.getKey();
					tnc.addLiteral(tv.getDecreased(), entry.getValue());
				} else
					tnc.addLiteral(entry.getKey(), entry.getValue());
			}
			nDNF.addClause(tnc);
		}
		return nDNF;

	}

	private DNF killDeadConstraints(DNF dnf) {
		DNF nDNF = new DNF(errSuf.getCMP(), false);
		OUT: for (Clause c : dnf.getClauses()) {
			Clause tnc = new Clause(true);
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				if (entry.getKey() instanceof VVariable) {
					VVariable vv = (VVariable) entry.getKey();
					if (vv.getContext() < 0){
						if(entry.getValue() == BoolDomain.T) {
							continue OUT;
						}
						else continue;
					}
					else
						tnc.addLiteral(entry.getKey(), entry.getValue());
				} else
					tnc.addLiteral(entry.getKey(), entry.getValue());
			}
			nDNF.addClause(tnc);
		}
		return nDNF;
	}

	private DNF chopNonParameter(DNF dnf) {
		if (dnf.isFalse() || dnf.isTrue())
			return dnf;
		DNF ret = new DNF(dnf.getCMP(), false);
		OUT: for (Clause c : dnf.getClauses()) {
			Clause rc = new Clause();
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				//If we allow to track all superfluous cases, there would be trouble
				if((!(entry.getKey() instanceof OVariable) && !(entry.getKey() instanceof STVariable)))
					if(BoolDomain.T == entry.getValue())
						continue OUT;
					else
						continue;
				rc.addLiteral(entry.getKey(), entry.getValue());
			}
			ret.addClause(rc);
		}
		return ret;
	}

	/**
	 * Check whether current instruction is an instruction after a method invoke
	 * 
	 * @param wpe
	 */
	private void checkInvoke(IWrappedPE<Edge, Edge> wpe) {
		if (wpe.getWSE() != null) {  // if wse!=null, wpe is a path edge
			IWrappedPE<Edge,Edge> callWPE = wpe.getWPE();
			Inst inst = callWPE.getInst();
			Quad invoke = (Quad)inst;
			if (SharedData.isRelaxMethod(Invoke.getMethod(invoke).getMethod())) {
				Set<Integer> relaxedVs = new ArraySet<Integer>();
				ParamListOperand paramList = Invoke.getParamList(invoke);
				for (int i = 0; i < paramList.length(); i++) {
					RegisterOperand ro = paramList.get(i);
					Register r = ro.getRegister();
					int rIdx = SharedData.domU.indexOf(r);
					relaxedVs.add(rIdx);
				}
				DNF nErrSuf = new DNF(new ClauseSizeCMP(),false);
				OUT: for(Clause c : errSuf.getClauses()){
					Clause nc = new Clause(true);
					for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
						Variable v = predicate.getKey();
						Domain d = predicate.getValue();
						VVariable vv = null;
						if(v instanceof VVariable){
							vv =  (VVariable)v;
							if(!relaxedVs.contains(vv.getIdx()))
								vv = null;
						}
						if(vv != null){
							if(d.equals(BoolDomain.T))
								continue OUT;
							else
								continue;
						}
						else{
							nc.addLiteral(v, d);
						}
					}
					nErrSuf.addClause(nc);
				}
				this.preSuf = errSuf;
				errSuf = nErrSuf;
			}//end of isRelaxedMethod if
			
			//***********************
			// relax_all_FIELDX_TAGY
			//***********************
			if (SharedData.isRelaxAllMethod(Invoke.getMethod(invoke).getMethod())) {
				Set<Integer> relaxedVs = new ArraySet<Integer>();
				Set<Pair<Integer,Integer>> relaxedFs = new ArraySet<Pair<Integer,Integer>>();
				Set<Integer> relaxedGs = new ArraySet<Integer>();
				ParamListOperand paramList = Invoke.getParamList(invoke);
				if (paramList.length() == 0) {	// global
					int gidx = SharedData.getFieldIdx(Invoke.getMethod(invoke).getMethod());
					relaxedGs.add(gidx);
				} else if (paramList.length() > 0) { // object.field or arrays
					Register r = paramList.get(paramList.length()-1).getRegister();
					if (!r.getType().isPrimitiveType()) { 
			            Set<Quad> allocSites = SharedData.cipa.pointsTo(r).pts;
			            for (Quad h: allocSites) {
			              if(SharedData.R2_LOG)
			            	  System.out.println("*** R2_LOG: [Backward] relax_all - allocation site = " + h.toString());
			        	  String tag = SharedData.getTag(Invoke.getMethod(invoke).getMethod());
			        	  if (!tag.equalsIgnoreCase(SharedData.quadToTagMap.get(h))){
			        		  if(SharedData.R2_LOG)
			        			  System.out.println("*** R2_LOG: [Backward] relax_all - Tag unmatched!");
			        		  continue;
			        	  }
			              int hidx = SharedData.domH.indexOf(h);
			              int fidx = SharedData.getFieldIdx(h, Invoke.getMethod(invoke).getMethod());
			              Pair<Integer,Integer> hf = new Pair<Integer,Integer>(hidx,fidx);
			              if (SharedData.R2_LOG)
			        		System.out.println("*** R2_LOG: [Backward] relax_all - relaxed! array or object.field(" + hf.toString() + ")  (" + invoke.toString() + ")");
			              relaxedFs.add(hf);
			            }
					} else 
						throw new RuntimeException("Error! relax_all shouldn't have a primitive-type parameter");
				} 
				DNF nErrSuf = new DNF(new ClauseSizeCMP(),false);
				OUT: for(Clause c : errSuf.getClauses()){
					Clause nc = new Clause(true);
					for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
						Variable v = predicate.getKey();
						Domain d = predicate.getValue();
						VVariable vv = null;
			            HFVariable hfv = null;
			            GVariable gv = null;
						if(v instanceof VVariable){
							vv =  (VVariable)v;
							if(!relaxedVs.contains(vv.getIdx()))
								vv = null;
							else{
								if (SharedData.R2_LOG)
									System.out.println("*** R2_LOG: [Backward] relax_all - REMOVED VVariable(" + vv.toString() + ")  (" + invoke.toString() + ")");
							}
						}
						else if(v instanceof HFVariable){
			            	hfv = (HFVariable)v;
			            	if(!relaxedFs.contains(new Pair<Integer,Integer>(hfv.getHIdx(),hfv.getFIdx())))
			            		hfv = null;
			            	else{
			            		if (SharedData.R2_LOG)
			            			System.out.println("*** R2_LOG: [Backward] relax_all - REMOVED HFVariable(" + hfv.toString() + ")  (" + invoke.toString() + ")");
			            	}
			            }
						else if(v instanceof GVariable){
			            	gv = (GVariable)v;
			            	if(!relaxedGs.contains(gv.getIdx()))
			            		gv = null;
			            	else{
			            		if (SharedData.R2_LOG)
			            			System.out.println("*** R2_LOG: [Backward] relax_all - REMOVED GVariable(" + gv.toString() + ")  (" + invoke.toString() + ")");
			            	}
			            }
						if(vv != null){
							if(d.equals(BoolDomain.T))
								continue OUT;
							else
								continue;
						}
						else if(hfv != null){
			              if(d.equals(BoolDomain.T))
			                continue OUT;
			              else
			                continue;
			            }
						else if(gv != null){
			              if(d.equals(BoolDomain.T))
			                continue OUT;
			              else
			                continue;
			            }
						else{
							nc.addLiteral(v, d);
						}
					}
					nErrSuf.addClause(nc);
				}
				this.preSuf = errSuf;
				errSuf = nErrSuf;
			}//end of isRelaxedAllMethod if
			callStack.push(wpe.getWPE());
		} 
	}
	
	private DNF checkPrecise(DNF in, Quad q){
		jq_Method ivkedMeth = Invoke.getMethod(q).getMethod();
		if(SharedData.isRestrictMethod(ivkedMeth)){ 		
			Set<Integer> preciseVs = new ArraySet<Integer>();
			ParamListOperand paramList = Invoke.getParamList(q);
			for (int i = 0; i < paramList.length(); i++) {
				RegisterOperand ro = paramList.get(i);
				Register r = ro.getRegister();
				int rIdx = SharedData.domU.indexOf(r);
				preciseVs.add(rIdx);
			}
			DNF nDNF = new DNF(new ClauseSizeCMP(),false);
			OUT: for(Clause c : in.getClauses()){
				DNF cDNF = new DNF(new ClauseSizeCMP(),true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					DNF dnf1 = new DNF(new ClauseSizeCMP(),v,d);
					if(v instanceof ErrVariable){
						if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
							for(Integer pvidx : preciseVs){
								DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.T);
								dnf1 = dnf1.join(dnf2);
							}
						}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
							for(Integer pvidx : preciseVs){
								DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.F);
								dnf1 = dnf1.intersect(dnf2);
							}
						}
					}else if(v instanceof VVariable){
						if(d == BoolDomain.T && preciseVs.contains(((VVariable) v).getIdx())){//tainted(p) -> false
							continue OUT;
						}
					}
					cDNF = cDNF.intersect(dnf1);
				}
				nDNF = nDNF.join(cDNF);
			}
			return nDNF;
		}
		
		//***********************
		// precise_all_FIELDX_TAGY
		//***********************
		if(SharedData.isRestrictAllMethod(ivkedMeth)){
			Set<Integer> preciseVs = new ArraySet<Integer>();
			Set<Pair<Integer,Integer>> preciseFs = new ArraySet<Pair<Integer,Integer>>();
			Set<Integer> preciseGs = new ArraySet<Integer>();
			ParamListOperand paramList = Invoke.getParamList(q);
			if (paramList.length() == 0){ // global
				int gidx = SharedData.getFieldIdx(ivkedMeth);
				preciseGs.add(gidx);
			} else if (paramList.length() > 0) { // object.field or arrays
				Register r = paramList.get(paramList.length()-1).getRegister();
				if (!r.getType().isPrimitiveType()) { 
					Set<Quad> allocSites = SharedData.cipa.pointsTo(r).pts;
					for (Quad h: allocSites) {
						if(SharedData.R2_LOG)
							System.out.println("*** R2_LOG: [Backward] precise_all - allocation site = " + h.toString());
						String tag = SharedData.getTag(ivkedMeth);
			        	if (!tag.equalsIgnoreCase(SharedData.quadToTagMap.get(h))){
			        		if(SharedData.R2_LOG)
			        			System.out.println("*** R2_LOG: [Backward] precise_all - Tag unmatched!");
			        		continue;
			        	}
						int hidx = SharedData.domH.indexOf(h);
						int fidx = SharedData.getFieldIdx(h, ivkedMeth);
						Pair<Integer,Integer> hf = new Pair<Integer,Integer>(hidx,fidx);
						if (SharedData.R2_LOG)
							System.out.println("*** R2_LOG: [Backward] precise_all - array or object.field(" + hf.toString() + ")  (" + q.toString() + ")");
						preciseFs.add(hf);
					}
				} else 
					throw new RuntimeException("Error! relax_all shouldn't have a primitive-type parameter");
			} 
			
			DNF nDNF = new DNF(new ClauseSizeCMP(),false);
			OUT: for(Clause c : in.getClauses()){
				DNF cDNF = new DNF(new ClauseSizeCMP(),true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					DNF dnf1 = new DNF(new ClauseSizeCMP(),v,d);
					if(v instanceof ErrVariable){
						if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)}
							for(Integer vidx : preciseVs){
								DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(vidx),BoolDomain.T);
			            		if (SharedData.R2_LOG)
			            			System.out.println("*** R2_LOG: [Backward] precise_all - ADDED VVariable(" + dnf2.toString() + ") (" + q.toString() + ")");
								dnf1 = dnf1.join(dnf2);
							}
							for(Pair<Integer,Integer> phfidx : preciseFs){
								DNF dnf2 = new DNF(new ClauseSizeCMP(),new HFVariable(phfidx.val0,phfidx.val1),BoolDomain.T);
			            		if (SharedData.R2_LOG)
			            			System.out.println("*** R2_LOG: [Backward] precise_all - ADDED HFVariable(" + dnf2.toString() + ") (" + q.toString() + ")");
				                dnf1 = dnf1.join(dnf2);
				            }
							for(Integer gidx : preciseGs){
								DNF dnf2 = new DNF(new ClauseSizeCMP(),new GVariable(gidx),BoolDomain.T);
			            		if (SharedData.R2_LOG)
			            			System.out.println("*** R2_LOG: [Backward] precise_all - ADDED GVariable(" + dnf2.toString() + ") (" + q.toString() + ")");
								dnf1 = dnf1.join(dnf2);
							}
						}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
							for(Integer vidx : preciseVs){
				            	DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(vidx),BoolDomain.F);
								dnf1 = dnf1.intersect(dnf2);
							}
				            for(Pair<Integer,Integer> phfidx : preciseFs){
				            	DNF dnf2 = new DNF(new ClauseSizeCMP(),new HFVariable(phfidx.val0,phfidx.val1),BoolDomain.F);
				            	dnf1 = dnf1.intersect(dnf2);
				            }
				            for(Integer gidx : preciseGs){
				            	DNF dnf2 = new DNF(new ClauseSizeCMP(),new GVariable(gidx),BoolDomain.F);
								dnf1 = dnf1.intersect(dnf2);
							}
						}
					} else if(v instanceof VVariable){
			        	if(d == BoolDomain.T && preciseVs.contains(((VVariable) v).getIdx())){
							continue OUT;
						}
			        } else if(v instanceof HFVariable){
						Pair<Integer,Integer> hf = new Pair<Integer,Integer>(((HFVariable)v).getHIdx(),((HFVariable)v).getFIdx());
			            if(d == BoolDomain.T && preciseFs.contains(hf)){
			            	continue OUT;
			            }
			        } else if(v instanceof GVariable){
			        	if(d == BoolDomain.T && preciseGs.contains(((GVariable) v).getIdx())){
							continue OUT;
						}
			        }
					cDNF = cDNF.intersect(dnf1);
				}
				nDNF = nDNF.join(cDNF);
			}
			return nDNF;
		}
		return in;
	}
	
	/**
	 * Get the weakest precondition of es over inst
	 * 
	 * @param es
	 * @param inst
	 * @return
	 */
	private DNF backTransfer(DNF es, Inst inst) {
		if (inst instanceof BasicBlock) {
			BasicBlock bb = (BasicBlock) inst;
			// bb might be entry, exit, or empty basic block
			assert (bb.size() == 0);
			return es;
		}
		qv.iDNF = es;
		qv.oDNF = es;
		Quad q = (Quad) inst;
		q.accept(qv);
		return qv.oDNF;
	}

	// dnf is fixed when it only contains clauses over allocation sites
	public boolean isFixed(DNF dnf) {
		if (dnf.isTrue() || dnf.isFalse())
			return true;
		for (Clause c : dnf.getClauses()) {
			for (Map.Entry<Variable, Domain> e : c.getLiterals().entrySet()) {
				if (!(e.getKey() instanceof OVariable))
					return false;
			}
		}
		return true;
	}

	// Here we encode the forward state lazily, only encode the part relative to
	// the backward DNF
	private Clause encodePathEdge(IWrappedPE<Edge, Edge> wpe, DNF dnf) {
		Clause ret = new Clause(true);
		AbstractState dNode = wpe.getPE().dstNode;
		if(dNode.isErr){
			ret.addLiteral(ErrVariable.EV, BoolDomain.T);
			return ret;
		}
		Set<VVariable> relV = new HashSet<VVariable>();
		Set<Integer> relG = new HashSet<Integer>();
		Set<Integer> relO = new HashSet<Integer>();
		boolean isRetRel = false;
		boolean isRelax = false; // jspark: for PVariable
		Set<Pair<Integer,Integer>> relHF = new HashSet<Pair<Integer,Integer>>();
		Set<Pair<Integer,Integer>> relST = new HashSet<Pair<Integer,Integer>>();
		for (Clause c : dnf.getClauses())
			for (Map.Entry<Variable, Domain> entry : c.getLiterals().entrySet()) {
				Variable v = entry.getKey();
				if(v instanceof RVariable)
					isRetRel = true;
				if(v instanceof R2Variable){
					isRelax = true;
				}
				if(v instanceof VVariable){
					VVariable vv = (VVariable)v;
					relV.add(vv);
				}
				if(v instanceof GVariable){
					GVariable gv = (GVariable)v;
					relG.add(gv.getIdx());
				}
				if(v instanceof HFVariable){
					HFVariable hfv = (HFVariable)v;
					relHF.add(new Pair<Integer,Integer>(hfv.getHIdx(),hfv.getFIdx()));
				}
				if(v instanceof OVariable){
					OVariable ov = (OVariable)v;
					relO.add(ov.getIdx());
				}
				if(v instanceof STVariable){
					STVariable stv = (STVariable)v;
					relST.add(new Pair<Integer,Integer>(stv.getHIdx(),stv.getFIdx()));
				}
			}
		if(isRetRel){//return value
			ret.addLiteral(RVariable.singleton, dNode.taintedRet?BoolDomain.T:BoolDomain.F);
		}
		if(isRelax){// jspark: this will be ignored
			ret.addLiteral(R2Variable.singleton, BoolDomain.T);
		}
		for(VVariable vv : relV){// local variables
			int cl = vv.getContext();
			AbstractState dNode1 = dNode;
			if(cl >0){
				int stackSize = callStack.size();					
				IWrappedPE<Edge,Edge> wpe1 = callStack.get(stackSize-cl);
				dNode1 = wpe1.getPE().dstNode;
			}
			if(dNode1.taintedVars.contains(vv.getIdx()))
				ret.addLiteral(new VVariable(vv.getIdx(),cl), BoolDomain.T);
			else
				ret.addLiteral(new VVariable(vv.getIdx(),cl), BoolDomain.F);
		}
		for(int i : relG){// global variables
			if(dNode.taintedGlobals.contains(i))
				ret.addLiteral(new GVariable(i), BoolDomain.T);
			else
				ret.addLiteral(new GVariable(i), BoolDomain.F);
		}
		for(int i : relO){// operations
			if(abs.approxStatements.contains(i))
				ret.addLiteral(new OVariable(i), BoolDomain.T);
			else
				ret.addLiteral(new OVariable(i), BoolDomain.F);
		}
		for(Pair<Integer,Integer> hf : relHF){
			if(dNode.taintedFields.contains(hf))
				ret.addLiteral(new HFVariable(hf.val0,hf.val1), BoolDomain.T);
			else
				ret.addLiteral(new HFVariable(hf.val0,hf.val1), BoolDomain.F);
		}
		for(Pair<Integer,Integer> st : relST){
			if(abs.approxStorage.contains(st))
				ret.addLiteral(new STVariable(st.val0,st.val1), BoolDomain.T);
			else
				ret.addLiteral(new STVariable(st.val0,st.val1), BoolDomain.F);
		}
		ret.addLiteral(ErrVariable.EV, BoolDomain.F); // !err
		return ret;
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public static void setDEBUG(boolean dEBUG) {
		DEBUG = dEBUG;
	}

	public static boolean isOptimizeSumms() {
		return optimizeSumms;
	}

	public static void setOptimizeSumms(boolean optimizeSumms) {
		MetaBackAnalysis.optimizeSumms = optimizeSumms;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static void setTimeout(int timeout) {
		MetaBackAnalysis.timeout = timeout;
	}

	public static int getErrSufSize() {
		return errSufSize;
	}

	public static void setErrSufSize(int size) {
		errSufSize = size;
	}

	public static void setDNegation(boolean dn){
		dnegation = dn;
	}
	
	public static void setPrune(boolean p){
		prune = p;
	}
	
	class BackQuadVisitor extends QuadVisitor.EmptyVisitor {
		DNF iDNF;
		DNF oDNF;

		/**
		 * Invoke is like a group of moves before the method call
		 */
		@Override
		public void visitInvoke(Quad obj) {
			oDNF = iDNF;
			if (SharedData.isSkippedMethod(obj)) {
				return;
			}
			if (!callStack.empty()) {
				IWrappedPE<Edge, Edge> wpe = callStack.pop();
				if (!wpe.getInst().equals(obj))
					throw new RuntimeException("Unmatch invoke!" + wpe.getInst() + " " + obj);
			}
			
			// jspark: if this mehtod is in precise.lst, put src register to oDNF
			if (isInPreciseLst(obj)) {
				oDNF = addPreciseVariables(oDNF, obj);
				return;
			}
			
			ParamListOperand args = Invoke.getParamList(obj);
			int numArgs = args.length();
			RegisterFactory rf = pre.getInst().getMethod().getCFG().getRegisterFactory();
			oDNF = decreaseContext(oDNF);
			for (int i = 0; i < numArgs; i++) {
				RegisterOperand ro = args.get(i);
				if (ro.getType().isPrimitiveType()) {
					int fromIdx = SharedData.domU.indexOf(ro.getRegister()); //actual parameter
					Register r = rf.get(i);
					int toIdx = SharedData.domU.indexOf(r);//formal parameter
					DNF tDNF = new DNF(new ClauseSizeCMP(),false); 
					for(Clause c : oDNF.getClauses()){
						Clause nc = new Clause(true);
						for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
							Variable v = predicate.getKey();
							Domain d = predicate.getValue();
							if(v instanceof VVariable){
								VVariable vv = (VVariable)v;
								if(vv.getContext() < 0){
									if(vv.getIdx() == toIdx){
										nc.addLiteral(new VVariable(fromIdx), d);
									}else
										nc.addLiteral(v, d);
								}else
									nc.addLiteral(v, d);
							}else
								nc.addLiteral(v, d);
						}
						tDNF.addClause(nc);
					}
					oDNF = tDNF;
				}
			}
			oDNF = killDeadConstraints(oDNF);
			oDNF = checkPrecise(oDNF,obj);
		}

		private boolean isInPreciseLst(Quad invoke) {
			for (Pair<String,Integer> pair : SharedData.preciseLst) {
				if (pair.val0.equalsIgnoreCase(Invoke.getMethod(invoke).getMethod().toString()))
					return true;
			}
			return false;
		}

		/**
		 * Put the parameters of methods in precise.lst into error sufficient condition
		 * It is similar to precise(parameters)
		 * @author jspark
		 */
		private DNF addPreciseVariables(DNF oDNF, Quad obj) {
			Integer argNum = -1;
			for (Pair<String,Integer> pair : SharedData.preciseLst) {
				if (pair.val0.equals(Invoke.getMethod(obj).getMethod().toString())) {
					argNum = pair.val1;
				}
			}
			ParamListOperand args = Invoke.getParamList(obj);
			RegisterOperand ro = args.get(argNum);
			if (ro.getType().isPrimitiveType()) {
				int rIdx = SharedData.domU.indexOf(ro.getRegister());
				Clause nc = new Clause(true);
				nc.addLiteral(new VVariable(rIdx), BoolDomain.T);
				oDNF.addClause(nc);
			}
			
			return oDNF;
		}

		/**
		 * Return is like a move statement after the method call
		 */
		@Override
		public void visitReturn(Quad q) {
			oDNF = iDNF;
			if (retIdx == -1)
				return;
			if (!(q.getOperator() instanceof THROW_A)) {
				Operand rx = Return.getSrc(q);
				if (rx instanceof RegisterOperand) {
					RegisterOperand ro = (RegisterOperand) rx;
					if (ro.getType().isPrimitiveType()) {
						Register r = ro.getRegister();
						int retIdx = SharedData.domU.indexOf(r);
						oDNF = new DNF(new ClauseSizeCMP(), false);
						for(Clause c : iDNF.getClauses()){
							Clause nc = new Clause(true);
							for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
								Variable v = predicate.getKey();
								Domain d = predicate.getValue(); 
								if(v instanceof RVariable){
									nc.addLiteral(new VVariable(retIdx), d);
								}else if (v instanceof R2Variable){ // jspark: adding false to oDNF means ignoring PVariable 
									nc = new Clause(false);
								}else
									nc.addLiteral(v, d);
							}
							oDNF.addClause(nc);
						}
					}
				}
			}
		}

		private DNF processGetField(Quad q, Register baseR, int fldIdx, Register dstR, DNF dnf){
			if(!dstR.getType().isPrimitiveType())
				return dnf;
			DNF retDNF = new DNF(new ClauseSizeCMP(),false);
			int operIdx = SharedData.domP.indexOf(q);
			Set<Quad> allocSites = SharedData.cipa.pointsTo(baseR).pts;
			int dstIdx = SharedData.domU.indexOf(dstR);
			for(Clause c : dnf.getClauses()){
				DNF nc = new DNF(new ClauseSizeCMP(),true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					DNF lDNF = new DNF(new ClauseSizeCMP(),v,d);
					if(v instanceof VVariable){
						VVariable vv = (VVariable)v;
						if(vv.getIdx() == dstIdx){
							lDNF = new DNF(new ClauseSizeCMP(), new OVariable(operIdx),d); 
							for(Quad h : allocSites){
								int hIdx = SharedData.domH.indexOf(h);
								DNF hfDNF = new DNF(new ClauseSizeCMP(),new HFVariable(hIdx,fldIdx),d);
								if(d.equals(BoolDomain.T)){
									lDNF = lDNF.join(hfDNF);
								}else{
									lDNF = lDNF.intersect(hfDNF);
								}
							}
						}
					}
					nc = nc.intersect(lDNF);
				}
				retDNF = retDNF.join(nc);
			}
			return retDNF;
		}
		
		/**
		 * Make sure oDNF has been assigned to appropriate value if iDNF does not need to be changed
		 * @param q
		 * @param baseR
		 * @param fldIdx
		 * @param srcO
		 */
		private DNF processPutField(Quad q, Register baseR, int fldIdx, Operand srcO, DNF iDNF){
			if(srcO instanceof AConstOperand){
				return iDNF;
			}
			DNF retDNF = new DNF(new ClauseSizeCMP(),false);
			int operIdx = SharedData.domP.indexOf(q);
        	Set<Quad> allocSites = SharedData.cipa.pointsTo(baseR).pts;
			for(Clause c : iDNF.getClauses()){
				DNF nc = new DNF(new ClauseSizeCMP(),true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					if(v instanceof HFVariable){
						HFVariable hfv = (HFVariable)v;
						DNF pDNF = new DNF(new ClauseSizeCMP(),hfv, d);
						if(allocSites.contains(hfv.getH()) && fldIdx == hfv.getFIdx()){
							DNF pDNF1 = new DNF(new ClauseSizeCMP(),new OVariable(operIdx), d);
							if(d == BoolDomain.T)
								pDNF = pDNF.join(pDNF1);
							else
								pDNF = pDNF.intersect(pDNF1);
							if(srcO instanceof RegisterOperand){
								Register srcR = ((RegisterOperand)srcO).getRegister();
								if(srcR.getType().isPrimitiveType()){
									int srcIdx = SharedData.domU.indexOf(srcR);
									DNF pDNF2 = new DNF(new ClauseSizeCMP(), new VVariable(srcIdx),d);
									if(d == BoolDomain.T){
										pDNF = pDNF.join(pDNF2);
									}
									else
										pDNF = pDNF.intersect(pDNF2);
								}
							}
						}
						nc = nc.intersect(pDNF);
					}else{
						DNF pDNF = new DNF(new ClauseSizeCMP(),v,d);
						nc = nc.intersect(pDNF);
					}
				}
				retDNF = retDNF.join(nc);
			}
			return retDNF;
		}
		
		@Override
		public void visitALoad(Quad obj) {
			oDNF = iDNF;
			int idxIndx = -1;
			Operand idxO = ALoad.getIndex(obj);
			if (idxO instanceof RegisterOperand) {
				RegisterOperand idxRO = (RegisterOperand)idxO;
				Register idxR = idxRO.getRegister();
	    		if(idxR.getType().isPrimitiveType()){
	    			idxIndx = SharedData.domU.indexOf(idxR);
	    			oDNF = new DNF(new ClauseSizeCMP(), false);
	            	OUT: for (Clause c : iDNF.getClauses()) {
	            		DNF nc = new DNF(new ClauseSizeCMP(),true);
	            		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
	            			Variable v = predicate.getKey();
	            			Domain d = predicate.getValue();
	            			DNF ldnf = new DNF(new ClauseSizeCMP(), v, d);
	            			if(v instanceof VVariable){
	            				VVariable vv = (VVariable)v;
	            				if(vv.getIdx() == idxIndx){
	            					if(d.equals(BoolDomain.T))
	            						continue OUT;
	            				}
	            			}
	            			if(v instanceof ErrVariable){
	            				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
	        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(idxIndx),BoolDomain.T);
	        						ldnf = ldnf.join(dnf2);
	            				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
	        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(idxIndx),BoolDomain.F);
	        						ldnf = ldnf.intersect(dnf2);
	            				}
	            			}
	    	        		nc = nc.intersect(ldnf);
	            		}
	            		oDNF = oDNF.join(nc);
	            	}
	    		}        
			}
			Operator op  = obj.getOperator();
        	if (!((ALoad) op).getType().isPrimitiveType()){
        		return;
        	}
        	Register base = ((RegisterOperand)ALoad.getBase(obj)).getRegister();
        	oDNF = this.processGetField(obj, base, -1, ALoad.getDest(obj).getRegister(), oDNF);
		}

		@Override
		public void visitAStore(Quad obj) {
			oDNF = iDNF;
			int idxIndx = -1;
        	Operand idxO = AStore.getIndex(obj);
        	if (idxO instanceof RegisterOperand) {
        		RegisterOperand idxRO = (RegisterOperand)idxO;
	    		Register idxR = idxRO.getRegister();
	    		if(idxR.getType().isPrimitiveType()){
	    			idxIndx = SharedData.domU.indexOf(idxR);
	    			oDNF = new DNF(new ClauseSizeCMP(), false);
	            	OUT: for (Clause c : iDNF.getClauses()) {
	            		DNF nc = new DNF(new ClauseSizeCMP(),true);
	            		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
	            			Variable v = predicate.getKey();
	            			Domain d = predicate.getValue();
	            			DNF ldnf = new DNF(new ClauseSizeCMP(), v, d);
	            			if(v instanceof VVariable){
	            				VVariable vv = (VVariable)v;
	            				if(vv.getIdx() == idxIndx){
	            					if(d.equals(BoolDomain.T))
	            						continue OUT;
	            				}
	            			}
	            			if(v instanceof ErrVariable){
	            				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
	        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(idxIndx),BoolDomain.T);
	        						ldnf = ldnf.join(dnf2);
	            				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
	        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(idxIndx),BoolDomain.F);
	        						ldnf = ldnf.intersect(dnf2);
	            				}
	            			}
	    	        		nc = nc.intersect(ldnf);
	            		}
	            		oDNF = oDNF.join(nc);
	            	}
	    		}
        	}
        	Operator op = obj.getOperator();
        	//Reference operations are precise by default
        	if (!((AStore) op).getType().isPrimitiveType()){
        		return;
        	}
        	Register base = ((RegisterOperand)AStore.getBase(obj)).getRegister();
        	oDNF = this.processPutField(obj, base, -1, AStore.getValue(obj), oDNF);
		}

		@Override
		public void visitALength(Quad obj) {
			oDNF = iDNF;
			int operIdx = SharedData.domP.indexOf(obj);
			Register r = ALength.getDest(obj).getRegister();
			int ridx = SharedData.domU.indexOf(r);
			oDNF = new DNF(new ClauseSizeCMP(), false);
			for(Clause c : iDNF.getClauses()){
				Clause nc = new Clause(true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					if(v instanceof VVariable){
						VVariable vv = (VVariable)v;
						if (vv.getIdx() == ridx) 
							nc.addLiteral(new OVariable(operIdx), d);
						else 
							nc.addLiteral(v, d);
					}else
						nc.addLiteral(v, d);
				}
				oDNF.addClause(nc);
			}
		}

		/**
		 * Return the register of o represents if o is a register operand, otherwise return -1
		 * @param o
		 * @return
		 */
		private int getRegisterIdx(Operand o){
			if(o == null)
				return -1;
			if(!(o instanceof RegisterOperand))
				return -1;
			RegisterOperand ro = (RegisterOperand)o;
			Register r = ro.getRegister();
			if(!r.getType().isPrimitiveType())
				return -1;
			return SharedData.domU.indexOf(r);
		}
		
		private DNF processAssignment(DNF dnf, Quad q, Register dest, Operand src1, Operand src2){			
        	int dstIndx = SharedData.domU.indexOf(dest);
        	DNF ret = new DNF(new ClauseSizeCMP(),false);
        	int oIdx = SharedData.domP.indexOf(q);
        	for(Clause c : dnf.getClauses()){
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF ldnf = new DNF(new ClauseSizeCMP(),v,d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if(vv.getIdx() == dstIndx){
        					ldnf = new DNF(new ClauseSizeCMP(), new OVariable(oIdx),d);
        					if(d.equals(BoolDomain.T)){//dest register tainted
        						int src1Idx = this.getRegisterIdx(src1);
        						int src2Idx = this.getRegisterIdx(src2);
        						if(src1Idx >= 0){
        							ldnf = ldnf.join(new DNF(new ClauseSizeCMP(),new VVariable(src1Idx),d));
        						}
        						if(src2Idx >= 0){
         							ldnf = ldnf.join(new DNF(new ClauseSizeCMP(),new VVariable(src2Idx),d));       							
        						}
        					}else{
         						int src1Idx = this.getRegisterIdx(src1);
        						int src2Idx = this.getRegisterIdx(src2);
        						if(src1Idx >= 0){
        							ldnf = ldnf.intersect(new DNF(new ClauseSizeCMP(),new VVariable(src1Idx),d));
        						}
        						if(src2Idx >= 0){
         							ldnf = ldnf.intersect(new DNF(new ClauseSizeCMP(),new VVariable(src2Idx),d));       							
        						}       						
        					}
        				}
        			}
        			nc = nc.intersect(ldnf);
        		}
        		ret = ret.join(nc);
        	}
        	return ret;
		}
		
		@Override
		public void visitBinary(Quad obj) {
			oDNF = iDNF;
	       	RegisterOperand dstO = Binary.getDest(obj);
        	Register dstR = dstO.getRegister();
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	Operand src1 = Binary.getSrc1(obj);
        	Operand src2 = Binary.getSrc2(obj);
        	oDNF = processAssignment(oDNF, obj, dstR, src1, src2);
		}

		@Override
		public void visitGetfield(Quad obj) {
			oDNF = iDNF;
        	jq_Field f = Getfield.getField(obj).getField();
        	if(!f.getType().isPrimitiveType())
        		return;
        	int fIdx = SharedData.domF.indexOf(f);
        	Register base = ((RegisterOperand)(Getfield.getBase(obj))).getRegister();
        	oDNF = this.processGetField(obj, base, fIdx, Getfield.getDest(obj).getRegister(), oDNF);
		}

		@Override
		public void visitGetstatic(Quad obj) {
			oDNF = iDNF;
			int operIdx = SharedData.domP.indexOf(obj);
			RegisterOperand dstO = Getstatic.getDest(obj);
        	Register dstR = dstO.getRegister();
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	jq_Field global = Getstatic.getField(obj).getField();
        	Integer gindx = SharedData.domF.indexOf(global);
        	Integer dstindx = SharedData.domU.indexOf(dstR);
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	for (Clause c : iDNF.getClauses()) {
				DNF nc = new DNF(new ClauseSizeCMP(), true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					DNF ldnf = new DNF(new ClauseSizeCMP(),v,d);
					if(v instanceof VVariable){
						VVariable vv = (VVariable)v;
						if (vv.getIdx() == dstindx) { 
							ldnf = new DNF(new ClauseSizeCMP(), new OVariable(operIdx),d);
        					if(d.equals(BoolDomain.T)){
        						ldnf = ldnf.join(new DNF(new ClauseSizeCMP(),new GVariable(gindx),d));
        					}	
        					else {
        						ldnf = ldnf.intersect(new DNF(new ClauseSizeCMP(),new GVariable(gindx),d));
        					}
						}
					}
					nc = nc.intersect(ldnf);
				}
				oDNF = oDNF.join(nc);
        	}
		}

		@Override
		public void visitIntIfCmp(Quad obj) {
			oDNF = iDNF;
			Boolean isApproxIf = SharedData.approxIfConditional.get(obj); 
			if(isApproxIf != null && isApproxIf == true)
				return;
        	Operand srcO1 = IntIfCmp.getSrc1(obj);
        	Operand srcO2 = IntIfCmp.getSrc2(obj);
        	int src1Idx = -1;
        	int src2Idx = -1;
        	Set<Integer> preciseVs = new ArraySet<Integer>();
        	if(srcO1 instanceof RegisterOperand){
        		Register srcR1 = ((RegisterOperand) srcO1).getRegister();
        		if(srcR1.getType().isPrimitiveType()){
        			src1Idx = SharedData.domU.indexOf(srcR1);
        			preciseVs.add(src1Idx);
        		} else {
        			
        		}
        	}
        	if(srcO2 instanceof RegisterOperand){
        		Register srcR2 = ((RegisterOperand) srcO2).getRegister();
        		if(srcR2.getType().isPrimitiveType()){
        			src2Idx = SharedData.domU.indexOf(srcR2);
        			preciseVs.add(src2Idx);
        		}
        	}
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	OUT: for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF ldnf = new DNF(new ClauseSizeCMP(),v,d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if(vv.getIdx() == src1Idx || vv.getIdx() == src2Idx){
        					if(d.equals(BoolDomain.T))
        						continue OUT;
        				}
        			}
        			if(v instanceof ErrVariable){
        				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
        					for(Integer pvidx : preciseVs){
        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.T);
        						ldnf = ldnf.join(dnf2);
        					}
        				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
        					for(Integer pvidx : preciseVs){
        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.F);
        						ldnf = ldnf.intersect(dnf2);
        					}
        				}
        			}
	        		nc = nc.intersect(ldnf);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}

		@Override
		public void visitLookupSwitch(Quad obj) {
			oDNF = iDNF;
        	Operand src = LookupSwitch.getSrc(obj);
        	int srcIndx = -1;
        	if (src instanceof RegisterOperand) {
        		Register srcR = ((RegisterOperand) src).getRegister();
        		if(srcR.getType().isPrimitiveType()){
        			srcIndx = SharedData.domU.indexOf(srcR);
        		}
        		else
        			return;
        	}
        	else
        		return;
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	OUT: for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF ldnf = new DNF(new ClauseSizeCMP(), v, d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if(vv.getIdx() == srcIndx){
        					if(d.equals(BoolDomain.T))
        						continue OUT;
        				}
        			}
        			if(v instanceof ErrVariable){
        				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.T);
    						ldnf = ldnf.join(dnf2);
        				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.F);
    						ldnf = ldnf.intersect(dnf2);
        				}
        			}
	        		nc = nc.intersect(ldnf);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}

		@Override
		public void visitMove(Quad obj) {
			oDNF = iDNF;
			RegisterOperand dstO = Move.getDest(obj);
			Register dstR = dstO.getRegister();
			if(!dstR.getType().isPrimitiveType())
				return;
			Operand src = Move.getSrc(obj);
			oDNF = processAssignment(oDNF,obj,dstR,src,null);
		}

		/**
		 * This is needed because of type conversion
		 * @author jspark
		 */
		@Override
		public void visitUnary(Quad obj) {
			oDNF = iDNF;
			RegisterOperand dstO = Unary.getDest(obj);
			Register dstR = dstO.getRegister();
			if(!dstR.getType().isPrimitiveType())
				return;
			Operand src = Unary.getSrc(obj);
			oDNF = processAssignment(oDNF,obj,dstR,src,null);
		}
		
		@Override
		public void visitPutfield(Quad obj) {
			oDNF = iDNF;
        	jq_Field f = Putfield.getField(obj).getField();
        	if(!f.getType().isPrimitiveType())
        		return;
        	int fIdx = SharedData.domF.indexOf(f);
        	Register base = ((RegisterOperand)(Putfield.getBase(obj))).getRegister();
        	oDNF = this.processPutField(obj, base, fIdx, Putfield.getSrc(obj),oDNF);
		}

		@Override
		public void visitPutstatic(Quad obj) {
			oDNF = iDNF;
			int operIdx = SharedData.domP.indexOf(obj);
			jq_Field dst = Putstatic.getField(obj).getField();
			if(!dst.getType().isPrimitiveType())
				return;
			int fIdx = SharedData.domF.indexOf(dst);
			Operand src = Putstatic.getSrc(obj);
			oDNF = new DNF(new ClauseSizeCMP(), false);
			for (Clause c : iDNF.getClauses()) {
				DNF nc = new DNF(new ClauseSizeCMP(), true);
				for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
					Variable v = predicate.getKey();
					Domain d = predicate.getValue();
					DNF ldnf = new DNF(new ClauseSizeCMP(),v,d);
					if(v instanceof GVariable){
						GVariable gv = (GVariable)v;
						if (gv.getIdx() == fIdx) { 
							ldnf = new DNF(new ClauseSizeCMP(), new OVariable(operIdx),d);
							if(src instanceof RegisterOperand){
								Register srcR = ((RegisterOperand)src).getRegister();
								if(srcR.getType().isPrimitiveType()){
									int srcIdx = SharedData.domU.indexOf(srcR);
									if(d == BoolDomain.T)
										ldnf = ldnf.join(new DNF(new ClauseSizeCMP(),new VVariable(srcIdx), d));
									else
										ldnf = ldnf.intersect(new DNF(new ClauseSizeCMP(),new VVariable(srcIdx), d));
								}
							}
						}
					}
					nc = nc.intersect(ldnf);
				}
				oDNF = oDNF.join(nc);
        	}
		}

		@Override
		public void visitTableSwitch(Quad obj) {
			oDNF = iDNF;
        	Operand src = TableSwitch.getSrc(obj);
        	int srcIndx = -1;
        	if (src instanceof RegisterOperand) {
        		Register srcR = ((RegisterOperand) src).getRegister();
        		if(srcR.getType().isPrimitiveType()){
        			srcIndx = SharedData.domU.indexOf(srcR);
        		}
        		else
        			return;
        	}else
        		return;
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	OUT: for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF ldnf = new DNF(new ClauseSizeCMP(), v, d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if(vv.getIdx() == srcIndx){
        					if(d.equals(BoolDomain.T))
        						continue OUT;
        				}
        			}
        			if(v instanceof ErrVariable){
        				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.T);
    						ldnf = ldnf.join(dnf2);
        				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.F);
    						ldnf = ldnf.intersect(dnf2);
        				}
        			}
	        		nc = nc.intersect(ldnf);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}

		public void visitNew(Quad obj) {
			oDNF = new DNF(new ClauseSizeCMP(), false);
        	for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF lDNF = new DNF(new ClauseSizeCMP(), v, d);
        			if(v instanceof HFVariable){
        				HFVariable hfv = (HFVariable)v;
						int hidx = SharedData.domH.indexOf(obj);
						Pair<Integer,Integer> pair = new Pair<Integer,Integer>(hidx,hfv.getFIdx());
						System.out.println("*** R2_META: pair = " + pair.toString());
						if(hfv.getHIdx() == hidx && abs.approxStorage.contains(pair)){
							DNF stDNF = new DNF(new ClauseSizeCMP(),new STVariable(hfv.getHIdx(),hfv.getFIdx()), d);
							System.out.println("*** R2_META: stDNF = " + stDNF.toString());
							lDNF = lDNF.join(stDNF);
							System.out.println("*** R2_META: lDNF = " + lDNF.toString());
						}
        			}
        			nc = nc.intersect(lDNF);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}
		
		/**
		 * @author jspark 
		 */
		public void visitNewArray(Quad obj) {
			oDNF = iDNF;
			List<RegisterOperand> roList = NewArray.getReg2(obj);
			int srcIndx = -1;
			if (roList.size() != 0) {
	        	for (RegisterOperand srcO : roList) {
	        		Register srcR = ((RegisterOperand) srcO).getRegister();
	        		if(srcR.getType().isPrimitiveType()){
	        			srcIndx = SharedData.domU.indexOf(srcR);
	        		}        		
	        	}
			}
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	OUT: for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF lDNF = new DNF(new ClauseSizeCMP(), v, d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if(vv.getIdx() == srcIndx){
        					if(d.equals(BoolDomain.T))
        						continue OUT;
        				}
        			}
        			if(v instanceof HFVariable){
        				HFVariable hfv = (HFVariable)v;
						DNF pDNF = new DNF(new ClauseSizeCMP(),hfv,d);
						int hidx = SharedData.domH.indexOf(obj);
						Pair<Integer,Integer> pair = new Pair<Integer,Integer>(hidx,-1);
						if(hfv.getHIdx() == hidx && hfv.getFIdx() == -1 && abs.approxStorage.contains(pair)){
							DNF stDNF = new DNF(new ClauseSizeCMP(),new STVariable(hfv.getHIdx(),hfv.getFIdx()), d);
							pDNF = pDNF.join(stDNF);
						}
						lDNF = lDNF.join(pDNF);
        			}
        			if(v instanceof ErrVariable){
        				if(d.equals(BoolDomain.T)){
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.T);
    						if (srcIndx != -1)
    							lDNF = lDNF.join(dnf2);
        				}else{
    						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(srcIndx),BoolDomain.F);
    						if (srcIndx != -1)
    							lDNF = lDNF.intersect(dnf2);
        				}
        			}
	        		nc = nc.intersect(lDNF);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}
		
		/**
		 * Not sure if this is needed becuase we tries to remove all MULTINEWARRAY manually from analyzed benchmarks
		 * But just take care of the case where tainted variables are used in indices
		 * @author jspark
		 */
		public void visitMultiNewArray(Quad obj) {
			oDNF = iDNF;
        	ParamListOperand po = MultiNewArray.getParamList(obj);
        	Operand srcO1, srcO2, srcO3;
        	srcO1 = srcO2 = srcO3 = null;
        	if (po.length() >= 1) 
        		srcO1 = po.get(0);
        	if (po.length() >= 2)
        		srcO2 = po.get(1);
        	if (po.length() >= 3)
        		srcO3 = po.get(2);
        	int src1Idx = -1;
        	int src2Idx = -1;
        	int src3Idx = -1;
        	Set<Integer> preciseVs = new ArraySet<Integer>();
        	if(srcO1 != null && srcO1 instanceof RegisterOperand){
        		Register srcR1 = ((RegisterOperand) srcO1).getRegister();
        		if(srcR1.getType().isPrimitiveType()){
        			src1Idx = SharedData.domU.indexOf(srcR1);
        			preciseVs.add(src1Idx);
        		}
        	}
        	if(srcO2 != null && srcO2 instanceof RegisterOperand){
        		Register srcR2 = ((RegisterOperand) srcO2).getRegister();
        		if(srcR2.getType().isPrimitiveType()){
        			src2Idx = SharedData.domU.indexOf(srcR2);
        			preciseVs.add(src2Idx);
        		}
        	}
        	if(srcO3 != null && srcO3 instanceof RegisterOperand){
        		Register srcR3 = ((RegisterOperand) srcO3).getRegister();
        		if(srcR3.getType().isPrimitiveType()){
        			src3Idx = SharedData.domU.indexOf(srcR3);
        			preciseVs.add(src3Idx);
        		}
        	}
        	oDNF = new DNF(new ClauseSizeCMP(), false);
        	OUT: for (Clause c : iDNF.getClauses()) {
        		DNF nc = new DNF(new ClauseSizeCMP(),true);
        		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()){
        			Variable v = predicate.getKey();
        			Domain d = predicate.getValue();
        			DNF ldnf = new DNF(new ClauseSizeCMP(),v,d);
        			if(v instanceof VVariable){
        				VVariable vv = (VVariable)v;
        				if((srcO1 != null && vv.getIdx() == src1Idx) || (srcO2 != null && vv.getIdx() == src2Idx) || (srcO3 != null && vv.getIdx() == src3Idx)){
        					if(d.equals(BoolDomain.T))
        						continue OUT;
        				}
        			}
        			if(v instanceof ErrVariable){
        				if(d.equals(BoolDomain.T)){//err. err = err \/ tainted(v)
        					for(Integer pvidx : preciseVs){
        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.T);
        						ldnf = ldnf.join(dnf2);
        					}
        				}else{//!err. !err = !err/\!tainted(v). Not sure if this is needed
        					for(Integer pvidx : preciseVs){
        						DNF dnf2 = new DNF(new ClauseSizeCMP(),new VVariable(pvidx),BoolDomain.F);
        						ldnf = ldnf.intersect(dnf2);
        					}
        				}
        			}
	        		nc = nc.intersect(ldnf);
        		}
        		oDNF = oDNF.join(nc);
        	}
		}
		
	}
}

