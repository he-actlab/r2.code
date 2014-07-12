package jnt.scimark2;


/* Random.java based on Java Numerical Toolkit (JNT) Random.UniformSequence
	 class.  We do not use Java's own java.util.Random so that we can compare
	 results with equivalent C and Fortran coces.
 */

public class Random {


	/* ------------------------------------------------------------------------------
		 CLASS VARIABLES
		 ------------------------------------------------------------------------------ */

	int seed = 0;	// approx: 2: PUTFIELD_I R0, .seed, IConst: 0

	private int m[];
	private int i = 4;
	private int j = 16;

	private final int mdig = 32;	// approx: 5: PUTFIELD_I R0, .mdig, IConst: 32
	private final int one = 1;	// approx: 6: PUTFIELD_I R0, .one, IConst: 1
	private final int m1 = (one << mdig-2) + ((one << mdig-2)-one);	// approx: 7: PUTFIELD_I R0, .m1, IConst: 2147483647
	private final int m2 = one << mdig/2;	// approx: 8: PUTFIELD_I R0, .m2, IConst: 65536

	/* For mdig = 32 : m1 =          2147483647, m2 =      65536
		 For mdig = 64 : m1 = 9223372036854775807, m2 = 4294967296 
	 */

	private double dm1 = 1.0 / (double) m1;	// approx: 9: PUTFIELD_D R0, .dm1, DConst: 4.656612875245797E-10

	private boolean haveRange = false;
	private double left  = 0.0;	// approx: 11: PUTFIELD_D R0, .left, DConst: 0.0
	private double right = 1.0;	// approx: 12: PUTFIELD_D R0, .right, DConst: 1.0
	private double width = 1.0;	// approx: 13: PUTFIELD_D R0, .width, DConst: 1.0


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
		double nextValue;

		k = m[i] - m[j]; 	// approx: 8: MOVE_I R16, T15	// approx: 7: SUB_I T15, T12, T14	// approx: 6: ALOAD_I T14, T13, T5	// approx: 3: ALOAD_I T12, T10, T11
		// additional accept
		k = accept(k);
		if (k < 0) 
			k += m1; 	// approx: 12: ADD_I T19, R18, IConst: 2147483647	// approx: 13: MOVE_I R20, T19
		m[j] = k; 	// approx: 16: ASTORE_I R21, T22, T23

		// additional accept
		accept_all_FIELD2_TAG();

		if (i == 0) 
			i = 16;
		else 
			i--;

		if (j == 0) 
			j = 16 ;
		else 
			j--;

		if (haveRange) 
			return  left +  dm1 * k * width; 	// approx: 46: ADD_D T39, T33, T38	// approx: 45: MUL_D T38, T36, T37	// approx: 44: GETFIELD_D T37, R0, .width	// approx: 43: MUL_D T36, T34, T35	// approx: 41: GETFIELD_D T34, R0, .dm1	// approx: 40: GETFIELD_D T33, R0, .left
		else
			return dm1 * k; 	// approx: 38: MUL_D T42, T40, T41	// approx: 36: GETFIELD_D T40, R0, .dm1

	} 

	/*----------------------------------------------------------------------------
		PRIVATE METHODS
		------------------------------------------------------------------------ */

	private void initialize (int seed) {

		int jseed, k0, k1, j0, j1, iloop;

		this.seed = seed;	// approx: 1: PUTFIELD_I R0, .seed, R1

		m = new int[17];

		jseed = Math.min(Math.abs(seed),m1);
		if (jseed % 2 == 0) --jseed;	// approx: 10: ADD_I R19, R17, IConst: -1
		k0 = 9069 % m2;	// approx: 11: MOVE_I R6, IConst: 9069
		k1 = 9069 / m2;	// approx: 12: MOVE_I R7, IConst: 0
		j0 = jseed % m2;	// approx: 13: REM_I T21, R20, IConst: 65536	// approx: 14: MOVE_I R22, T21
		j1 = jseed / m2;	// approx: 16: MOVE_I R24, T23	// approx: 15: DIV_I T23, R20, IConst: 65536
		for (iloop = 0; iloop < 17; ++iloop) 
		{
			jseed = j0 * k0;	// approx: 23: MOVE_I R30, T29	// approx: 22: MUL_I T29, R28, R6
			j1 = (jseed / m2 + j0 * k1 + j1 * k0) % (m2 / 2);	// approx: 25: MUL_I T32, R28, R7	// approx: 24: DIV_I T31, R30, IConst: 65536	// approx: 29: REM_I T36, T35, IConst: 32768	// approx: 28: ADD_I T35, T33, T34	// approx: 27: MUL_I T34, R27, R6	// approx: 26: ADD_I T33, T31, T32	// approx: 30: MOVE_I R37, T36
			j0 = jseed % m2;	// approx: 31: REM_I T38, R30, IConst: 65536	// approx: 32: MOVE_I R39, T38
			m[iloop] = j0 + m2 * j1;	// approx: 34: MUL_I T12, IConst: 65536, R37	// approx: 35: ADD_I T13, R39, T12	// approx: 36: ASTORE_I T13, T11, R26
		}
		i = 4;
		j = 16;

	}

	//jspark
	private int accept (int i){return i;}
	private void accept_all_FIELD2_TAG(){}
	//krapsj

}
