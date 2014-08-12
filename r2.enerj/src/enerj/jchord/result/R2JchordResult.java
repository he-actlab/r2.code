package enerj.jchord.result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.tools.javac.jvm.r2.R2ASTNodeInfo.R2ASTNodeInfoEntry;

public class R2JchordResult {
	
	public static Set<String> approxClasses = new HashSet<String>();
	
	private boolean R2_DEBUG = false;
	private static final String R2SEP = "#";
	private Set<R2JchordResultOpEntry> resultOpSet;
	private Set<R2JchordResultStorageEntry> resultStorageSet;
	private Set<R2JchordResultParamsEntry> resultParamSet;
	
	public R2JchordResult (boolean DEBUG){
		 resultOpSet = new HashSet<R2JchordResultOpEntry>();
		 resultStorageSet = new HashSet<R2JchordResultStorageEntry>();
		 resultParamSet = new HashSet<R2JchordResultParamsEntry>();
		 this.R2_DEBUG = DEBUG;
	}
	
	public Set<R2JchordResultOpEntry> getResultOpSet(){
		return resultOpSet;
	}
	
	public Set<R2JchordResultStorageEntry> getResultStorageSet(){
		return resultStorageSet;
	}
	
	public Set<R2JchordResultParamsEntry> getResultParamsSet(){
		return resultParamSet;
	}
	
	public void read(String resultFilePath){
		if (resultFilePath == "")
			throw new RuntimeException("Error! analysis result filename has not been specified!");
		if (resultOpSet.size() != 0 || resultStorageSet.size() != 0 || resultParamSet.size() != 0)
			throw new RuntimeException("Error! When read analysis result information, resultSet should be an empty set. ");
		try {
			File file = new File(resultFilePath);
			if (!file.exists())
				throw new RuntimeException("Error! analysis result file is missing!");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			read(br);
			br.close();
			fr.close();
			if(R2_DEBUG)	print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void read(BufferedReader br) {
		try {
			String sizeStr = br.readLine();
			if(R2_DEBUG)
				System.out.println("*** R2_DEBUG[R2JchordResult]: <read> analysis result size (op)= " + sizeStr);
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String resultLine = br.readLine();
				R2JchordResultOpEntry result = new R2JchordResultOpEntry();
				StringTokenizer st = new StringTokenizer(resultLine,R2SEP);
				result.readInfo(st);
				if(R2_DEBUG)
					System.out.println("*** R2_DEBUG[R2JchordResult]: <read> result (op) = " + result.toString());
				resultOpSet.add(result);
			}
			sizeStr = br.readLine();
			if(R2_DEBUG)
				System.out.println("*** R2_DEBUG[R2JchordResult]: <read> analysis result size (storage)= " + sizeStr);
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String resultLine = br.readLine();
				R2JchordResultStorageEntry result = new R2JchordResultStorageEntry();
				StringTokenizer st = new StringTokenizer(resultLine,R2SEP);
				result.readInfo(st);
				if(R2_DEBUG)
					System.out.println("*** R2_DEBUG[R2JchordResult]: <read> result (storage) = " + result.toString());
				resultStorageSet.add(result);
			}
			sizeStr = br.readLine();
			if(R2_DEBUG)
				System.out.println("*** R2_DEBUG[R2JchordResult]: <read> analysis result size (params)= " + sizeStr);
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String resultLine = br.readLine();
				R2JchordResultParamsEntry result = new R2JchordResultParamsEntry();
				StringTokenizer st = new StringTokenizer(resultLine,R2SEP);
				result.readInfo(st);
				if(R2_DEBUG)
					System.out.println("*** R2_DEBUG[R2JchordResult]: <read> result (params) = " + result.toString());
				resultParamSet.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(){
		System.out.println("*** R2_DEBUG[R2JchordResult]: all = ");
		for(R2JchordResultOpEntry entry : resultOpSet){
			System.out.println("*** R2_DEBUG[R2JchordResult]: " + entry.toString());
		}
	}
	
	public class R2JchordResultOpEntry{
		
		// classname + methodname + return type + bytecode offset + quad
		private String className;
		private String methName;
		private String retType;
		private int bytecodeOffset;
		private String treeTag;
		private String quad;
		
		public R2JchordResultOpEntry() {}
		
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public String getMethName() {
			return methName;
		}
		public void setMethName(String methName) {
			this.methName = methName;
		}
		public String getRetType() {
			return retType;
		}
		public void setRetType(String retType) {
			this.retType = retType;
		}
		public int getBytecodeOffset() {
			return bytecodeOffset;
		}
		public void setBytecodeOffset(int bytecodeOffset) {
			this.bytecodeOffset = bytecodeOffset;
		}
		public String getTreeTag() {
			return treeTag;
		}
		public void setTreeTag(String treeTag) {
			this.treeTag = treeTag;
		}
		
		public void readInfo(StringTokenizer st) {
			try {
				this.className = st.nextToken(); 
				this.methName = st.nextToken();
				this.retType = st.nextToken();
				this.bytecodeOffset = Integer.parseInt(st.nextToken());
				String quadStr = st.nextToken();
				this.treeTag = convertFromQuadToTag(quadStr);
				this.quad = quadStr;
			} catch (NoSuchElementException e) {
				System.out.println("Error! NoSuchElementException!");
				e.printStackTrace();
			}
		}
		
		private String convertFromQuadToTag(String quadStr) {
			StringTokenizer st = new StringTokenizer(quadStr, " ");
			st.nextToken();
			String tempStr = st.nextToken();
			
			if (tempStr.startsWith("ADD")) return "PLUS";
			if (tempStr.startsWith("SUB")) return "MINUS";
			if (tempStr.startsWith("MUL")) return "MUL";
			if (tempStr.startsWith("DIV")) return "DIV";
			if (tempStr.startsWith("REM")) return "MOD";
			if (tempStr.startsWith("SHL")) return "SL";
			if (tempStr.startsWith("SHR")) return "SR";
			if (tempStr.startsWith("AND")) return "BITAND";
			if (tempStr.startsWith("OR")) return "BITOR";
			if (tempStr.startsWith("XOR")) return "BITXOR";
			if (tempStr.startsWith("MOVE")) return "ASSIGN";
			if (tempStr.startsWith("ALOAD")) return "INDEXED";
			if (tempStr.startsWith("ASTORE")) return "ASSIGN";
			if (tempStr.startsWith("GETFIELD")) return "SELECT";
			if (tempStr.startsWith("PUTFIELD")) return "ASSIGN";
			if (tempStr.startsWith("GETSTATIC")) return "SELECT";
			if (tempStr.startsWith("PUTSTATIC")) return "ASSIGN";
			
			return quadStr;
		}

		public String toString(){
			return	className + " " + 
					methName + " " +
					retType + " " +
					bytecodeOffset + " " + 
					treeTag + " " +
					quad;
 		}
		
		public void print(){
			System.out.println("*** R2_DEBUG[R2JchordResultOpEntry]: <print> " + toString());
		}

		public boolean compareWithASTInfo(R2ASTNodeInfoEntry info) {
			if (info.getClassName().equalsIgnoreCase(className)) {	
				if(info.getMethName().equalsIgnoreCase(methName)) {
					if(info.getRetTypeName().equalsIgnoreCase(retType)) {
						if(info.getBytecodeOffset() == bytecodeOffset) {
							return true;
						} 
					}
				}
			} 
			return false;
		}
	}
	
	public class R2JchordResultStorageEntry extends R2JchordResultOpEntry{
		private String fieldName;
		private String desc;
		private String declClass;
		
		public R2JchordResultStorageEntry() {}
		
		public String toString(){
			return	super.toString() + " " + fieldName + " " + desc + " " + declClass;
 		}
		
		public void readInfo(StringTokenizer st) {
			super.readInfo(st);
			this.fieldName = st.nextToken();
			this.desc = st.nextToken();
			this.declClass = st.nextToken();
			if(!R2JchordResult.approxClasses.contains(declClass)) {
				R2JchordResult.approxClasses.add(declClass);
				if(R2_DEBUG) System.out.println("*** R2_DEBUG[R2JchordResultStorageEntry]: " + declClass + " is added to approxClass");
			}
		}
		
		public String getField(){
			return fieldName;
		}
		
		public String getDesc(){
			return desc;
		}
		
		public String getDeclClass(){
			return declClass;
		}
		
	}
	
	public class R2JchordResultParamsEntry{
		private String className;
		private String methName;
		private String retType;
		private boolean[] bitVector;
		private String bitVecStr;
		
		public R2JchordResultParamsEntry() {}
		
		public void readInfo(StringTokenizer st) {
			try {
				this.className = st.nextToken(); 
				this.methName = st.nextToken();
				this.retType = st.nextToken();
				bitVecStr = st.nextToken();
				generateBitVector();
			} catch (NoSuchElementException e) {
				System.out.println("Error! NoSuchElementException!");
				e.printStackTrace();
			}
		}
		
		private void generateBitVector(){
			char[] charArr = bitVecStr.toCharArray();
			bitVector = new boolean[charArr.length];
			for (int i=0; i<charArr.length; i++) {
				if (charArr[i] == '1'){
					bitVector[i] = true;
				} else {
					bitVector[i] = false;
				}
			}
		}
		
		public String toString(){
			return className + " " + methName + " " + retType + " " + bitVecStr;
 		}
		
		public boolean compare(String className, String methName, String retType) {
			if (this.className.equalsIgnoreCase(className)) {
				if(this.methName.equalsIgnoreCase(methName)) {
					if(this.retType.equalsIgnoreCase(retType)) {
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean isApproxParam(int index) {
			return bitVector[index];
		}
		
	}
}
