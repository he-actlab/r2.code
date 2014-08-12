package enerj.jchord.result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.tools.javac.jvm.expax.ExpaxASTNodeInfo.ExpaxASTNodeInfoEntry;

public class ExpaxJchordResult {
	
	private static final boolean EXPAX_JCHORD = false;
	private static final String EXPAXSEP = "#";
	private Set<ExpaxJchordResultOpEntry> resultOpSet;
	private Set<ExpaxJchordResultStorageEntry> resultStorageSet;
	
	public ExpaxJchordResult (){
		 resultOpSet = new HashSet<ExpaxJchordResultOpEntry>();
		 resultStorageSet = new HashSet<ExpaxJchordResultStorageEntry>();
	}
	
	public Set<ExpaxJchordResultOpEntry> getResultOpSet(){
		return resultOpSet;
	}
	
	public Set<ExpaxJchordResultStorageEntry> getResultStorageSet(){
		return resultStorageSet;
	}
	
	public void read(String resultFilePath){
		if (resultFilePath == "")
			throw new RuntimeException("Error! analysis result filename has not been specified!");
		if (resultOpSet.size() != 0)
			throw new RuntimeException("Error! When read analysis result information, resultSet should be an empty set. ");
		if (resultStorageSet.size() != 0)
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
			print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void read(BufferedReader br) {
		try {
			String sizeStr = br.readLine();
			if(EXPAX_JCHORD)
				System.out.println("*** EXPAX_JCHORD: analysis result size (op)= " + sizeStr);
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String resultLine = br.readLine();
				ExpaxJchordResultOpEntry result = new ExpaxJchordResultOpEntry();
				StringTokenizer st = new StringTokenizer(resultLine,EXPAXSEP);
				result.readInfo(st);
				if(EXPAX_JCHORD)
					System.out.println("*** EXPAX_JCHORD: result (op) = " + result.toString());
				resultOpSet.add(result);
			}
			sizeStr = br.readLine();
			if(EXPAX_JCHORD)
				System.out.println("*** EXPAX_JCHORD: analysis result size (storage)= " + sizeStr);
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String resultLine = br.readLine();
				ExpaxJchordResultStorageEntry result = new ExpaxJchordResultStorageEntry();
				StringTokenizer st = new StringTokenizer(resultLine,EXPAXSEP);
				result.readInfo(st);
				if(EXPAX_JCHORD)
					System.out.println("*** EXPAX_JCHORD_READER: result (storage) = " + result.toString());
				resultStorageSet.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(){
		System.out.println("*** EXPAX_JC: all = \n");
		for(ExpaxJchordResultOpEntry entry : resultOpSet){
			System.out.println(entry.toString());
		}
	}
	
	public class ExpaxJchordResultOpEntry{
		
		// classname + methodname + return type + bytecode offset + quad
		private String className;
		private String methName;
		private String retType;
		private int bytecodeOffset;
		private String treeTag;
		
		public ExpaxJchordResultOpEntry() {}
		
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
					treeTag;
 		}
		
		public void print(){
			System.out.println("*** EXPAX_JCHORD: " + toString());
		}

		public boolean compareWithASTInfo(ExpaxASTNodeInfoEntry info) {
			if(EXPAX_JCHORD)
				info.print();
			if (info.getClassName().equalsIgnoreCase(className)) {	
				if(info.getMethName().equalsIgnoreCase(methName)) {
					if(info.getRetTypeName().equalsIgnoreCase(retType)) {
						if(info.getBytecodeOffset() == bytecodeOffset) {
							if(EXPAX_JCHORD){
								System.out.println("*** EXPAX_JCHORD: tree matched = " + toString());
								System.out.println("*** EXPAX_JCHORD: jresult's tree tag = " + treeTag);
								System.out.println("*** EXPAX_JCHORD: info's tree tag = " + info.getTreeTagStr());
								System.out.println("*** EXPAX_JCHORD: expax approximate success!");
							}
							return true;
						} else {
							if(EXPAX_JCHORD)
								System.out.println("*** EXPAX_JCHORD: bytecode offset unmatched = (AST)[" + info.getBytecodeOffset() + "] (jchord)[" + bytecodeOffset + "]\n" + toString());
						}
					} else
						if(EXPAX_JCHORD)
							System.out.println("*** EXPAX_JCHORD: ret type matched = (AST)[" + info.getRetTypeName() + "] vs. (jchord)[" + retType + "]\n" + toString());
				} else
					if(EXPAX_JCHORD)
						System.out.println("*** EXPAX_JCHORD: method unmatched = (AST)[" + info.getMethName() + "] vs. (jchord)[" + methName + "]\n" + toString());
			} else 
				if(EXPAX_JCHORD)
					System.out.println("*** EXPAX_JCHORD: class unmatched = (AST)[" + info.getClassName() + "] vs. (jchord)[" + className + "]\n" + toString());
			return false;
		}
	}
	
	public class ExpaxJchordResultStorageEntry extends ExpaxJchordResultOpEntry{
		private String fieldName;
		private String desc;
		private String declClass;
		
		public ExpaxJchordResultStorageEntry() {}
		
		public String toString(){
			return	super.toString() + " " + fieldName + " " + desc + " " + declClass;
 		}
		
		public void readInfo(StringTokenizer st) {
			super.readInfo(st);
			this.fieldName = st.nextToken();
			this.desc = st.nextToken();
			this.declClass = st.nextToken();
			
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
}
