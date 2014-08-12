package chord.analyses.r2.metaback;

import joeq.Compiler.Quad.Quad;
import chord.util.dnf.Variable;

public class OVariable implements Variable {
	private int idx;
	
	public OVariable(int idx){
		this.idx = idx;
	}
	
	public Quad getQuad(){
		return (Quad)SharedData.domP.get(idx);
	}
	
	public int getIdx(){
		return idx;
	}

	@Override
	public String encode() {
		return "O"+idx;
	}

	@Override
	public String toString() {
		return "OVariable [idx=" + idx + "]";
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
		OVariable other = (OVariable) obj;
		if (idx != other.idx)
			return false;
		return true;
	}
	
}
