package chord.analyses.r2.metaback;

import java.util.StringTokenizer;

import chord.util.dnf.DNFFactory;
import chord.util.dnf.Domain;
import chord.util.dnf.Variable;

public class ExpDNFFactory implements DNFFactory {
	public final static ExpDNFFactory singleton = new ExpDNFFactory();
	
	private ExpDNFFactory(){} 
	
	@Override
	public Domain genDomainFromStr(String str) {
		if(str.equals("T"))
			return BoolDomain.T;
		return BoolDomain.F;
	}

	@Override
	public Variable genVarFromStr(String str) {
		String indicator = str.substring(0, 1);
		if(indicator.equals("O")){
			int idx = Integer.parseInt(str.substring(1,str.length()));
			return new OVariable(idx);
		} 
		indicator = str.substring(0,2);
		String[] indexNums = str.substring(2).split(",");
		if (indicator.equals("ST")){
			return new STVariable(Integer.parseInt(indexNums[0]),Integer.parseInt(indexNums[1]));
		}
		else
			throw new RuntimeException("Unexpected varialbe: "+str);
	}

}
