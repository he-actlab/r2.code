package jnt.scimark2;

import chord.analyses.expax.lang.*;

public class kernel
{

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

		System.out.print("LU vector: ");
		for (int i = 0; i < N; ++i) {
			y = Accept.accept_all_FIELD1_TAG1(y);
			System.out.print((y[i]) + " ");
		}
		System.out.println("");

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

	private static void CopyMatrix( double B[][],  double A[][])
	{
		int M = A.length;
		int N = A[0].length;

		int remainder = N & 3;		 

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

	private static double[] RandomVector(int N, Random R)
	{
		double A[] = new double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

	private static double[] matvec( double A[][],  double x[])
	{
		int N = x.length;
		Alloc.alloc_TAG1();
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
			double sum = 0.0; 
			double Ai[] = A[i];
			for (int j=0; j<N; j++) {

				sum += Ai[j] * x[j]; 
			}

			y[i] = sum; 
		}
	}

}
