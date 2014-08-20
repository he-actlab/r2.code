package chord.analyses.r2.metaback;

import chord.util.dnf.Variable;
/**
 * Abstract return variable for relax/restrict functions
 * @author jspark
 *
 */
public class R2Variable implements Variable {
	public final static R2Variable singleton = new R2Variable();
	
	private R2Variable(){}
	
	@Override
	public String encode() {
		return "RelaxRestrict";
	}

	@Override
	public String toString() {
		return "R2Variable []";
	}

}
