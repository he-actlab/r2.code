package chord.analyses.r2.metaback;

import java.util.Set;

import chord.util.ArraySet;
import chord.util.dnf.Domain;

public class BoolDomain implements Domain {
	private boolean value;
	public final static BoolDomain T = new BoolDomain(true);
	public final static BoolDomain F = new BoolDomain(false);
	private final static Set<Domain> space;
	static{
		space = new ArraySet<Domain>();
		space.add(T);
		space.add(F);
	}
	
	private BoolDomain(boolean b){
		this.value = b;
	}
	
	public BoolDomain getBoolDomain(boolean v){
		if(v)
			return T;
		return F;
	}
	
	@Override
	public int size() {
		return 2;
	}

	@Override
	public boolean equals(Domain other) {
		return this == other;
	}

	@Override
	public String encode() {
		if (this == T)
			return "T";
		else
			return "F";
	}

	@Override
	public Set<Domain> space() {
		return space;
	}
	
	public String toString(){
		return encode();
	}
	
}
