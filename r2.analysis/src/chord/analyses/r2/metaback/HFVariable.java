package chord.analyses.r2.metaback;

import joeq.Compiler.Quad.Quad;
import chord.util.dnf.Variable;

public class HFVariable implements Variable {
	private int hIdx;
	private int fIdx;
	
	public HFVariable(int hIdx, int fIdx){
		this.hIdx = hIdx;
		this.fIdx = fIdx;
	}
	
	public Quad getH(){
		return (Quad)SharedData.domH.get(hIdx);
	}
	
	public int getHIdx(){
		return hIdx;
	}
	
	public int getFIdx(){
		return fIdx;
	}
	
	@Override
	public String encode() {
		return "H" + hIdx+","+fIdx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fIdx;
		result = prime * result + hIdx;
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
		HFVariable other = (HFVariable) obj;
		if (fIdx != other.fIdx)
			return false;
		if (hIdx != other.hIdx)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HFVariable [hIdx=" + hIdx + ", fIdx=" + fIdx + "]";
	}
	
	

}
