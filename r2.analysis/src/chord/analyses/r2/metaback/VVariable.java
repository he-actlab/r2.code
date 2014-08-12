package chord.analyses.r2.metaback;

import joeq.Compiler.Quad.RegisterFactory.Register;
import chord.util.dnf.Variable;

public class VVariable implements Variable {
	private int idx;// the index in domV
	private int context;

	public VVariable(int i) {
		this.idx = i;
		context = 0;
	}

	public VVariable(Register r){
		this.idx = SharedData.domU.indexOf(r);
		context = 0;
	}
	
	public VVariable(int idx, int context){
		this.idx = idx;
		this.context = context;
	}
	
	public int getIdx() {
		return idx;
	}

	public Register getRegister(){
		return (Register)SharedData.domU.get(idx);
	}
	
	@Override
	public String encode() {
		return "V"+Integer.toString(idx);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + context;
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
		VVariable other = (VVariable) obj;
		if (context != other.context)
			return false;
		if (idx != other.idx)
			return false;
		return true;
	}

	public VVariable getIncreased(){
		VVariable ret = new VVariable(idx);
		ret.context = this.context+1;
		return ret;
	}
	
	public VVariable getDecreased(){
		VVariable ret = new VVariable(idx);
		ret.context = this.context - 1;
		return ret;
	}
	
	public int getContext(){
		return context;
	}
	
	@Override
	public String toString() {
		return "taintedV "+SharedData.domU.get(idx).toString()+"("+idx+","+context+")";
	}
	

}
