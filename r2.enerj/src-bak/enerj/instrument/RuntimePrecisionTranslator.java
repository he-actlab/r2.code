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
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo;
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;

import enerj.PrecisionChecker;
import enerj.jchord.result.ExpaxJchordResult;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultOpEntry;
import enerj.jchord.result.ExpaxJchordResult.ExpaxJchordResultStorageEntry;

// Adds calls to the runtime system to keep track of the dynamic precision
// state of each object as it is instantiated.
public class RuntimePrecisionTranslator extends HelpfulTreeTranslator<PrecisionChecker> {
	
	private static final boolean EXPAX_RPT = false;
	private boolean ENERJ;
	
	public int count = 0;
	public String curClassName = "";
	public String curMethName = "";
	public String curRetTypeName = "";
	
    public RuntimePrecisionTranslator(PrecisionChecker checker,
                                      ProcessingEnvironment env,
                                      TreePath p,
                                      boolean ENERJ) {
        super(checker, env, p);
        this.ENERJ = ENERJ;
        if (EXPAX_RPT){
        	if(ENERJ)
        		System.out.println("*** EXPAX_RPT: ENERJ");
        	else 
        		System.out.println("*** EXPAX_RPT: EXPAX	");
        	System.out.println("*** EXPAX_RPT: RuntimePrecisionTranslator start!");
        }
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
    	if (EXPAX_RPT)
    		System.out.println("*** EXPAX_RPT: visitNewClass = " + tree.toString());
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
	        } else if ( type.hasEffectiveAnnotation(checker.CONTEXT) ) {
	        	JCTree.JCExpression curIsApproxMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate");
	        	isApprox = maker.Apply(null, curIsApproxMeth, List.of(thisExp()));
	        } else {
	        	isApprox = maker.Literal(TypeTags.BOOLEAN, 0);
	        }
	        sizes = PrecisionChecker.objectSizes(type, atypeFactory, typeutils, checker, false, tree);
    	} else {
    		boolean approx = expaxIsApprox(tree); // TODO call functions to determine if this is approximable  
    		if (approx) 
	        	isApprox = maker.Literal(TypeTags.BOOLEAN, 1);
	        else
	        	isApprox = maker.Literal(TypeTags.BOOLEAN, 0);
    		sizes = PrecisionChecker.objectSizes(type, atypeFactory, typeutils, checker, approx, tree);
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
    	if (EXPAX_RPT)
    		System.out.println("*** EXPAX_RPT: visitNewArray = " + tree.toString());
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
	        } else if ( elType.hasEffectiveAnnotation(checker.CONTEXT) ) {
	        	isApprox = maker.Apply(null,
	        	    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate"),
	        	    List.of(thisExp())
	        	);
	        } else {
	        	isApprox = boolExp(false);
	        }
        } else {
        	boolean approx = expaxIsApprox(tree);
        	sizes = PrecisionChecker.typeSizes(
                    elType,
                    true,
                    checker,
                    approx,
                    "ARRAY",
                    tree
                );
        	if (approx)
	        	isApprox = boolExp(true);
	        else 
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
    		if(EXPAX_RPT)
    			System.out.println("*** EXPAX_RPT: class name is changed to = " + curClassName);
    	}
    	super.visitClassDef(node);
    }
    
    @Override
    public void visitMethodDef(JCMethodDecl tree) {
    	curMethName = tree.sym.toString();
    	if (EXPAX_RPT)
    		System.out.println("*** EXPAX_RPT: method name is changed to = " + curMethName);
    	if(tree.getReturnType() != null) { 
            curRetTypeName = tree.getReturnType().toString();
            if (EXPAX_RPT)
            	System.out.println("*** EXPAX_RPT: return type name is changed to = " + curRetTypeName);
    	}
    	else // constructor or destructor
    		curRetTypeName = "";
    	
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
    
    /** expaxIsApprox */
    public boolean expaxIsApprox(JCTree tree) {
    	if (EXPAX_RPT)
    		System.out.println("*** EXPAX_RPT: expaxIsApprox - tree = " + tree.toString());
    	if(PrecisionChecker.expaxBcInfo == null)
    		throw new RuntimeException("Error! expaxBcInfo is null");
    	if(PrecisionChecker.expaxJchordResult == null)
    		throw new RuntimeException("Error! expaxJchordResult is null");
		
    	Set<ExpaxASTNodeInfoEntry> bcInfoSet = PrecisionChecker.expaxBcInfo.getInfoSet();
    	Set<ExpaxJchordResultStorageEntry> jResultSet = PrecisionChecker.expaxJchordResult.getResultStorageSet();
    	
    	// find a bc info generated in 1st phase compilation
    	for (ExpaxASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			if (EXPAX_RPT)
    				System.out.println("*** EXPAX_RPT: AST info matched");
    			// found a same tree node, now match an analysis result with this node
    			for(ExpaxJchordResultStorageEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					if (EXPAX_RPT) {
	    					count ++;
	    					System.out.println("*** EXPAX_RPT: expaxIsApprox return true = " + count);
	    					System.out.println("*** EXPAX_RPT: jchord result = " + result.toString());
    					}
    					PrecisionChecker.treeToJchordStorageResult.put(tree, result);
    					return true;
    				}
    			}
    		}
    	}
    	if (EXPAX_RPT)
    		System.out.println("*** EXPAX_RPT: expaxIsApprox return false!");
    	return false;
    }
}
