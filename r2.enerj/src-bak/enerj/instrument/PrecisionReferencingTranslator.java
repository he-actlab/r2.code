package enerj.instrument;

import enerj.PrecisionChecker;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultOpEntry;
import enerj.lang.Approx;
import enerj.lang.Context;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import java.util.Set;
import java.util.HashSet;

import checkers.runtime.instrument.ReferencingTranslator;
import checkers.types.AnnotatedTypeMirror;

// Tightly couples with the SimulationTranslator, this pass replaces all local
// variable references (and method parameters) with reference objects. This
// allows us to simulate pass-by-reference for instrumentation of local variable
// reads and writes.
public class PrecisionReferencingTranslator extends ReferencingTranslator<PrecisionChecker> {
	
	public static final boolean EXPAX_PRT = false;
	
	public int count = 0;
	public String curClassName = "";
	public String curMethName = "";
	public String curRetTypeName = "";
	
    public PrecisionReferencingTranslator(PrecisionChecker checker,
                                 ProcessingEnvironment env,
                                 TreePath p) {
        super(checker, env, p);

        // Use our references (which include an "approx" flag) instead of the
        // provided reference class. Should change this eventually (FIXME).
        REFERENCE_CLASS = enerj.rt.Reference.class.getName();
    }

    // An *extremely hacky* way to make a few more trees behave approximately
    // than are those that annotated by the atypeFactory.
    protected static Set<JCTree> approxTrees = new HashSet<JCTree>();

    protected boolean isApprox(JCTree tree) {
    	boolean ret;
        AnnotatedTypeMirror treeType = atypeFactory.getAnnotatedType(tree);
        if (treeType.hasAnnotation(Approx.class)) {
        	ret = true;
        } else if (treeType.hasAnnotation(Context.class)) {
        	ret = true; // TODO! Look up precision from runtime index.
        } else if (approxTrees.contains(tree)) {
        	ret = true;
        } else {
        	ret = false;
        }
        if (ret == true){
        	if (EXPAX_PRT){
        		count++;
        		System.out.println("*** EXPAX_PRT: approx is true = " + tree.getTag().toString());
        		System.out.println("*** EXPAX_PRT: approx is true = " + tree.toString());
        		System.out.println("*** EXPAX_PRT: isApprox return true: count = " + count);
        	}
        } else {
        	if (EXPAX_PRT){
        		System.out.println("*** EXPAX_PRT: approx is false = " + tree.getTag().toString());
        		System.out.println("*** EXPAX_PRT: approx is false = " + tree.toString());
        	}
        }
        return ret;
    }

    
    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if (EXPAX_PRT)
    			System.out.println("*** EXPAX_PRT: class name is changed to = " + curClassName);
    	}
    	super.visitClassDef(node);
    }

    /** get curMethName and curRetTypeName */
    public void visitMethodDef(JCTree.JCMethodDecl node) {
    	curMethName = node.sym.toString();
    	if (EXPAX_PRT)
    		System.out.println("*** EXPAX_PRT: method name is changed to = " + curMethName);
    	if(node.getReturnType() != null) { 
            curRetTypeName = node.getReturnType().toString();
            if (EXPAX_PRT)
            	System.out.println("*** EXPAX_PRT: return type name is changed to = " + curRetTypeName);
    	}
    	else // constructor or destructor
    		curRetTypeName = "";
    	super.visitMethodDef(node);
    }
    
    /** 
     * (1) Find a bytecode info by matching {expaxBcInfo} and the current tree node
     * (2) Find an analysis result corressponding to the tree node. 
     * (3) If there is the node, return true, otherwise return false
     */
    public boolean expaxIsApprox(JCTree tree) {
    	if (EXPAX_PRT)
    		System.out.println("*** EXPAX_PRT: expaxIsApprox - tree = " + tree.toString());
    	if(PrecisionChecker.expaxBcInfo == null)
    		throw new RuntimeException("Error! expaxBcInfo is null");
    	if(PrecisionChecker.expaxJchordResult == null)
    		throw new RuntimeException("Error! expaxJchordResult is null");
		
    	Set<ExpaxASTNodeInfoEntry> bcInfoSet = PrecisionChecker.expaxBcInfo.getInfoSet();
    	Set<ExpaxJchordResultOpEntry> jResultSet = PrecisionChecker.expaxJchordResult.getResultOpSet();
    	
    	// find a bc info generated in 1st phase compilation
    	for (ExpaxASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			if (EXPAX_PRT)
    				System.out.println("*** EXPAX_PRT: AST info matched");
    			// found a same tree node, now match an analysis result with this node
    			for(ExpaxJchordResultOpEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					if (EXPAX_PRT) {
	    					count ++;
	    					System.out.println("*** EXPAX_PRT: expaxIsApprox return true = " + count);
	    					System.out.println("*** EXPAX_PRT: jchord result = " + result.toString());
    					}
    					return true;
    				}
    			}
    		}
    	}
    	if (EXPAX_PRT)
    		System.out.println("*** EXPAX_PRT: expaxIsApprox return false!");
    	return false;
    }
    
    @Override
    public JCTree.JCExpression createNewInitializer(JCTree.JCVariableDecl tree, JCTree.JCExpression boxedOldType,
            JCTree.JCExpression newType, JCTree.JCExpression init, boolean primitive) {
        // Was the old variable approximate?
        boolean approx;
        if(checker.ENERJ) {
        	if (EXPAX_PRT)
        		System.out.println("*** EXPAX_PRT: ENERJ");
        	approx = isApprox(tree);
        } else {
        	if (EXPAX_PRT)
        		System.out.println("*** EXPAX_PRT: EXPAX");
        	approx = expaxIsApprox(tree);
        }

        JCTree.JCExpression newInit = maker.NewClass(
            null,
            List.of(boxedOldType),
            newType,
            List.<JCTree.JCExpression>of(
                init,
                boolExp(approx),
                boolExp(primitive)
            ),
            null
        );

        return newInit;
    }

}
