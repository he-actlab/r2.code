package chord.analyses.r2.metaback;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Comparator;
import java.util.Map;

import chord.util.dnf.Clause;
import chord.util.dnf.Domain;
import chord.util.dnf.Variable;

/**
 * A comparator which compares the size of clauses
 * @author xin
 *
 */
public class ApproxNumCMP implements Comparator<Clause>{
	
	private TObjectIntHashMap<Clause> cToI = new TObjectIntHashMap<Clause>();
	@Override
	public int compare(Clause o1, Clause o2) {
		if(o1.equals(o2))
			return 0;
		int anum1 = 0;
		int anum2 = 0;
		for(Map.Entry<Variable, Domain> predicate : o1.getLiterals().entrySet()){
			if(predicate.getKey() instanceof OVariable && BoolDomain.F.equals(predicate.getValue())){
				anum1++;
			}
			if(predicate.getKey() instanceof STVariable && BoolDomain.F.equals(predicate.getValue())){
				anum1++;
			}
		}
		for(Map.Entry<Variable, Domain> predicate : o2.getLiterals().entrySet()){
			if(predicate.getKey() instanceof OVariable && BoolDomain.F.equals(predicate.getValue())){
				anum2++;
			}
			if(predicate.getKey() instanceof STVariable && BoolDomain.F.equals(predicate.getValue())){
				anum2++;
			}
		}
		int result = anum1 - anum2;
		if(result==0){
			int i1 = cToI.get(o1);
			if(i1==0){
				i1 = cToI.size()+1;
				cToI.put(o1, i1);}
			int i2 = cToI.get(o2);
			if(i2==0){
				i2 = cToI.size()+1;
				cToI.put(o2, i2);	
			}
			return i1 - i2;
		}
		return result;
	}

}
