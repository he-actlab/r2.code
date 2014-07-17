package expax.instrument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.runtime.instrument.HelpfulTreeTranslator;
import checkers.runtime.instrument.ReferencingTranslator;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

import enerj.PrecisionChecker;
import enerj.instrument.PrecisionReferencingTranslator;
import enerj.jchord.result.ExpaxJchordResult;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultOpEntry;

public class GenerateApproxLocalVariableTranslator extends HelpfulTreeTranslator<PrecisionChecker>{
	
	public int count = 0;
	public String curClassName = " ";
	public String curMethName = " ";
	public String curRetTypeName = " ";
	public Map<String,Set<Name>> approxNameSet;
	
	public GenerateApproxLocalVariableTranslator(PrecisionChecker checker, ProcessingEnvironment env, TreePath p) {
		super(checker, env, p);
		this.approxNameSet = new HashMap<String,Set<Name>>();
	}
	
	public void visitUnary(JCTree.JCUnary node) {
		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitUnary> start");
		boolean approx = expaxIsApprox(node);
		if(approx) {
			if(node.arg instanceof JCTree.JCIdent) {
				JCTree.JCIdent ident = (JCTree.JCIdent) node.arg;
				if (!approxNameSet.containsKey(curMethName)) {
					Set<Name> newSet = new HashSet<Name>();
					approxNameSet.put(curMethName, newSet);
				}
				(approxNameSet.get(curMethName)).add(ident.getName());
				if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitUnary> add to approxNameSet = " + ident.getName().toString() + " is added to approxNameSet (method=" + curMethName + ")");
			}
		}
		super.visitUnary(node);
	}
	
	public void visitAssign(JCTree.JCAssign node) {
		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitAssign> start");
		boolean approx = expaxIsApprox(node);
		if(approx){
			JCTree.JCExpression lhs = node.lhs;
			if(lhs instanceof JCTree.JCIdent) {
				JCTree.JCIdent ident = (JCTree.JCIdent) lhs;
				if (!approxNameSet.containsKey(curMethName)) {
					Set<Name> newSet = new HashSet<Name>();
					approxNameSet.put(curMethName, newSet);
				}
				(approxNameSet.get(curMethName)).add(ident.getName());
				if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitAssign> add to approxNameSet = " + ident.getName().toString() + " is added to approxNameSet (method=" + curMethName + ")");
			}
		}
		super.visitAssign(node);
	}
	
	public void visitAssignop(JCTree.JCAssignOp node) {
		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitAssignop> start");
		boolean approx = expaxIsApprox(node);
		if(approx){
			JCTree.JCExpression lhs = node.lhs;
			if(lhs instanceof JCTree.JCIdent) {
				JCTree.JCIdent ident = (JCTree.JCIdent) lhs;
				if (!approxNameSet.containsKey(curMethName)) {
					Set<Name> newSet = new HashSet<Name>();
					approxNameSet.put(curMethName, newSet);
				}
				(approxNameSet.get(curMethName)).add(ident.getName());
				if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitAssignop> add to approxNameSet = " + ident.getName().toString() + " is added to approxNameSet (method=" + curMethName + ")");
			}
		}
		super.visitAssignop(node);
	}
	
	public void visitVarDef(JCTree.JCVariableDecl node) {
		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitVarDef> start");
		boolean approx = expaxIsApprox(node);
		if(approx){
			if (!approxNameSet.containsKey(curMethName)) {
				Set<Name> newSet = new HashSet<Name>();
				approxNameSet.put(curMethName, newSet);
			}
			(approxNameSet.get(curMethName)).add(node.getName());
			if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitVarDef> add to approxNameSet = " + node.getName().toString() + " is added to approxNameSet (method=" + curMethName + ")");
		}
		super.visitVarDef(node);
	}
	
	/** 
     * (1) Find a bytecode info by matching {expaxBcInfo} and the current tree node
     * (2) Find an analysis result corressponding to the tree node. 
     * (3) If there is the node, return true, otherwise return false
     */
    public boolean expaxIsApprox(JCTree tree) {
    	if (PrecisionChecker.EXPAX_DEBUG)
    		System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <expaxIsApprox> start - tree = " + tree.toString());
    	if(PrecisionChecker.expaxBcInfo == null)
    		throw new RuntimeException("Error! expaxBcInfo is null");
    	if(PrecisionChecker.expaxJchordResult == null)
    		throw new RuntimeException("Error! expaxJchordResult is null");
   
    	Set<ExpaxASTNodeInfoEntry> bcInfoSet = PrecisionChecker.expaxBcInfo.getInfoSet();
    	Set<ExpaxJchordResultOpEntry> jResultSet = PrecisionChecker.expaxJchordResult.getResultOpSet();
    	// find a bc info generated in 1st phase compilation
    	boolean astFound = false;
    	for (ExpaxASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			astFound = true;
    			if (PrecisionChecker.EXPAX_DEBUG)
    				System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> [MATCHED] AST info b/w 1st and 2nd compilation paths");
    			// found a same tree node, now match an analysis result with this node
    			for(ExpaxJchordResultOpEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					if (PrecisionChecker.EXPAX_DEBUG) {
	    					count ++;
    						System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> [MATCHED] jchord entry with AST info");
    						System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> return true! count[" + count + "]");
    					}
    					return true;
    				}
    			}
    		}
    	}
    	if(PrecisionChecker.EXPAX_DEBUG){
	    	if(!astFound)
	    		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> [NOT MATCHED] any AST info NOT matched b/w 1st and 2nd compilation paths");
	    	else
	    		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> [NOT MATCHED] any jchord entry NOT matched with AST info");
	    	System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> return false!");
    	}
    	return false;
    }
    
    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if (PrecisionChecker.EXPAX_DEBUG)
    			System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitClassDef> class name is changed to = " + curClassName);
    	}
    	curMethName = " ";
		if(PrecisionChecker.EXPAX_DEBUG)
			System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitClassDef> method name is changed to = " + curMethName);
    	curRetTypeName = "void";
    	super.visitClassDef(node);
    }

    /** get curMethName and curRetTypeName */
    public void visitMethodDef(JCTree.JCMethodDecl node) {
    	MethodSymbol meth = node.sym;
        curMethName = node.getName().toString();
        if(!(curMethName.equalsIgnoreCase("<init>") || curMethName.equalsIgnoreCase("<clinit>"))){
        	if (curMethName.equalsIgnoreCase("__htt_staticInitializerMethod")) 
            	curMethName = "<clinit>()";
            else
            	curMethName = node.sym.toString();
        } else {
        	int index = node.sym.toString().indexOf((int)'(');
        	String params = node.sym.toString().substring(index);
        	curMethName += params;
        }
        if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[GenerateApproxLocalVariableTranslator]: <visitMethodDef>: method name is changed to " + curMethName);
        if(meth.getReturnType() != null)
        	curRetTypeName = meth.getReturnType().toString();
        else
        	curRetTypeName = "void";
    	super.visitMethodDef(node);
    }
}
