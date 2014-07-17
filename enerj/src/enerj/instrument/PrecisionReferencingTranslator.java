package enerj.instrument;

import enerj.PrecisionChecker;
import enerj.jchord.result.ExpaxJchordResult;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultOpEntry;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultParamsEntry;
import enerj.lang.Approx;
import enerj.lang.Context;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import javax.annotation.processing.ProcessingEnvironment;

import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;
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
        	if (PrecisionChecker.EXPAX_DEBUG){
        		count++;
        		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <isApprox> return true");
        	}
        } else {
        	if (PrecisionChecker.EXPAX_DEBUG){
        		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <isApprox> return false");
        	}
        }
        return ret;
    }

    
    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if (PrecisionChecker.EXPAX_DEBUG)
    			System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <visitClassDef> class name is changed to = " + curClassName);
    	}
    	curMethName = " ";
		if(PrecisionChecker.EXPAX_DEBUG)
			System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <visitClassDef> method name is changed to = " + curMethName);
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
        if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <visitMethodDef> method name is changed to " + curMethName);
        if(meth.getReturnType() != null)
        	curRetTypeName = meth.getReturnType().toString();
        else
        	curRetTypeName = "void";
        nodeParams = node.getParameters();
    	super.visitMethodDef(node);
    }
    
    /** 
     * (1) Find a bytecode info by matching {expaxBcInfo} and the current tree node
     * (2) Find an analysis result corressponding to the tree node. 
     * (3) If there is the node, return true, otherwise return false
     */
    public boolean expaxIsApprox(JCTree tree) {
    	if (PrecisionChecker.EXPAX_DEBUG)
    		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> start - tree = " + tree.toString());
    	if(PrecisionChecker.expaxBcInfo == null)
    		throw new RuntimeException("Error! expaxBcInfo is null");
    	if(PrecisionChecker.expaxJchordResult == null)
    		throw new RuntimeException("Error! expaxJchordResult is null");
    	
    	if(tree instanceof JCTree.JCIdent){
    		String typeStr = ((JCTree.JCIdent)tree).type.toString();
    		if(ExpaxJchordResult.approxClasses.contains(typeStr)){
    			if(PrecisionChecker.EXPAX_DEBUG) {
	    			System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> " + typeStr + " is one of approx classes");
	    			System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <expaxIsApprox> return true! count[" + (count++) + "]");
    			}
    			return true;
    		}
    	}
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
    
    @Override
    public JCTree.JCExpression createNewInitializer(JCTree.JCVariableDecl tree, JCTree.JCExpression boxedOldType,
            JCTree.JCExpression newType, JCTree.JCExpression init, boolean primitive) {
        // Was the old variable approximate?
        boolean approx;
        if(PrecisionChecker.ENERJ) {
        	if (PrecisionChecker.EXPAX_DEBUG)
        		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> " + tree.toString());
        	approx = isApprox(tree);
        } else {
        	if (PrecisionChecker.EXPAX_DEBUG)
        		System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> " + tree.toString());
        	if (nodeParams.contains(tree) && arrayAccessFlag == 0){
        		int index = nodeParams.indexOf(tree);
        		Set<ExpaxJchordResultParamsEntry> pResultSet = PrecisionChecker.expaxJchordResult.getResultParamsSet();
        		boolean find = false;
        		for (ExpaxJchordResultParamsEntry entry : pResultSet) {
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
				if(PrecisionChecker.EXPAX_DEBUG)
    				System.out.println("*** EXPAX_DEBUG[PrecisionReferencingTranslator]: <createNewInitializer> approxNameSet contains " + tree.toString());
				approx = true;
			} else {
	        	if(arrayAccessFlag == 0)
	        		approx = expaxIsApprox(tree);
	        	else 
	        		approx = false;
			}
        }

        if(approx){
        	if(PrecisionChecker.ENERJ){
        		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** ENERJ_APPROX(createNewInitializer): approx createNew = " + tree.toString());
        	} else {
        		if(PrecisionChecker.EXPAX_DEBUG) System.out.println("*** EXPAX_APPROX(createNewInitializer): approx createNew = " + tree.toString());
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
