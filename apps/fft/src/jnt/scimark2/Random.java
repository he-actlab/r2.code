package jnt.scimark2;


/* Random.java based on Java Numerical Toolkit (JNT) Random.UniformSequence
	 class.  We do not use Java's own java.util.Random so that we can compare
	 results with equivalent C and Fortran coces.
 */

public class Random {


	/* ------------------------------------------------------------------------------
		 CLASS VARIABLES
		 ------------------------------------------------------------------------------ */

	int seed = 0;	// op: 2: PUTFIELD_I R0, .seed, IConst: 0

	private int m[];
	private int i = 4;
	private int j = 16;

	private final int mdig = 32;	// op: 5: PUTFIELD_I R0, .mdig, IConst: 32
	private final int one = 1;	// op: 6: PUTFIELD_I R0, .one, IConst: 1
	private final int m1 = (one << mdig-2) + ((one << mdig-2)-one);	// op: 7: PUTFIELD_I R0, .m1, IConst: 2147483647
	private final int m2 = one << mdig/2;	// op: 8: PUTFIELD_I R0, .m2, IConst: 65536

	/* For mdig = 32 : m1 =          2147483647, m2 =      65536
		 For mdig = 64 : m1 = 9223372036854775807, m2 = 4294967296 
	 */

	private double dm1 = 1.0 / (double) m1;	// op: 9: PUTFIELD_D R0, .dm1, DConst: 4.656612875245797E-10

	private boolean haveRange = false;
	private double left  = 0.0;	// op: 11: PUTFIELD_D R0, .left, DConst: 0.0
	private double right = 1.0;	// op: 12: PUTFIELD_D R0, .right, DConst: 1.0
	private double width = 1.0;	// op: 13: PUTFIELD_D R0, .width, DConst: 1.0


	/* ------------------------------------------------------------------------------
		 CONSTRUCTORS
		 ------------------------------------------------------------------------------ */

	/**
		Initializes a sequence of uniformly distributed quasi random numbers with a
		seed based on the system clock.
	 */
	public Random () {
		initialize( 123456 ); // EnerJ determinism
	}

	/**
		Initializes a sequence of uniformly distributed quasi random numbers on a
		given half-open interval [left,right) with a seed based on the system
		clock.

		@param <B>left</B> (double)<BR>

		The left endpoint of the half-open interval [left,right).

		@param <B>right</B> (double)<BR>

		The right endpoint of the half-open interval [left,right).
	 */
	public Random ( double left, double right) {
		initialize( 123456 );
		this.left = left;
		this.right = right;
		width = right - left;
		haveRange = true;
	}

	/**
		Initializes a sequence of uniformly distributed quasi random numbers with a
		given seed.

		@param <B>seed</B> (int)<BR>

		The seed of the random number generator.  Two sequences with the same
		seed will be identical.
	 */
	public Random (int seed) {
		initialize( seed);
	}

	/**
		Initializes a sequence of uniformly distributed quasi random numbers
		with a given seed on a given half-open interval [left,right).

		@param <B>seed</B> (int)<BR>

		The seed of the random number generator.  Two sequences with the same
		seed will be identical.

		@param <B>left</B> (double)<BR>

		The left endpoint of the half-open interval [left,right).

		@param <B>right</B> (double)<BR>

		The right endpoint of the half-open interval [left,right).
	 */
	public Random (int seed, double left, double right) {
		initialize( seed);
		this.left = left;
		this.right = right;
		width = right - left;
		haveRange = true;
	}

	/* ------------------------------------------------------------------------------
		 PUBLIC METHODS
		 ------------------------------------------------------------------------------ */

	/**
		Returns the next random number in the sequence.
	 */
	// EnerJ TODO
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
			return  left +  dm1 * k * width; 	// op: 41: GETFIELD_D T34, R0, .left	// op: 42: GETFIELD_D T35, R0, .dm1	// op: 43: MOVE_I R36, R24	// op: 48: ADD_D T41, T34, T40	// op: 44: INT_2DOUBLE T37, R36	// op: 45: MUL_D T38, T35, T37	// op: 46: GETFIELD_D T39, R0, .width	// op: 47: MUL_D T40, T38, T39
		else
			return dm1 * k; 	// op: 36: GETFIELD_D T42, R0, .dm1	// op: 37: MOVE_I R43, R24	// op: 38: INT_2DOUBLE T44, R43	// op: 39: MUL_D T45, T42, T44

	} 

	/*----------------------------------------------------------------------------
		PRIVATE METHODS
		------------------------------------------------------------------------ */

	private void initialize (int seed) {

		int jseed, k0, k1, j0, j1, iloop;

		this.seed = seed;	// op: 2: PUTFIELD_I R0, .seed, R14

		m = new int[17];

		jseed = Math.min(Math.abs(seed),m1);
		if (jseed % 2 == 0) --jseed;
		k0 = 9069 % m2;	// op: 14: MOVE_I R6, IConst: 9069
		k1 = 9069 / m2;	// op: 15: MOVE_I R7, IConst: 0
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
