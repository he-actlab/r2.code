package jnt.scimark2;

/** Computes FFT's of complex, double precision data where n is an integer power of 2.
 * This appears to be slower than the Radix2 method,
 * but the code is smaller and simpler, and it requires no extra storage.
 * <P>
 *
 * @author Bruce R. Miller bruce.miller@nist.gov,
 * @author Derived from GSL (Gnu Scientific Library), 
 * @author GSL's FFT Code by Brian Gough bjg@vvv.lanl.gov
 */

/* See {@link ComplexDoubleFFT ComplexDoubleFFT} for details of data layout.
 */

public class FFT {

	/** Compute Fast Fourier Transform of (complex) data, in place.*/
	public static void transform (double data[]) {
		transform_internal(data, -1); } 	// approx: 2: MOVE_I T1, IConst: -1

	/** Compute Inverse Fast Fourier Transform of (complex) data, in place.*/
	public static void inverse (double data[]) {
		transform_internal(data, +1); 	// approx: 2: MOVE_I T1, IConst: 1
		// Normalize
		int nd=data.length;
		int n =nd/2; 	// approx: 5: DIV_I T13, R3, IConst: 2	// approx: 6: MOVE_I R4, T13
		int aprN = n; 	// approx: 7: MOVE_I R5, R4
		double norm=1.0/aprN;	// approx: 8: INT_2DOUBLE T14, R5	// approx: 9: DIV_D T7, DConst: 1.0, T14	// approx: 10: MOVE_D R8, T7
		for(int i=0; i<nd; i++)
			data[i] *= norm; 	// approx: 18: ASTORE_D T18, T11, T10	// approx: 17: MUL_D T18, T17, R8	// approx: 16: ALOAD_D T17, R0, R16
	}

	protected static int log2 (int n){
		int log = 0;
		for(int k=1; k < n; k *= 2, log++);
		if (n != (1 << log))
			throw new Error("FFT: Data length is not a power of 2!: "+n);
		return log; 
	}

	protected static void transform_internal (double data[], int direction) {
		if (data.length == 0) return;    
		int n = data.length/2;
		if (n == 1) return;         // Identity operation!
		int logn = log2(n);

		/* bit reverse the input data for decimation in time algorithm */
		bitreverse(data) ;

		/* apply fft recursion */
		/* this loop executed log2(N) times */
		for (int bit = 0, dual = 1; bit < logn; bit++, dual *= 2) {
			double w_real = 1.0; 	// approx: 14: MOVE_D R39, DConst: 1.0
			double w_imag = 0.0; 	// approx: 15: MOVE_D R40, DConst: 0.0

			double theta = 2.0 * direction * Math.PI / (2.0 * (double) dual);	// approx: 22: MOVE_D R12, T45	// approx: 21: DIV_D T45, T43, T44	// approx: 20: MUL_D T44, DConst: 2.0, T11	// approx: 19: INT_2DOUBLE T11, R37	// approx: 17: MUL_D T42, DConst: 2.0, T41	// approx: 18: MUL_D T43, T42, DConst: 3.141592653589793	// approx: 16: INT_2DOUBLE T41, R1
			double s = Math.sin(theta);	// approx: 24: MOVE_D R13, T46
			double t = Math.sin(theta / 2.0); 	// approx: 27: MOVE_D R14, T48	// approx: 25: DIV_D T47, R12, DConst: 2.0
			double s2 = 2.0 * t * t; 	// approx: 30: MOVE_D R15, T50	// approx: 29: MUL_D T50, T49, R14	// approx: 28: MUL_D T49, DConst: 2.0, R14

			/* a = 0 */
			for (int b = 0; b < n; b += 2 * dual) {
				int i = 2*b ;
				int j = 2*(b + dual);

				double wd_real = data[j] ; 	// approx: 106: MOVE_D R59, T58	// approx: 105: ALOAD_D T58, R0, R57
				double wd_imag = data[j+1] ; 	// approx: 109: MOVE_D R30, T61	// approx: 108: ALOAD_D T61, R0, T60

				data[j]   = data[i]   - wd_real; 	// approx: 113: ASTORE_D T64, T62, R57	// approx: 112: SUB_D T64, T63, R59	// approx: 111: ALOAD_D T63, R0, R54
				data[j+1] = data[i+1] - wd_imag; 	// approx: 119: ASTORE_D T69, T67, T65	// approx: 118: SUB_D T69, T68, R30	// approx: 117: ALOAD_D T68, R0, T66
				data[i]  += wd_real; 	// approx: 122: ALOAD_D T72, R0, R54	// approx: 123: ADD_D T73, T72, R59	// approx: 124: ASTORE_D T73, T71, T70
				data[i+1]+= wd_imag; 	// approx: 128: ALOAD_D T77, R0, T74	// approx: 129: ADD_D T78, T77, R30	// approx: 130: ASTORE_D T78, T76, T75
			}

			/* a = 1 .. (dual-1) */
			for (int a = 1; a < dual; a++) {
				/* trignometric recurrence for w-> exp(i theta) w */
				{
					double tmp_real = w_real - s * w_imag - s2 * w_real; 	// approx: 43: MOVE_D R17, T89	// approx: 41: MUL_D T88, R15, R85	// approx: 42: SUB_D T89, T87, T88	// approx: 39: MUL_D T86, R13, R84	// approx: 40: SUB_D T87, R85, T86
					double tmp_imag = w_imag + s * w_real - s2 * w_imag; 	// approx: 48: MOVE_D R94, T93	// approx: 47: SUB_D T93, T91, T92	// approx: 46: MUL_D T92, R15, R84	// approx: 45: ADD_D T91, R84, T90	// approx: 44: MUL_D T90, R13, R85
					w_real = tmp_real; 	// approx: 49: MOVE_D R95, R17
					w_imag = tmp_imag; 	// approx: 50: MOVE_D R96, R94
				}
				for (int b = 0; b < n; b += 2 * dual) {
					int i = 2*(b + a);
					int j = 2*(b + a + dual);

					double z1_real = data[j]; 	// approx: 145: ALOAD_D T105, R0, R22	// approx: 146: MOVE_D R23, T105
					double z1_imag = data[j+1]; 	// approx: 148: ALOAD_D T107, R0, T106	// approx: 149: MOVE_D R24, T107

					double wd_real = w_real * z1_real - w_imag * z1_imag; 	// approx: 152: SUB_D T110, T108, T109	// approx: 153: MOVE_D R25, T110	// approx: 150: MUL_D T108, R95, R23	// approx: 151: MUL_D T109, R96, R24
					double wd_imag = w_real * z1_imag + w_imag * z1_real; 	// approx: 157: MOVE_D R26, T113	// approx: 156: ADD_D T113, T111, T112	// approx: 154: MUL_D T111, R95, R24	// approx: 155: MUL_D T112, R96, R23

					data[j]   = data[i]   - wd_real; 	// approx: 161: ASTORE_D T116, T114, R22	// approx: 160: SUB_D T116, T115, R25	// approx: 159: ALOAD_D T115, R0, R101
					data[j+1] = data[i+1] - wd_imag; 	// approx: 165: ALOAD_D T120, R0, T118	// approx: 167: ASTORE_D T121, T119, T117	// approx: 166: SUB_D T121, T120, R26
					data[i]  += wd_real; 	// approx: 172: ASTORE_D T125, T123, T122	// approx: 171: ADD_D T125, T124, R25	// approx: 170: ALOAD_D T124, R0, R101
					data[i+1]+= wd_imag; 	// approx: 178: ASTORE_D T130, T128, T127	// approx: 176: ALOAD_D T129, R0, T126	// approx: 177: ADD_D T130, T129, R26
				}
			}
		}
	}


	protected static void bitreverse(double data[]) {
		/* This is the Goldrader bit-reversal algorithm */
		int n=data.length/2;
		int nm1 = n-1;
		int i=0; 
		int j=0;
		for (; i < nm1; i++) {

			//int ii = 2*i;
			int ii = i << 1;

			//int jj = 2*j;
			int jj = j << 1;

			//int k = n / 2 ;
			int k = n >> 1;

			if (i < j) {
				double tmp_real    = data[ii]; 	// approx: 17: ALOAD_D T27, R0, R6	// approx: 18: MOVE_D R10, T27
				double tmp_imag    = data[ii+1]; 	// approx: 20: ALOAD_D T29, R0, T28	// approx: 21: MOVE_D R12, T29
				data[ii]   = data[jj]; 	// approx: 23: ALOAD_D T31, R0, R7	// approx: 24: ASTORE_D T31, T30, R6
				data[ii+1] = data[jj+1]; 	// approx: 28: ALOAD_D T34, R0, T15	// approx: 29: ASTORE_D T34, T33, T32
				data[jj]   = tmp_real; 	// approx: 30: ASTORE_D R10, R0, R7
				data[jj+1] = tmp_imag; } 	// approx: 32: ASTORE_D R12, R0, T35

				while (k <= j) 
				{
					//j = j - k ;
					j -= k;

					//k = k / 2 ; 
					k >>= 1 ; 
				}
				j += k ;
		}
	}
}








