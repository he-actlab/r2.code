package chord.analyses.r2.metaback;

import chord.util.dnf.Variable;
import joeq.Class.jq_Field;

public class GVariable implements Variable{
	private int idx;
	
	public GVariable(int idx){
		this.idx = idx;
	}

	public jq_Field getField(){
		return SharedData.domF.get(idx);
	}
	
	public int getIdx(){
		return idx;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idx;
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
		GVariable other = (GVariable) obj;
		if (idx != other.idx)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GVariable [idx=" + idx + "]";
	}

	@Override
	public String encode() {
		return "G"+Integer.toString(idx);
	}
	
	
}
