package chord.analyses.r2.metaback;

import joeq.Compiler.Quad.Inst;
import chord.project.analyses.metaback.Query;

/**
 * The query represents that the state of the program shouldn't be error before quad q.
 * @author xin
 *
 */
public class ExpQuery implements Query {
	Inst inst;
	
	public ExpQuery(){}
	
	public ExpQuery(Inst inst){
		this.inst = inst;
	}
	
	@Override
	public void decode(String s) {
		int idx = Integer.parseInt(s.trim());
		inst = SharedData.domP.get(idx);
	}

	@Override
	public String encode() {
		return ""+SharedData.domP.indexOf(inst);
	}

	@Override
	public String encodeForXML() {
		return "<query>\n"+inst.toVerboseStr()+"\n</query>";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inst == null) ? 0 : inst.hashCode());
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
		ExpQuery other = (ExpQuery) obj;
		if (inst == null) {
			if (other.inst != null)
				return false;
		} else if (!inst.equals(other.inst))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExpQuery [inst=" + inst + "]";
	}

}
