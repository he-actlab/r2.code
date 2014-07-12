package chord.analyses.expax.metaback;

import chord.util.dnf.Variable;
/**
 * Abstract return variable for accept/precise functions
 * @author jspark
 *
 */
public class APVariable implements Variable {
	public final static APVariable singleton = new APVariable();
	
	private APVariable(){}
	
	@Override
	public String encode() {
		return "AcceptPrecise";
	}

	@Override
	public String toString() {
		return "APVariable []";
	}

}
