package com.sun.tools.javac.jvm.expax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.tools.javac.tree.JCTree;

/**
 * Data structure to store a mapping from an AST tree node to a bytecode offset
 * Since source position is not an unique value for each tree node, tag info of each tree node has been added.
 * Tree string exists just for debugging reason.
 *  
 * @author jspark
 *
 */
public class ExpaxASTNodeInfo {
	
	private boolean EXPAX_AST = false;
	private Set<ExpaxASTNodeInfoEntry> infoSet;
	
	public ExpaxASTNodeInfo() {
		infoSet = new HashSet<ExpaxASTNodeInfoEntry>();
	}
	
	public ExpaxASTNodeInfo(boolean EXPAX_AST) {
		infoSet = new HashSet<ExpaxASTNodeInfoEntry>();
		this.EXPAX_AST = EXPAX_AST;
	}
	
	public void add(String className, 
					String methName, 
					String retTypeName,
					int bytecodeOffset,
					int sourcePos, 
					String treeTagStr,
					JCTree tree){
		
		ExpaxASTNodeInfoEntry info = new ExpaxASTNodeInfoEntry(className, 
															   methName, 
															   retTypeName,
															   bytecodeOffset,
															   sourcePos, 	
															   treeTagStr,
															   tree.toString());
		infoSet.add(info);
		if (EXPAX_AST) {
			System.out.println("*** EXPAX_AST: add to expaxInfo = " + info.toString());
			System.out.println("*** EXPAX_AST: tree = " + tree.toString());
		}
	}
	
	public Set<ExpaxASTNodeInfoEntry> getInfoSet() {
		return infoSet; 
	}
	
	public void write(String expaxInfoPath){
		if (expaxInfoPath == "") 
			return;
		try{
			File file = new File(expaxInfoPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			PrintWriter writer = new PrintWriter(file);
			write(writer);
			writer.close();
		} catch(Exception e) { 
			e.printStackTrace();
		}
	}
	
	public void write(PrintWriter writer){
		writer.write(infoSet.size() + "\n");
		for (ExpaxASTNodeInfoEntry info : infoSet)
			info.writeString(writer);
	}
	
	public void read(String expaxInfoPath){
		if (expaxInfoPath == "")
			return;
		if (infoSet.size() != 0)
			throw new RuntimeException("Error! When read bytecode offset information, infoSet should be an empty set. "
											+ "If not, read is called when 1st-phase compilation");
		try {
			File file = new File(expaxInfoPath);
			if (!file.exists())
				throw new RuntimeException("Error! bytecode offset information file is missing!");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			read(br);
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void read(BufferedReader br){
		try {
			String sizeStr = br.readLine();
			for (int i=0; i<Integer.parseInt(sizeStr); i++){
				String infoLine = br.readLine();
				ExpaxASTNodeInfoEntry info = new ExpaxASTNodeInfoEntry();
				info.readInfo(infoLine);
				infoSet.add(info);
				if(EXPAX_AST)
					System.out.println("*** EXPAX_ASTNODE_INFO: info = " + info.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(){
		String all = "";
		for(ExpaxASTNodeInfoEntry entry : infoSet){
			all += "*** EXPAX_AST: " + entry.toString() + "\n";
		}
		System.out.println("*** EXPAX_AST: all = ");
		System.out.println(all);
	}
	
	public class ExpaxASTNodeInfoEntry {
		private String className;
		private String methName;
		private String retTypeName;
		private int bytecodeOffset;
		private int sourcePos;
		private String treeTagStr;
		private String treeStr;
		
		private static final String EXPAXSEP = "#";
		
		ExpaxASTNodeInfoEntry (){}
		ExpaxASTNodeInfoEntry (String className, 
				   			   String methName, 
				   			   String retTypeName,
				   			   int bytecodeOffset, 
				   			   int sourcePos, 
				   			   String treeTagStr,
				   			   String treeStr){
			
			this.className = className;
			this.methName = methName;
			this.retTypeName = retTypeName;
			this.bytecodeOffset = bytecodeOffset;
			this.sourcePos = sourcePos;
			this.treeTagStr = treeTagStr;
			this.treeStr = treeStr;
		}
		
		public void readInfo(String infoLine){
			try {
				StringTokenizer st = new StringTokenizer(infoLine, EXPAXSEP);
				this.className = st.nextToken();
				this.methName = st.nextToken();
				this.retTypeName = st.nextToken();
				this.bytecodeOffset = Integer.parseInt(st.nextToken());
				this.sourcePos = Integer.parseInt(st.nextToken());
				this.treeTagStr = st.nextToken();
				this.treeStr = st.nextToken();
			} catch (NoSuchElementException e) {
				System.out.println("NoSuchElementException infoLine" + infoLine);
				e.printStackTrace();
			}
		}
		
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
		public String getRetTypeName() {
			return retTypeName;
		}
		public void setRetTypeName(String retTypeName) {
			this.retTypeName = retTypeName;
		}
		public int getBytecodeOffset() {
			return bytecodeOffset;
		}
		public void setBytecodeOffset(int bytecodeOffset) {
			this.bytecodeOffset = bytecodeOffset;
		}
		public int getSourcePos() {
			return sourcePos;
		}
		public void setSourcePos(int sourcePos) {
			this.sourcePos = sourcePos;
		}
		public String getTreeTagStr() {
			return treeTagStr;
		}
		public void setTreeTagStr(String treeTagStr) {
			this.treeTagStr = treeTagStr;
		}
		
		public void print(){
			System.out.println("*** EXPAX_AST: " + toString());
		}
		
		public void writeString(PrintWriter writer) {
			writer.write(className + EXPAXSEP +
						 methName + EXPAXSEP + 
						 retTypeName + EXPAXSEP + 
						 bytecodeOffset + EXPAXSEP + 
						 sourcePos + EXPAXSEP + 
						 treeTagStr + EXPAXSEP + 
						 treeStr + 
						 "\n");
		}
		
		public String toString(){
			return 
					className + " " + 
					methName + " " + 
					retTypeName + " " +
					bytecodeOffset + " " + 
					sourcePos + " " +
					treeTagStr;
		}
		
		public boolean compareWithTree(JCTree tree, 
									   String curClassName,
									   String curMethName, 
									   String curRetTypeName) {
			if(EXPAX_AST) {
				System.out.println("*** EXPAX_AST#0: AST info = " + treeStr);
				System.out.println("*** EXPAX_AST#0: current tree = " + tree.toString());
			}
			if(curClassName.equalsIgnoreCase(className)) { 
				if(curMethName.equalsIgnoreCase(methName)) {
					if(curRetTypeName.equalsIgnoreCase(retTypeName)) {
						if (tree.pos == sourcePos) {
							if(EXPAX_AST)
								System.out.println("*** EXPAX_AST#1: source position matched!");
							if(tagMatched(tree.getTag().toString(),treeTagStr)){
								if(EXPAX_AST)
									System.out.println("*** EXPAX_AST#1: tree matched = " + toString());
								return true;
							} else {
								if(EXPAX_AST)
									System.out.println("*** EXPAX_AST#1: tag unmatched = (1st)[" + treeTagStr + "] vs. (2nd)[" + tree.getTag().toString() + "]");
							}
						} else {
							if(EXPAX_AST)
								System.out.println("*** EXPAX_AST#1: source position unmatched = (1st)[" + sourcePos + "] vs. (2nd)[" + tree.pos + "]");
						}
					} else {
						if(EXPAX_AST)
							System.out.println("*** EXPAX_AST#1: ret type unmatched = (1st)[" + retTypeName + "] vs. (2nd)[" + curRetTypeName + "]");						
					}
				} else if (methName.startsWith("<init>") && curMethName == " ") {
					if ((tree.getTag().toString().equalsIgnoreCase("VARDEF") && treeTagStr.equalsIgnoreCase("ASSIGN"))) {
						if(tree.getTag().toString().equalsIgnoreCase(treeTagStr)) 
							if(EXPAX_AST) 
								System.out.println("*** EXPAX_AST#2-2: tree.getTag().toString().equalsIgnoreCase(treeTagStr) ");
						if(tree.getTag().toString().equalsIgnoreCase("VARDEF") && treeTagStr.equalsIgnoreCase("ASSIGN"))
							if(EXPAX_AST) 
								System.out.println("*** EXPAX_AST#2-2: (tree.getTag().toString().equalsIgnoreCase(VARDEF) && treeTagStr.equalsIgnoreCase(ASSIGN))");
						if(EXPAX_AST)
							System.out.println("*** EXPAX_AST#2-2: tree matched = " + toString());
						return true;
					}
				} else {
					if(EXPAX_AST)
						System.out.println("*** EXPAX_AST#3: methName unmatched = (1st)[" + methName + "] vs. (2nd)[" + curMethName + "]");
				}
			}
			return false;
		}
		
		private boolean tagMatched(String str1, String str2) {
			if(str1.equalsIgnoreCase(str2)) 
				return true;
			if(str2.equalsIgnoreCase("PREINC") && str1.equalsIgnoreCase("POSTINC"))
				return true;
			if(str2.equalsIgnoreCase("PREDEC") && str1.equalsIgnoreCase("POSTDEC"))
				return true;
			return false;
		}
		private boolean isPostFix(String str1, String str2){
			char[] arr1 = str1.toCharArray();
			char[] arr2 = str2.toCharArray();
			arr1 = reverse(arr1);
			arr2 = reverse(arr2);
			for (int i=0; i < arr2.length; i++){
				if (arr1[i] != arr2[i]) 
					return false;
			}
			return true;
		}
		
		private char[] reverse(char[] arr){
			char[] newarr = new char[arr.length];
			int j = 0;
			for (int i = arr.length - 1; i >= 0; i--) {
				newarr[j++] = arr[i];
			}
			return newarr;
		}
	}
}
