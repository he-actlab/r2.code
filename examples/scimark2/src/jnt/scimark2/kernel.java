package jnt.scimark2;

public class kernel
{

	public static double measureFFT(int N, double mintime, Random R)
	{
		double x[] = RandomVector(2*N, R);
		double oldx[] = NewVectorCopy(x);
		long cycles = 100;

		for (int i=0; i<cycles; i++)
		{
			FFT.transform(x);	// forward transform
			FFT.inverse(x);		// backward transform
		}

		x = accept_all(x);
		System.out.print("FFT vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((x[i]) + " ");
		}
		System.out.println("");
		x = precise_all(x);

		return 0.0;
	}

	public static double measureSOR(int N, double min_time, Random R)
	{
		double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); // approx: load 1.25 

		G[0] = accept_all(G[0]);

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				System.out.print((G[i][j]) + " ");
			}
		}
		System.out.println("");

		G[0] = precise_all(G[0]);

		return 0.0;
	}

	public static double measureMonteCarlo(double min_time, Random R, String seed)
	{
		int cycles=1492;
		double out = 0.0; // approx: move to out
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); // approx: move to out

		out = accept(out);

		System.out.println("MonteCarlo out: " + (out));

		out = precise(out);

		return 0.0;
	}


	public static double measureSparseMatmult(int N, int nz, double min_time, Random R)
	{
		double x[] = RandomVector(N, R);
		double y[] = new  double[N];

		int nr = nz/N; 		// average number of nonzeros per row
		int anz = nr *N;   // _actual_ number of nonzeros


		double val[] = RandomVector(anz, R);
		int col[] = new int[anz];
		int row[] = new int[N+1];

		row[0] = 0;	
		for (int r=0; r<N; r++)
		{
			// initialize elements for row r
			int rowr = row[r];
			row[r+1] = rowr + nr;
			int step = r/ nr;
			if (step < 1) 
				step = 1;   // take at least unit steps

			for (int i=0; i<nr; i++)
				col[rowr+i] = i*step;
		}

		int cycles=100;
		SparseCompRow.matmult(y, val, row, col, x, cycles);

		y = accept_all(y);

		System.out.print("SparseMatMult vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((y[i]) + " ");
		}
		System.out.println("");

		y = precise_all(y);

		return 0.0;
	}


	public static double measureLU(int N, double min_time, Random R)
	{
		double A[][] = RandomMatrix(N, N,  R);
		double lu[][] = new double[N][];
		for (int i=0; i<N; i++){
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

		y = accept_all(y);

		System.out.print("LU vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((y[i]) + " ");
		}
		System.out.println("");

		y = precise_all(y);

		return 0.0;
	}

	private static  double[] NewVectorCopy( double x[])
	{
		int N = x.length;

		double y[] = new  double[N];
		for (int i=0; i<N; i++)
			y[i] = x[i];

		return y;
	}

	private static void CopyVector( double B[],  double A[])
	{
		int N = A.length;

		for (int i=0; i<N; i++)
			B[i] = A[i];
	}


	private static  double normabs( double x[],  double y[])
	{
		int N = x.length;
		double sum = 0.0;

		for (int i=0; i<N; i++) {
			sum += Math.abs(x[i]-y[i]);
		}

		return sum;
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
				Bi[j] = Ai[j];
			for (int j=remainder; j<N; j+=4)
			{
				Bi[j] = Ai[j];
				Bi[j+1] = Ai[j+1];
				Bi[j+2] = Ai[j+2];
				Bi[j+3] = Ai[j+3];
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
				A[i][j] = R.nextDouble();
		return A;
	}

	private static  double[] RandomVector(int N, Random R)
	{
		double A[] = new  double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); //approx: (smm) move to A[i]
		return A;
	}

	private static  double[] matvec( double A[][],  double x[])
	{
		int N = x.length;
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
			double sum = 0.0; // approx: move to sum
			double Ai[] = A[i];
			for (int j=0; j<N; j++) {

				sum += Ai[j] * x[j]; // approx: load Ai[j] + x[j], mul, add, move to sum
			}

			y[i] = sum; // approx: move to y[i]
		}
	}

	private static double accept(double d){
		return d;
	}

	private static double precise(double d){
		return d;
	}

	private static double[] accept_all(double[] d){
		return d;
	}

	private static double[] precise_all(double[] d){
		return d;
	}

	private static double[][] accept_all(double[][] d){
		return d;
	}

	private static double[][] precise_all(double[][] d){
		return d;
	}



}
