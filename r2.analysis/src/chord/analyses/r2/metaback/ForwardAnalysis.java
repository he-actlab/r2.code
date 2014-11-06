package chord.analyses.r2.metaback;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.Dominators;
import joeq.Compiler.Quad.Dominators.DominatorNode;
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
import joeq.Compiler.Quad.Operator.Goto;
import joeq.Compiler.Quad.Operator.IntIfCmp;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.LookupSwitch;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.MultiNewArray;
import joeq.Compiler.Quad.Operator.MultiNewArray.MULTINEWARRAY;
import joeq.Compiler.Quad.Operator.New.NEW;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.NewArray.NEWARRAY;
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
import chord.analyses.alias.ICICG;
import chord.program.Loc;
import chord.project.Config;
import chord.project.analyses.rhs.IWrappedPE;
import chord.project.analyses.rhs.RHSAnalysis;
import chord.project.analyses.rhs.TimeoutException;
import chord.util.ArraySet;
import chord.util.Timer;
import chord.util.tuple.object.Pair;

public class ForwardAnalysis extends RHSAnalysis<Edge, Edge> {
	MyQuadVisitor visitor = new MyQuadVisitor();
	ExpAbstraction abs;
	boolean isTimeOut;
	Set<Quad> totalVisitOpSet = new HashSet<Quad>();
	Map<jq_Method, Set<Quad>> unusedQuad = new HashMap<jq_Method,Set<Quad>>();
	Map<jq_Method,Set<Register>> alreadyUsedQuad = new HashMap<jq_Method,Set<Register>>();
	Map<jq_Method, Set<Register>> unusedReg = new HashMap<jq_Method,Set<Register>>();
	Map<jq_Method,Map<Register,Quad>> regToQuad = new HashMap<jq_Method,Map<Register,Quad>>();
	
	boolean DEBUG = false;
	boolean R2_EXPERIMENT = true;
	
	public ForwardAnalysis(ExpAbstraction abs){
		this.abs = abs;
	}
	
	@Override
	public void run() {
		init();
		Timer timer = new Timer("forward-timer");
		timer.init();
		try{
			Set<Quad> curOpSet = new ArraySet<Quad>();
			Set<Quad> curStSet = new ArraySet<Quad>();
			if(R2_EXPERIMENT) System.out.println("R2_EXPERIMENT # of safe-to-approximate operations = " + (abs.approxStatements.size() + abs.approxStorage.size()));
			if(DEBUG) {System.out.println("Approximated operations: ");
				for(Integer i : abs.approxStatements){
					Inst inst = SharedData.domP.get(i);
					curOpSet.add((Quad)inst);
					System.out.println(inst.toVerboseStr());
				}
				System.out.println("Approximated storage: ");
				for(Pair<Integer,Integer> pair : abs.approxStorage) {
					Pair<Quad,jq_Field> qj = SharedData.idxFieldMap.get(pair);
					curStSet.add(qj.val0);
					if(qj.val1 == null)
						System.out.println("h(" + qj.val0.toString() + "," + pair.val0 + ") f(ARRAY,-1)");
					else
						System.out.println("h(" + qj.val0.toString() + "," + pair.val0 + ") f(" + qj.val1.toString() + "," + pair.val1 + ")");
				}
			}
			SharedData.previousAllApproxOpSet.removeAll(curOpSet);
			SharedData.previousAllApproxOpSet.clear();
			SharedData.previousAllApproxStorageSet.clear();
			SharedData.previousAllApproxOpSet.addAll(curOpSet);
			SharedData.previousAllApproxStorageSet.addAll(curStSet);
			SharedData.approxIfConditional.clear();
			SharedData.unusedQuads.clear();
			runPass();
		}
		catch(TimeoutException ex){
			isTimeOut = true;
			System.out.println("TIMED OUT");
		}
		timer.done();
		System.out.println(timer.getInclusiveTimeStr());
		if (DEBUG)
			print();
		done();
	}
	
	public boolean checkQuery(ExpQuery eq){
		Inst inst = eq.inst;
		Set<Edge> states = this.getAllPEs().get(inst);
		for(Edge e : states)
			if(e.dstNode.isErr)
				return false;
		return true;
	}
	
	public IWrappedPE<Edge,Edge> getErrEdge(ExpQuery eq){
		Inst inst = eq.inst;
		Set<Edge> states = this.getAllPEs().get(inst);
		for(Edge e : states)
			if(e.dstNode.isErr)
				return this.wpeMap.get(new Pair<Inst,Edge>(inst,e));
		return null;	
	}
	
	@Override
	public Set<Pair<Loc, Edge>> getInitPathEdges() {
		Set<jq_Method> roots = cicg.getRoots();
		Set<Pair<Loc, Edge>> initPEs = new ArraySet<Pair<Loc, Edge>>(roots.size());
		for (jq_Method m : roots) {
			Edge pe = getRootPathEdge(m);
			BasicBlock bb = m.getCFG().entry();
			Loc loc = new Loc(bb, -1);
			Pair<Loc, Edge> pair = new Pair<Loc, Edge>(loc, pe);
			initPEs.add(pair);
		}
		return initPEs;
	}

	// m is either the main method or the thread root method
	private Edge getRootPathEdge(jq_Method m) {
		assert (SharedData.isMainMethod(m)||
				SharedData.isThreadStartMethod(m) ||
				SharedData.isCinit(m));
		Set<Integer> taintedGlosSrc = new ArraySet<Integer>();
		Set<Integer> taintedVarsSrc = new ArraySet<Integer>();
		Set<Pair<Integer,Integer>> taintedFieldsSrc = new HashSet<Pair<Integer,Integer>>();
		Set<Integer> taintedVarsDst = new ArraySet<Integer>();
		Set<Integer> taintedGlosDst = new ArraySet<Integer>();
		Set<Pair<Integer,Integer>> taintedFieldsDst = new HashSet<Pair<Integer,Integer>>();
		AbstractState srcNode = new AbstractState(taintedGlosSrc, taintedVarsSrc, taintedFieldsSrc, false, false);
		AbstractState dstNode = new AbstractState(taintedGlosDst, taintedVarsDst, taintedFieldsDst, false, false);
		Edge pe = new Edge(srcNode, dstNode);
		return pe;
	}

	private void clearUnusedQuadTracknig(jq_Method m) {
		if(unusedQuad.get(m) != null) unusedQuad.get(m).clear();
		if(unusedReg.get(m) != null) unusedReg.get(m).clear();
		if(regToQuad.get(m) != null) regToQuad.get(m).clear();
	}
	
	@Override
	public Edge getInitPathEdge(Quad q, jq_Method m, Edge pe) {
		clearUnusedQuadTracknig(m);
		//Let's assume the parameter passing is always precise
		AbstractState abs = this.getInitState(pe.dstNode, q, m, pe);
		return new Edge(abs,abs);
	}

	private AbstractState getInitState(AbstractState abs,Quad invoke,jq_Method m,Edge pe){
		Edge tmpPE = null;
		// jspark: FIXME when a function in precise.lst is not a native one, it may have to be precise 
        if (isPreciseLst()) {
        	tmpPE = addPreciseVariables(invoke, pe);
        	if (!pe.equals(tmpPE)){
    			if (SharedData.R2_LOG)
    				System.out.println("*** R2_LOG: ERROR! preciseLst - quad(" + invoke.toString() + ")");
        		return this.getErrState();
        	}
        }
        
		Set<Integer> oldtvs = abs.taintedVars;
		Set<Integer> newtvs = new ArraySet<Integer>(); 
		Set<Pair<Integer,Integer>> oldtfs = abs.taintedFields; 
		Set<Pair<Integer,Integer>> newtfs = new ArraySet<Pair<Integer,Integer>>(abs.taintedFields);
		Set<Integer> oldtgs = abs.taintedGlobals;
		Set<Integer> newtgs = new ArraySet<Integer>(abs.taintedGlobals);
		ParamListOperand args = Invoke.getParamList(invoke);
		RegisterFactory rf = m.getCFG().getRegisterFactory();
		
		//Any argument follows into m should be precise
		if(SharedData.isRestrictMethod(m)){
			for (int i = 0; i < args.length(); i++) {
				Register reg = args.get(i).getRegister();
				Integer ridx = SharedData.domU.indexOf(reg);
				if(oldtvs.contains(ridx)){
					return this.getErrState();
				}
			}
		}
		
		// jspark: precise_all_FIELDX_TAGY
		if (SharedData.isRestrictAllMethod(m)) {
	    	if (args.length() == 0) { // static field
	    		int gidx = SharedData.getFieldIdx(m);
	    		if(SharedData.R2_LOG)
	    			System.out.println("*** R2_LOG: [Forward] precise_all - oldtgs.toString = " + oldtgs.toString());
	    		if (oldtgs.contains(gidx)) {
					if(SharedData.R2_LOG)
						System.out.println("*** R2_LOG: [Forward] precise_all - ERROR! global (" + gidx + ") quad(" + invoke.toString() + ")");
					return this.getErrState();	
	    		}
	    	} else if (args.length() > 0) { // arrays or object field
				Register reg = args.get(args.length()-1).getRegister(); // last argument is a real object 
			    if (!reg.getType().isPrimitiveType()){ 
					Set<Quad> allocSites = SharedData.cipa.pointsTo(reg).pts;
					for (Quad h : allocSites) {
						String tag = SharedData.getTag(m);
			        	if (!tag.equalsIgnoreCase(SharedData.quadToTagMap.get(h)))
			        		continue; // not remove a field from tainted set when the tag is unmatched
						int hidx = SharedData.domH.indexOf(h);
						int fidx = SharedData.getFieldIdx(h, m);
						Pair<Integer, Integer> hf = new Pair<Integer, Integer>(hidx,fidx);
						if(SharedData.R2_LOG){
							System.out.println("*** R2_LOG: [Forward] precise_all - allocation site h = " + h.toString());
							System.out.println("*** R2_LOG: [Forward] precise_all - oldtfs = " + oldtfs.toString());
						}
						if (oldtfs.contains(hf)){
							if(SharedData.R2_LOG)
								System.out.println("*** R2_LOG: [Forward] precise_all - ERROR! array or object.field (" + hf.toString() + ") quad(" + invoke.toString() + ")");
							return this.getErrState();
						}
					}
			    } else {
		        	throw new RuntimeException("Error! precise_all shouldn't have a primitive-type parameter");
		        }
	    	} 
		}
		
		// jspark: relax_all_FIELDX_TAGY
	    if(SharedData.isRelaxAllMethod(m)) {
	    	if (args.length() == 0){
	    		int gidx = SharedData.getFieldIdx(m);
	    		if(SharedData.R2_LOG)
	    			System.out.println("*** R2_LOG: [Forward] relax_all - oldtgs = " + newtgs.toString());
	    		newtgs.remove(gidx);
	    		if(SharedData.R2_LOG)
	    			System.out.println("*** R2_LOG: [Forward] relax_all - newtgs = " + newtgs.toString());
	    	} else if (args.length() > 0) {
	    		Register reg = args.get(args.length()-1).getRegister(); 
		        if (!reg.getType().isPrimitiveType()) { // arrays or class objects
			        Set<Quad> allocSites = SharedData.cipa.pointsTo(reg).pts;
			        for (Quad h: allocSites) {
			        	String tag = SharedData.getTag(m);
			        	if (!tag.equalsIgnoreCase(SharedData.quadToTagMap.get(h)))
			        		continue;
			        	int hidx = SharedData.domH.indexOf(h);
			        	int fidx = SharedData.getFieldIdx(h, m);
			        	Pair<Integer,Integer> hf = new Pair<Integer,Integer>(hidx,fidx);
		        		if(SharedData.R2_LOG){
		        			System.out.println("*** R2_LOG: [Forward] relax_all - allocation site h = " + h.toString());
		        			System.out.println("*** R2_LOG: [Forward] relax_all - oldtfs = " + newtfs.toString());
		        			if (newtfs.contains(hf))
		        				if(SharedData.R2_LOG)
		        					System.out.println("*** R2_LOG: [Forward] relax_all - REMOVE array or class objects (" + hf.toString() + ") quad(" + invoke.toString() + ")");
		        		}
			        	newtfs.remove(hf);
			        	if(SharedData.R2_LOG){
			        		System.out.println("*** R2_LOG: [Forward] relax_all - newtfs = " + newtfs.toString());
			        	}
			        	
			        }
		        } else 
		        	throw new RuntimeException("Error! relax_all shouldn't have a primitive-type parameter");
	    	} 
	    }

	    // jspark: alloc_TAGX
	    if(SharedData.isAllocMethod(m)) {
	    	SharedData.parseAllocTag(m);
	    }
	    
	    // jspark: store approximate parameters
	    String excludeStr = SharedData.excludeStr;
		String[] excludes = excludeStr.split(",");
		boolean excludeClass = false;
		for (String exclude : excludes) {
			if (m.getDeclaringClass().toString().startsWith(exclude)) {
				excludeClass = true;
				break;
			}
		}
		if(!excludeClass) {
		    if (!SharedData.approxParams.keySet().contains(m) && args.length() != 0) {
		    	Set<Integer> newSet = new HashSet<Integer>();
		    	SharedData.approxParams.put(m, newSet);
		    }
		}
		for (int i = 0; i < args.length(); i++) {
			Register actualReg = args.get(i).getRegister();
			// track unused approximate quads
        	rmRegFromUnused(invoke.getBasicBlock().getMethod(),actualReg);
			Register formalReg = rf.get(i);
			Integer aridx = SharedData.domU.indexOf(actualReg);
			if(oldtvs.contains(aridx)){
				Integer fridx = SharedData.domU.indexOf(formalReg);
				newtvs.add(fridx);
				if(!excludeClass)
					SharedData.approxParams.get(m).add(i);
			}
		}
		AbstractState newAbs = new AbstractState(newtgs, newtvs, newtfs, false, abs.isErr);
		return newAbs;
	}
	
	@Override
	public Edge getMiscPathEdge(Quad q, Edge pe) {
		visitor.inNode = pe.dstNode;
		visitor.outNode = pe.dstNode;
		totalVisitOpSet.add(q);
		
		// jspark:
		// even in error state, the forward analysis should visit allocation quads to assign TAGs
    	// when an object (or an array) is created, create a mapping between the NEW quad and allocTag
		if (q.getOperator() instanceof NEW ||
			q.getOperator() instanceof NEWARRAY ||
			q.getOperator() instanceof MULTINEWARRAY) {
			// if this allocTag is the one right before this allocation
        	if (!SharedData.tag.equalsIgnoreCase("")) {
        		SharedData.quadToTagMap.put(q, SharedData.tag);
        		SharedData.tag = ""; // initialize tag to avoid that other allocating quad use this TAG
        	}
		}	
		
		if(!visitor.outNode.isErr) {//we do not need to anything abour \top
			q.accept(visitor);
		}
		Operator op = q.getOperator();
		
		// jspark: for debug
		if (op instanceof MultiNewArray)
			System.out.println("*** MultiNewArray: " + q.getBasicBlock().getMethod().toString() + " ***");
		
		Edge ret = new Edge(pe.srcNode,visitor.outNode);
		return ret;
	}

	private void collectUnusedQuads(jq_Method m) {
		if(unusedReg.get(m) == null)
			return;
		for (Register reg: unusedReg.get(m)) {
			if(!alreadyUsedQuad.get(m).contains(reg)) {
				Quad q = regToQuad.get(m).get(reg);
				unusedQuad.get(m).add(q);
			}
		}
		SharedData.unusedQuads.addAll(unusedQuad.get(m));
	}
	
	@Override
	public Edge getInvkPathEdge(Quad q, Edge clrPE, jq_Method m, Edge tgtSE) {
		collectUnusedQuads(m);
		AbstractState asp = this.getInitState(clrPE.dstNode, q, m, clrPE);
		if(!asp.equals(tgtSE.srcNode)) 
			return null;
		if(tgtSE.dstNode.isErr){
			AbstractState newDst = this.getErrState(); 
			return new Edge(clrPE.srcNode,newDst);
		}
		Set<Integer> taintedVs = new ArraySet<Integer>(clrPE.dstNode.taintedVars);
		Set<Integer> taintedGs = tgtSE.dstNode.taintedGlobals;
		Set<Pair<Integer,Integer>> taintedFs = tgtSE.dstNode.taintedFields;
		Register tgtRetReg = (Invoke.getDest(q) != null) ? Invoke.getDest(q).getRegister() : null;
		if(tgtRetReg != null){
			Integer tgtRetIndx = SharedData.domU.indexOf(tgtRetReg);
			if(tgtSE.dstNode.taintedRet){
				taintedVs.add(tgtRetIndx);
			}else
				taintedVs.remove(tgtRetIndx);
		}
		ParamListOperand paramList = Invoke.getParamList(q);
		if (SharedData.isRelaxMethod(m)) {
			for (int i = 0; i < paramList.length(); i++) {
				RegisterOperand ro = paramList.get(i);
				Register r = ro.getRegister();
				int rIdx = SharedData.domU.indexOf(r);
				taintedVs.remove(rIdx);
			}
		}
		AbstractState newDst = new AbstractState(taintedGs, taintedVs, taintedFs, clrPE.dstNode.taintedRet,clrPE.dstNode.isErr);
		return new Edge(clrPE.srcNode,newDst);
	}

	public AbstractState getErrState(){
		return new AbstractState(new ArraySet<Integer>(), new ArraySet<Integer>(), new HashSet<Pair<Integer,Integer>>(), false, true);
	}
	
	@Override
	public Edge getPECopy(Edge pe) {
		return new Edge(pe.srcNode,pe.dstNode);
	}

	@Override
	public Edge getSECopy(Edge pe) {
		return new Edge(pe.srcNode,pe.dstNode);
	}

	@Override
	public Edge getSummaryEdge(jq_Method m, Edge pe) {
		AbstractState summDstNode = this.getLiftedAbstractState(pe.dstNode);
		return new Edge(pe.srcNode,summDstNode);
	}
	
	private AbstractState getLiftedAbstractState(AbstractState as){
		Set<Integer> emptyTs = new HashSet<Integer>();
		return new AbstractState(as.taintedGlobals, emptyTs, as.taintedFields, as.taintedRet, as.isErr);
	}

	@Override
	public ICICG getCallGraph() {
		return SharedData.cicg;
	}
	
	private boolean isApprox(Quad q){
		return this.abs.approxStatements.contains(SharedData.domP.indexOf(q));
	}
	
	/**
	 * for debug
	 * @author jspark
	 */
	public void printApproxAfterSize(int iterations, double forwardTimeSum, double backwardTimeSum) {
		System.out.println("R2_EXPERIMENT total # of operations analyzed (visited) = " + totalVisitOpSet.size());
		System.out.println("R2_EXPERIMENT # approximate operations = " + SharedData.allApproxStatements.size());
		System.out.println("R2_EXPERIMENT # approximate memory locations = " + SharedData.allApproxStorage.size());
		System.out.println("R2_EXPERIMENT # Safe to Approximate Operations = " + abs.approxStatements.size());
		System.out.println("R2_EXPERIMENT # Safe to Approximate memeory Locatioons = " + abs.approxStorage.size());
		System.out.println("R2_EXPERIMENT # iterations = " + iterations);
		double avgBitFlip = ((double)(SharedData.allApproxStatements.size() - abs.approxStatements.size()) + (double)(SharedData.allApproxStorage.size() - abs.approxStorage.size())) / (double)iterations;
		System.out.println("R2_EXPERIMENT avg # of bit flips = " + avgBitFlip);
		System.out.println("R2_EXPERIMENT avg time for each iteration of backward analysis = " + SharedData.formatTime((backwardTimeSum / (double)iterations)));
		System.out.println("R2_EXPERIMENT avg time for each iteration of forward analysis = " + SharedData.formatTime((forwardTimeSum / (double)iterations)));
		double totalTime = forwardTimeSum + backwardTimeSum;
		System.out.println("R2_EXPERIMENT Total Time = " + SharedData.formatTime(totalTime));
		System.out.println("R2_EXPERIMENT Average time per operation (total time/# operations + memeory locations) = " + SharedData.formatTime((totalTime / (SharedData.allApproxStatements.size() + SharedData.allApproxStorage.size()))));
		System.out.println("R2_EXPERIMENT Average time per operation (total time/# total operations analyzed + total memeory locations analyzed) = " + SharedData.formatTime((totalTime / totalVisitOpSet.size())));
	}
	
	/**
	 * for debug
	 * @author jspark
	 */
	public void printApproxSize() {
		System.out.println("*** R2_COUNT - approxStatements size: " + abs.approxStatements.size());
		System.out.println("*** R2_COUNT - approxStorage size: " + abs.approxStorage.size());
	}
	
	
	
	/**
	 * Write three files: after, analysis.result, analysis flag
	 * (1) after: a list of approximate quads and markJava.py will use it to attach comments on Java source code
	 * (2) analysis.result: enerj compiler will read this file to apply approximation on anlyzed quads
	 * (3) analysis.flag: it has a flag, "true", when analysis has been successfully done and analysis.result generated
	 * 
	 * when analysis.flag is set, enerj framework would read analysis result and compile it using analysis.result
	 * 
	 *  @author jspark
	 */
	final String R2SEP = "#";
	public void writeResultFile(){
		
		// *******
		// "after"
		// *******
		String afterFileName = "after";
		try {
			File afterFile = new File(afterFileName);
			PrintWriter afterWriter = new PrintWriter(afterFile);
			for(Integer i : abs.approxStatements){
				Inst inst = SharedData.domP.get(i);
				afterWriter.write(inst.toVerboseStr() + "\n");
			}
			for(Pair<Integer,Integer> pair : abs.approxStorage){
				Pair<Quad,jq_Field> pair2 = SharedData.idxFieldMap.get(pair);
				Quad q = pair2.val0;
				if(pair2.val1 != null)
					afterWriter.write(q.toVerboseStr() + " " + pair2.val1.toString() + "\n");
				else
					afterWriter.write(q.toVerboseStr() + " ARRAY\n");
			}
			afterWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// *****************
		// "analysis.result"
		// *****************
		
		String resultFileName = Config.analysisResultFileName;
		if (resultFileName.equalsIgnoreCase(""))
			return;
		Set<Quad> writeSet = new HashSet<Quad>();
		for(Integer i : abs.approxStatements){
			Inst inst = SharedData.domP.get(i);
			Quad q = (Quad) inst;
			int bytecodeOffset = q.getBCI();
			if (bytecodeOffset != -1)
				writeSet.add(q);
		}
		try {
			// classname + methodname + return type + bytecode offset + quad
			File resultFile = new File(resultFileName);
			PrintWriter resultWriter = new PrintWriter(resultFile);
			/** approximate operations **/
			resultWriter.write(writeSet.size() + "\n");
			for(Quad q : writeSet){
				jq_Method method = q.getBasicBlock().getMethod();
				writeMethod(method, resultWriter);
				resultWriter.write(q.getBCI() + R2SEP);
				resultWriter.write(q.toString() + "\n");
			}
			/** approximate storage **/
			resultWriter.write(abs.approxStorage.size() + "\n");
			for (Pair<Integer,Integer> pair : abs.approxStorage) {
				Quad q = SharedData.idxFieldMap.get(pair).val0;
				jq_Method method = q.getBasicBlock().getMethod();
				jq_Field field = SharedData.idxFieldMap.get(pair).val1;
				String fieldStr = null;
				String descStr = null;
				String declClassStr = null;
				if (field != null) {
					fieldStr = field.getName().toString();
					descStr = field.getDesc().toString();
					String className = field.getDeclaringClass().toString();
					className = SharedData.convertClassName(className);
					declClassStr = className;
				} else {
					fieldStr = "ARRAY";
					TypeOperand to = NewArray.getType(q);
					descStr = to.toString();
					declClassStr = "ARRAY";
				}
				writeMethod(method, resultWriter);
				resultWriter.write(q.getBCI() + R2SEP);
				resultWriter.write(q.toString() + R2SEP);
				resultWriter.write(fieldStr + R2SEP);
				resultWriter.write(descStr + R2SEP);
				resultWriter.write(declClassStr + '\n');
			}
			/** approximate parameters **/
			Set<jq_Method> keyset = SharedData.approxParams.keySet();
			int size = 0;
			for (jq_Method method : keyset) {
				if(SharedData.isRelaxMethod(method) ||
						   SharedData.isRelaxMethod(method) ||
						   SharedData.isRestrictMethod(method) ||
						   SharedData.isRestrictAllMethod(method))
					continue;
				Set<Integer> indexSet = SharedData.approxParams.get(method);
				if(indexSet.isEmpty())
					continue;
				size++;
			}
			resultWriter.write(size + "\n");
			for (jq_Method method : keyset) {
				if(SharedData.isRelaxMethod(method) ||
						   SharedData.isRelaxMethod(method) ||
						   SharedData.isRestrictMethod(method) ||
						   SharedData.isRestrictAllMethod(method))
							continue;
				Set<Integer> indexSet = SharedData.approxParams.get(method);
				if(indexSet.isEmpty())
					continue;
				writeMethod(method, resultWriter);
				jq_Type[] types = method.getParamTypes();
				int paramSize = types.length;
				int initialIndex = 1;
				if(method.isStatic())
					initialIndex = 0;
				for (int i = initialIndex; i < paramSize; i++){
					if (indexSet.contains(i))
						resultWriter.write('1');
					else
						resultWriter.write('0');
				}
				resultWriter.write('\n');
			}
			resultWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// *************
		// analysis.flag
		// *************
		String flagFileName = Config.analysisFlag;
		try {
			File flagFile = new File(flagFileName);
			PrintWriter flagWriter = new PrintWriter(flagFile);
			flagWriter.write("true");
			flagWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeMethod(jq_Method method, PrintWriter resultWriter) {
		String className = method.getDeclaringClass().toString();
		className = SharedData.convertClassName(className);
		resultWriter.write(className + R2SEP);
		String methName = method.getName().toString();
		resultWriter.write(methName + "(");
		jq_Type[] types = method.getParamTypes();
		int initialIndex = 1;
		if(method.isStatic())
			initialIndex = 0;
		if (types.length-initialIndex != 0){
			int i;
			for (i=initialIndex; i<types.length-1; i++){
				jq_Type type = types[i];
				resultWriter.write(type.toString() + ",");
			}
			resultWriter.write(types[i] + ")" + R2SEP);
		} else{
			resultWriter.write(")" + R2SEP);
		}
		resultWriter.write(method.getReturnType().toString() + R2SEP);
	}
	
	public String getQuadType(Quad q){
		/*Operator o = q.getOperator();
		if(o instanceof Move)
		if(o instanceof ALoad)
		if(o instanceof AStore)
		if(o instanceof ALength)
			
		if(o instanceof Getfield){
			
		if(o instanceof Putfield)
		if(o instanceof Getstatic)
		if(o instanceof Putstatic)
		if(o instanceof Binary)*/
		return null;
	}
	
	/**
	 * @author jspark
	 */
	@Override
    public boolean isPreciseLst() {
    	//check if there is a file, currently it always returns true
    	String preciseLstFileName = Config.preciseListName;
    	File preciseLstFile = null;
    	preciseLstFile = new File(preciseLstFileName);
    	if (preciseLstFile.exists()) {
    		return true;
    	}
    	return false;
    }
	
	/**
	 * Read precise.lst and add all methods specified in this file 
	 * so that the analysis knows that the parameters of the methods should be precise.
	 * @author jspark	  
	 */
	@Override
	public Edge addPreciseVariables(Quad invoke, Edge pe) {	
		AbstractState abs = pe.dstNode;
		Set<Integer> oldtvs = abs.taintedVars;
		jq_Method invokeMethod = Invoke.getMethod(invoke).getMethod();
		String invokeMethodStr = invokeMethod.toString();
		
		for (Pair<String,Integer> pair : SharedData.preciseLst) {
			String methodStr = pair.val0;
			if (invokeMethodStr.equalsIgnoreCase(methodStr)) {
				ParamListOperand args = Invoke.getParamList(invoke);
				Integer argNum = pair.val1;
				Register actualReg = args.get(argNum).getRegister();
				Integer aridx = SharedData.domU.indexOf(actualReg);
				if (oldtvs.contains(aridx)){
					if(SharedData.R2_LOG)
						System.out.println("*** R2_LOG: [Forward] addPreciseVariables: " + invoke.toString());
					abs = this.getErrState();
					if(SharedData.R2_LOG)
						System.out.println("*** R2_LOG: [Forward] getErrState(addPreciseVariables) - quad(" + invoke.toVerboseStr() + ")");
					return new Edge(abs,abs);
				}
			}
		}
		return pe;
	}
	
	private void rmRegFromUnused(jq_Method m, Register reg) {
		if(alreadyUsedQuad.get(m) != null) alreadyUsedQuad.get(m).add(reg);
		if(unusedReg.get(m) != null) unusedReg.get(m).remove(reg);
		if(regToQuad.get(m) != null) regToQuad.get(m).remove(reg);
	}
	
	
	private void addRegToUnused(jq_Method m, Register reg, Quad quad) {
		if(unusedReg.get(m) == null) {
			Set<Register> newRegSet1 = new ArraySet<Register>();
			unusedReg.put(m, newRegSet1);
			Set<Register> newRegSet2 = new ArraySet<Register>();
			alreadyUsedQuad.put(m,newRegSet2);
			Set<Quad> newQuadSet = new ArraySet<Quad>();
			unusedQuad.put(m, newQuadSet);
			Map<Register,Quad> newRtoQMap = new HashMap<Register,Quad>();
			regToQuad.put(m, newRtoQMap);
			
		}
		if (unusedReg.get(m).contains(reg)) 
			unusedQuad.get(m).add(regToQuad.get(m).get(reg));
		unusedReg.get(m).add(reg);
		regToQuad.get(m).put(reg, quad);
	}
	
	public void removeUnusedApproxQuads() {
		for (jq_Method m : unusedQuad.keySet()) {
			collectUnusedQuads(m);
		}
		Set<Quad> unusedQuads = SharedData.unusedQuads;
		for(Quad q : unusedQuads) {
			abs.approxStatements.remove(SharedData.domP.indexOf(q));
		}
	}
	
	/**
	 * The transfer functions for atomic commands are defined here. I could have ignored some commands
	 * by accident. Please double check in the future.
	 * @author xin
	 *
	 */
	class MyQuadVisitor extends QuadVisitor.EmptyVisitor{
		AbstractState inNode;
		AbstractState outNode;
		
		private boolean isOperandApproximate(Operand o){
			//Constants
			if(!(o instanceof RegisterOperand))
				return false;
			RegisterOperand ro = (RegisterOperand)o;
			Register r = ro.getRegister();
			//reference type and other non-primitive type registers
			if(!r.getType().isPrimitiveType())
				return false;
			int rIdx = SharedData.domU.indexOf(r);
			if(!inNode.taintedVars.contains(rIdx))
				return false;
			return true;
		}
		
		/**
		 * A helper method to handle field read in a uniformed way
		 * @param obj
		 * @param base
		 * @param findx
		 * @param dst
		 * @param newRegToField 
		 * @param newRegToAllocSites 
		 */
		private void processGetField(Quad obj, Register base, int findx, Register dst){
			if(!dst.getType().isPrimitiveType())
				return;
			Set<Quad> allocSites = SharedData.cipa.pointsTo(base).pts;
			Set<Integer> tvs = new ArraySet<Integer>(inNode.taintedVars);
			Set<Pair<Integer,Integer>> tfs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
			Set<Integer> tgs = new ArraySet<Integer>(inNode.taintedGlobals);
			if(isApprox(obj)){
				tvs.add(SharedData.domU.indexOf(dst));
				outNode = new AbstractState(tgs, tvs,tfs,inNode.taintedRet,inNode.isErr);
				return;
			}
			for(Quad h : allocSites){
				int hi = SharedData.domH.indexOf(h);
				Pair<Integer,Integer> hf = new Pair<Integer,Integer>(hi,findx);
				if(inNode.taintedFields.contains(hf)){
					//hf in tainted set
					tvs.add(SharedData.domU.indexOf(dst));
					outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
					return;
				}
			}
			tvs.remove(SharedData.domU.indexOf(dst));			
			outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
			// track unused approximate quads
			addRegToUnused(obj.getBasicBlock().getMethod(),dst,obj);
			return;
		}
		
        /** An array load instruction. */
        public void visitALoad(Quad obj) {
        	// jspark: return error if idx variable is tainted
        	Operand idxO = ALoad.getIndex(obj);
        	if (idxO instanceof RegisterOperand) {
        		RegisterOperand idxRO = (RegisterOperand)idxO;
	        	Register idxR = idxRO.getRegister();
	        	int idxIndx = SharedData.domU.indexOf(idxR);
	    		if(inNode.taintedVars.contains(idxIndx)){
	    			outNode = getErrState();
	    			return;
	    		}
        	}
        	Operator op = obj.getOperator();
			Set<Integer> tvs = new ArraySet<Integer>(inNode.taintedVars);
			Set<Pair<Integer,Integer>> tfs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
			Set<Integer> tgs = new ArraySet<Integer>(inNode.taintedGlobals);
			//Reference operations are precise by default
			if (!((ALoad) op).getType().isPrimitiveType()) {
				outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
				return;
			}
        	RegisterOperand baseO = (RegisterOperand)ALoad.getBase(obj);
        	RegisterOperand dstO = ALoad.getDest(obj);
        	Register baseR = baseO.getRegister();
        	Register dstR = dstO.getRegister();
        	this.processGetField(obj, baseR, -1, dstR);
        }
        
        private void processPutField(Quad obj, Register base, int findx, Operand src){
        	//NULL or String constant
        	if(src instanceof AConstOperand)
        		return;
        	if(!this.isOperandApproximate(src) && !isApprox(obj))
            	return;
        	Set<Quad> allocSites = SharedData.cipa.pointsTo(base).pts;
        	Set<Integer> tvs = new ArraySet<Integer>(inNode.taintedVars);
        	Set<Pair<Integer,Integer>> tfs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	Set<Integer> tgs = new ArraySet<Integer>(inNode.taintedGlobals);
        	for(Quad h : allocSites){
        		int hidx = SharedData.domH.indexOf(h);
        		tfs.add(new Pair<Integer,Integer>(hidx,findx)); // jspark: it used to be -1 instead of findx
        	}
        	outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
			// track unused approximate quads
        	if(src instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)src).getRegister());
        }
        
        /** An array store instruction. */
        public void visitAStore(Quad obj) {
        	// jspark: if idx variable is tainted, return error
        	Operand idxO = AStore.getIndex(obj);
        	if (idxO instanceof RegisterOperand) {
        		RegisterOperand idxRO = (RegisterOperand)idxO;
	        	Register idxR = idxRO.getRegister();
	        	int idxIndx = SharedData.domU.indexOf(idxR);
	    		if(inNode.taintedVars.contains(idxIndx)){
	    			outNode = getErrState();
	    			return;
	    		}
        	}
        	Operator op = obj.getOperator();
        	//Reference operations are precise by default
        	if (!((AStore) op).getType().isPrimitiveType())
        		return;
        	Register base = ((RegisterOperand)AStore.getBase(obj)).getRegister();
        	this.processPutField(obj, base, -1, AStore.getValue(obj));
        }
        
        /** An array length instruction. */
        public void visitALength(Quad obj) {
        	Register v = ALength.getDest(obj).getRegister();
        	int ridx = SharedData.domU.indexOf(v);
        	Set<Integer> tvs = new ArraySet<Integer>(inNode.taintedVars);
        	Set<Pair<Integer,Integer>> tfs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	Set<Integer> tgs = new ArraySet<Integer>(inNode.taintedGlobals);
        	if(isApprox(obj))
        		tvs.add(ridx);
        	else 
        		tvs.remove(ridx);
        	outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
			// track unused approximate quads
			addRegToUnused(obj.getBasicBlock().getMethod(),v,obj);
        }
        
        /** A get instance field instruction. */
        public void visitGetfield(Quad obj) {
			//Reference operations are precise by default
        	RegisterOperand baseO = (RegisterOperand)Getfield.getBase(obj);
        	RegisterOperand dstO = Getfield.getDest(obj);
        	Register baseR = baseO.getRegister();
        	Register dstR = dstO.getRegister();
        	jq_Field f = Getfield.getField(obj).getField();
        	this.processGetField(obj, baseR, SharedData.domF.indexOf(f), dstR);
        }        
        
       /**
        * Binary operations like add, sub 
        */
		public void visitBinary(Quad obj) {
        	RegisterOperand dstO = Binary.getDest(obj);
        	Register dstR = dstO.getRegister();
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	int dstIndx = SharedData.domU.indexOf(dstR);
        	Set<Integer> tvs = new ArraySet<Integer>(inNode.taintedVars);
        	Set<Pair<Integer,Integer>> tfs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	Set<Integer> tgs = new ArraySet<Integer>(inNode.taintedGlobals);
        	Operand src1O = Binary.getSrc1(obj); Operand src2O = Binary.getSrc2(obj);
        	if(isApprox(obj) || this.isOperandApproximate(src1O) || this.isOperandApproximate(src2O))
        		tvs.add(dstIndx);
        	else 
        		tvs.remove(dstIndx);
        	outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
			// track unused approximate quads
        	if(src1O instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)src1O).getRegister());
        	if(src2O instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)src2O).getRegister());
			addRegToUnused(obj.getBasicBlock().getMethod(),dstR,obj);
		}
        
		/**
		 * 
		 * jspakr: this is needed because of type conversion 
		 */
		public void visitUnary(Quad obj) {
			RegisterOperand dstOperand = Unary.getDest(obj);
        	Register dstR = dstOperand.getRegister();
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	int dstIndx = SharedData.domU.indexOf(dstR);
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	Operand srcO = Unary.getSrc(obj);
        	if(isApprox(obj) || this.isOperandApproximate(srcO))
        		taintedVars.add(dstIndx);
        	else
        		taintedVars.remove(dstIndx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        	// track unused approximate quads
        	if(srcO instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO).getRegister());
			addRegToUnused(obj.getBasicBlock().getMethod(),dstR,obj);
		}

		/** A get static field instruction. */
        public void visitGetstatic(Quad obj) {
        	RegisterOperand dstO = Getstatic.getDest(obj);
        	Register dstR = dstO.getRegister();
        	//Reference type instructions should be precise
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	jq_Field global = Getstatic.getField(obj).getField();
        	Integer gindx = SharedData.domF.indexOf(global);
        	Integer dstindx = SharedData.domU.indexOf(dstR);
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	if(isApprox(obj) || inNode.taintedGlobals.contains(gindx))
        		taintedVars.add(dstindx);
        	else
        		taintedVars.remove(dstindx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        	// track unused approximate quads
			addRegToUnused(obj.getBasicBlock().getMethod(),dstR,obj);
        }
        
        private void generateDominateSets(Dominators dominators, jq_Method method, boolean dom){
        	
        	Map<jq_Method,Map<BasicBlock,Set<BasicBlock>>> map = null;
        	if(dom)
        		map = SharedData.domMap;
        	else
        		map = SharedData.pdomMap;
        	
        	List<BasicBlock> allBBs = method.getCFG().reversePostOrder();

        	Map<BasicBlock,Set<BasicBlock>> domMap = new HashMap<BasicBlock,Set<BasicBlock>>();

        	for(BasicBlock outbb : allBBs) {
	        	Set<BasicBlock> domSet = new ArraySet<BasicBlock>();
	        	domSet.add(outbb);
	        	boolean change = true;
	        	while(change){
	        		change = false;
	        		for(BasicBlock bb2 : allBBs) {
	        			Set<BasicBlock> addSet = new ArraySet<BasicBlock>();
	        			for(BasicBlock bb3 : domSet) {
		        			DominatorNode dNode = dominators.getDominatorNode(bb2);
		        			DominatorNode parent = dNode.getParent();
		        			if(parent != null) {
		        				BasicBlock parentBB = parent.getBasicBlock();
		        				if(parentBB.equals(bb3) && !domSet.contains(bb2)) { 
		        					addSet.add(bb2);
		        					change = true;
		        				}
		        			}
	        			}
	        			domSet.addAll(addSet);
	        		}
	        	}
	        	domMap.put(outbb, domSet);
        	}
    		map.put(method, domMap);
        }
        
        private void generateDominateSets(boolean dom, jq_Method method) {
        	Dominators dominators = new Dominators(dom);
    		dominators.visitMethod(method);
    		dominators.computeTree();
    		generateDominateSets(dominators, method, dom);
        }
        
        private List<BasicBlock> getControlDependentBasicBlocks(jq_Method method, BasicBlock bb) {
        	List<BasicBlock> ctrlDepdBBs = SharedData.ctrlDependence.get(new Pair<jq_Method,BasicBlock>(method,bb));
        	if (ctrlDepdBBs == null) {
        		ctrlDepdBBs = new ArrayList<BasicBlock>();
	        	Map<BasicBlock,Set<BasicBlock>> domMap = SharedData.domMap.get(method);
	        	if (domMap == null) {
	        		generateDominateSets(true, method);
	        		domMap = SharedData.domMap.get(method);
	        	}
	        	Map<BasicBlock,Set<BasicBlock>> pdomMap = SharedData.pdomMap.get(method);
	        	if (pdomMap == null) {
	        		generateDominateSets(false, method);
	        		pdomMap = SharedData.pdomMap.get(method);
	        	}
	        	Set<BasicBlock> successors = new ArraySet<BasicBlock>();
	        	successors.addAll(bb.getSuccessors());
	        	Set<List<BasicBlock>> paths = new ArraySet<List<BasicBlock>>();
	        	Map<BasicBlock,Integer> pathCounter = new HashMap<BasicBlock,Integer>();
	        	for(BasicBlock bb2 : successors){
	        		List<BasicBlock> path = new ArrayList<BasicBlock>();
	        		path.add(bb2);
	        		paths.add(path);
	        		pathCounter.put(bb2, 1);
	        	}
	        	Set<BasicBlock> handledLast = new HashSet<BasicBlock>();
	        	while(!paths.isEmpty()) {
	        		Set<List<BasicBlock>> rmvdPaths = new HashSet<List<BasicBlock>>();
	        		for (List<BasicBlock> path : paths) {
	        			BasicBlock last = path.get(path.size()-1);
	        			if(handledLast.contains(last))
	        				continue;
	        			Set<List<BasicBlock>> allPaths = new HashSet<List<BasicBlock>>();
	        			if(pathCounter.get(last) != last.getPredecessors().size()) {
	        				rmvdPaths.add(path);
	        			} else {
	        				for (List<BasicBlock> path2 : paths) {
	        					if (path2.get(path2.size()-1).equals(last))
	        						allPaths.add(path2);
	        				}
		        			boolean isCtrlDepd = true;
		        			if (allPaths.size() == 0 || pdomMap.get(last).contains(bb))
		        				isCtrlDepd = false;
		        			else {
			        			for(List<BasicBlock> path2 : allPaths) {
			        				for (BasicBlock bb2 : path2) {
			        					if (!pdomMap.get(last).contains(bb2)) {
			        						rmvdPaths.add(path2);
			        						isCtrlDepd = false;
			        						break;
			        					}
			        				}
			        				if(!isCtrlDepd) break;
			        			}
		        			}
		        			if(isCtrlDepd) {
		        				ctrlDepdBBs.add(last);
		        			}
	        			}
	        			handledLast.add(last);
	        		}
	    			paths.removeAll(rmvdPaths);
	    			rmvdPaths.clear();
	    			Set<List<BasicBlock>> addPaths = new ArraySet<List<BasicBlock>>();
	    			for(List<BasicBlock> path2 : paths) {
	    				BasicBlock last = path2.get(path2.size()-1);
	    				List<BasicBlock> lastSuccs = last.getSuccessors();
	    				for(BasicBlock lastSucc : lastSuccs) {
	    					if(handledLast.contains(lastSucc))
	    						continue;
	    					List<BasicBlock> newPath = new ArrayList<BasicBlock>(path2);
	    					newPath.add(lastSucc);
	    					addPaths.add(newPath);
	    					pathCounter.put(lastSucc, lastSucc.getNumberOfPredecessors());
	    				}
	    				rmvdPaths.add(path2);
	    			}
	    			paths.addAll(addPaths);	
	    			paths.removeAll(rmvdPaths);
	        	}
	        	ctrlDepdBBs.remove(bb);
	        	SharedData.ctrlDependence.put(new Pair<jq_Method,BasicBlock>(method,bb), ctrlDepdBBs);
        	} 
//        	System.out.println("For basic block: " + bb.toString());
//        	for(BasicBlock bb2: ctrlDepdBBs) { 
//        		System.out.println("Control dependent blocks: bb[" + bb2.toString() + "]");
//        	}
	        return ctrlDepdBBs;
        }
        
        private boolean isLoop(BasicBlock curBB, List<BasicBlock> ctrlDepdBBs) {
        	for (BasicBlock bb : ctrlDepdBBs) {
        		if(bb.getSuccessors().contains(curBB)) {
        			return true;
        		}
        	}
        	return false;
        }
        
        private boolean isPredicable(BasicBlock curBB, jq_Method method, List<BasicBlock> ctrlDepdBBs, int counter) {
        	if(counter == 30) { // FIXME workaround to avoid stack overflow exception. find a way to resolve the fundamental reason. 
        		System.out.println("isPredicable killed: counter is 10 -> method[" + method.toString() + "] bb[" + curBB.fullDump() + "]");
        		return false;
        	}
        	boolean isPredicatePossible = true;
        	if(isLoop(curBB, ctrlDepdBBs)) {
        		isPredicatePossible = false;
        	} else {
        		boolean isApproxOpExist = false; 
	        	for(BasicBlock bb : ctrlDepdBBs) {
	        		List<Quad> qList = bb.getQuads();
	        		for(Quad q : qList) {
	        			if (q.getOperator() instanceof IntIfCmp) { // Another If conditionals
	        				if (SharedData.approxIfConditional.get(q) != null) {
	        					if(SharedData.approxIfConditional.get(q) == false) {
	    	        				isPredicatePossible = false;
	    	        				break;
	        					}
	        				}
	        				isPredicatePossible = isPredicable(bb, method, getControlDependentBasicBlocks(method, bb), counter + 1);
	        				if(isPredicatePossible) {
	        					SharedData.approxIfConditional.put(q, true);
	        				} else {
	        					SharedData.approxIfConditional.put(q, false);
	        					isPredicatePossible = false;
	        					break;
	        				}
	        			} else if (q.getOperator() instanceof Invoke || q.getOperator() instanceof Return )  { // Invoke, Return FIXME: New and NewArray operator should be filtered as well
	        				isPredicatePossible = false;
	        				break;
	        			} else {
	        				if (abs.approxStatements.contains(SharedData.domP.indexOf(q)))
	        					isApproxOpExist = true;
	        			}
	        		}
	        		if(!isPredicatePossible)
	        			break;
	        	}
	        	if(isApproxOpExist == false)
	        		isPredicatePossible = false;
        	}
        	return isPredicatePossible;
        }
        
        /** A compare and branch instruction. */
        public void visitIntIfCmp(Quad obj) {
        	BasicBlock curBB = obj.getBasicBlock();
        	jq_Method method = obj.getBasicBlock().getMethod();
        	if(SharedData.approxIfConditional.get(obj) == null) {
        		String excludeStr = SharedData.excludeStr;
        		String[] excludes = excludeStr.split(",");
        		String classStr = obj.getBasicBlock().getMethod().getDeclaringClass().toString();
        		boolean isLib = false;
        		for (String exclude : excludes) {
        			if(classStr.startsWith(exclude)) {
        				isLib = true;
        				SharedData.approxIfConditional.put(obj, false);
        				break;
        			}
        		}
        		if(!isLib) {
		        	List<BasicBlock> ctrlDepdBBs = getControlDependentBasicBlocks(method, curBB);
		        	boolean isPredicatePossible = isPredicable(curBB, method, ctrlDepdBBs, 0);
		        	if(isPredicatePossible) {
		        		SharedData.approxIfConditional.put(obj, true);
		        	} else {
		        		SharedData.approxIfConditional.put(obj, false);
		        	}
        		}
        	}
        	Operand srcO1 = IntIfCmp.getSrc1(obj);
        	Operand srcO2 = IntIfCmp.getSrc2(obj);
        	if(srcO1 instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO1).getRegister());
        	if(srcO2 instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO2).getRegister());
        	if(SharedData.approxIfConditional.get(obj))
        		return;
        	if(srcO1 instanceof RegisterOperand){        		
        		Register srcR1 = ((RegisterOperand) srcO1).getRegister();
        		int src1Indx = SharedData.domU.indexOf(srcR1);
        		if(inNode.taintedVars.contains(src1Indx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        	if(srcO2 instanceof RegisterOperand){
        		Register srcR2 = ((RegisterOperand) srcO2).getRegister();
        		int src2Indx = SharedData.domU.indexOf(srcR2);
        		if(inNode.taintedVars.contains(src2Indx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        }
        
        /** A lookup switch instruction. */
        public void visitLookupSwitch(Quad obj) {
        	Operand src = LookupSwitch.getSrc(obj);
        	if (src instanceof RegisterOperand) {
            	// track unused approximate quads
            	if(src instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)src).getRegister());
        		Register srcR = ((RegisterOperand) src).getRegister();
        		int srcIndx = SharedData.domU.indexOf(srcR);
        		if (inNode.taintedVars.contains(srcIndx)) {
        			outNode = getErrState();
        			return;
        		}
        	}
        }
        
        /** A register move instruction. */
        public void visitMove(Quad obj) {
        	RegisterOperand dstOperand = Move.getDest(obj);
        	Register dstR = dstOperand.getRegister();
        	if(!dstR.getType().isPrimitiveType())
        		return;
        	int dstIndx = SharedData.domU.indexOf(dstR);
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	Operand srcO = Move.getSrc(obj);
        	if(isApprox(obj) || this.isOperandApproximate(srcO))
        		taintedVars.add(dstIndx);
        	else
        		taintedVars.remove(dstIndx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        	// track unused approximate quads
        	if(srcO instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO).getRegister());
			addRegToUnused(obj.getBasicBlock().getMethod(),dstR,obj);
        }
        
        /** A phi instruction. (For SSA.) */
        public void visitPhi(Quad obj) {
        	throw new RuntimeException("Use nophi option to remove phi statements");
        }
        
        /** A put instance field instruction. */
        public void visitPutfield(Quad obj) {
        	Register base = ((RegisterOperand)Putfield.getBase(obj)).getRegister();
        	jq_Field f = Putfield.getField(obj).getField();
        	int fIdx = SharedData.domF.indexOf(f);
        	this.processPutField(obj, base, fIdx, Putfield.getSrc(obj));
        }
        
        /** A put static field instruction. */
        public void visitPutstatic(Quad obj) {
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	jq_Field dst = Putstatic.getField(obj).getField();
        	if(!dst.getType().isPrimitiveType()) 
        		return;
        	int fIdx = SharedData.domF.indexOf(dst);
        	Operand srcO = Putstatic.getSrc(obj);
        	if(isApprox(obj) || this.isOperandApproximate(srcO))
        		taintedGlobals.add(fIdx);
        	else 
        		taintedGlobals.remove(fIdx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        	// track unused approximate quads
        	if(srcO instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO).getRegister());
        }
        
        /** A return from method instruction. */
        public void visitReturn(Quad obj) {
        	//let us handle exceptions unsoundly
			Set<Integer> taintedVars = new ArraySet<Integer>();
			Set<Integer> taintedGloabals = new ArraySet<Integer>(inNode.taintedGlobals);
			Set<Pair<Integer,Integer>> taintedFields = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
			boolean isRetTainted = false;
			Operand srcO = Return.getSrc(obj);
			if (obj.getOperator() instanceof THROW_A){
				
			}
			else if(srcO instanceof RegisterOperand) {
				Register tgtR = ((RegisterOperand) (Return.getSrc(obj)))
						.getRegister();
				//jspark: we don't have to care about return values from relax or restrict as tainted 
				if(inNode.taintedVars.contains(SharedData.domU.indexOf(tgtR)) 
						&& !SharedData.isRelaxMethod(obj.getMethod())    
						&& !SharedData.isRestrictMethod(obj.getMethod())
						&& !SharedData.isRelaxAllMethod(obj.getMethod()) 
						&& !SharedData.isRestrictAllMethod(obj.getMethod()))
					isRetTainted = true;
			}
			outNode = new AbstractState(taintedGloabals, taintedVars, taintedFields, isRetTainted, inNode.isErr);
        	// track unused approximate quads
        	if(srcO instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO).getRegister());
        }
        /** A jump table switch instruction. */
        public void visitTableSwitch(Quad obj) {
        	Operand src = TableSwitch.getSrc(obj);
        	if(src instanceof RegisterOperand){
            	// track unused approximate quads
            	if(src instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)src).getRegister());
        		Register srcR = ((RegisterOperand) src).getRegister();
        		int srcIndx = SharedData.domU.indexOf(srcR);
        		if(inNode.taintedVars.contains(srcIndx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        }
        /** A divide-by-zero check instruction. */
        public void visitZeroCheck(Quad obj) {}
        
        
        public void visitNew(Quad obj){
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	String excludeStr = SharedData.excludeStr;
        	String[] excludes = excludeStr.split(",");
        	OUT: for (Operand op : ((Quad) obj).getAllOperands()){
            	// track unused approximate quads
            	if(op instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)op).getRegister());
				if (op instanceof TypeOperand){
					TypeOperand top = (TypeOperand) op;
					for (String exclude : excludes) {
						if (top.toString().contains(exclude))
							continue OUT;
					}
					if (top.getType().isClassType()) {
						StringTokenizer st = new StringTokenizer(SharedData.formatClsName(top.toString())," ");
						jq_Class clazz = (jq_Class)jq_Type.read(st);
						jq_Field[] insFields = clazz.getDeclaredInstanceFields();
						for (jq_Field f : insFields) {
							if  (SharedData.domF.contains(f)) {
								Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(obj),SharedData.domF.indexOf(f));
								if (abs.approxStorage.contains(pair))
									taintedFs.add(pair);
							}
						}
						jq_Field[] stFields = clazz.getDeclaredStaticFields();
						for (jq_Field f : stFields) {
							if  (SharedData.domF.contains(f)) { 
								Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(obj),SharedData.domF.indexOf(f));
								if (abs.approxStorage.contains(pair))
									taintedFs.add(pair);
							}
						}
					}
				}
			}
        	outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        }
        
        /**
         * @author jspark
         */
        public void visitNewArray(Quad obj) {
        	Set<Integer> taintedGlobals = new ArraySet<Integer>(inNode.taintedGlobals);
        	Set<Integer> taintedVars = new ArraySet<Integer>(inNode.taintedVars);     	
        	Set<Pair<Integer,Integer>> taintedFs = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
        	List<RegisterOperand> roList = NewArray.getReg2(obj);
        	for (RegisterOperand srcO : roList) {
            	// track unused approximate quads
            	if(srcO instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO).getRegister());
	    		Register srcR = ((RegisterOperand) srcO).getRegister();
	    		int srcIndx = SharedData.domU.indexOf(srcR);
	    		if(inNode.taintedVars.contains(srcIndx)){
	    			outNode = getErrState();
	    			return;
	    		}
        	}
        	Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(obj),-1);
			if (abs.approxStorage.contains(pair))
				taintedFs.add(pair);
			outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        }
        
        /**
         * MultiNewArray is not sound in chord
         * But still, just return error state when tainted variables are used in indices
         * @author jspark
         */
        public void visitMultiNewArray(Quad obj) {
        	ParamListOperand po = MultiNewArray.getParamList(obj);
        	Operand srcO1, srcO2, srcO3;
        	srcO1 = srcO2 = srcO3 = null; //jspark: indices of MULTINEWARRAY
        	if (po.length() >= 1) 
        		srcO1 = po.get(0);
        	if (po.length() >= 2)
        		srcO2 = po.get(1);
        	if (po.length() >= 3)
        		srcO3 = po.get(2);
        	// track unused approximate quads
        	if(srcO1 instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO1).getRegister());
        	if(srcO2 instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO2).getRegister());
        	if(srcO3 instanceof RegisterOperand) rmRegFromUnused(obj.getBasicBlock().getMethod(),((RegisterOperand)srcO3).getRegister());
        	if(srcO1 != null && srcO1 instanceof RegisterOperand){
        		Register srcR1 = ((RegisterOperand) srcO1).getRegister();
        		int src1Indx = SharedData.domU.indexOf(srcR1);
        		if(inNode.taintedVars.contains(src1Indx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        	if(srcO2 != null && srcO2 instanceof RegisterOperand){
        		Register srcR2 = ((RegisterOperand) srcO2).getRegister();
        		int src2Indx = SharedData.domU.indexOf(srcR2);
        		if(inNode.taintedVars.contains(src2Indx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        	if(srcO3 != null && srcO3 instanceof RegisterOperand){
        		Register srcR3 = ((RegisterOperand) srcO3).getRegister();
        		int src3Indx = SharedData.domU.indexOf(srcR3);
        		if(inNode.taintedVars.contains(src3Indx)){
        			outNode = getErrState();
        			return;
        		}
        	}
        }
	}
}
