package chord.analyses.r2.metaback;

import java.util.Set;

import chord.util.tuple.object.Pair;

public class AbstractState {
	//Tainted set of static fields
	Set<Integer> taintedGlobals;
	// Tainted set of variables: V
	Set<Integer> taintedVars;
	// Tainted allocation site and fields: H \times F
	Set<Pair<Integer,Integer>> taintedFields;
	
	boolean taintedRet;
	boolean isErr;
	
	public AbstractState(Set<Integer> taintedGlobals,
						 Set<Integer> taintedVars, 
						 Set<Pair<Integer,Integer>> taintedFields,  
						 boolean taintedRet, 
						 boolean isErr){
		this.taintedGlobals = taintedGlobals;
		this.taintedVars = taintedVars;
		this.taintedFields = taintedFields;
		this.taintedRet = taintedRet;
		this.isErr = isErr;
	}
	
	public boolean contains(AbstractState other){
		if(this.isErr)
			return true;
		if(other.isErr)
			return false;
		return taintedGlobals.containsAll(other.taintedGlobals) &&
			   taintedVars.containsAll(other.taintedVars) && 
			   taintedFields.containsAll(other.taintedFields) &&
			   (taintedRet || !other.taintedRet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isErr ? 1231 : 1237);
		result = prime * result + ((taintedFields == null) ? 0 : taintedFields.hashCode());
		result = prime * result + (taintedRet ? 1231 : 1237);
		result = prime * result + ((taintedVars == null) ? 0 : taintedVars.hashCode());
		result = prime * result + ((taintedGlobals == null) ? 0 : taintedGlobals.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractState other = (AbstractState) obj;
		if (isErr != other.isErr)
			return false;
		if( isErr && other.isErr)
			return true;
		if (taintedFields == null) {
			if (other.taintedFields != null)
				return false;
		} else if (!taintedFields.equals(other.taintedFields))
			return false;
		if (taintedRet != other.taintedRet)
			return false;
		if (taintedVars == null) {
			if (other.taintedVars != null)
				return false;
		} else if (!taintedVars.equals(other.taintedVars))
			return false;
		if (taintedGlobals == null) {
			if (other.taintedGlobals != null)
				return false;
		} else if (!taintedGlobals.equals(other.taintedGlobals))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractState [taintedGlobals=" + taintedGlobals + ", taintedVars=" + taintedVars + ", taintedFields=" + taintedFields 
				+ ", taintedRet=" + taintedRet + ", isErr=" + isErr + "]";
	}

	
}
