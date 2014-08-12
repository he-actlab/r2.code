package enerj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import checkers.basetype.BaseTypeVisitor;
import checkers.quals.TypeQualifiers;
import checkers.runtime.InstrumentingChecker;
import checkers.runtime.instrument.InstrumentingTranslator;
import checkers.source.SupportedLintOptions;
import checkers.types.*;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.AnnotationUtils;
import checkers.util.ElementUtils;
import checkers.util.GraphQualifierHierarchy;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

import enerj.instrument.MethodBindingTranslator;
import enerj.instrument.RuntimePrecisionTranslator;
import enerj.instrument.SimulationTranslator;
import enerj.jchord.result.R2JchordResult;
import enerj.jchord.result.R2JchordResult.R2JchordResultStorageEntry;
import enerj.lang.*;
import enerj.rt.Reference;
import r2.instrument.GenerateApproxLocalVariableTranslator;

/**
 * The precision type checker.
 */
@TypeQualifiers({Approx.class, Precise.class, Top.class, Context.class})
@SupportedLintOptions( { PrecisionChecker.STRELAXED,
	PrecisionChecker.MBSTATIC, PrecisionChecker.MBDYNAMIC,
	PrecisionChecker.SIMULATION } )
/* A note about how to pass these options:
 * Do not use:
 *   -Alint=strelaxed -Alint=mbdynamic
 * but instead use:
 *   -Alint=strelaxed,mbdynamic
 * You will not get a warning about this...
 */
public class PrecisionChecker extends InstrumentingChecker {
		
	public static boolean ENERJ = true;
	public static boolean R2_DEBUG = false;
	
	// Subtyping lint options
	// We currently only have one option, STRELAXED.
	// If the option is not present, we use the stricter subtyping
	// hierarchy.
	public static final boolean STRELAXED_DEFAULT = false;
	public static final String STRELAXED = "strelaxed";

	// Method binding lint options
	// We have two options: MBSTATIC and MBDYNAMIC
	// If neither option is given, we do not modify method calls
	public static final boolean MBSTATIC_DEFAULT = false;
	public static final String MBSTATIC = "mbstatic";

	public static final boolean MBDYNAMIC_DEFAULT = false;
	public static final String MBDYNAMIC = "mbdynamic";

	// Whether to simulate the approximate execution of the program
	public static final boolean SIMULATION_DEFAULT = false;
	public static final String SIMULATION = "simulation";


	// The method name post-fixes that are used for approximate/precise methods
	public static final String MB_APPROX_POST = "_APPROX";
	public static final String MB_PRECISE_POST = "_PRECISE";

    public AnnotationMirror APPROX, PRECISE, TOP, CONTEXT;

    public static R2ASTNodeInfo r2BcInfo; 
    public static R2JchordResult r2JchordResult;
    public static Map<JCTree,R2JchordResultStorageEntry> treeToJchordStorageResult = new HashMap<JCTree,R2JchordResultStorageEntry>();
    public static boolean analysisFlag = false;

    @Override
    public void initChecker(ProcessingEnvironment env) {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);
        APPROX = annoFactory.fromClass(Approx.class);
        PRECISE = annoFactory.fromClass(Precise.class);
        TOP = annoFactory.fromClass(Top.class);
        CONTEXT = annoFactory.fromClass(Context.class);

        super.initChecker(env);
    
        readR2Options(env);	
    }

    /**
     * Read three files: 
     * (1) analysis.flag: jchord analysis flag 
     * (2) XXXXXX-bc-offset.info: bytecode offset info 
     * (3) analysis.result: analysis result
     * @param env
     */
    public static void readR2Options(ProcessingEnvironment env) {
    	Collection<String> values = env.getOptions().values();
        for (String value: values) {
        	if(R2_DEBUG)
        		System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> value = " + value);
        	StringTokenizer st = new StringTokenizer(value, ",");
        	while(st.hasMoreTokens()){
        		String token = st.nextToken();
        		if (token.equalsIgnoreCase("r2")){
        			ENERJ = false;
        		} else if (token.equalsIgnoreCase("r2debug")){ 
        			R2_DEBUG = true;
        		} else if (token.contains(".flag")) {
        			String analysisFlagFilename = token;
	    			FileReader fr = null;
	    			BufferedReader br = null;
	    			try { 
	    				fr = new FileReader(analysisFlagFilename);
	    				br = new BufferedReader(fr);
	    				String analysisFlagStr = br.readLine();
	    				analysisFlag = (analysisFlagStr.equalsIgnoreCase("true")) ? true : false;
	    				if(R2_DEBUG)
	    					System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> read analysis flag: " + analysisFlag);
	     			} catch(Exception e){
	    				e.printStackTrace();
	    			}
        		} else if (token.contains(".info")){
        			if (analysisFlag) {
        				r2BcInfo = new R2ASTNodeInfo(false);  // param: debug flag; here, it fixed as false
		    			String r2BcInfoFileName = token;
		    			r2BcInfo.read(r2BcInfoFileName);
		    			if(R2_DEBUG)
		    				System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> read bytecode offset information");
        			}
	    		} else if (token.contains(".result")){
	    			if (analysisFlag) {
	    				r2JchordResult = new R2JchordResult(false);  // param: debug flag; here, it fixed as false
		    			String r2AnalResultFileName = token;
		    			r2JchordResult.read(r2AnalResultFileName);
		    			if(R2_DEBUG)
		    				System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> read jchord analysis result");
	    			}
	    		}
	    	}
        	if(ENERJ) {
    			if(R2_DEBUG)
    				System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> work on EnerJ!");
        	} else {
        		if(R2_DEBUG)
        			System.out.println("*** R2_DEBUG[PrecisionChecker]: <readR2Options> work on R2!");
        	}
        		
        }
    }
    
    /**
     * Returning null here turns off instrumentation in the superclass.
     */
    @Override
    public InstrumentingTranslator<PrecisionChecker> getTranslator(TreePath path) {
        return null;
    }

    // Hook to run tree translators (AST transformation step).
    @Override
    public void typeProcess(TypeElement e, TreePath p) {  
		JCTree tree = (JCTree) p.getCompilationUnit(); // or maybe p.getLeaf()?
		
        if (debug()) {
            System.out.println("Translating from:");
            System.out.println(tree);
        }
        
		// first: determine what method to call
		if (getLintOption(PrecisionChecker.MBSTATIC, PrecisionChecker.MBSTATIC_DEFAULT)
			|| getLintOption(PrecisionChecker.MBDYNAMIC, PrecisionChecker.MBDYNAMIC_DEFAULT)) {
			if (ENERJ) {
				if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> MethodBindingTranslator start");
				tree.accept(new MethodBindingTranslator(this, processingEnv, p, null, null));
				if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> MethodBindingTranslator end");
			} else {
				if (analysisFlag){
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> MethodBindingTranslator start");
					tree.accept(new MethodBindingTranslator(this, processingEnv, p, r2BcInfo, r2JchordResult));
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> MethodBindingTranslator end");
				}
			}
		}

        // Run the checker next and ensure everything worked out.
        super.typeProcess(e, p);

		if (getLintOption(PrecisionChecker.MBSTATIC, PrecisionChecker.MBSTATIC_DEFAULT)
				|| getLintOption(PrecisionChecker.MBDYNAMIC, PrecisionChecker.MBDYNAMIC_DEFAULT)
				|| getLintOption(PrecisionChecker.SIMULATION, PrecisionChecker.SIMULATION_DEFAULT)) {
			if (ENERJ) {
				if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> RuntimePrecisionTranslator start");
				tree.accept(new RuntimePrecisionTranslator(this, processingEnv, p, true));
				if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> RuntimePrecisionTranslator end");
			} else {
				if (analysisFlag){
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> RuntimePrecisionTranslator start");
					tree.accept(new RuntimePrecisionTranslator(this, processingEnv, p, false));
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> RuntimePrecisionTranslator end");
				}
			}

			if (getLintOption(PrecisionChecker.SIMULATION, PrecisionChecker.SIMULATION_DEFAULT)) {
				if (ENERJ) {
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> SimulationTranslator start");
					tree.accept(new SimulationTranslator(this, processingEnv, p, null, null, null));
					if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> SimulationTranslator end");
				} else {
					if (analysisFlag) {
						if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> GenerateApproxLocalVariableTranslator start");
//						GenerateApproxLocalVariableTranslator gTranslator = new GenerateApproxLocalVariableTranslator(this, processingEnv, p);
//						tree.accept(gTranslator);
						if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> GenerateApproxLocalVariableTranslator end");
						if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> SimulationTranslator start");
//						tree.accept(new SimulationTranslator(this, processingEnv, p, r2BcInfo, r2JchordResult, gTranslator.approxNameSet));
						tree.accept(new SimulationTranslator(this, processingEnv, p, r2BcInfo, r2JchordResult, new HashMap<String,Set<Name>>()));
						if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeProcess> SimulationTranslator end");
					}
				}
			}
		}

        if (debug()) {
            System.out.println("Translated to:");
            System.out.println(tree);
        }
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    	
        Set<Class<? extends Annotation>> typeQualifiers
            = new HashSet<Class<? extends Annotation>>();

        typeQualifiers.add(Precise.class);
        typeQualifiers.add(Approx.class);
        typeQualifiers.add(Context.class);

        // Always allow Top as top modifier, regardless of subtyping hierarchy
		// if(!getLintOption(STRELAXED, STRELAXED_DEFAULT)) {
        typeQualifiers.add(Top.class);

        return Collections.unmodifiableSet(typeQualifiers);
    }

    @Override
    protected QualifierHierarchy createQualifierHierarchy() {
        AnnotationUtils annoFactory = AnnotationUtils.getInstance(env);

        GraphQualifierHierarchy.GraphFactory factory = new GraphQualifierHierarchy.GraphFactory(this);

        AnnotationMirror typeQualifierAnno, superAnno;
        
		if(getLintOption(STRELAXED, STRELAXED_DEFAULT)) {
			typeQualifierAnno= annoFactory.fromClass(Precise.class);
			superAnno = annoFactory.fromClass(Approx.class);
			factory.addSubtype(typeQualifierAnno, superAnno);

			typeQualifierAnno= annoFactory.fromClass(Context.class);
			factory.addSubtype(typeQualifierAnno, superAnno);

			// To allow one annotation for classes like Endorsements, we still
			// add the Top qualifier.
			typeQualifierAnno= annoFactory.fromClass(Top.class);
			factory.addSubtype(superAnno, typeQualifierAnno);
		} else {
			typeQualifierAnno= annoFactory.fromClass(Precise.class);
			superAnno = annoFactory.fromClass(Top.class);
			factory.addSubtype(typeQualifierAnno, superAnno);

			typeQualifierAnno= annoFactory.fromClass(Approx.class);
			factory.addSubtype(typeQualifierAnno, superAnno);

			typeQualifierAnno= annoFactory.fromClass(Context.class);
			factory.addSubtype(typeQualifierAnno, superAnno);
		}

        QualifierHierarchy hierarchy = factory.build();
        if (hierarchy.getTypeQualifiers().size() < 2) {
            throw new IllegalStateException("Invalid qualifier hierarchy: hierarchy requires at least two annotations: " + hierarchy.getTypeQualifiers());
        }
        return hierarchy;
    }

    // Removed (moved, really) in Checker Framework 1.3.0, but I'm leaving this
    // here for earlier versions.
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType) {
		// The checker calls this method to compare the annotation used in a
		// type to the modifier it adds to the class declaration. As our default
		// modifier is Precise, this results in an error when Approx is used
    	// as type annotation. Just ignore this check here and do them manually
    	// in the visitor.
    	return true;
    }

    /**
     * For primitive types we allow an assignment from precise types to
     * approximate types.
     * In the other cases, follow the specified hierarchy in the qualifiers.
     */
    @Override
    public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup) {
    	// System.out.println("Call of isSubtype; sub: " + sub + ", sup: " + sup);

    	// Null is always a subtype of any reference type.
    	if (sub.getKind() == TypeKind.NULL &&
    			(sup.getKind() == TypeKind.DECLARED || sup.getKind() == TypeKind.TYPEVAR)) {
    		return true;
    	}

    	// TODO: I think this special case is only needed for the strict subtyping
    	// option. But it also shouldn't break the relaxed subtyping hierarchy.
    	
    	if( sub.getUnderlyingType().getKind().isPrimitive() &&
    		sup.getUnderlyingType().getKind().isPrimitive() ) {
    		if ( sup.getAnnotations().contains(APPROX) ||
    			 sup.getAnnotations().contains(TOP) ||
    			 (sup.getAnnotations().contains(CONTEXT) &&
    			  sub.getAnnotations().contains(PRECISE)) ||
    			 sup.getAnnotations().equals(sub.getAnnotations())){
    			return true;
    		} else {
    			return false;
    		}
    	}
    	return super.isSubtype(sub, sup);
    }

	/**
	 * Determine whether the two given methods are substitutable, that is, every
	 * possible call of origexe will also succeed if we substitute newexe for
	 * it.
	 *
	 * @param origexe The original method.
	 * @param newexe The new method that we want to substitute.
	 * @return True, iff we can safely substitute the method.
	 */
    public boolean isCompatible(AnnotatedExecutableType origexe, AnnotatedExecutableType newexe) {
    	if (origexe.getParameterTypes().size() != newexe.getParameterTypes().size()) {
    		return false;
    	}

    	/* TODO: when depending on the context, this is not good... should we always
    	 * check this? Is this only a problem with strict subtyping?
    	if (!isSubtype( origexe.getReturnType(), newexe.getReturnType() ) ) {
    		return false;
    	}
    	*/

    	List<AnnotatedTypeMirror> origparams = origexe.getParameterTypes();
    	List<AnnotatedTypeMirror> newparams = newexe.getParameterTypes();

    	for(int i=0; i<origparams.size(); ++i) {
    		if(!isSubtype(origparams.get(i), newparams.get(i))) {
    			return false;
    		}
    	}

		return true;
	}

	/**
	 * Explicitly construct components that weren't being automatically
	 * detected by introspection for some reason. (Seems to be necessary
     * on at least some Mac OS X systems.)
	 */
	@Override
	public AnnotatedTypeFactory createFactory(CompilationUnitTree root) {
	    return new PrecisionAnnotatedTypeFactory(this, root);
	}

	@Override
	protected BaseTypeVisitor<?> createSourceVisitor(CompilationUnitTree root) {
	    return new PrecisionVisitor(this, root);
	}

	/**
	 * Determines whether a given declared type (i.e., class) is approximable
	 * (has an @Approximable annotation) or whether the whole package is approximable.
	 */
	public static boolean isApproximable(DeclaredType type) {
		Element tyelem = type.asElement();
		Approximable ann = tyelem.getAnnotation(Approximable.class);
	    if (ann != null) {
	    	return true;
	    }
	    tyelem = ElementUtils.enclosingPackage(tyelem);
	    if (tyelem!=null) {
	    	ann = tyelem.getAnnotation(Approximable.class);
		    if (ann != null) {
		    	return true;
		    }
	    }
	    return false;
	}


	/**** Object size calculation. ****/

	private static final int POINTER_SIZE = 8; // on 64-bit VM
	private static final int LINE_SIZE = 64; // x86

	// Get the size of a Reference (local variable) at runtime.
	public static <T> int[] referenceSizes(Reference<T> ref) {
//		System.out.println("*** R2_DEBUG: referenceSizes called");
	    int preciseSize = 0;
	    int approxSize = 0;

        if (ref.primitive) {
            int size = 0;
            if (ref.value instanceof Byte) size = 1;
    	    else if (ref.value instanceof Short) size = 2;
    	    else if (ref.value instanceof Integer) size = 4;
    	    else if (ref.value instanceof Long) size = 8;
    	    else if (ref.value instanceof Float) size = 4;
    	    else if (ref.value instanceof Double) size = 8;
    	    else if (ref.value instanceof Character) size = 2;
    	    else if (ref.value instanceof Boolean) size = 1; // not defined
    	    else assert false;
            
    	    if (ref.approx) 
                approxSize = size;
            else
                preciseSize = size;

        } else { // Object or array type.
            preciseSize = POINTER_SIZE;
        }

//        System.out.println("*** precise(" + preciseSize + ") approxSize(" + approxSize + ")");
	    return new int[] {preciseSize, approxSize};
	}

	// Get the size of a particular static type.
	public static int[] typeSizes(AnnotatedTypeMirror type, boolean apprCtx, PrecisionChecker checker, boolean approxField, String field, JCTree tree) {
	    int preciseSize = 0;
	    int approxSize = 0;

	    if (type.getKind() == TypeKind.DECLARED) {
            // References are always precise.
            preciseSize += POINTER_SIZE;
        } else {

            int size = 0;
            switch (type.getKind()) {
    	    case ARRAY: size = 0; break;  
    	    case BOOLEAN: size = 1; break; 
    	    case BYTE: size = 1; break;
    	    case CHAR: size = 2; break;
    	    case DOUBLE: size = 8; break;
    	    case FLOAT: size = 4; break;
    	    case INT: size = 4; break;
    	    case LONG: size = 8; break;
    	    case SHORT: size = 2; break;
    	    default: assert false;
    	    }

            if(ENERJ){
	            if (type.hasEffectiveAnnotation(checker.APPROX) ||
	                    (apprCtx && type.hasEffectiveAnnotation(checker.CONTEXT))) {
		            approxSize += size;
	            } else {
		            preciseSize += size;
	            }
            } else {
            	boolean approx = false;
            	if(approxField) {
                	// The fact that 'approxField == true' means that there are approximate fields in this class instances
            		// Find the fields here
            		if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeSizes> type = " + type.toString());
        			for (Map.Entry<JCTree, R2JchordResultStorageEntry> entry : treeToJchordStorageResult.entrySet()){
        				JCTree entryTree = entry.getKey();
        				if(entryTree.equals(tree)) {
        					R2JchordResultStorageEntry jResult = entry.getValue();
        					if(field.toString().equalsIgnoreCase(jResult.getField())) {
        						if(R2_DEBUG) System.out.println("*** R2_DEBUG[PrecisionChecker]: <typeSizes> determine this field as approximate = " + field.toString());
        						approx = true;
        					}
        				}
        			}
            	}
            	if (approx){
 		            approxSize += size;
 	            } else {
 		            preciseSize += size;
 	            }
            }
        }

        return new int[]{preciseSize, approxSize};
	}

	// Get the size of an instance of a given class type (at compile time).
	public static int[] objectSizes(AnnotatedTypeMirror type,
	                                AnnotatedTypeFactory factory,
	                                Types typeutils,
	                                PrecisionChecker checker,
	                                boolean approxClass,
	                                JCTree tree) {
	    boolean approx;
	    if(ENERJ)
	    	approx = type.hasEffectiveAnnotation(checker.APPROX);
	    else
	    	approx = approxClass; 
	    int preciseSize = 0;
	    int approxSize = 0;

	    List<? extends Element> members =
	        ((TypeElement)typeutils.asElement(type.getUnderlyingType())).getEnclosedElements();

	    for (VariableElement field : ElementFilter.fieldsIn(members)) {
	        AnnotatedTypeMirror fieldType = factory.getAnnotatedType(field);
	        int[] sizes = typeSizes(fieldType, approx, checker, approxClass, field.toString(), tree);
	        preciseSize += sizes[0];
	        approxSize  += sizes[1];
	    }

	    preciseSize += POINTER_SIZE; // vtable

	    int wastedApprox = Math.min(
	    	LINE_SIZE - (preciseSize % LINE_SIZE), // remainder of last precise line
	    	approxSize // all the approximate data
	    );
	    preciseSize += wastedApprox;
	    approxSize -= wastedApprox;

	    if (wastedApprox != 0 || approxSize != 0) {
	    	System.out.println(preciseSize + " " + approxSize + "; " + wastedApprox);
	    }

	    return new int[]{preciseSize, approxSize};
	}
}
