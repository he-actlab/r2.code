package enerj.instrument;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.code.Symbol;

import javax.annotation.processing.ProcessingEnvironment;
import javax.management.RuntimeErrorException;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo;
import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.List;

import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import enerj.PrecisionChecker;
import enerj.jchord.result.ExpaxJchordResult;
import enerj.rt.PrecisionRuntime.MemKind;

// Instruments the source code to simulate running code on an approximate
// architecture (as well as count various events).
public class SimulationTranslator extends PrecisionReferencingTranslator {
	
	private static boolean ENERJ = true;
	private static final boolean EXPAX_ST = false;
	public ExpaxASTNodeInfo expaxBcInfo;
	public ExpaxJchordResult expaxJchordResult;
	public Stack<JCTree> globalParent = new Stack<JCTree>();

    // Keeps track of expressions that should *not* be instrumented as rvalues
    // (i.e., loads).
    private Set<JCTree.JCExpression> lvalues =
        new HashSet<JCTree.JCExpression>();

    public SimulationTranslator(PrecisionChecker checker,
                                ProcessingEnvironment env,
                                TreePath p,
                                ExpaxASTNodeInfo expaxBcInfo,
                                ExpaxJchordResult expaxJchordResult) {
        super(checker, env, p);
        if (expaxBcInfo != null && expaxJchordResult != null)
        	ENERJ = false;
        this.expaxBcInfo = expaxBcInfo;
        this.expaxJchordResult = expaxJchordResult;
        if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: SimulationTranslator start!");
    }
    
    private JCTree.JCExpression numKindExp(Type type) {
        String name = type.toString();
        String kindVal = "enerj.rt.PrecisionRuntime.NumberKind.";
        if (type.tag == TypeTags.INT || name.equals("java.lang.Integer"))
            kindVal += "INT";
        else if (type.tag == TypeTags.LONG || name.equals("java.lang.Long"))
            kindVal += "LONG";
        else if (type.tag == TypeTags.FLOAT || name.equals("java.lang.Float"))
            kindVal += "FLOAT";
        else if (type.tag == TypeTags.DOUBLE
                 || name.equals("java.lang.Double"))
            kindVal += "DOUBLE";
        else if (type.tag == TypeTags.SHORT || name.equals("java.lang.Short"))
            kindVal += "SHORT";
        else if (type.tag == TypeTags.BYTE || name.equals("java.lang.Byte"))
            kindVal += "BYTE";
        else {
            System.out.println("unknown numeric type! " + type);
            return null;
        }
        return dotsExp(kindVal);
    }

    // Coerce "char" to "int".
    private JCTree.JCExpression makeNumeric(JCTree.JCExpression expr) {
        if (expr.type.tag == TypeTags.CHAR) {
            return maker.TypeCast(
                symtab.intType,
                expr
            );
        } else {
            return expr;
        }
    }

    @Override
    public void visitCase(JCTree.JCCase node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitCase: tree = " + node.toString());
        // This is a little bit hacky, but mark "case" patterns as lvalues.
        // They are not, of course, actually lvalues, but they should not be
        // instrumented as loads because they must be constants at the source
        // level (and, also, they're not really loads at the JVM level, are
        // they?).
        lvalues.add(node.pat);

        super.visitCase(node);
    }


    // Instrumentation of operations.

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
    	boolean approximate = false;
    	if(ENERJ) {
    		if(EXPAX_ST)
    			System.out.println("*** EXPAX_ST: ENERJ");
    		approximate = isApprox(tree);
    	}
    	if (EXPAX_ST) {
        	System.out.println("*** EXPAX_ST: visitBinary: tree = " + tree.toString());
    		System.out.println("*** EXPAX_ST: call isApprox - visitBinary");
    		System.out.println("*** EXPAX_ST: tag = " + tree.getTag().toString()	);
    		System.out.println("*** EXPAX_ST: kind = " + tree.getKind().toString()	);
    	}
    	globalParent.push(tree);
        super.visitBinary(tree);
        globalParent.pop();
        
        if(EXPAX_ST)
        	System.out.println("*** EXPAX_ST: ident end?");
        // Avoid instrumenting string concatenation.
        if (tree.type.toString().equals("java.lang.String")) {
            return;
        }

        switch (tree.getKind()) {
        case PLUS:
        case MINUS:
        case MULTIPLY:
        case DIVIDE:
            // Handle as arithmetic operator.
            break;
        case LESS_THAN:
        case GREATER_THAN:
        case LESS_THAN_EQUAL:
        case GREATER_THAN_EQUAL:
        case EQUAL_TO:
        case NOT_EQUAL_TO:
        case CONDITIONAL_AND:
        case CONDITIONAL_OR:
        	// Handle as logical operator.
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.countLogicalOp");
            JCTree.JCMethodInvocation call = maker.Apply(null, meth,
                List.<JCTree.JCExpression>of(
                	tree
                ));
            JCTree.JCExpression expr = unbox(call, tree.type);
            attribute(expr, tree);
            result = expr;
        	return;
        default:
            // Not instrumented.
            return;
        }

        // Special-case: prevent instrumentation of binary operators on
        // two literals. This was causing a problem with code size explosion
        // in some large literal arrays in JLayer; also, these operations
        // will be optimized away by the compiler anyway.
        if (tree.lhs instanceof JCTree.JCLiteral &&
        		tree.rhs instanceof JCTree.JCLiteral) {
        	return;
        }

        // Get the kind of operator to pass to the runtime.
        String opVal = "enerj.rt.PrecisionRuntime.ArithOperator.";
        opVal += tree.getKind();

        // Create the new tree (call to operator replacement method).
        JCTree.JCExpression meth =
            dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.binaryOp");
    	if (!ENERJ) {
    		if(EXPAX_ST)
    			System.out.println("*** EXPAX_ST: EXPAX");
    		approximate = expaxIsApprox(tree);
    	}
    	if (approximate) {
    		if(ENERJ)
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(BINARY) - " + tree.toString());
    		else
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(BINARY) - " + tree.toString());
    	}
        JCTree.JCMethodInvocation call = maker.Apply(null, meth,
            List.of(
                makeNumeric(tree.lhs),
                makeNumeric(tree.rhs),
                dotsExp(opVal),
                numKindExp(tree.type),
                boolExp(approximate)
            ));
        JCTree.JCExpression expr = unbox(call, tree.type);

        // Attribute the new tree and replace the old one.
        attribute(expr, tree);
        result = expr;
    }

    @Override
    public void visitUnary(JCTree.JCUnary node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitUnary: tree = " + node.toString());
        String kind = null;
        boolean returnOld = false;
        switch (node.getKind()) {
        case PREFIX_INCREMENT:
            kind = "PLUS";
            returnOld = false;
            break;
        case PREFIX_DECREMENT:
            kind = "MINUS";
            returnOld = false;
            break;
        case POSTFIX_INCREMENT:
            kind = "PLUS";
            returnOld = true;
            break;
        case POSTFIX_DECREMENT:
            kind = "MINUS";
            returnOld = true;
            break;

        default:
            super.visitUnary(node);
            return;
        }

        // Finish visiting the tree, but don't instrument the value
        // as a load.
        lvalues.add(node.arg);
        JCTree.JCExpression oldArg = node.arg;
        if(EXPAX_ST)
        	System.out.println("*** EXPAX_ST: call isApprox - visitUnary");
        if (ENERJ)
        	isApprox(oldArg);
        super.visitUnary(node);

        // Instrumented assignop call.
        result = assignopCall(
            node.arg,
            oldArg,
            maker.Literal(1),
            kind,
            node,
            returnOld
        );
    }


    // Instrumentation of loads and stores.

    private JCTree.JCExpression storeCall(JCTree typedTree,
                                          JCTree.JCExpression rhs,
                                          MemKind kind) {
    	
        // Create a call that transfroms the RHS and logs the store.
        JCTree.JCExpression meth =
            dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.storeValue");
        if(EXPAX_ST)
        	System.out.println("*** EXPAX_ST: call isApprox  - storeCall");
        boolean approximate;
        if(ENERJ) {
        	if(EXPAX_ST)
        		System.out.println("*** EXPAX_ST: ENERJ");
        	approximate = isApprox(typedTree);
        } else {
        	if(EXPAX_ST)
        		System.out.println("*** EXPAX_ST: EXPAX");
        	approximate = expaxIsApprox(typedTree);
        }
        if (approximate) {
    		if(ENERJ)
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(storeCall) - " + typedTree.toString());
    		else
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(storeCall) - " + typedTree.toString());
    	}
        if (rhs == null)
        	throw new RuntimeException("rhs is null");
        JCTree.JCExpression expr = maker.Apply(null, meth,
            List.of(
                box(rhs), boolExp(approximate), memKindExp(kind)
            )
        );
        expr = unbox(expr, rhs.type);

        // To avoid "possible loss of precision", cast explicitly to the store target type.
        expr = maker.TypeCast(typedTree.type, expr);

        attribute(expr, rhs, typedTree.type);
        return expr;
    }

    private MemKind valSymbolKind(Symbol sym) {
        if (sym instanceof Symbol.VarSymbol) {
            if (sym.toString().equals("this") || sym.toString().equals("super"))
                return null; // Don't consider "this" and "super" as field accesses.
            else if (sym.owner instanceof Symbol.ClassSymbol)
                return MemKind.FIELD;
            else if (sym.owner instanceof Symbol.MethodSymbol)
                return MemKind.VARIABLE;
            else
                return null;
        } else {
            // Not a value.
            return null;
        }
    }

    private JCTree.JCExpression memKindExp(MemKind kind) {
        String chain = "enerj.rt.PrecisionRuntime.MemKind." + kind.toString();
        return dotsExp(chain);
    }

    private JCTree.JCExpression boxedTypeExp(Type type, boolean abort) {
        String className;
//        if(type == null)
//        	throw new RuntimeException("type is null");
        if (type.tag == TypeTags.BYTE)
            className = "Byte";
        else if (type.tag == TypeTags.CHAR)
            className = "Character";
        else if (type.tag == TypeTags.SHORT)
            className = "Short";
        else if (type.tag == TypeTags.INT)
            className = "Integer";
        else if (type.tag == TypeTags.LONG)
            className = "Long";
        else if (type.tag == TypeTags.FLOAT)
            className = "Float";
        else if (type.tag == TypeTags.DOUBLE)
            className = "Double";
        else if (type.tag == TypeTags.BOOLEAN)
            className = "Boolean";
        else
        	if (abort)
        		return null;
        	else
        		return maker.Type(type);
        return dotsExp("java.lang." + className);
    }

    private JCTree.JCExpression boxedTypeExp(Type type) {
    	return boxedTypeExp(type, false);
    }

    private JCTree.JCExpression box(JCTree.JCExpression unboxed) {
//    	if (unboxed.type == null)
//    		throw new RuntimeException("unboxed.type is null = " + unboxed.toString());
        JCTree.JCExpression castType = boxedTypeExp(unboxed.type, true);
        if (castType == null) {
            // Don't do anything to non-primitive types.
            return unboxed;
        }
        // Perform cast.
        return maker.TypeCast(castType, unboxed);
    }

    private JCTree.JCExpression unbox(JCTree.JCExpression boxed, Type type) {
        String methName;
        if (type.tag == TypeTags.BYTE)
            methName = "byteValue";
        else if (type.tag == TypeTags.CHAR)
            methName = "charValue";
        else if (type.tag == TypeTags.SHORT)
            methName = "shortValue";
        else if (type.tag == TypeTags.INT)
            methName = "intValue";
        else if (type.tag == TypeTags.LONG)
            methName = "longValue";
        else if (type.tag == TypeTags.FLOAT)
            methName = "floatValue";
        else if (type.tag == TypeTags.DOUBLE)
            methName = "doubleValue";
        else if (type.tag == TypeTags.BOOLEAN)
            methName = "booleanValue";
        else
            return boxed;

        return maker.Apply(
            null,
            maker.Select(boxed, names.fromString(methName)),
            List.<JCTree.JCExpression>nil()
        );
    }

    private boolean sneakyFieldAccess(JCTree.JCIdent node) {
    	return node.sym instanceof Symbol.VarSymbol &&
        	((Symbol.VarSymbol)(node.sym)).owner instanceof Symbol.ClassSymbol;
    }

    private JCTree.JCExpression sneakySelected(JCTree.JCIdent node) {
        if (node.sym.isStatic()) {
            // Reference to static field from static method.
            return dotsExp(node.sym.owner.toString() + ".class");
        } else {
            // Implicit "this".
            return thisExp();
        }
    }

    @Override
    public void visitIdent(JCTree.JCIdent node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitIdent: tree = " + node.toString());
        super.visitIdent(node);

        JCTree parent = null;
        if(globalParent != null) {
	        if (globalParent.size() != 0) {
	        	JCTree temp = globalParent.lastElement();
		        if (!(temp instanceof JCTree.JCBinary ||
		        	  temp instanceof JCTree.JCAssign ||
		        	  temp instanceof JCTree.JCAssignOp ||
		        	  temp instanceof JCTree.JCVariableDecl ||
		        	  temp instanceof JCTree.JCFieldAccess ||
		        	  temp instanceof JCTree.JCArrayAccess))
		        	parent = null;
		        
		        if (temp instanceof JCTree.JCBinary) parent = (JCTree.JCBinary) temp;
		        if (temp instanceof JCTree.JCAssign) parent = (JCTree.JCAssign) temp;
		        if (temp instanceof JCTree.JCAssignOp) parent = (JCTree.JCAssignOp) temp;
		        if (temp instanceof JCTree.JCVariableDecl) parent = (JCTree.JCVariableDecl) temp;
		        if (temp instanceof JCTree.JCFieldAccess) parent = (JCTree.JCFieldAccess) temp;
		        if (temp instanceof JCTree.JCArrayAccess) parent = (JCTree.JCArrayAccess) temp;
		        
		        if(EXPAX_ST)
		        	System.out.println("*** EXPAX_ST: parent = " + parent.toString());
	        }
        } else
        	throw new RuntimeException("Error! globalParent is null?");
        
        // If this identifier is a variable name that's being used as an rvalue,
        // instrument it as a load.
        if (!lvalues.contains(node) && valSymbolKind(node.sym) != null) {
        	if (this.sneakyFieldAccess(node)) {
        		// Not a local variable!
        		JCTree.JCExpression selected = this.sneakySelected(node);

        		JCTree.JCExpression meth =
                    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.loadField");
        		if(EXPAX_ST)
        			System.out.println("*** EXPAX_ST: call isApprox - visitIdent #1");
        		boolean approximate;
        		if (ENERJ) {
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_ST: ENERJ");
        			approximate = isApprox(node);
        		} else {
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_ST: EXPAX");
        			if (parent != null)
        				approximate = expaxIsApprox(parent);
        			else
        				approximate = false;
        		}
        		if (approximate) {
            		if(ENERJ)
            			if(EXPAX_ST)
            				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(IDENT) - " + node.toString());
            		else
            			if(EXPAX_ST)
            				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(IDENT) - " + node.toString());
            	}
        		JCTree.JCExpression expr = maker.Apply(
                    	List.of(boxedTypeExp(node.type)),
                    	meth,
                        List.of(
                            selected,
                            maker.Literal(node.name.toString()),
                            boolExp(approximate)
                        )
                    );
                expr = unbox(expr, node.type);
                attribute(expr, node, node.type);
                result = expr;

        		return;
        	}

            // Replaced with a reference?
            JCTree.JCExpression ref;
            if (result == null || !(result instanceof JCTree.JCFieldAccess))	
            	return;
            ref = ((JCTree.JCFieldAccess)result).selected;
            // Call into runtime to execute load.
            // Ignore the replacement created by the referencing translator.
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.loadLocal");
            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - visitIdent #2");
    		boolean approximate;
    		if (ENERJ) {
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: ENERJ");
    			approximate = isApprox(node);
    		} else {
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: EXPAX");
    			if(parent != null)
    				approximate = expaxIsApprox(parent);
    			else
    				approximate = false;
    		}
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(IDENT) - " + node.toString());
        		else 
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(IDENT) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(node.type)),
            	meth,
                List.of(
                    ref, boolExp(approximate)
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;
        }
    }

    @Override
    public void visitSelect(JCTree.JCFieldAccess node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitSelect: tree = " + node.toString());
    	globalParent.push(node);
        super.visitSelect(node);
        globalParent.pop();

        // Skip any references to our own instrumentation stuff.
        if (node.toString().startsWith("enerj.rt."))
            return;

        // Instrument field accesses as loads. This excludes package
        // accesses and nested class accesses.
        if (!lvalues.contains(node) &&
                !node.name.toString().equals("class") &&
                valSymbolKind(node.sym) != null) {
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.loadField");

            // Static accesses get classes instead of objects.
            JCTree.JCExpression obj = node.selected;
            if (node.sym.isStatic()) {
            	obj = maker.Select(obj, names.fromString("class"));
            }

            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - visitSelect");
            boolean approximate;
            if (ENERJ) {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: ENERJ");
            	approximate = isApprox(node);
            } else {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: EXPAX");
            	approximate = expaxIsApprox(node);
            }
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(SELECT) - " + node.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(SELECT) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(node.type)),
            	meth,
                List.of(
                    obj,
                    maker.Literal(node.name.toString()),
                    boolExp(approximate)
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;
        } else {
        	if(EXPAX_ST)
        		System.out.println("*** EXPAX_ST: lvalues contains = " + node.toString());
        }
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitIndexed: tree = " + node.toString());
    	
    	globalParent.push(node);
        super.visitIndexed(node);
        globalParent.pop();
        
        if (!lvalues.contains(node)) {
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.loadArray");
            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - visitIndexed");
            boolean approximate;
            if (ENERJ) {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: ENERJ");
            	approximate = isApprox(node);
            } else {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: EXPAX");
            	approximate = expaxIsApprox(node);
            }
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(SELECT) - " + node.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(SELECT) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(node.type)),
            	meth,
                List.of(
                    node.indexed,
                    node.index,
                    boolExp(approximate)
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;
        } else 
        	if(EXPAX_ST)
        		System.out.println("*** EXPAX_ST: lvalues contains " + node);
    }

    @Override
    public void visitAssign(JCTree.JCAssign node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitAssign: tree = " + node.toString());
        JCTree.JCExpression oldLhs = node.lhs;
        boolean approximate = false;
        if(ENERJ) {
        	if(EXPAX_ST)
        		System.out.println("*** EXPAX_ST: ENERJ");
        	approximate = isApprox(oldLhs);
        }
        
        lvalues.add(node.lhs);

        if (node.lhs instanceof JCTree.JCIdent) {
            JCTree.JCIdent id = (JCTree.JCIdent)node.lhs;
            if (id.sym instanceof Symbol.MethodSymbol) {
                // It's an annotation value! Skip it.
                return;
            }
        }

        node.rhs = maker.TypeCast(node.lhs.type, node.rhs);
        
        globalParent.push(node);
        super.visitAssign(node);
        globalParent.pop();
        
        if (result != null)
            node = (JCTree.JCAssign)result;

        // Replace the RHS of the assignment with an instrumentation call.

        if (oldLhs instanceof JCTree.JCFieldAccess ||
        		(oldLhs instanceof JCTree.JCIdent &&
        				sneakyFieldAccess((JCTree.JCIdent)oldLhs))) {
        	
        	JCTree.JCExpression selected;
        	Name selector;
        	if (oldLhs instanceof JCTree.JCFieldAccess) {
        		JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess)oldLhs;
        		selected = fa.selected;
        		selector = fa.name;

        		if (selected instanceof JCTree.JCIdent &&
        				((JCTree.JCIdent)selected).sym instanceof ClassSymbol) {
        			// Explicit static field.
        			selected = maker.Select(selected, names.fromString("class"));
        		}
        	} else {
        		JCTree.JCIdent ident = (JCTree.JCIdent)oldLhs;
        		selected = sneakySelected(ident);
        		selector = ident.name;
        	}

        	// Call into runtime to execute store.
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.storeField");
            if(!ENERJ){
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: EXPAX");
            	approximate = expaxIsApprox(node);
            }
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(ASSIGN) - " + node.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(ASSIGN) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(oldLhs.type)),
            	meth,
                List.of(
                    selected,
                    maker.Literal(selector.toString()),
                    boolExp(approximate),
                    node.rhs
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;

        } else if (oldLhs instanceof JCTree.JCIdent) {
        	
        	// Replaced with a reference?
            JCTree.JCExpression ref;
            if (!instrumented.contains(node.lhs))
            	return;
            ref = ((JCTree.JCFieldAccess)node.lhs).selected;

            // Call into runtime to execute store.
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.storeLocal");
            if(!ENERJ) {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: EXPAX");
            	approximate = expaxIsApprox(node);
            }
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(ASSIGN) - " + node.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(ASSIGN) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(oldLhs.type)),
            	meth,
                List.of(
                    ref,
                    boolExp(approximate),
                    node.rhs
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;

        } else if (oldLhs instanceof JCTree.JCArrayAccess) {
        	
        	JCTree.JCArrayAccess aa = (JCTree.JCArrayAccess)oldLhs;

        	// Call into runtime to execute store.
            JCTree.JCExpression meth =
                dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.storeArray");
            if(!ENERJ) {
            	if(EXPAX_ST)
            		System.out.println("*** EXPAX_ST: EXPAX");
            	approximate = expaxIsApprox(node);
            }
            if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(ASSIGN) - " + node.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(ASSIGN) - " + node.toString());
        	}
            JCTree.JCExpression expr = maker.Apply(
            	List.of(boxedTypeExp(oldLhs.type)),
            	meth,
                List.of(
                    aa.indexed,
                    aa.index,
                    boolExp(approximate),
                    node.rhs
                )
            );
            expr = unbox(expr, node.type);
            attribute(expr, node, node.type);
            result = expr;

        }
    }


    // Assign-operations: combined operation/accesses.

    private JCTree.JCExpression assignopCall(
        JCTree.JCExpression arg,
        JCTree.JCExpression oldArg,
        JCTree.JCExpression rhs,
        String opName,
        JCTree.JCExpression repl,
        boolean returnOld
    ) {
        // Operation kind.
        String opExp = "enerj.rt.PrecisionRuntime.ArithOperator." + opName;

        // Local variables.
        if (oldArg instanceof JCTree.JCIdent &&
            ((JCTree.JCIdent)oldArg).sym instanceof Symbol.VarSymbol &&
            ((Symbol.VarSymbol)((JCTree.JCIdent)oldArg).sym).owner
                instanceof Symbol.MethodSymbol)
        {
            JCTree.JCExpression ref =
                ((JCTree.JCFieldAccess)arg).selected;
            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - assignopCall #1");
    		boolean approximate;
    		if (ENERJ) {
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: ENERJ");
    			approximate = isApprox(oldArg);
    		} else {
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: EXPAX");
    			approximate = expaxIsApprox(oldArg);
    		}
    		if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(assignopCall) - " + repl.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(assignopCall) - " + repl.toString());
        	}
            JCTree.JCExpression call = maker.Apply(null,
              dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.assignopLocal"),
              List.of(
                ref,
                dotsExp(opExp),
                rhs,
                boolExp(returnOld),
                numKindExp(oldArg.type),
                boolExp(approximate)
              )
            );
            attribute(call, repl);
            return call;

        // Array accesses.
        } else if (arg instanceof JCTree.JCArrayAccess) {
            JCTree.JCArrayAccess access =
                (JCTree.JCArrayAccess)arg;
            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - assignopCall #2");
    		boolean approximate;
    		if (ENERJ){
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: ENERJ");
    			approximate = isApprox(oldArg);
    		}else{
    			if(EXPAX_ST)
    				System.out.println("*** EXPAX_ST: EXPAX");
    			approximate = expaxIsApprox(oldArg);
    		}
    		if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(assignopCall) - " + repl.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(assignopCall) - " + repl.toString());
        	}
            JCTree.JCExpression call = maker.Apply(
              List.of(boxedTypeExp(oldArg.type)),
              dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.assignopArray"),
              List.of(
                access.indexed,
                access.index,
                dotsExp(opExp),
                rhs,
                boolExp(returnOld),
                numKindExp(oldArg.type),
                boolExp(approximate)
              )
            );
            attribute(call, repl);
            return call;

        // Field accesses.
        } else if (arg instanceof JCTree.JCFieldAccess ||
            (arg instanceof JCTree.JCIdent &&
            		sneakyFieldAccess((JCTree.JCIdent)arg)))
        {
            JCTree.JCExpression selected;
            String name;
            if (arg instanceof JCTree.JCFieldAccess) {
                // Explicit field access.
                JCTree.JCFieldAccess access =
                    (JCTree.JCFieldAccess)arg;
                selected = access.selected;
                name = access.name.toString();
            } else {
                JCTree.JCIdent ident = (JCTree.JCIdent)arg;
                name = ident.name.toString();
                selected = sneakySelected(ident);
            }
            if(EXPAX_ST)
            	System.out.println("*** EXPAX_ST: call isApprox - assignopCall #3");
    		boolean approximate;
    		if (ENERJ)
    			approximate = isApprox(oldArg);
    		else
    			approximate = expaxIsApprox(oldArg);
    		if (approximate) {
        		if(ENERJ)
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: ENERJ(assignopCall) - " + repl.toString());
        		else
        			if(EXPAX_ST)
        				System.out.println("*** EXPAX_APPROX_COUNT: EXPAX(assignopCall) - " + repl.toString());
        	}
            JCTree.JCExpression call = maker.Apply(
              List.of(boxedTypeExp(oldArg.type)),
              dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.assignopField"),
              List.of(
                selected,
                maker.Literal(name),
                dotsExp(opExp),
                rhs,
                boolExp(returnOld),
                numKindExp(oldArg.type),
                boolExp(approximate)
              )
            );
            attribute(call, repl);
            return call;
        }

        return repl;
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitAssignop: tree = " + node.toString());
        lvalues.add(node.lhs);
        JCTree.JCExpression oldLhs = node.lhs;
        if(EXPAX_ST)
        	System.out.println("*** EXPAX_ST: call isApprox - visitAssignop");
        if (ENERJ)
        	isApprox(oldLhs);

        globalParent.push(node);
        super.visitAssignop(node);
        globalParent.pop();

        // Skip non-numeric types.
        if (oldLhs.type.tag == TypeTags.BOOLEAN) {
            attribute(node, node);
            return;
        }
        if (node.type.toString().equals("java.lang.String")) {
            attribute(node, node);
            return;
        }

        String kind = null;
        switch (node.getKind()) {
        case PLUS_ASSIGNMENT:
            kind = "PLUS";
            break;
        case MINUS_ASSIGNMENT:
            kind = "MINUS";
            break;
        case MULTIPLY_ASSIGNMENT:
            kind = "MULTIPLY";
            break;
        case DIVIDE_ASSIGNMENT:
            kind = "DIVIDE";
            break;
        case XOR_ASSIGNMENT:
            kind = "BITXOR";
            break;
        // Probably need to add more here due to weird type conversion issues!
        default:
            // Unsupported operation.
            node.rhs = maker.TypeCast(oldLhs.type, node.rhs);
            attribute(node, node);
            return;
        }

        result = assignopCall(
            node.lhs,
            oldLhs,
            node.rhs,
            kind,
            node,
            false
        );
    }

    // Variable definitions: initial values instrumented as stores.
    @Override
    public void visitVarDef(JCTree.JCVariableDecl node) {
    	if (EXPAX_ST)
        	System.out.println("*** EXPAX_ST: visitVarDef: tree = " + node.toString());
    	
    	globalParent.push(node);
        super.visitVarDef(node);
        globalParent.pop();
        if (result != null)
            node = (JCTree.JCVariableDecl)result;

        // Don't instrument statements inserted by ReferencingTranslator.
        if (replDecls.contains(node))
            return;
        
        // Instrument variable initializations like assignments.
        if (node.init != null) {
        	if ((node.sym.type.tsym.flags() & Flags.ENUM) != 0) {
        		// Don't instrument assigns to enum values.
        		return;
        	}
            node.init = storeCall(node, node.init, valSymbolKind(node.sym));
            result = node;
        }
    }
    
  
    
}
