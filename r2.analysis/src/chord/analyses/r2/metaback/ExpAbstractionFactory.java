package chord.analyses.r2.metaback;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import joeq.Compiler.Quad.Quad;
import chord.project.analyses.metaback.Abstraction;
import chord.project.analyses.metaback.AbstractionFactory;
import chord.util.dnf.Clause;
import chord.util.dnf.DNF;
import chord.util.dnf.Domain;
import chord.util.dnf.Variable;
import chord.util.tuple.object.Pair;

public class ExpAbstractionFactory implements AbstractionFactory {
	public final static ExpAbstractionFactory singleton = new ExpAbstractionFactory();
	
	private ExpAbstractionFactory(){}
	
	@Override
	public Abstraction genAbsFromNC(DNF dnf) {
		Set<Integer> approxQ = new HashSet<Integer>(SharedData.allApproxStatements);
		Set<Pair<Integer,Integer>> approxS = new HashSet<Pair<Integer,Integer>>(SharedData.allApproxStorage);
		if(dnf.isTrue())
			return new ExpAbstraction(approxQ, approxS);
		if(dnf.isFalse())
			throw new RuntimeException();
		Clause c = dnf.getClauses().first();
		int removed = 0;
		for(Map.Entry<Variable, Domain> predicate : c.getLiterals().entrySet()) {
			if(predicate.getKey() instanceof OVariable){
				OVariable ov = (OVariable)predicate.getKey();
				if(BoolDomain.F.equals(predicate.getValue())) {
					// jspark: for debug
					Quad q = SharedData.indexQuadMap.get(ov.getIdx()); 
					if (q != null) {
						if (!SharedData.prevRemovedQuad.contains(q)) {
							SharedData.prevRemovedQuad.add(q);
							removed++;
						}
					}
					approxQ.remove(ov.getIdx());
				}
			} else if (predicate.getKey() instanceof STVariable) {
				STVariable stv = (STVariable)predicate.getKey();
				if(BoolDomain.F.equals(predicate.getValue())) {
					approxS.remove(stv.getPair());
				}
			}
			else
				throw new RuntimeException();
		}
		System.out.println("R2_EXPERIMENT # of bit-flips = " + removed);
		
		return new ExpAbstraction(approxQ, approxS);
	}

	@Override
	public Abstraction genAbsFromStr(String s) {
		ExpAbstraction ret = new ExpAbstraction();
		ret.decode(s);
		return ret;
	}

}
