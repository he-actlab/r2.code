package enerj.instrument;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import checkers.runtime.instrument.HelpfulTreeTranslator;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.TreeUtils;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo;
import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo.R2ASTNodeInfoEntry;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import enerj.PrecisionChecker;
import enerj.jchord.result.R2JchordResult;
import enerj.jchord.result.R2JchordResult.R2JchordResultOpEntry;
import enerj.lang.Approx;


// Performs (static or dynamic) method binding depending on the precision of the
// receiver object. This implements _APPROX-suffixed methods on @Approximable
// classes. Also tracks the current "approximate context" used to perform
// return-type overloading (bidirectional typing).
public class MethodBindingTranslator extends HelpfulTreeTranslator<PrecisionChecker> {
	
    public MethodBindingTranslator(PrecisionChecker checker,
                                   ProcessingEnvironment env,
                                   TreePath p,
                                   R2ASTNodeInfo r2BcInfo,
                                   R2JchordResult r2JchordResult 
                                   ) {
        super(checker, env, p);
        if (r2BcInfo != null || r2JchordResult != null)
        	ENERJ = true;
        this.r2BcInfo = r2BcInfo;
        this.r2JchordResult = r2JchordResult;
    }

    // Keeps track of the current context for our simplified form of
    // bidirectional typing.
    private boolean ctxApprox = false;
    public boolean ENERJ = true;
    public R2ASTNodeInfo r2BcInfo;
    public R2JchordResult r2JchordResult;
	public int count = 0;
	public String curClassName = " ";
	public String curMethName = " ";
	public String curRetTypeName = " ";
    
    /** get curClassName */
    public void visitClassDef(JCTree.JCClassDecl node) {
    	if (node.getKind().toString().equalsIgnoreCase("CLASS")) {
    		curClassName = node.sym.toString();
    		if(PrecisionChecker.R2_DEBUG)
    			System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitClassDef> class name is changed to = " + curClassName);
    	}
    	curMethName = " ";
		if(PrecisionChecker.R2_DEBUG)
			System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitClassDef> method name is changed to = " + curMethName);
    	curRetTypeName = "void";
    	super.visitClassDef(node);
    }
    
    /**
     * It'll be only needed for visitAssign in this translator
     */
    public boolean isApprox(JCTree tree) {
    	if(PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> start - tree = " + tree.toString());
    	if(PrecisionChecker.r2BcInfo == null)
    		throw new RuntimeException("Error! r2BcInfo is null");
    	if(PrecisionChecker.r2JchordResult == null)
    		throw new RuntimeException("Error! r2JchordResult is null");
		
    	Set<R2ASTNodeInfoEntry> bcInfoSet = PrecisionChecker.r2BcInfo.getInfoSet();
    	Set<R2JchordResultOpEntry> jResultSet = PrecisionChecker.r2JchordResult.getResultOpSet();
    	
    	// find a bc info generated in 1st phase compilation
    	boolean astFound = false;
    	for (R2ASTNodeInfoEntry info : bcInfoSet) {    		
    		if (info.compareWithTree(tree, curClassName, curMethName, curRetTypeName)) {
    			astFound = true;
    			if(PrecisionChecker.R2_DEBUG)
    				System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> [MATCHED] AST info b/w 1st and 2nd compilation paths");
    			// found a same tree node, now match an analysis result with this node
    			for(R2JchordResultOpEntry result : jResultSet) {
					//found a matched node
    				if (result.compareWithASTInfo(info)){
    					count ++;
    					if(PrecisionChecker.R2_DEBUG){
    						System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> [MATCHED] jchord entry with AST info");
    						System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> return true! count[" + count + "]");
    					}
    					return true;
    				}
    			}
    		}
    	}
    	if(PrecisionChecker.R2_DEBUG){
	    	if(!astFound)
	    		System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> [NOT MATCHED] any AST info NOT matched b/w 1st and 2nd compilation paths");
	    	else
	    		System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> [NOT MATCHED] any jchord entry NOT matched with AST info");
	    	System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <isApprox> return false!");
    	}
    	return false;
    }
    
    @Override
    public void visitAssign(JCTree.JCAssign tree) {
		if (checker.getLintOption(PrecisionChecker.MBSTATIC, PrecisionChecker.MBSTATIC_DEFAULT)) {
			tree.lhs = translate(tree.lhs);

			// determine the type of the lhs
			AnnotatedTypeMirror lhs = atypeFactory.getAnnotatedType(tree.lhs);
			// store the current approximation state so that we can restore it
			boolean prevCtxApprox = ctxApprox;

			// check whether the lhs is approximate
			if(ENERJ) {
				if (lhs.hasAnnotation(Approx.class)) {
					ctxApprox = true;
				}
			} else {
				// TODO do we need this?
				ctxApprox = isApprox(tree);
			}

			tree.rhs = translate(tree.rhs);

			// restore context
			ctxApprox = prevCtxApprox;

			result = tree;

			// note that we didn't call super.visitAssign(node), but
			// instead did the translations separately
		} else {
			super.visitAssign(tree);
		}
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        tree.mods = translate(tree.mods);
        tree.vartype = translate(tree.vartype);

        // note: do not use only tree.vartype, as the annotation would get lost!
		AnnotatedTypeMirror lhs = atypeFactory.getAnnotatedType(tree);
		// store the current approximation state so that we can restore it
		boolean prevCtxApprox = ctxApprox;

		// check whether the lhs is approximate
		if(ENERJ) {
			if (lhs.hasAnnotation(Approx.class)) {
				ctxApprox = true;
				if(PrecisionChecker.R2_DEBUG)
					System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitVarDef> " + tree.toString());
			}
		} else {
			// FIXME Do we need this part? R2 doesn't take care of @Context of EnerJ anyway.. 
			ctxApprox = isApprox(tree);
			if(ctxApprox) {
				if(PrecisionChecker.R2_DEBUG)
					System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitVarDef> " + tree.toString());
			}
		}

        tree.init = translate(tree.init);

		// restore context
		ctxApprox = prevCtxApprox;

        result = tree;

        // no need to call super
        // super.visitVarDef(tree);
    }


    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {

        if(checker.getLintOption(PrecisionChecker.MBSTATIC, PrecisionChecker.MBSTATIC_DEFAULT)) {
    		methodBindingStatic(tree);
    	} else {
    		// only apply the super implementation if we do not want to mess around.
            super.visitApply(tree);
    	}
    	// The dynamic method binding doesn't modify the method invocations, but the method declarations.
    	// Go look there.
    }

    private void methodBindingStatic(JCTree.JCMethodInvocation tree) {
    	JCTree.JCExpression et = tree.getMethodSelect();

    	if (et instanceof JCTree.JCFieldAccess) {
    		JCTree.JCFieldAccess mst = (JCTree.JCFieldAccess) et;
    		mst.selected = translate(mst.selected);
    		AnnotatedTypeMirror recv = atypeFactory.getAnnotatedType(mst.selected);

			if (recv.hasAnnotation(Approx.class) ||
				ctxApprox) {
				TypeMirror precv = recv.getUnderlyingType();

				if (precv instanceof DeclaredType
						|| precv instanceof TypeVariable) {

		    		Name origcalled = mst.getIdentifier();
		    		replaceMethod(tree, mst.selected, precv, origcalled, (MethodSymbol) mst.sym);
		    		return;
				}
			}
    	} else if (et instanceof IdentifierTree) {
    		// The receiver type for a call on an implicit receiver is always Context.
    		// We only need to check whether the Calling Context is Approximate and switch out the methods.

    		JCIdent it = (JCIdent) et;

			if (ctxApprox) {
	    		Name origcalled = it.getName();

	    		AnnotatedTypeMirror recv = atypeFactory.getAnnotatedType(TreeUtils.enclosingClass(atypeFactory.getPath(tree)));
				TypeMirror precv = recv.getUnderlyingType();

	    		replaceMethod(tree, thisExp(), precv, origcalled, (MethodSymbol) it.sym);
	    		return;
			}

			// If the receiver is implicit, "it" is just the method name!
			// if (!names._super.equals(it.getName()) &&
			//		!names._this.equals(it.getName()) ) {
    	}
    	// like calling super.visitApply in the calling method.
    	tree.meth = translate(tree.meth);
        tree.args = translate(tree.args);
    	result = tree;
    }

    private void replaceMethod(JCTree.JCMethodInvocation tree,
    		JCExpression recvExpr, TypeMirror recvType,
    		Name origcalled, MethodSymbol origelem) {

		Name newcalled = names.fromString(origcalled + PrecisionChecker.MB_APPROX_POST);
		AnnotatedExecutableType origexe = atypeFactory.getAnnotatedType(origelem);

		for (Element elem : typeutils.asElement(recvType).getEnclosedElements()) {
			if (elem.getSimpleName().equals(newcalled)) {
				{
					AnnotatedExecutableType newexe = atypeFactory.getAnnotatedType((ExecutableElement) elem);

					if (!checker.isCompatible(origexe,	newexe)) {
						if(checker.debug()) {
							System.out.println("EnerJ: methods not compatible: " + origexe + " and " + newexe);
						}
						// We continue, because there might be a different
						// overloaded method that fits.
						continue;
					}
				}

				JCTree.JCExpression newmethodselect = maker.Select(recvExpr, (Symbol) elem);

				// We need to translate the arguments, to recursively adapt the call
				// We also need them to construct a valid new method call that we can attribute.
				List<VarSymbol> params = ((MethodSymbol) elem).params;
				AnnotatedTypeMirror ptype;
				boolean prevCtxApprox;

				// pass through the parameters as arguments for the new call
				List<JCExpression> args = List.<JCExpression>nil();
				for (JCExpression arg : tree.getArguments()) {
					ptype = atypeFactory.getAnnotatedType(params.head);

					prevCtxApprox = ctxApprox;

					// check whether the lhs is approximate
					if (ptype.hasAnnotation(Approx.class)) {
						ctxApprox = true;
					} else {
						ctxApprox = false;
					}

					arg = translate(arg);
					args = args.prepend(arg);

			        params = params.tail;

					// restore context (TODO: needed every time or outside of loop?
					ctxApprox = prevCtxApprox;
				}
				JCTree.JCMethodInvocation approxCall = maker.Apply(null, newmethodselect, args);

				// TODO: what if the return type was @Context?
				// We also need to substitute it.
				attr.attribExpr(approxCall, this.getAttrEnv(tree), (Type)((ExecutableElement)elem).getReturnType());

				if (checker.debug()) {
					// TODO: how do we find the location + line number for a Symbol/Tree?
					System.out.println("EnerJ: changing call \"" + recvExpr + "." + origcalled
							+ "\" to call method \"" + newcalled + "\".");
				}


				// only replace the receiver expression, the rest is already set
				tree.meth = newmethodselect;
				tree.args = args;
				result = tree;

				// there is only one method we need to look at
				return;
			}
			// TODO: log/warning if no substitution found?
		}
		tree.meth = translate(tree.meth);
        tree.args = translate(tree.args);
        result = tree;
	}

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
    	if(PrecisionChecker.R2_DEBUG)
    		System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitMethodDef> tree = " + tree.toString());
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
        if(PrecisionChecker.R2_DEBUG) System.out.println("*** R2_DEBUG[MethodBindingTranslator]: <visitMethodDef> method name is changed to " + curMethName);
        if(meth.getReturnType() != null)
        	curRetTypeName = meth.getReturnType().toString();
        else
        	curRetTypeName = "void";
        
        super.visitMethodDef(tree);
		if (checker.getLintOption(PrecisionChecker.MBDYNAMIC, PrecisionChecker.MBDYNAMIC_DEFAULT)) {
			methodBindingDynamic(tree);
		}
    }

    private void methodBindingDynamic(JCTree.JCMethodDecl tree) {
    	if (tree.getName().toString().endsWith(PrecisionChecker.MB_APPROX_POST)) {
    		// Do not instrument the approximate methods
    		return;
    	}

		Name origcalled = tree.getName();
		Name newcalled = names.fromString(origcalled + PrecisionChecker.MB_APPROX_POST);

		ExecutableElement meth = TreeUtils.elementFromDeclaration(tree);
		AnnotatedExecutableType origexe = atypeFactory.getAnnotatedType( meth );
		AnnotatedExecutableType newexe = null;

		for (ExecutableElement memb : ElementFilter.methodsIn(meth.getEnclosingElement().getEnclosedElements()) ) {
			// cls.getMembers()) {
			if (memb.getSimpleName().equals(newcalled) ) {
				newexe = atypeFactory.getAnnotatedType( memb );

				if( !checker.isCompatible(origexe, newexe) ) {
					// TODO: should we report this?
					// System.out.println("Not compatible: " + origexe + " and " + newexe);
					// We continue, because there might be a different overloaded method
					// that fits.
					newexe = null;
					continue;
				}
			}
		}

		if (newexe==null) {
			// we didn't find a compatible method with the appropriate name -> quit
			return;
		}


		/* We want to add the following code:
		 *
		 * if (PrecisionRuntimeRoot.impl.isApproximate(this)) {
		 *   return this.m_APPROX(...);
		 * }
		 *
		 * If there is no return type we add a call to the method and then a return statement:
		 *
		 * if (PrecisionRuntimeRoot.impl.isApproximate(this)) {
		 *   this.m_APPROX(...);
		 *   return;
		 * }
		 */

		JCTree.JCExpression isApprMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate");
		JCTree.JCMethodInvocation condcall = maker.Apply(null, isApprMeth, List.of(thisExp()));

		JCTree.JCExpression approxMeth = maker.Ident(newcalled);
		// pass through the parameters as arguments for the new call
		List<JCExpression> args = List.<JCExpression>nil();
		for (JCVariableDecl var : tree.getParameters()) {
			args = args.prepend(maker.Ident(var.name));
		}
		JCTree.JCMethodInvocation approxCall = maker.Apply(null, approxMeth, args);

		JCTree.JCStatement thenbranch;

		JCTree rettype = tree.getReturnType();
		if (rettype==null ||
			(rettype instanceof JCPrimitiveTypeTree &&
				((JCPrimitiveTypeTree)rettype).getPrimitiveTypeKind()==TypeKind.VOID)) {
			// If this is a void method, first call the approximate method and then return.
			thenbranch = maker.Block(0, List.of(maker.Exec(approxCall), maker.Return(null)));
		} else {
			// If the method is non-void, return the result of calling the approximate method.
			thenbranch = maker.Return(approxCall);
		}

		JCTree.JCIf ifApprox = maker.If(condcall, thenbranch, null);

		// Start attribution of the new AST part
		attr.attribStat(ifApprox, getAttrEnv(tree));

		// Change the method body
		tree.body = maker.Block(0, tree.body.getStatements().prepend(ifApprox));

		if(checker.debug()) {
    		System.out.println("EnerJ: Changing method\"" + tree.getName());
    	}
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess tree) {
    	boolean prevCtxApprox = ctxApprox;
    	ctxApprox = false;
    	super.visitIndexed(tree);
    	ctxApprox = prevCtxApprox;
    }

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
    	super.visitBinary(tree);

    	if (ctxApprox && !atypeFactory.getAnnotatedType(tree).hasEffectiveAnnotation(checker.APPROX)) {
    		// System.err.println("might have been approximate: " + tree);
    		// A terrible, terrible way to do this:
    		PrecisionReferencingTranslator.approxTrees.add(tree);
    	}
    }
}
