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
		transform_internal(data, -1); } 	// op: 2: MOVE_I T1, IConst: -1

	/** Compute Inverse Fast Fourier Transform of (complex) data, in place.*/
	public static void inverse (double data[]) {
		transform_internal(data, +1); 	// op: 2: MOVE_I T1, IConst: 1
		// Normalize
		int nd=data.length;
		int n =nd/2; 	// op: 6: DIV_I T15, R14, IConst: 2	// op: 7: MOVE_I R16, T15
		int aprN = n; 	// op: 9: MOVE_I R18, R17	// op: 8: MOVE_I R17, R16
		double norm=1.0/aprN;	// op: 10: MOVE_I R19, R18	// op: 11: INT_2DOUBLE T20, R19	// op: 12: DIV_D T7, DConst: 1.0, T20	// op: 13: MOVE_D R21, T7
		for(int i=0; i<nd; i++)
			data[i] *= norm; 	// op: 35: ASTORE_D T31, T11, T10	// op: 33: MOVE_D R30, R24	// op: 34: MUL_D T31, T29, R30	// op: 32: ALOAD_D T29, R0, R28
	}

	protected static int log2 (int n){
		int log = 0;
		for(int k=1; k < n; k *= 2, log++);
		if (n != (1 << log))
			throw new Error("FFT: Data length is not a power of 2!: "+n);
		return log; 
	}

	private static double accept(double d){return d;}	// op: 1: MOVE_D R0, R0
	
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
			double w_real = 1.0; 	// op: 204: MOVE_D R48, DConst: 1.0
			double w_imag = 0.0; 	// op: 205: MOVE_D R49, DConst: 0.0

			double theta = 2.0 * direction * Math.PI / (2.0 * (double) dual);	// op: 206: MOVE_I R1, R1	// op: 214: MOVE_D R56, T55	// op: 213: DIV_D T55, T52, T54	// op: 212: MUL_D T54, DConst: 2.0, T11	// op: 211: INT_2DOUBLE T11, R53	// op: 209: MUL_D T52, T51, DConst: 3.141592653589793	// op: 208: MUL_D T51, DConst: 2.0, T50	// op: 207: INT_2DOUBLE T50, R1
			theta = accept(theta);	// op: 217: MOVE_D R59, T58	// op: 215: MOVE_D R57, R56
			double s = Math.sin(theta);	// op: 220: MOVE_D R62, T61	// op: 218: MOVE_D R60, R59
			double param = theta / 2.0;	// op: 222: DIV_D T64, R63, DConst: 2.0	// op: 221: MOVE_D R63, R60	// op: 223: MOVE_D R65, T64
			param = accept(param);	// op: 224: MOVE_D R66, R65	// op: 226: MOVE_D R68, T67
			double t = Math.sin(param); 	// op: 227: MOVE_D R69, R68	// op: 229: MOVE_D R71, T70
			double s2 = 2.0 * t * t; 	// op: 231: MUL_D T73, DConst: 2.0, R72	// op: 232: MOVE_D R74, R72	// op: 233: MUL_D T75, T73, R74	// op: 234: MOVE_D R76, T75	// op: 230: MOVE_D R72, R71

			/* a = 0 */
			for (int b = 0; b < n; b += 2 * dual) {
				int i = 2*b ;
				int j = 2*(b + dual);

				double wd_real = data[j] ; 	// op: 402: ALOAD_D T92, R0, R91	// op: 403: MOVE_D R93, T92
				double wd_imag = data[j+1] ; 	// op: 406: ALOAD_D T96, R0, T95	// op: 407: MOVE_D R97, T96

				data[j]   = data[i]   - wd_real; 	// op: 414: ASTORE_D T103, T100, R98	// op: 413: SUB_D T103, T101, R102	// op: 412: MOVE_D R102, R93	// op: 411: ALOAD_D T101, R0, R99
				data[j+1] = data[i+1] - wd_imag; 	// op: 422: SUB_D T111, T109, R110	// op: 421: MOVE_D R110, R97	// op: 423: ASTORE_D T111, T108, T105	// op: 420: ALOAD_D T109, R0, T107
				data[i]  += wd_real; 	// op: 429: ADD_D T117, T115, R116	// op: 430: ASTORE_D T117, T114, T113	// op: 427: ALOAD_D T115, R0, R112	// op: 428: MOVE_D R116, R102
				data[i+1]+= wd_imag; 	// op: 437: ADD_D T124, T122, R123	// op: 438: ASTORE_D T124, T121, T120	// op: 435: ALOAD_D T122, R0, T119	// op: 436: MOVE_D R123, R110
			}

			/* a = 1 .. (dual-1) */
			for (int a = 1; a < dual; a++) {
				/* trignometric recurrence for w-> exp(i theta) w */
				{
					double tmp_real = w_real - s * w_imag - s2 * w_real; 	// op: 530: MOVE_D R149, T148	// op: 521: MOVE_D R140, R135	// op: 527: MOVE_D R146, R140	// op: 526: MOVE_D R145, R132	// op: 529: SUB_D T148, T144, T147	// op: 528: MUL_D T147, R145, R146	// op: 523: MOVE_D R142, R134	// op: 522: MOVE_D R141, R133	// op: 525: SUB_D T144, R140, T143	// op: 524: MUL_D T143, R141, R142
					double tmp_imag = w_imag + s * w_real - s2 * w_imag; 	// op: 534: MUL_D T153, R151, R152	// op: 535: ADD_D T154, R150, T153	// op: 536: MOVE_D R155, R145	// op: 537: MOVE_D R156, R150	// op: 531: MOVE_D R150, R142	// op: 532: MOVE_D R151, R141	// op: 533: MOVE_D R152, R146	// op: 538: MUL_D T157, R155, R156	// op: 539: SUB_D T158, T154, T157	// op: 540: MOVE_D R159, T158
					w_real = tmp_real; 	// op: 542: MOVE_D R161, R160	// op: 541: MOVE_D R160, R149
					w_imag = tmp_imag; 	// op: 543: MOVE_D R162, R159	// op: 544: MOVE_D R163, R162
				}
				for (int b = 0; b < n; b += 2 * dual) {
					int i = 2*(b + a);
					int j = 2*(b + a + dual);

					double z1_real = data[j]; 	// op: 568: MOVE_D R187, T186	// op: 567: ALOAD_D T186, R0, R185
					double z1_imag = data[j+1]; 	// op: 571: ALOAD_D T190, R0, T189	// op: 572: MOVE_D R191, T190

					double wd_real = w_real * z1_real - w_imag * z1_imag; 	// op: 573: MOVE_D R192, R168	// op: 575: MUL_D T194, R192, R193	// op: 574: MOVE_D R193, R187	// op: 577: MOVE_D R196, R191	// op: 576: MOVE_D R195, R167	// op: 579: SUB_D T198, T194, T197	// op: 578: MUL_D T197, R195, R196	// op: 580: MOVE_D R199, T198
					double wd_imag = w_real * z1_imag + w_imag * z1_real; 	// op: 584: MOVE_D R203, R195	// op: 585: MOVE_D R204, R193	// op: 586: MUL_D T205, R203, R204	// op: 587: ADD_D T206, T202, T205	// op: 588: MOVE_D R207, T206	// op: 581: MOVE_D R200, R192	// op: 583: MUL_D T202, R200, R201	// op: 582: MOVE_D R201, R196

					data[j]   = data[i]   - wd_real; 	// op: 592: ALOAD_D T211, R0, R209	// op: 593: MOVE_D R212, R199	// op: 594: SUB_D T213, T211, R212	// op: 595: ASTORE_D T213, T210, R208
					data[j+1] = data[i+1] - wd_imag; 	// op: 603: SUB_D T221, T219, R220	// op: 602: MOVE_D R220, R207	// op: 601: ALOAD_D T219, R0, T217	// op: 604: ASTORE_D T221, T218, T215
					data[i]  += wd_real; 	// op: 611: ASTORE_D T227, T224, T223	// op: 610: ADD_D T227, T225, R226	// op: 609: MOVE_D R226, R212	// op: 608: ALOAD_D T225, R0, R222
					data[i+1]+= wd_imag; 	// op: 618: ADD_D T234, T232, R233	// op: 619: ASTORE_D T234, T231, T230	// op: 616: ALOAD_D T232, R0, T229	// op: 617: MOVE_D R233, R220
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
				double tmp_real    = data[ii]; 	// op: 94: ALOAD_D T42, R0, R41	// op: 95: MOVE_D R43, T42
				double tmp_imag    = data[ii+1]; 	// op: 98: ALOAD_D T46, R0, T45	// op: 99: MOVE_D R47, T46
				data[ii]   = data[jj]; 	// op: 104: ASTORE_D T51, T50, R48	// op: 103: ALOAD_D T51, R0, R49
				data[ii+1] = data[jj+1]; 	// op: 111: ASTORE_D T56, T55, T53	// op: 110: ALOAD_D T56, R0, T15
				data[jj]   = tmp_real; 	// op: 114: ASTORE_D R58, R0, R57	// op: 113: MOVE_D R58, R43
				data[jj+1] = tmp_imag; } 	// op: 117: MOVE_D R61, R47	// op: 118: ASTORE_D R61, R0, T60

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








