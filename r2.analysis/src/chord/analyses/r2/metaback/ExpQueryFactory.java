package chord.analyses.r2.metaback;

import chord.project.analyses.metaback.Query;
import chord.project.analyses.metaback.QueryFactory;
import chord.project.analyses.metaback.QueryResult;
import chord.util.dnf.DNF;

public class ExpQueryFactory implements QueryFactory {
	public final static String SEP = "#R#";
	public final static ExpQueryFactory singleton = new ExpQueryFactory();

	private ExpQueryFactory(){}
	
	@Override
	public QueryResult genResultFromStr(String s) {
		String segs[] = s.split(SEP);
		ExpQuery q = new ExpQuery();
		q.decode(segs[0]);
		int result = Integer.parseInt(segs[1]);
		DNF nc = null;
		if(segs.length > 2){
			nc = new DNF(new ApproxNumCMP(),ExpDNFFactory.singleton,segs[2]);
			assert(result == QueryResult.REFINE);
		}
		ExpQueryResult r = new ExpQueryResult(q,result,nc);
		return r;
	}

	@Override
	public Query getQueryFromStr(String s) {
		Query q = new ExpQuery();
		q.decode(s);
		return q;
	}

}

class ExpQueryResult implements QueryResult{	
	private ExpQuery q;
	private int result;
	private DNF nc;
	
	public ExpQueryResult(ExpQuery q, int result, DNF nc) {
		super();
		this.q = q;
		this.result = result;
		this.nc = nc;
	}

	@Override
	public int getResult() {
		return result;
	}

	@Override
	public Query getQuery() {
		return q;
	}

	@Override
	public DNF getNC() {
		return nc;
	}

	@Override
	public String encode() {
		return q.encode()+ExpQueryFactory.SEP+result+ExpQueryFactory.SEP+((nc==null)?"":nc.encode());
	}
	
}