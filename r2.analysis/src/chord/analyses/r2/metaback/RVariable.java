package chord.analyses.r2.metaback;

import chord.util.dnf.Variable;

public class RVariable implements Variable {
	public final static RVariable singleton = new RVariable();
	
	private RVariable(){}
	
	@Override
	public String encode() {
		return "R";
	}

	@Override
	public String toString() {
		return "RVariable []";
	}

}
