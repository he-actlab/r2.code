package chord.analyses.expax.metaback;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	
	public ForwardAnalysis(ExpAbstraction abs){
		this.abs = abs;
	}
	
	@Override
	public void run() {
		init();
		Timer timer = new Timer("forward-timer");
		timer.init();
		try{
		System.out.println("Approximated operations: ");
		for(Integer i : abs.approxStatements){
			Inst inst = SharedData.domP.get(i);
			System.out.println(inst.toVerboseStr());
		}
		System.out.println("Approximated storage: ");
		for(Pair<Integer,Integer> pair : abs.approxStorage) {
			Pair<Quad,jq_Field> qj = SharedData.idxFieldMap.get(pair);
			if(qj.val1 == null)
				System.out.println("h(" + qj.val0.toString() + "," + pair.val0 + ") f(ARRAY,-1)");
			else
				System.out.println("h(" + qj.val0.toString() + "," + pair.val0 + ") f(" + qj.val1.toString() + "," + pair.val1 + ")");
		}
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

	
	@Override
	public Edge getInitPathEdge(Quad q, jq_Method m, Edge pe) {        
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
    			if (SharedData.EXPAX_LOG)
    				System.out.println("*** EXPAX_LOG: ERROR! preciseLst - quad(" + invoke.toString() + ")");
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
		if(SharedData.isPreciseMethod(m)){
			for (int i = 0; i < args.length(); i++) {
				Register reg = args.get(i).getRegister();
				Integer ridx = SharedData.domU.indexOf(reg);
				if(oldtvs.contains(ridx)){
					return this.getErrState();
				}
			}
		}
		
		// jspark: precise_all_FIELDX_TAGY
		if (SharedData.isPreciseAllMethod(m)) {
	    	if (args.length() == 0) { // static field
	    		int gidx = SharedData.getFieldIdx(m);
	    		if(SharedData.EXPAX_LOG)
	    			System.out.println("*** EXPAX_LOG: [Forward] precise_all - oldtgs.toString = " + oldtgs.toString());
	    		if (oldtgs.contains(gidx)) {
					if(SharedData.EXPAX_LOG)
						System.out.println("*** EXPAX_LOG: [Forward] precise_all - ERROR! global (" + gidx + ") quad(" + invoke.toString() + ")");
					return this.getErrState();	
	    		}
	    	} else if (args.length() > 0) { // arrays or object field
				Register reg = args.get(args.length()-1).getRegister(); // last argument is a real object 
			    if (!reg.getType().isPrimitiveType()){ 
					Set<Quad> allocSites = SharedData.cipa.pointsTo(reg).pts;
					for (Quad h : allocSites) {
						String tag = SharedData.getTag(m);
			        	if (!tag.equalsIgnoreCase(SharedData.allocToTagMap.get(h)))
			        		continue; // not remove a field from tainted set when the tag is unmatched
						int hidx = SharedData.domH.indexOf(h);
						int fidx = SharedData.getFieldIdx(h, m);
						Pair<Integer, Integer> hf = new Pair<Integer, Integer>(hidx,fidx);
						if(SharedData.EXPAX_LOG){
							System.out.println("*** EXPAX_LOG: [Forward] precise_all - allocation site h = " + h.toString());
							System.out.println("*** EXPAX_LOG: [Forward] precise_all - oldtfs = " + oldtfs.toString());
						}
						if (oldtfs.contains(hf)){
							if(SharedData.EXPAX_LOG)
								System.out.println("*** EXPAX_LOG: [Forward] precise_all - ERROR! array or object.field (" + hf.toString() + ") quad(" + invoke.toString() + ")");
							return this.getErrState();
						}
					}
			    } else {
		        	throw new RuntimeException("Error! precise_all shouldn't have a primitive-type parameter");
		        }
	    	} 
		}
		
		// jspark: accept_all_FIELDX_TAGY
	    if(SharedData.isAcceptAllMethod(m)) {
	    	if (args.length() == 0){
	    		int gidx = SharedData.getFieldIdx(m);
	    		if(SharedData.EXPAX_LOG)
	    			System.out.println("*** EXPAX_LOG: [Forward] accept_all - oldtgs = " + newtgs.toString());
	    		newtgs.remove(gidx);
	    		if(SharedData.EXPAX_LOG)
	    			System.out.println("*** EXPAX_LOG: [Forward] accept_all - newtgs = " + newtgs.toString());
	    	} else if (args.length() > 0) {
	    		Register reg = args.get(args.length()-1).getRegister(); 
		        if (!reg.getType().isPrimitiveType()) { // arrays or class objects
			        Set<Quad> allocSites = SharedData.cipa.pointsTo(reg).pts;
			        for (Quad h: allocSites) {
			        	String tag = SharedData.getTag(m);
			        	if (!tag.equalsIgnoreCase(SharedData.allocToTagMap.get(h)))
			        		continue;
			        	int hidx = SharedData.domH.indexOf(h);
			        	int fidx = SharedData.getFieldIdx(h, m);
			        	Pair<Integer,Integer> hf = new Pair<Integer,Integer>(hidx,fidx);
		        		if(SharedData.EXPAX_LOG){
		        			System.out.println("*** EXPAX_LOG: [Forward] accept_all - allocation site h = " + h.toString());
		        			System.out.println("*** EXPAX_LOG: [Forward] accept_all - oldtfs = " + newtfs.toString());
		        			if (newtfs.contains(hf))
		        				if(SharedData.EXPAX_LOG)
		        					System.out.println("*** EXPAX_LOG: [Forward] accept_all - REMOVE array or class objects (" + hf.toString() + ") quad(" + invoke.toString() + ")");
		        		}
			        	newtfs.remove(hf);
			        	if(SharedData.EXPAX_LOG){
			        		System.out.println("*** EXPAX_LOG: [Forward] accept_all - newtfs = " + newtfs.toString());
			        	}
			        	
			        }
		        } else 
		        	throw new RuntimeException("Error! accept_all shouldn't have a primitive-type parameter");
	    	} 
	    }

	    // jspark: alloc_TAGX
	    if(SharedData.isAllocMethod(m)) {
	    	SharedData.parseAllocTag(m);
	    }
	    
	    // jspark: store approximate parameters
	    String excludeStr = System.getProperty("chord.check.exclude","java.,com.,sun.,sunw.,javax.,launchrer.,org.");
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
		
		// jspark:
		// even in error state, the forward analysis should visit allocation quads to assign TAGs
    	// when an object (or an array) is created, create a mapping between the NEW quad and allocTag
		if (q.getOperator() instanceof NEW ||
			q.getOperator() instanceof NEWARRAY ||
			q.getOperator() instanceof MULTINEWARRAY) {
			// if this allocTag is the one right before this allocation
        	if (!SharedData.allocTag.equalsIgnoreCase("")) {
        		SharedData.allocToTagMap.put(q, SharedData.allocTag);
        		SharedData.allocTag = ""; // initialize tag to avoid that other allocating quad use this TAG
        	}
		}	
		
		if(!visitor.outNode.isErr) //we do not need to anything abour \top
			q.accept(visitor); 
		Operator op = q.getOperator();
		
		// jspark: for debug
		if (op instanceof MultiNewArray)
			System.out.println("*** MultiNewArray: " + q.getBasicBlock().getMethod().toString() + " ***");
		
		Edge ret = new Edge(pe.srcNode,visitor.outNode);
		return ret;
	}

	@Override
	public Edge getInvkPathEdge(Quad q, Edge clrPE, jq_Method m, Edge tgtSE) {
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
		if (SharedData.isAcceptMethod(m)) {
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
	public void printApproxAfterSize() {
		System.out.println("*** EXPAX_COUNT - Finished! size of approxStatements AFTER analysis: " + abs.approxStatements.size());
		System.out.println("*** EXPAX_COUNT - Finished! size of approxStorage AFTER analysis: " + abs.approxStorage.size());
	}
	
	/**
	 * for debug
	 * @author jspark
	 */
	public void printApproxSize() {
		System.out.println("*** EXPAX_COUNT - approxStatements size: " + abs.approxStatements.size());
		System.out.println("*** EXPAX_COUNT - approxStorage size: " + abs.approxStorage.size());
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
	final String EXPAXSEP = "#";
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
				afterWriter.write(q.toVerboseStr() + "\n");
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
				resultWriter.write(q.getBCI() + EXPAXSEP);
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
				resultWriter.write(q.getBCI() + EXPAXSEP);
				resultWriter.write(q.toString() + EXPAXSEP);
				resultWriter.write(fieldStr + EXPAXSEP);
				resultWriter.write(descStr + EXPAXSEP);
				resultWriter.write(declClassStr + '\n');
			}
			/** approximate parameters **/
			Set<jq_Method> keyset = SharedData.approxParams.keySet();
			int size = 0;
			for (jq_Method method : keyset) {
				if(SharedData.isAcceptMethod(method) ||
						   SharedData.isAcceptMethod(method) ||
						   SharedData.isPreciseMethod(method) ||
						   SharedData.isPreciseAllMethod(method))
					continue;
				Set<Integer> indexSet = SharedData.approxParams.get(method);
				if(indexSet.isEmpty())
					continue;
				size++;
			}
			resultWriter.write(size + "\n");
			for (jq_Method method : keyset) {
				if(SharedData.isAcceptMethod(method) ||
						   SharedData.isAcceptMethod(method) ||
						   SharedData.isPreciseMethod(method) ||
						   SharedData.isPreciseAllMethod(method))
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
		resultWriter.write(className + EXPAXSEP);
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
			resultWriter.write(types[i] + ")" + EXPAXSEP);
		} else{
			resultWriter.write(")" + EXPAXSEP);
		}
		resultWriter.write(method.getReturnType().toString() + EXPAXSEP);
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
					if(SharedData.EXPAX_LOG)
						System.out.println("*** EXPAX_LOG: [Forward] addPreciseVariables: " + invoke.toString());
					abs = this.getErrState();
					if(SharedData.EXPAX_LOG)
						System.out.println("*** EXPAX_LOG: [Forward] getErrState(addPreciseVariables) - quad(" + invoke.toVerboseStr() + ")");
					return new Edge(abs,abs);
				}
			}
		}
		return pe;
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
        	if(isApprox(obj) || this.isOperandApproximate(Binary.getSrc1(obj)) || this.isOperandApproximate(Binary.getSrc2(obj)))
        		tvs.add(dstIndx);
        	else 
        		tvs.remove(dstIndx);
        	outNode = new AbstractState(tgs,tvs,tfs,inNode.taintedRet,inNode.isErr);
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
        	if(isApprox(obj) || this.isOperandApproximate(Unary.getSrc(obj)))
        		taintedVars.add(dstIndx);
        	else
        		taintedVars.remove(dstIndx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
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
        }
        
        /** A compare and branch instruction. */
        public void visitIntIfCmp(Quad obj) {
        	Operand srcO1 = IntIfCmp.getSrc1(obj);
        	Operand srcO2 = IntIfCmp.getSrc2(obj);
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
        	if(isApprox(obj) || this.isOperandApproximate(Move.getSrc(obj)))
        		taintedVars.add(dstIndx);
        	else
        		taintedVars.remove(dstIndx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
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
        	if(isApprox(obj) || this.isOperandApproximate(Putstatic.getSrc(obj)))
        		taintedGlobals.add(fIdx);
        	else 
        		taintedGlobals.remove(fIdx);
        	this.outNode = new AbstractState(taintedGlobals, taintedVars, taintedFs, inNode.taintedRet, inNode.isErr);
        }
        
        /** A return from method instruction. */
        public void visitReturn(Quad obj) {
        	//let us handle exceptions unsoundly
			Set<Integer> taintedVars = new ArraySet<Integer>();
			Set<Integer> taintedGloabals = new ArraySet<Integer>(inNode.taintedGlobals);
			Set<Pair<Integer,Integer>> taintedFields = new HashSet<Pair<Integer,Integer>>(inNode.taintedFields);
			boolean isRetTainted = false;
			if (obj.getOperator() instanceof THROW_A){
				
			}
			else if (Return.getSrc(obj) instanceof RegisterOperand) {
				Register tgtR = ((RegisterOperand) (Return.getSrc(obj)))
						.getRegister();
				//jspark: we don't have to care about return values from accept or precise as tainted 
				if(inNode.taintedVars.contains(SharedData.domU.indexOf(tgtR)) 
						&& !SharedData.isAcceptMethod(obj.getMethod())    
						&& !SharedData.isPreciseMethod(obj.getMethod())
						&& !SharedData.isAcceptAllMethod(obj.getMethod()) 
						&& !SharedData.isPreciseAllMethod(obj.getMethod()))
					isRetTainted = true;
			}
			outNode = new AbstractState(taintedGloabals, taintedVars, taintedFields, isRetTainted, inNode.isErr);
        }
        /** A jump table switch instruction. */
        public void visitTableSwitch(Quad obj) {
        	Operand src = TableSwitch.getSrc(obj);
        	if(src instanceof RegisterOperand){
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
        	String excludeStr = System.getProperty("chord.check.exclude","java.,com.,sun.,sunw.,javax.,launchrer.,org.");
        	String[] excludes = excludeStr.split(",");
        	OUT: for (Operand op : ((Quad) obj).getAllOperands()){
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
