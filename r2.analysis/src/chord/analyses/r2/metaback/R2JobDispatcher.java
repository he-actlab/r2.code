package chord.analyses.r2.metaback;

import java.util.HashMap;
import java.util.Map;

import chord.analyses.parallelizer.ParallelAnalysis;
import chord.project.analyses.metaback.AbstractJobDispatcher;
import chord.project.analyses.metaback.AbstractionFactory;
import chord.project.analyses.metaback.Query;
import chord.project.analyses.metaback.QueryFactory;
import chord.util.dnf.DNF;

public class R2JobDispatcher extends AbstractJobDispatcher {
	private ExpQuery theQuery;
	public final static String MINOR_SEP = "MINOR";
	public final static String MAJOR_SEP = "MAJOR";
	
	public R2JobDispatcher(String xmlToHtmlTask, ParallelAnalysis masterAnalysis) {
		super(xmlToHtmlTask, masterAnalysis);
		theQuery = new ExpQuery(SharedData.exitMain);
	}

	@Override
	protected Map<Query, DNF> getInitialANCS() {
		Map<Query,DNF> initANCS = new HashMap<Query,DNF>();
		initANCS.put(theQuery, new DNF(new ApproxNumCMP(),true));
		return initANCS;
	}

	@Override
	public String getMinorSep() {
		return MINOR_SEP;
	}

	@Override
	public String getMajorSep() {
		return MAJOR_SEP;
	}

	@Override
	protected AbstractionFactory getAbsFactory() {
		return ExpAbstractionFactory.singleton;
	}

	@Override
	protected QueryFactory getQueryFactory() {
		return ExpQueryFactory.singleton;
	}


}
