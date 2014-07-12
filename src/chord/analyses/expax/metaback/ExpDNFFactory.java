package chord.analyses.expax.metaback;

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
		System.out.println("*** EXPAX_EDNFF: str = " + str);
		if(indicator.equals("O")){
			int idx = Integer.parseInt(str.substring(1,str.length()));
			System.out.println("*** EXPAX_EDNFF: idx = " + idx);
			return new OVariable(idx);
		} 
		indicator = str.substring(0,2);
		System.out.println("*** EXPAX_EDNFF: str = " + str);
		String[] indexNums = str.substring(2).split(",");
		System.out.println("*** EXPAX_EDNFF: indexNums[0] = " + indexNums[0]);
		System.out.println("*** EXPAX_EDNFF: indexNums[0] = " + indexNums[1]);
		if (indicator.equals("ST")){
			return new STVariable(Integer.parseInt(indexNums[0]),Integer.parseInt(indexNums[1]));
		}
		else
			throw new RuntimeException("Unexpected varialbe: "+str);
	}

}
