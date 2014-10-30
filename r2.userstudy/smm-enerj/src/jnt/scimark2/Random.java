package jnt.scimark2;

/**
  Evaluation for EnerJ Framework
*/

import enerj.lang.*;

public class Random {


	/* ------------------------------------------------------------------------------
		 CLASS VARIABLES
		 ------------------------------------------------------------------------------ */

	int seed = 0;

	public @Approx int m[];
	public int i = 4;
	public int j = 16;

	public @Approx int mdig = 32;
	public @Approx int one = 1;
	public @Approx int m1 = (one << mdig-2) + ((one << mdig-2)-one);
	public @Approx int m2 = one << mdig/2;

	public @Approx double dm1 = 1.0 / (@Approx double) m1;

	public boolean haveRange = false;
	public @Approx double left  = 0.0;
	public @Approx double width = 1.0;


	/* ------------------------------------------------------------------------------
		 CONSTRUCTORS
		 ------------------------------------------------------------------------------ */

	public Random (int seed) {
		initialize(seed);
	}

	/* ------------------------------------------------------------------------------
		 PUBLIC METHODS
		 ------------------------------------------------------------------------------ */

	public final synchronized @Approx double nextDouble () {

		@Approx double ret;
		@Approx int k;

		k = m[i] - m[j]; 
		if (Endorsements.endorse(k) < 0) 
			k += m1; 
		m[j] = k; 

		if (i == 0) 
			i = 16;
		else 
			i--;

		if (j == 0) 
			j = 16 ;
		else 
			j--;

		if (haveRange) 
			ret = left +  dm1 * k * width; 
		else
			ret = dm1 * k;

		return ret;

	} 

	/*----------------------------------------------------------------------------
		PRIVATE METHODS
		------------------------------------------------------------------------ */

	private void initialize (int seed) {

		@Approx int jseed, k0, k1, j0, j1;
		int iloop;
	
		this.seed = seed;

		m = new int[17];

		jseed = Math.min(Math.abs(this.seed),Endorsements.endorse(m1));
		if (Endorsements.endorse(jseed) % 2 == 0) --jseed;
		k0 = 9069 % m2;
		k1 = 9069 / m2;
		j0 = jseed % m2;
		j1 = jseed / m2;
		for (iloop = 0; iloop < 17; ++iloop) 
		{
			jseed = j0 * k0;
			j1 = (jseed / m2 + j0 * k1 + j1 * k0) % (m2 / 2);
			j0 = jseed % m2;
			m[iloop] = j0 + m2 * j1;
		}
		i = 4;
		j = 16;

	}

}
