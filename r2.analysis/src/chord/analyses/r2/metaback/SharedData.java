package chord.analyses.r2.metaback;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import joeq.Class.jq_Field;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.Dominators;
import joeq.Compiler.Quad.Inst;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Operand.TypeOperand;
import joeq.Compiler.Quad.Operator.ALoad;
import joeq.Compiler.Quad.Operator.AStore;
import joeq.Compiler.Quad.Operator.Binary;
import joeq.Compiler.Quad.Operator.Getfield;
import joeq.Compiler.Quad.Operator.Getstatic;
import joeq.Compiler.Quad.Operator.Move;
import joeq.Compiler.Quad.Operator.New.NEW;
import joeq.Compiler.Quad.Operator.Putfield;
import joeq.Compiler.Quad.Operator.Putstatic;
import joeq.Compiler.Quad.Operator.Unary;
import joeq.Compiler.Quad.Quad;
import chord.analyses.alias.CIPAAnalysis;
import chord.analyses.alias.ICICG;
import chord.analyses.alloc.DomH;
import chord.analyses.basicblock.DomB;
import chord.analyses.basicblock.RelPostDomBB;
import chord.analyses.field.DomF;
import chord.analyses.point.DomP;
import chord.project.Config;
import chord.util.ArraySet;
import chord.util.DomU;
import chord.util.tuple.object.Pair;

public class SharedData {
	static DomP domP;
	static DomU domU;
	static DomH domH;
	static DomF domF;
	static DomB domB;
	static ICICG cicg;
	static Inst exitMain;
	static CIPAAnalysis cipa;
	static jq_Method mainMethod;
	static jq_Method threadStartMethod;
	static Set<String> restrictMethList;
	static Set<String> relaxMethList;
	static Set<Integer> allApproxStatements;
	static Set<Pair<Integer,Integer>> allApproxStorage;
	static Map<Quad,Set<jq_Method>> targetsMap = new HashMap<Quad,Set<jq_Method>>();
	
	static final String restrictAllMethPrefix = "restrict_all";
	static final String relaxAllMethPrefix = "relax_all";
	static final String tagMethPrefix = "TAG";
	// jspark: temporary variable to store a tag for the following new object
	static String tag = ""; 
	static Map<Quad,String> quadToTagMap = new HashMap<Quad,String>();
	// jspark: pair of method name and the argument index number
	static Set<Pair<String,Integer>> preciseLst = new HashSet<Pair<String,Integer>>();
	// jspark: FIXME assigned a fixed filename for now. should it be a property?
	static String fieldTxtFileName = "field.txt"; 
	// jspark: map from field index to pair of field name and description 
	static Map<String,Pair<String,String>> hfFieldMap = new HashMap<String,Pair<String,String>>();
	// jspark: map from field index to tuple of class name, field name, and description
	static Map<String,Pair<String,Pair<String,String>>> gFieldMap = new HashMap<String,Pair<String,Pair<String,String>>>();
	// jspark: map from field index to jq_Field
	static Map<Pair<Integer,Integer>,Pair<Quad,jq_Field>> idxFieldMap = new HashMap<Pair<Integer,Integer>,Pair<Quad,jq_Field>>();
	// jspark: approx parameters' set
	static Map<jq_Method,Set<Integer>> approxParams = new HashMap<jq_Method,Set<Integer>>();
	// jspark: post dominator map
	static Map<jq_Method,Dominators> postDominatorsMap = new HashMap<jq_Method,Dominators>();
	// jspark: dominator map
	static Map<jq_Method,Map<BasicBlock,Set<BasicBlock>>> domMap = new HashMap<jq_Method,Map<BasicBlock,Set<BasicBlock>>>();
	// jspark: postdominator map
	static Map<jq_Method,Map<BasicBlock,Set<BasicBlock>>> pdomMap = new HashMap<jq_Method,Map<BasicBlock,Set<BasicBlock>>>();
	// jspark: control dependence
	static Map<Pair<jq_Method,BasicBlock>, List<BasicBlock>> ctrlDependence = new HashMap<Pair<jq_Method,BasicBlock>, List<BasicBlock>>();
	// jspark: approximable if-conditionals set 
	static Map<Quad, Boolean> approxIfConditional = new HashMap<Quad, Boolean>();
	// jspark: keep unused approx operations
	static Set<Quad> unusedQuads = new ArraySet<Quad>();
	
	static String excludeStr = System.getProperty("chord.check.exclude","java.,com.,sun.,sunw.,javax.,launchrer.,org.");
	
	// jspark: DEBUG - stores the mapping between a quad and an index corresponding to it
	static Map<Integer,Quad> indexQuadMap = new HashMap<Integer,Quad>();
	// jspark: DEBUG - stores a list of quads that have already removed from approximate operation list
	static Set<Quad> prevRemovedQuad = new ArraySet<Quad>();  
	static Set<Quad> previousAllApproxOpSet = new ArraySet<Quad>();
	static Set<Quad> previousAllApproxStorageSet = new ArraySet<Quad>();
	static boolean R2_LOG = false; 
	
	// jspark: Experiment - Total # of operations analyzed (visited)
	
	
	static{
		restrictMethList = new HashSet<String>();
		restrictMethList.add("tighten");
	}

	static{
		relaxMethList = new HashSet<String>();
		relaxMethList.add("loosen");
	}
	
	public static boolean isMainMethod(jq_Method m){
		return m == mainMethod;
	}
	
	public static boolean isThreadStartMethod(jq_Method m){
		return m == threadStartMethod;
	}
	
	public static boolean isCinit(jq_Method m){
		return m.getName().toString().equals("<clinit>");
	}
	
	public static boolean isRestrictMethod(jq_Method m){
		return restrictMethList.contains(m.getName().toString());
	}
	
	public static boolean isRelaxMethod(jq_Method m){
		return relaxMethList.contains(m.getName().toString());
	}
	
	public static boolean isRestrictAllMethod(jq_Method m){
		String methName = m.getName().toString();
		return methName.startsWith(restrictAllMethPrefix);
	}
	
	public static boolean isRelaxAllMethod(jq_Method m){
		String methName = m.getName().toString();
		return methName.startsWith(relaxAllMethPrefix);
	}
	
	public static boolean isAllocMethod(jq_Method m){
		String methName = m.getName().toString();
		return methName.startsWith(tagMethPrefix);
	}
	
	/**
	 * Parse the method name of "TAGX" and get "X" from it
	 */
	public static void parseAllocTag(jq_Method m){
		String methName = m.getName().toString();
		StringTokenizer st = new StringTokenizer(methName,tagMethPrefix + "TAG");
		tag = st.nextToken();
	}
	
	/**
	 * For hf field, parse the method name of "relax_all_FIELDX_TAGY" or "precise_all_FIELDX_TAGY"
	 * Return an index number correspnoding to the field when TAG Y is matched with the tag of allocation sites
	 */
	public static int getFieldIdx(Quad q, jq_Method m){
		// get fieldname
		String methName = m.getName().toString();
		StringTokenizer st = new StringTokenizer(methName,"_");
		st.nextToken();
		st.nextToken();
		String fieldToken = st.nextToken();
		if(!fieldToken.startsWith("FIELD"))
			throw new RuntimeException("Wrong method name: " + methName);
		String fieldIdx = fieldToken.substring(5);
		Pair<String,String> pair = hfFieldMap.get(fieldIdx);
		String fieldname = pair.val0;
		String desc = pair.val1;
		if (!desc.equalsIgnoreCase("ARRAY")) { // class object.field
 			// get classname
			String clsName = null;
			if (q.getOperator() instanceof NEW){
				Operand op = q.getOp2();
				TypeOperand top = (TypeOperand) op;
				clsName = top.toString();
				clsName = formatClsName(clsName);
			} else {
				throw new RuntimeException("This shouldn't be an array!");
			}
			// get jq_Field object
			StringTokenizer fieldSt = new StringTokenizer(clsName + " " + fieldname + " " + desc, " ");
			jq_Member field = jq_Field.read(fieldSt);
			return domF.indexOf(field);
		} else { // arrays
			return -1;
		}
	}
	
	/**
	 * For global field (static field), parse the method name of "relax_all_FIELDX_TAGY" or "precise_all_FIELDX_TAGY"
	 * Return an index number corresponding to the field when TAG Y is matched with the tag of allocation sites
	 * In this case, class name is also provided by "field.txt" because there is no object for static field
	 */
	public static int getFieldIdx(jq_Method m){
		// get fieldname
		String methName = m.getName().toString();
		StringTokenizer st = new StringTokenizer(methName,"_");
		st.nextToken();
		st.nextToken();
		String fieldToken = st.nextToken();
		if(!fieldToken.startsWith("FIELD"))
			throw new RuntimeException("Wrong method name: " + methName);
		String fieldIdx = fieldToken.substring(5);

		// get classname
		String clsName = gFieldMap.get(fieldIdx).val0;
		clsName = formatClsName(clsName);
		Pair<String,String> pair = gFieldMap.get(fieldIdx).val1;
		String fieldname = pair.val0;
		String desc = pair.val1;

		// get jq_Field object
		StringTokenizer fieldSt = new StringTokenizer(clsName + " " + fieldname + " " + desc, " ");
		jq_Member field = jq_Field.read(fieldSt);
		return domF.indexOf(field);
	}
	
	/**
	 * Format class name as chord requires
	 */
	public static String formatClsName(String clsName) {
		clsName = clsName.replace(".", "/");
		return "L" + clsName + ";";
	}
	
	/**
	 * From "relax_all_FEILDX_TAGY" or "restrict_all_FEILDX_TAGY", get Y
	 */
	public static String getTag(jq_Method m){
		// get fieldname
		String methName = m.getName().toString();
		StringTokenizer st = new StringTokenizer(methName,"_");
		st.nextToken();
		st.nextToken();
		st.nextToken();
		String tagToken = st.nextToken();
		if(!tagToken.startsWith("TAG"))
			throw new RuntimeException("Wrong method name: " + methName);
		String tag = tagToken.substring(3);
		return tag;
	}
	
	public static boolean isQuadApproximable(Quad q){
		Operator o = q.getOperator();
		if(o instanceof Move){
			if(o instanceof Move.MOVE_A || o instanceof Move.MOVE_P)
				return false;
			return true;
		}
		if(o instanceof ALoad){
			if(o instanceof ALoad.ALOAD_A || o instanceof ALoad.ALOAD_P)
				return false;
			return true;
		}
		if(o instanceof AStore){
			if(o instanceof AStore.ASTORE_A || o instanceof AStore.ASTORE_P)
				return false;
			return true;
		}
		if(o instanceof Getfield){
			if(o instanceof Getfield.GETFIELD_A || o instanceof Getfield.GETFIELD_A_DYNLINK || o instanceof Getfield.GETFIELD_P || o instanceof Getfield.GETFIELD_P_DYNLINK )
				return false;
			return true;
		}
		if(o instanceof Putfield){
			if(o instanceof Putfield.PUTFIELD_A || o instanceof Putfield.PUTFIELD_A_DYNLINK || o instanceof Putfield.PUTFIELD_P || o instanceof Putfield.PUTFIELD_P_DYNLINK )
				return false;
			return true;
		}
		if(o instanceof Getstatic){
			if(o instanceof Getstatic.GETSTATIC_A || o instanceof Getstatic.GETSTATIC_A_DYNLINK || o instanceof Getstatic.GETSTATIC_P || o instanceof Getstatic.GETSTATIC_P_DYNLINK )
				return false;
			return true;
		}
		if(o instanceof Putstatic){
			if(o instanceof Putstatic.PUTSTATIC_A || o instanceof Putstatic.PUTSTATIC_A_DYNLINK || o instanceof Putstatic.PUTSTATIC_P || o instanceof Putstatic.PUTSTATIC_P_DYNLINK )
				return false;
			return true;
		}
		if(o instanceof Unary){
			if(o instanceof Unary.ADDRESS_2INT || o instanceof Unary.ADDRESS_2OBJECT || o instanceof Unary.INT_2ADDRESS || o instanceof Unary.ISNULL_P || o instanceof Unary.OBJECT_2ADDRESS)
				return false;
			return true;
		}
		if(o instanceof Binary)
			return true;
		else {
			System.out.println("not approximable operation: " + o.toString());	
			return false;
		}
	}
	
	public static boolean isSkippedMethod(Quad i) {
		Set<jq_Method> targets = targetsMap.get(i);
		if (targets == null) {
			targets = cicg.getTargets(i);
			targetsMap.put(i, targets);
		}
		return targets.isEmpty();
	}
	
	public static String convertClassName(String className) {
		String newClassName = "";
		for (int i=0; i<className.length(); i++) {
			if (i == 0 && className.charAt(i) == 'L')
				continue;
			if(i == className.length() - 1 && className.charAt(i) == ';')
				continue;
			if(className.charAt(i) == '$' || className.charAt(i) == '/')
				newClassName += '.';
			else
				newClassName += className.charAt(i);
		}
		return newClassName;
	}

	/**
	 * Read a file, "precise.lst" and store information of it into "preciseLst"
	 */
	public static void readPreciseLst() {
		File preciseLstFile = null;
		BufferedReader reader = null;
		String preciseLstFileName = Config.preciseListName;
		if (preciseLstFileName == null)
			preciseLstFile = null;
		else 
			preciseLstFile = new File(preciseLstFileName);
		if(!preciseLstFile.exists()){
			System.out.println("*** R2_SHARED_DATA: NO precise.lst");
			return;
		}
		InputStream is;
		try {
			System.out.println("*** R2_SHARED_DATA: read precise.lst");
			is = new FileInputStream(preciseLstFile);
			reader = new BufferedReader(new InputStreamReader(is));
			
			String line = reader.readLine();
			while(line != null){
		        String[] tokens = line.split("\\s+");
		        if (tokens.length != 2) {
		        	reader.close();
		        	throw new RuntimeException("Error! precise.lst should have a tuple: (argument#, method identifier)");
		        }
		        preciseLst.add(new Pair<String,Integer>(tokens[1],new Integer(Integer.parseInt(tokens[0]))));
		        line = reader.readLine();
		    }       
			
			reader.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	/**
	 * Read a file, "field.txt", and store information of it into either hfFieldMap or gFieldMap
	 */
	public static void readFieldTextFile() {
		File fieldTxtFile = null;
		BufferedReader reader = null;
		fieldTxtFile = new File(fieldTxtFileName);
		if(!fieldTxtFile.exists())
			return;
		InputStream is;
		try {
			is = new FileInputStream(fieldTxtFile);
			reader = new BufferedReader(new InputStreamReader(is));
			
			String line = reader.readLine();
			while(line != null){
		        String[] tokens = line.split("\\s+");
		        if (tokens.length != 4 && tokens.length != 5) {
		        	reader.close();
		        	throw new RuntimeException("field.txt should have one of tuples: (fieldIdx# HF fieldname description) "
		        								+ "or (fieldIdx# G fieldname description classname)");
		        }
		        String fieldIdx = tokens[0];
		        String type = tokens[1];
		        if (type.equalsIgnoreCase("HF")) {
		        	// object.field
			        String fieldName = tokens[2];
			        String desc = tokens[3];
			        hfFieldMap.put(fieldIdx, new Pair<String,String>(fieldName,desc));
		        } else if (type.equalsIgnoreCase("G")) {
		        	// global
		        	String fieldname = tokens[2];
		        	String desc = tokens[3];
		        	String classname = tokens[4];
		        	Pair<String,String> pair = new Pair<String,String>(fieldname,desc);
		        	gFieldMap.put(fieldIdx, new Pair<String,Pair<String,String>>(classname,pair));
		        }
		        line = reader.readLine();
		    }       
			
			reader.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static String formatTime(double time) {
		int ms = (int)(time % 1000);
		int totalSeconds = (int)((time - ms) / 1000);
		int remain;
		int day = (int)(totalSeconds / 86400);
		remain = (int)(totalSeconds - day * 86400);
		int hour = (int)(remain / 3600);
		remain = (int)(remain - hour * 3600);
		int minute = (int)(remain / 60);
		remain = (int)(remain - minute * 60);
		int second = remain;
		return String.format("%02d:%02d:%02d:%02d:%03d dd:hh:mm:ss:ms",day,hour,minute,second,ms);
	}
}
