package jnt.scimark2;

/**
 Evaluation for EnerJ framework
*/

/* Random.java based on Java Numerical Toolkit (JNT) Random.UniformSequence
	 class.  We do not use Java's own java.util.Random so that we can compare
	 results with equivalent C and Fortran coces.
 */

import enerj.lang.*;

public class Random {


	/* ------------------------------------------------------------------------------
		 CLASS VARIABLES
		 ------------------------------------------------------------------------------ */

	int seed = 0;

	public int m[];
	public int i = 4;
	public int j = 16;

	public int mdig = 32;
	public int one = 1;
	public int m1 = (one << mdig-2) + ((one << mdig-2)-one);
	public int m2 = one << mdig/2;

	public double dm1 = 1.0 / (double) m1;

	public boolean haveRange = false;
	public double left  = 0.0;
	public double width = 1.0;


	/* ------------------------------------------------------------------------------
		 CONSTRUCTORS
		 ------------------------------------------------------------------------------ */

	public Random () {
		initialize( 123456 );
	}

	public Random (double left) {
		initialize( 123456 );
		this.left = left;
		haveRange = true;
	}

	public Random (int seed) {
		initialize(seed);
	}

	public Random (int seed, double left) {
		initialize(seed);
		this.left = left;
		haveRange = true;
	}

	/* ------------------------------------------------------------------------------
		 PUBLIC METHODS
		 ------------------------------------------------------------------------------ */

	public final synchronized double nextDouble () {

		int k;

		k = m[i] - m[j]; 
		if (k < 0) 
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
			return left +  dm1 * k * width; 
		else
			return dm1 * k;

	} 

	/*----------------------------------------------------------------------------
		PRIVATE METHODS
		------------------------------------------------------------------------ */

	private void initialize (int seed) {

		int jseed, k0, k1, j0, j1, iloop;

		this.seed = seed;

		m = new int[17];

		jseed = Math.min(Math.abs(this.seed),m1);
		if (jseed % 2 == 0) --jseed;
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
