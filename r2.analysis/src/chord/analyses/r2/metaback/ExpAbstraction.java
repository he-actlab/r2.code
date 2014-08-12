package chord.analyses.r2.metaback;

import java.util.HashSet;
import java.util.Set;

import joeq.Class.jq_Field;
import joeq.Compiler.Quad.Quad;
import chord.project.analyses.metaback.Abstraction;
import chord.util.tuple.object.Pair;

public class ExpAbstraction implements Abstraction {
	/**
	 * The statements that are approximate.
	 */
	Set<Integer> approxStatements;
	Set<Pair<Integer,Integer>> approxStorage; 
	public final static String RESULT_SEP = "@";
	public final static String QUAD_SEP = " ";
	public final static String STORAGE_SEP = " ";
	public final static String PAIR_SEP= ",";
	
	public ExpAbstraction(Set<Integer> approxes, Set<Pair<Integer,Integer>> approxStorage){
		this.approxStatements = approxes;
		this.approxStorage = approxStorage;
	}
	
	public ExpAbstraction() {
	}

	@Override
	public int compareTo(Abstraction o) {
		ExpAbstraction other = (ExpAbstraction)o;
		return this.preciseSize() - other.preciseSize();
	}

	public int preciseSize(){
		return SharedData.domP.size() + SharedData.allApproxStorage.size() - this.approxSize();
	}
	
	public int approxSize(){
		return approxStatements.size() + approxStorage.size();
	}
	
	@Override
	public String encode() {
		StringBuffer sb =  new StringBuffer();
		for(Integer i : approxStatements)
			sb.append(i+QUAD_SEP);
		sb.append(RESULT_SEP);
		for (Pair<Integer,Integer> p : approxStorage)
			sb.append(p.val0 + PAIR_SEP + p.val1 + STORAGE_SEP);
		return sb.toString().trim();
	}

	@Override
	public void decode(String s) {
		approxStatements = new HashSet<Integer>();
		approxStorage = new HashSet<Pair<Integer,Integer>>();
		if(s.equals(""))
			return;
		String results[] = s.split(RESULT_SEP);
		if(results.length == 0)
			return;
		String approxStatementsStr = results[0];
		String indicies[] = approxStatementsStr.split(QUAD_SEP);
		for(String ind : indicies)
			approxStatements.add(Integer.parseInt(ind));
		String approxStorageStr = null;
		if (results.length != 1){
			approxStorageStr = results[1];
			String pairs[] = approxStorageStr.split(STORAGE_SEP);
			for(String p : pairs) {
				String pair[] = p.split(PAIR_SEP);
				approxStorage.add(new Pair<Integer,Integer>(Integer.parseInt(pair[0]),Integer.parseInt(pair[1])));
			}
		}
	}
	
	@Override
	public String encodeForXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<abs>\n");
		for(Integer si : approxStatements){
			Quad q = (Quad)SharedData.domP.get(si);
			sb.append("<quad>\n");
			sb.append(q.toString());
			sb.append("</quad>\n");
			sb.append("<bytecode>\n");
			sb.append(q.toByteLocStr());
			sb.append("</bytecode>\n");
			sb.append("<java>\n");
			sb.append(q.toJavaLocStr());
			sb.append("</java>\n");
		}
		sb.append("</abs>\n");		
		sb.append("<abstr>\n");
		for (Pair<Integer,Integer> p : approxStorage){
			Quad h = (Quad)SharedData.domH.get(p.val0); 
			sb.append("<allocsite>\n");
			sb.append(h.toString());
			sb.append("</allocsite>\n");
			sb.append("<field>\n");
			sb.append(p.val1);
			sb.append("</field>\n");
		}
		sb.append("</abstr>\n");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((approxStatements == null) ? 0 : approxStatements.hashCode());
		result = prime * result + ((approxStorage == null) ? 0 : approxStorage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpAbstraction other = (ExpAbstraction) obj;
		if (approxStatements == null) {
			if (other.approxStatements != null)
				return false;
		} else if (!approxStatements.equals(other.approxStatements))
			return false;
		if (approxStorage == null) {
			if(other.approxStorage != null)
				return false;
		} else if (!approxStorage.equals(other.approxStorage))
			return false;
		return true;
	}
	
	

}
