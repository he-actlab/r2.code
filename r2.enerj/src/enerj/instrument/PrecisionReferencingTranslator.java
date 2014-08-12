package enerj.instrument;

import enerj.PrecisionChecker;
import enerj.jchord.result.R2JchordResult;
import enerj.jchord.result.R2JchordResult.R2JchordResultOpEntry;
import enerj.jchord.result.R2JchordResult.R2JchordResultParamsEntry;
import enerj.lang.Approx;
import enerj.lang.Context;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo.R2ASTNodeInfoEntry;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import checkers.runtime.instrument.ReferencingTranslator;
import checkers.types.AnnotatedTypeMirror;

// Tightly couples with the SimulationTranslator, this pass replaces all local
// variable references (and method parameters) with reference objects. This
// allows us to simulate pass-by-reference for instrumentation of local variable
// reads and writes.
public class PrecisionReferencingTranslator extends ReferencingTranslator<PrecisionChecker> {
		
	public int arrayAccessFlag = 0;
	
	public int count = 0;
	public String curClassName = " ";
	public String curMethName = " ";
	public String curRetTypeName = " ";
	public Map<String,Set<Name>> approxNameSet;
	public List<JCVariableDecl> nodeParams;
	
    public PrecisionReferencingTranslator(PrecisionChecker checker,
                                 ProcessingEnvironment env,
                                 TreePath p) {
        super(checker, env, p);

        // Use our references (which include an "approx" flag) instead of the
        // provided reference class. Should change this eventually .
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
        	ret = true;
        } else if (approxTrees.contains(tree)) {
        	ret = true;
        } else {
        	ret = false;
        }
        if (ret == true){
        	if (PrecisionChecker.R2_DEBUG){
        		count++;
        		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <isApprox> return true");
        	}
        } else {
        	if (PrecisionChecker.R2_DEBUG){
        		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <isApprox> return false");
        	}
        }
        return ret;
    }

    
    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if (PrecisionChecker.R2_DEBUG)
    			System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <visitClassDef> class name is changed to = " + curClassName);
    	}
    	curMethName = " ";
		if(PrecisionChecker.R2_DEBUG)
			System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <visitClassDef> method name is changed to = " + curMethName);
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
        if(PrecisionChecker.R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <visitMethodDef> method name is changed to " + curMethName);
        if(meth.getReturnType() != null)
        	curRetTypeName = meth.getReturnType().toString();
        else
        	curRetTypeName = "void";
        nodeParams = node.getParameters();
    	super.visitMethodDef(node);
    }
    
    /** 
     * (1) Find a bytecode info by matching {r2BcInfo} and the current tree node
     * (2) Find an analysis result corressponding to the tree node. 
     * (3) If there is the node, return true, otherwise return false
     */
    public boolean r2IsApprox(JCTree tree) {
    	if (PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> start - tree = " + tree.toString());
    	if(PrecisionChecker.r2BcInfo == null)
    		throw new RuntimeException("Error! r2BcInfo is null");
    	if(PrecisionChecker.r2JchordResult == null)
    		throw new RuntimeException("Error! r2JchordResult is null");
    	
    	if(tree instanceof JCTree.JCIdent){
    		String typeStr = ((JCTree.JCIdent)tree).type.toString();
    		if(R2JchordResult.approxClasses.contains(typeStr)){
    			if(PrecisionChecker.R2_DEBUG) {
	    			System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> " + typeStr + " is one of approx classes");
	    			System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> return true! count[" + (count++) + "]");
    			}
    			return true;
    		}
    	}
    	Set<R2ASTNodeInfoEntry> bcInfoSet = PrecisionChecker.r2BcInfo.getInfoSet();
    	Set<R2JchordResultOpEntry> jResultSet = PrecisionChecker.r2JchordResult.getResultOpSet();
    	// find a bc info generated in 1st phase compilation
    	boolean astFound = false;
    	for (R2ASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			astFound = true;
    			if (PrecisionChecker.R2_DEBUG)
    				System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> [MATCHED] AST info b/w 1st and 2nd compilation paths");
    			// found a same tree node, now match an analysis result with this node
    			for(R2JchordResultOpEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					if (PrecisionChecker.R2_DEBUG) {
	    					count ++;
    						System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> [MATCHED] jchord entry with AST info");
    						System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> return true! count[" + count + "]");
    					}
    					return true;
    				}
    			}
    		}
    	}
    	if(PrecisionChecker.R2_DEBUG){
	    	if(!astFound)
	    		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> [NOT MATCHED] any AST info NOT matched b/w 1st and 2nd compilation paths");
	    	else
	    		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> [NOT MATCHED] any jchord entry NOT matched with AST info");
	    	System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <r2IsApprox> return false!");
    	}
    	return false;
    }
    
    @Override
    public JCTree.JCExpression createNewInitializer(JCTree.JCVariableDecl tree, JCTree.JCExpression boxedOldType,
            JCTree.JCExpression newType, JCTree.JCExpression init, boolean primitive) {
        // Was the old variable approximate?
        boolean approx;
        if(PrecisionChecker.ENERJ) {
        	if (PrecisionChecker.R2_DEBUG)
        		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> " + tree.toString());
        	approx = isApprox(tree);
        } else {
        	if (PrecisionChecker.R2_DEBUG)
        		System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> " + tree.toString());
        	if (nodeParams.contains(tree) && arrayAccessFlag == 0){
        		int index = nodeParams.indexOf(tree);
        		Set<R2JchordResultParamsEntry> pResultSet = PrecisionChecker.r2JchordResult.getResultParamsSet();
        		boolean find = false;
        		for (R2JchordResultParamsEntry entry : pResultSet) {
        			if (entry.compare(curClassName, curMethName, curRetTypeName)) {
        				find = entry.isApproxParam(index);
        				break;
        			}
        		}
        		if(find) 
        			approx = true;
        		else 
        			approx = false;
        	} else if(approxNameSet.containsKey(curMethName) && approxNameSet.get(curMethName).contains(tree.getName()) && arrayAccessFlag == 0) {
				if(PrecisionChecker.R2_DEBUG)
    				System.out.println("*** R2_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> approxNameSet contains " + tree.toString());
				approx = true;
			} else {
	        	if(arrayAccessFlag == 0)
	        		approx = r2IsApprox(tree);
	        	else 
	        		approx = false;
			}
        }

        if(approx){
        	if(PrecisionChecker.ENERJ){
        		if(PrecisionChecker.R2_DEBUG) System.out.println("*** ENERJ_APPROX(createNewInitializer): approx createNew = " + tree.toString());
        	} else {
        		if(PrecisionChecker.R2_DEBUG) System.out.println("*** R2_APPROX(createNewInitializer): approx createNew = " + tree.toString());
        	}
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
