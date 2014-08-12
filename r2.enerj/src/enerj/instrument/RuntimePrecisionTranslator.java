package enerj.instrument;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.runtime.instrument.HelpfulTreeTranslator;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo.R2ASTNodeInfoEntry;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;

import enerj.PrecisionChecker;
import enerj.jchord.result.R2JchordResult.R2JchordResultStorageEntry;

// Adds calls to the runtime system to keep track of the dynamic precision
// state of each object as it is instantiated.
public class RuntimePrecisionTranslator extends HelpfulTreeTranslator<PrecisionChecker> {
	
	private boolean ENERJ;
	
	public int count = 0;
	public String curClassName = " ";
	public String curMethName = " ";
	public String curRetTypeName = " ";
	
    public RuntimePrecisionTranslator(PrecisionChecker checker,
                                      ProcessingEnvironment env,
                                      TreePath p,
                                      boolean ENERJ) {
        super(checker, env, p);
        this.ENERJ = ENERJ;
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
    	if (PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitNewClass> " + tree.toString());
    	super.visitNewClass(tree);
    	if (tree.clazz instanceof JCTree.JCIdent) {
    		Symbol sym = ((JCTree.JCIdent)tree.clazz).sym;
    		if ((sym.flags() & Flags.ENUM) != 0) {
    			// Instantiating an enum. Don't instrument.
    			return;
    		}
    	}

    	/*
    	 * We transform object instantiations
    	 *
    	 *   new @Mod C();
    	 *
    	 * to
    	 *
    	 *   wrappedNew(
    	 *     PrecisionRuntime.impl.beforeCreation(this, @Mod==@Approx),
    	 *     new @Mod C(),
    	 *     this
    	 *   );
    	 *
    	 * where
    	 *
    	 *   <T> T wrappedNew(boolean before, T created, Object creator) {
    	 *     PrecisionRuntime.impl.afterCreation(creator, created);
    	 *     return created;
    	 *   }
    	 *
    	 * The call to beforeCreation needs to happen before the real "new".
    	 * Therefore, we use it as the first argument in wrappedNew.
    	 * The call to afterCreation can happen directly after the "new", but needs
    	 * access to the newly created object. Therefore, instead of also making it an
    	 * argument, we call afterCreation in wrappedNew.
    	 *
    	 * In a static environment, instead of "this" we use the current Thread as creator.
    	 */
    	MethodTree enclMeth = TreeUtils.enclosingMethod(path);
    	boolean envIsStatic;
    	if (enclMeth==null) {
    		envIsStatic = true;
    	} else {
    		envIsStatic = ElementUtils.isStatic(TreeUtils.elementFromDeclaration(enclMeth));
    	}

    	JCTree.JCExpression beforeMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.beforeCreation");
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
    	JCExpression isApprox;
    	int[] sizes;
    	if (ENERJ) {
	        if ( type.hasEffectiveAnnotation(checker.APPROX) ) {
	        	isApprox = maker.Literal(TypeTags.BOOLEAN, 1);
	        	if(PrecisionChecker.R2_DEBUG)
    				System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitNewClass> " + tree.toString());
	        } else if ( type.hasEffectiveAnnotation(checker.CONTEXT) ) {
	        	JCTree.JCExpression curIsApproxMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate");
	        	isApprox = maker.Apply(null, curIsApproxMeth, List.of(thisExp()));
	        } else {
	        	isApprox = maker.Literal(TypeTags.BOOLEAN, 0);
	        }
	        sizes = PrecisionChecker.objectSizes(type, atypeFactory, typeutils, checker, false, tree);
    	} else {
    		boolean approxClass;
    		approxClass = isApproxStorage(tree);	// see if 'tree' is an approximate storage or not
    		if (approxClass)
    			isApprox = maker.Literal(TypeTags.BOOLEAN, 1);
    		else
    			isApprox = maker.Literal(TypeTags.BOOLEAN, 0);
    		sizes = PrecisionChecker.objectSizes(type, atypeFactory, typeutils, checker, approxClass, tree);
    	}

    	JCTree.JCExpression preciseSizeExp = maker.Literal(sizes[0]);
    	JCTree.JCExpression approxSizeExp  = maker.Literal(sizes[1]);
    	
    	List<JCExpression> beforeArgs;
    	if (envIsStatic) {
        	JCTree.JCExpression curThreadMeth = dotsExp("Thread.currentThread");
        	JCTree.JCMethodInvocation curThreadCall = maker.Apply(null, curThreadMeth, List.<JCExpression>nil());

    		beforeArgs = List.of(curThreadCall, isApprox, preciseSizeExp, approxSizeExp);
    	} else {
    		beforeArgs = List.of(thisExp(), isApprox, preciseSizeExp, approxSizeExp);
    	}

    	JCTree.JCMethodInvocation beforeCall = maker.Apply(null, beforeMeth, beforeArgs);

    	JCTree.JCExpression wrappedNewMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.wrappedNew");
    	List<JCExpression> wrappedArgs;
    	if (envIsStatic) {
        	JCTree.JCExpression curThreadMeth = dotsExp("Thread.currentThread");
        	JCTree.JCMethodInvocation curThreadCall = maker.Apply(null, curThreadMeth, List.<JCExpression>nil());

    		wrappedArgs = List.of(beforeCall, tree, curThreadCall);
    	} else {
    		wrappedArgs = List.of(beforeCall, tree, thisExp());
    	}

    	JCTree.JCMethodInvocation wrappedCall = maker.Apply(null, wrappedNewMeth, wrappedArgs);

    	attribute(wrappedCall, tree);

    	this.result = wrappedCall;
    }

    private void giveTypeToNewArray(JCTree.JCExpression elemtype, JCTree.JCNewArray newArray) {
        if (newArray.getInitializers() != null && newArray.elemtype == null) {
            newArray.elemtype = elemtype;

            // Recurse if multidimensional array.
            for (JCTree.JCExpression elem : newArray.getInitializers())
                if (elem instanceof JCTree.JCNewArray)
                    giveTypeToNewArray(
                        ((JCTree.JCArrayTypeTree)elemtype).elemtype,
                        (JCTree.JCNewArray)elem
                    );
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        // Give explicit types to array literals.
        if (tree.init instanceof JCTree.JCNewArray)
            giveTypeToNewArray(((JCTree.JCArrayTypeTree)tree.vartype).elemtype,
                               (JCTree.JCNewArray)tree.init);
        super.visitVarDef(tree);
    }

    private final Set<JCTree.JCNewArray> subInits = new HashSet<JCTree.JCNewArray>();

    @Override
    public void visitNewArray(JCNewArray tree) {
    	if (PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitNewArray> " + tree.toString());
        // Don't instrument array initializations inside of array
        // initialization literals.
        if (subInits.contains(tree)) {
            super.visitNewArray(tree);
            return;
        }
        if (tree.getInitializers() != null) {
            for (JCTree.JCExpression init : tree.getInitializers()) {
                if (init instanceof JCTree.JCNewArray) {
                    subInits.add((JCTree.JCNewArray)init);
                }
            }
        }

    	super.visitNewArray(tree);

    	// Translate array creations. We'll transform this expression:
    	//     new T[n]
    	// into this one:
    	//     enerj.rt.PrecisionRuntimeRoot.impl.newArray(
    	//          new T[n], 1, preciseElSize, approxElSize
        //     )

    	AnnotatedArrayType type =
    	    (atypeFactory.getAnnotatedType(tree));

    	// Recurse to true basic element type.
    	AnnotatedTypeMirror elType = type;
    	for (int i = 0; i < tree.dims.length(); ++i) {
    	    elType = ((AnnotatedArrayType)elType).getComponentType();
    	}
    	// ... and get the size of that element type.
        

        JCTree.JCExpression isApprox;
        int[] sizes;
        if (ENERJ) {
        	sizes = PrecisionChecker.typeSizes(
                    elType,
                    true,
                    checker,
                    // Get the approx-context size and switch to precise in precise
                    // contexts (determined by call below).
                    false,
                    null,
                    tree
                );
	        if ( elType.hasEffectiveAnnotation(checker.APPROX) ) {
	        	isApprox = boolExp(true);
    			if(PrecisionChecker.R2_DEBUG)
    				System.out.println("*** ENERJ_APPROX[RuntimePrecisionTranslator]: <visitNewArray>" + tree.toString());
	        } else if ( elType.hasEffectiveAnnotation(checker.CONTEXT) ) {
	        	isApprox = maker.Apply(null,
	        	    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate"),
	        	    List.of(thisExp())
	        	);
	        } else {
	        	isApprox = boolExp(false);
	        }
	        
        } else {
        	boolean approx = isApproxStorage(tree);
        	sizes = PrecisionChecker.typeSizes(
                    elType,
                    true,
                    checker,
                    approx,
                    "ARRAY",
                    tree
                );
        	if (approx) {
	        	isApprox = boolExp(true);
    			if(PrecisionChecker.R2_DEBUG){
    				System.out.println("*** R2_APPROX[RuntimePrecisionTranslator]: <visitNewArray>" + tree.toString());
    				System.out.println("*** R2_APPROX[RuntimePrecisionTranslator]: <visitNewArray> preciseSize(" + sizes[0] + ") approxSize(" + sizes[1] + ")");
    			}
        	} else 
	        	isApprox = boolExp(false);
        }
        
    	JCTree.JCExpression call = maker.Apply(null,
    	    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.newArray"),
    	    List.of(
    	        tree,
    	        maker.Literal(tree.dims.length()),
    	        isApprox,
    	        maker.Literal(sizes[0]),
    	        maker.Literal(sizes[1])
    	    )
    	);
    	attribute(call, tree);
    	result = call;
    }

    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if(PrecisionChecker.R2_DEBUG)
    			System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitClassDef> class name is changed to = " + curClassName);
    	}
    	curMethName = " ";
		if(PrecisionChecker.R2_DEBUG)
			System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitClassDef> method name is changed to = " + curMethName);
    	curRetTypeName = "void";
    	super.visitClassDef(node);
    }
    
    @Override
    public void visitMethodDef(JCMethodDecl tree) {
    	MethodSymbol meth = tree.sym;
        curMethName = tree.getName().toString();
        if(!(curMethName.equalsIgnoreCase("<init>") || curMethName.equalsIgnoreCase("<clinit>"))){
        	if (curMethName.equalsIgnoreCase("__htt_staticInitializerMethod")) 
            	curMethName = "<clinit>()";
            else
            	curMethName = tree.sym.toString();
        } else {
        	int index = tree.sym.toString().indexOf((int)'(');
        	String params = tree.sym.toString().substring(index);
        	curMethName += params;
        }
        if(PrecisionChecker.R2_DEBUG) System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <visitMethodDef> method name is changed to " + curMethName);
        if(meth.getReturnType() != null)
        	curRetTypeName = meth.getReturnType().toString();
        else
        	curRetTypeName = "void";
    	
    	super.visitMethodDef(tree);

    	if (TreeUtils.isConstructor(tree)) {
    		
    		JCTree.JCExpression enterSel = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.enterConstructor");
    		JCTree.JCMethodInvocation enterCall = maker.Apply(null, enterSel, com.sun.tools.javac.util.List.of(thisExp()));
    		JCTree.JCStatement enterStmt = maker.Exec(enterCall);

    		// Start attribution of the new AST part
    		attr.attribStat(enterStmt, getAttrEnv(tree));

    		List<JCStatement> stmts = tree.body.getStatements();
    		JCStatement first = stmts.head;

    		// Change the constructor body
    		tree.body = maker.Block(0, List.of(first).appendList(stmts.tail.prepend(enterStmt)));
    	}
    }
    
    /** r2IsApprox */
    public boolean isApproxStorage(JCTree tree) {
    	if (PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <isApproxStorage> start - tree = " + tree.toString());
    	if(PrecisionChecker.r2BcInfo == null)
    		throw new RuntimeException("Error! r2BcInfo is null");
    	if(PrecisionChecker.r2JchordResult == null)
    		throw new RuntimeException("Error! r2JchordResult is null");
		
    	Set<R2ASTNodeInfoEntry> bcInfoSet = PrecisionChecker.r2BcInfo.getInfoSet();
    	Set<R2JchordResultStorageEntry> jResultSet = PrecisionChecker.r2JchordResult.getResultStorageSet();
    	
    	// find a bc info generated in 1st phase compilation
    	boolean astFound = false;
    	for (R2ASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			astFound = true;
    			if (PrecisionChecker.R2_DEBUG)
    				System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <isApproxStorage> [MATCHED] AST info b/w 1st and 2nd compilation paths");
    			// found a same tree node, now match an analysis result with this node
    			for(R2JchordResultStorageEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					if (PrecisionChecker.R2_DEBUG) {
	    					count ++;
    						System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <isApproxStorage> [MATCHED] jchord entry with AST info");
    						System.out.println("*** R2_DEBUG[RuntimePrecisionTranslator]: <isApproxStorage> return true! count[" + count + "]");
    					}
    					PrecisionChecker.treeToJchordStorageResult.put(tree, result);
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
}
