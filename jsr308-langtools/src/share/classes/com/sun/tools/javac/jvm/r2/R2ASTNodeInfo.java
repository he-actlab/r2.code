package com.sun.tools.javac.jvm.r2;

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
public class R2ASTNodeInfo {
	
	private boolean R2_DEBUG = false;
	private Set<R2ASTNodeInfoEntry> infoSet;
	
	public R2ASTNodeInfo() {
		infoSet = new HashSet<R2ASTNodeInfoEntry>();
	}
	
	public R2ASTNodeInfo(boolean R2_DEBUG) {
		infoSet = new HashSet<R2ASTNodeInfoEntry>();
		this.R2_DEBUG = R2_DEBUG;
	}
	
	public void add(String className, 
					String methName, 
					String retTypeName,
					int bytecodeOffset,
					int sourcePos, 
					String treeTagStr,
					JCTree tree){
		
		R2ASTNodeInfoEntry info = new R2ASTNodeInfoEntry(className, 
															   methName, 
															   retTypeName,
															   bytecodeOffset,
															   sourcePos, 	
															   treeTagStr,
															   tree.toString());
		infoSet.add(info);
		if (R2_DEBUG) {
			System.out.println("*** R2_DEBUG[R2ASTNodeInfo]: <add> " + info.toString());
		}
	}
	
	public Set<R2ASTNodeInfoEntry> getInfoSet() {
		return infoSet; 
	}
	
	public void write(String r2InfoPath){
		if (r2InfoPath == "") 
			return;
		try{
			File file = new File(r2InfoPath);
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
		for (R2ASTNodeInfoEntry info : infoSet)
			info.writeString(writer);
	}
	
	public void read(String r2InfoPath){
		if (r2InfoPath == "")
			return;
		if (infoSet.size() != 0)
			throw new RuntimeException("Error! When read bytecode offset information, infoSet should be an empty set. "
											+ "If not, read is called when 1st-phase compilation");
		try {
			File file = new File(r2InfoPath);
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
				R2ASTNodeInfoEntry info = new R2ASTNodeInfoEntry();
				info.readInfo(infoLine);
				infoSet.add(info);
				if(R2_DEBUG)
					System.out.println("*** R2_DEBUG[R2ASTNodeInfo]: <read> info = " + info.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(){
		String all = "";
		for(R2ASTNodeInfoEntry entry : infoSet){
			all += "*** R2_DEBUG[R2ASTNodeInfo]: " + entry.toString() + "\n";
		}
		System.out.println("*** R2_DEBUG[R2ASTNodeInfo]: <print> all = ");
		System.out.println(all);
	}
	
	public class R2ASTNodeInfoEntry {
		private String className;
		private String methName;
		private String retTypeName;
		private int bytecodeOffset;
		private int sourcePos;
		private String treeTagStr;
		private String treeStr;
		
		private static final String R2SEP = "#";
		
		R2ASTNodeInfoEntry (){}
		R2ASTNodeInfoEntry (String className, 
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
				StringTokenizer st = new StringTokenizer(infoLine, R2SEP);
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
			System.out.println("*** R2_DEBUG: " + toString());
		}
		
		public void writeString(PrintWriter writer) {
			writer.write(className + R2SEP +
						 methName + R2SEP + 
						 retTypeName + R2SEP + 
						 bytecodeOffset + R2SEP + 
						 sourcePos + R2SEP + 
						 treeTagStr + R2SEP + 
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
			if(curClassName.equalsIgnoreCase(className)) { 
				if(curMethName.equalsIgnoreCase(methName)) {
					if(curRetTypeName.equalsIgnoreCase(retTypeName)) {
						if (tree.pos == sourcePos) {
							if(tagMatched(tree.getTag().toString(),treeTagStr)){
								return true;
							}
						} 
					} 
				} else if (methName.startsWith("<init>") && curMethName == " ") { // just pass the matching work to jchord comparing routine
					if ((tree.getTag().toString().equalsIgnoreCase("VARDEF") && treeTagStr.equalsIgnoreCase("ASSIGN"))) {
						return true;
					}
				} else if (curMethName.startsWith("<init>")) { // class instance initialization routine
					if ((tree.getTag().toString().equalsIgnoreCase(treeTagStr)))
						return true;
				}
			}
			return false;
		}
		
		private boolean tagMatched(String str1, String str2) {
			if(str1.equalsIgnoreCase(str2)) 
				return true;
			if(str2.equalsIgnoreCase("PREINC") && str1.equalsIgnoreCase("POSTINC")) // compiler sometimes changes codes from SOMETHING++ to ++SOMETHING
				return true;
			if(str2.equalsIgnoreCase("PREDEC") && str1.equalsIgnoreCase("POSTDEC"))
				return true;
			return false;
		}
		
	}
}
