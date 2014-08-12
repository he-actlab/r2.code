package chord.analyses.r2.metaback;

import chord.util.dnf.Variable;

public class ErrVariable implements Variable {
	public final static ErrVariable EV = new ErrVariable();
	
	private ErrVariable(){}
	
	@Override
	public String encode() {
		return "E";
	}

	@Override
	public String toString() {
		return "ErrVariable []";
	}

}
