package jnt.scimark2;

public class kernel
{

	public static double measureLU(int N, double min_time, Random R)
	{
		double A[][] = RandomMatrix(N, N,  R);
		double lu[][] = new double[N][];
		for (int i=0; i<N; i++){
			alloc_TAG3();
			lu[i] = new double[N];
		}
		int pivot[] = new int[N];

		int cycles=100;
		for (int i=0; i<cycles; i++)
		{
			CopyMatrix(lu, A);
			LU.factor(( double [][])lu, pivot);
		}

		// verify that LU is correct
		double b[] = RandomVector(N, R);
		double x[] = NewVectorCopy(b);

		LU.solve(( double [][])lu, pivot, x);

		double[] y = matvec(A, x);

		y = accept_all_FIELD1_TAG1(y);

		System.out.print("LU vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((y[i]) + " ");	// approx: 37: ALOAD_D T17, R36, R40
		}
		System.out.println("");

		y = precise_all_FIELD1_TAG1(y);

		return 0.0;
	}

	private static  double[] NewVectorCopy( double x[])
	{
		int N = x.length;

		alloc_TAG2();
		double y[] = new  double[N];
		for (int i=0; i<N; i++)
			y[i] = x[i];	// approx: 10: ASTORE_D T6, R4, R9	// approx: 9: ALOAD_D T6, R0, R9

		return y;
	}

	private static void CopyMatrix( double B[][],  double A[][])
	{
		int M = A.length;
		int N = A[0].length;

		int remainder = N & 3;		 // N mod 4;

		for (int i=0; i<M; i++)
		{
			double Bi[] = B[i];
			double Ai[] = A[i];
			for (int j=0; j<remainder; j++)
				Bi[j] = Ai[j];	// approx: 37: ALOAD_D T26, R9, R25	// approx: 38: ASTORE_D T26, R8, R25
			for (int j=remainder; j<N; j+=4)
			{
				Bi[j] = Ai[j];	// approx: 22: ASTORE_D T30, R8, R29	// approx: 21: ALOAD_D T30, R9, R29
				Bi[j+1] = Ai[j+1];	// approx: 26: ASTORE_D T33, R8, T31	// approx: 25: ALOAD_D T33, R9, T32
				Bi[j+2] = Ai[j+2];	// approx: 30: ASTORE_D T36, R8, T34	// approx: 29: ALOAD_D T36, R9, T35
				Bi[j+3] = Ai[j+3];	// approx: 34: ASTORE_D T39, R8, T37	// approx: 33: ALOAD_D T39, R9, T38
			}
		}
	}

	private static  double[][] RandomMatrix(int M, int N, Random R)
	{
		double A[][] = new double[M][];
		for (int i=0; i<M; i++) {
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();	// approx: 14: ASTORE_D T8, T18, R17
		return A;
	}

	private static  double[] RandomVector(int N, Random R)
	{
		double A[] = new  double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 	// approx: 7: ASTORE_D T5, R3, R8
		return A;
	}

	private static  double[] matvec( double A[][],  double x[])
	{
		int N = x.length;
		alloc_TAG1();
		double y[] = new  double[N];

		matvec(A, x, y);

		return y;
	}

	private static void matvec( double A[][],  double x[],  double y[])
	{
		int M = A.length;
		int N = A[0].length;

		for (int i=0; i<M; i++)
		{
			double sum = 0.0; 	// approx: 9: MOVE_D R20, DConst: 0.0
			double Ai[] = A[i];
			for (int j=0; j<N; j++) {

				sum += Ai[j] * x[j]; 	// approx: 21: MOVE_D R27, T14	// approx: 18: ALOAD_D T13, R1, R23	// approx: 17: ALOAD_D T25, R9, R23	// approx: 20: ADD_D T14, R24, T26	// approx: 19: MUL_D T26, T25, T13
			}

			y[i] = sum; 	// approx: 14: ASTORE_D R24, R2, R19
		}
	}

	private static void alloc_TAG1(){}
	private static void alloc_TAG2(){}
	private static void alloc_TAG3(){}
	private static double[] accept_all_FIELD1_TAG1(double[] d){return d;}
	private static double[] precise_all_FIELD1_TAG1(double[] d){return d;}


}
