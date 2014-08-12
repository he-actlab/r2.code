package chord.analyses.r2.metaback;

import java.util.HashSet;
import java.util.StringTokenizer;

import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operand.TypeOperand;
import joeq.Compiler.Quad.Operator.New.NEW;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.Operator.NewArray.NEWARRAY;
import joeq.Compiler.Quad.Quad;
import chord.analyses.alias.CICGAnalysis;
import chord.analyses.alias.CIPAAnalysis;
import chord.analyses.alloc.DomH;
import chord.analyses.basicblock.DomB;
import chord.analyses.basicblock.RelPostDomBB;
import chord.analyses.field.DomF;
import chord.analyses.parallelizer.JobDispatcher;
import chord.analyses.parallelizer.Mode;
import chord.analyses.parallelizer.ParallelAnalysis;
import chord.analyses.parallelizer.Scenario;
import chord.analyses.point.DomP;
import chord.program.Program;
import chord.project.Chord;
import chord.project.ClassicProject;
import chord.project.analyses.ProgramRel;
import chord.project.analyses.metaback.AbstractJobDispatcher;
import chord.project.analyses.metaback.QueryResult;
import chord.project.analyses.rhs.BackTraceIterator;
import chord.project.analyses.rhs.IWrappedPE;
import chord.util.DomU;
import chord.util.Utils;
import chord.util.dnf.ClauseSizeCMP;
import chord.util.dnf.DNF;
import chord.util.tuple.object.Pair;


@Chord(name = "r2-metaback-java", consumes = {"checkIncludedP"})
public class IterAnalysis extends ParallelAnalysis {
	private JobDispatcher dispatcher;
	private int iterations = 0;
	private double forwardTimeSum = 0.0;
	private double backwardTimeSum = 0.0;
	
	@Override
	public void init() {
		initSharedData();
		if (getMode() == Mode.MASTER)
			initMaster();
		else
			initWorker();
	}
	
	private void initSharedData(){
		SharedData.domP = (DomP) ClassicProject.g().getTrgt("P");
		ClassicProject.g().runTask(SharedData.domP);
		SharedData.domU = (DomU) ClassicProject.g().getTrgt("U");
		ClassicProject.g().runTask(SharedData.domU);
		SharedData.domF = (DomF) ClassicProject.g().getTrgt("F");
		ClassicProject.g().runTask(SharedData.domF);
		SharedData.domH = (DomH) ClassicProject.g().getTrgt("H");
		ClassicProject.g().runTask(SharedData.domH);
		SharedData.domB = (DomB) ClassicProject.g().getTrgt("B");
		ClassicProject.g().runTask(SharedData.domB);
		
		Program p = Program.g();
		SharedData.mainMethod = p.getMainMethod();
		SharedData.threadStartMethod = p.getThreadStartMethod();
		
		CICGAnalysis cicgAnalysis = (CICGAnalysis) ClassicProject.g().getTrgt("cicg-java");
		ClassicProject.g().runTask(cicgAnalysis);
		SharedData.cicg = cicgAnalysis.getCallGraph();
		
		SharedData.cipa = (CIPAAnalysis) ClassicProject.g().getTrgt("cipa-java");
		ClassicProject.g().runTask(SharedData.cipa);
		
		SharedData.exitMain = SharedData.mainMethod.getCFG().exit();
		
		ProgramRel checkIncludedP = (ProgramRel) ClassicProject.g().getTrgt(
				"checkIncludedP");
		checkIncludedP.load();
		
		SharedData.allApproxStatements = new HashSet<Integer>();
		SharedData.allApproxStorage = new HashSet<Pair<Integer,Integer>>();
		String excludeStr = System.getProperty("chord.check.exclude","java.,com.,sun.,sunw.,javax.,launchrer.,org.");
		String[] excludes = excludeStr.split(",");
		for(Object o : checkIncludedP.getAry1ValTuples()){
			if(o instanceof Quad ){
				if(SharedData.isQuadApproximable((Quad)o)){
					SharedData.allApproxStatements.add(SharedData.domP.indexOf(o));
					SharedData.indexQuadMap.put(new Integer(SharedData.domP.indexOf(o)), (Quad)o); 
				} 
				if(((Quad) o).getOperator() instanceof NEW) {
					OUT: for (Operand op : ((Quad) o).getAllOperands()){
						if (op instanceof TypeOperand){
							TypeOperand top = (TypeOperand) op;
							for (String exclude : excludes) {
								if (top.toString().contains(exclude))
									continue OUT;
							}
							if (top.getType().isClassType()) {
								StringTokenizer st = new StringTokenizer(SharedData.formatClsName(top.toString())," ");
								jq_Class clazz = (jq_Class)jq_Type.read(st);
								/* all approximable instance fields */ 
								jq_Field[] insFields = clazz.getDeclaredInstanceFields();
								INNEROUT1: for (jq_Field f : insFields) {
									String desc = SharedData.convertClassName(f.getDesc().toString());
									for (String exclude : excludes) {		
										if(desc.contains(exclude))
											continue INNEROUT1;
									}
									if  (SharedData.domF.contains(f)) {
										if(f.getType().isPrimitiveType()) {
											Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(o),SharedData.domF.indexOf(f));
											SharedData.allApproxStorage.add(pair);
											SharedData.idxFieldMap.put(pair, new Pair<Quad,jq_Field>((Quad)o,f));
											System.out.println("*** R2_IA: allApproxStorage += h(" + ((Quad)o).toString() + "," + SharedData.domH.indexOf(o)  
													+ ") f(" + f.getName() + "," + SharedData.domF.indexOf(f) + ")");
										}
									}
								}
								/* all approximable static fields */
								jq_Field[] stFields = clazz.getDeclaredStaticFields();
								INNEROUT2: for (jq_Field f : stFields) {
									String desc = SharedData.convertClassName(f.getDesc().toString());
									for (String exclude : excludes) {		
										if(desc.contains(exclude))
											continue INNEROUT2;
									}
									if  (SharedData.domF.contains(f)) { 
										if(f.getType().isPrimitiveType()){
											Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(o),SharedData.domF.indexOf(f));
											SharedData.allApproxStorage.add(pair);
											SharedData.idxFieldMap.put(pair, new Pair<Quad,jq_Field>((Quad)o,f));
											System.out.println("*** R2_IA: allApproxStorage += h(" + ((Quad)o).toString() + "," + SharedData.domH.indexOf(o)
													+ ") f(" + f.getName() + "," + SharedData.domF.indexOf(f) + ")");
										}
									}
								}
							}
						}
					}
				}
				if(((Quad) o).getOperator() instanceof NEWARRAY) {
					TypeOperand top = NewArray.getType((Quad)o);
					jq_Type type = ((jq_Array)top.getType()).getElementType();
					/* all approximable (primitive-type elements) arrays */
					if(type.isPrimitiveType()){
						Pair<Integer,Integer> pair = new Pair<Integer,Integer>(SharedData.domH.indexOf(o),-1);
						SharedData.allApproxStorage.add(pair);
						SharedData.idxFieldMap.put(pair, new Pair<Quad,jq_Field>((Quad)o,null));
						System.out.println("*** R2_IA: allApproxStorage += h(" + ((Quad)o).toString() + ") f(-1)");
					}
				}
			}
		}
		
		System.out.println("*** R2_COUNT - size of allApproxStatements BEFORE analysis: " + SharedData.allApproxStatements.size());
		System.out.println("*** R2_COUNT - size of allApproxStorage BEFORE analysis: " + SharedData.allApproxStorage.size());
		
		SharedData.readPreciseLst(); // jspark: read a file, precise.lst, which has a list of system functions such as println and print
		SharedData.readFieldTextFile(); // jspark: read field.txt and store the infomration into hfFieldMap and gFieldMap
	}
	
	private void initMaster(){
		dispatcher = new R2JobDispatcher(null,this);
	}
	
	private void initWorker(){
		MetaBackAnalysis.setErrSufSize(5);
		MetaBackAnalysis.setDNegation(true);
		MetaBackAnalysis.setPrune(true);
		MetaBackAnalysis.setDEBUG(true);
	}

	@Override
	public JobDispatcher getJobDispatcher() {
		return dispatcher;
	}

	@Override
	public String apply(String line) {
		iterations++;
		Scenario scenario = new Scenario(line,R2JobDispatcher.MAJOR_SEP);
		ExpAbstraction abs = (ExpAbstraction) ExpAbstractionFactory.singleton.genAbsFromStr(scenario.getIn());
		String qStrs[] = Utils.split(scenario.getOut(), R2JobDispatcher.MINOR_SEP, true,
				true, -1);
		assert(qStrs.length == 1);
		ExpQuery query = (ExpQuery) ExpQueryFactory.singleton.getQueryFromStr(qStrs[0]);
		ForwardAnalysis forward = new ForwardAnalysis(abs);
		forward.printApproxSize();
		double before, forwardTime, backwardTime;
		before = System.currentTimeMillis();
		forward.run();
		forwardTime = System.currentTimeMillis() - before;
		forwardTimeSum += forwardTime;
		IWrappedPE<Edge,Edge> errEdge = forward.getErrEdge(query);
		ExpQueryResult eqr = null;
		if(errEdge == null){
			eqr = new ExpQueryResult(query,QueryResult.PROVEN,null);
			forward.printApproxAfterSize(iterations, forwardTimeSum, backwardTimeSum); // jspark: when it's proven, print the statistic numbers
			forward.writeResultFile(); // jspark: write the analysis result into a file so that compiler can read it
		}else{
			BackTraceIterator<Edge,Edge> backIter = forward.getBackTraceIterator(errEdge);
			MetaBackAnalysis backward = new MetaBackAnalysis(getErrCondition(), backIter, abs);
			before = System.currentTimeMillis();
			DNF nc = backward.run();
			backwardTime = System.currentTimeMillis() - before;
			backwardTimeSum += backwardTime;
			assert(!nc.isFalse());
			eqr = new ExpQueryResult(query,QueryResult.REFINE,nc);
			System.out.println("R2_EXPERIMENT time for forward analysis = " + SharedData.formatTime(forwardTime));
			System.out.println("R2_EXPERIMENT time for backward analysis = " + SharedData.formatTime(backwardTime));
			System.out.println("R2_EXPERIMENT total time for iteration#" + iterations + " = " + SharedData.formatTime(forwardTime + backwardTime));
		}
		scenario.setOut(eqr.encode());
		scenario.setType(AbstractJobDispatcher.RESULT);
		return scenario.encode();
	}
	
	private DNF getErrCondition(){
		return new DNF(new ClauseSizeCMP(),ErrVariable.EV,BoolDomain.T);
	}

}
