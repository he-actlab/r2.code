package chord.analyses.r2.metaback;

import joeq.Compiler.Quad.Quad;
import chord.util.dnf.Variable;
import chord.util.tuple.object.Pair;

public class STVariable  implements Variable{
	private int hIdx;
	private int fIdx;
	
	public STVariable(int hIdx, int fIdx){
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
	
	public Pair<Integer,Integer> getPair(){
		return new Pair<Integer,Integer>(hIdx,fIdx);
	}
	
	@Override
	public String encode() {
		return "ST" + hIdx + "," + fIdx;
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
		STVariable other = (STVariable) obj;
		if (fIdx != other.fIdx)
			return false;
		if (hIdx != other.hIdx)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "STVariable [hIdx=" + hIdx + ", fIdx=" + fIdx + "]";
	}
}
