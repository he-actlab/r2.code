package jnt.scimark2;

import enerj.lang.*;

public class kernel
{

	public static double measureLU(int N, double min_time, Random R)
	{
		@Approx double A[][] = RandomMatrix(N, N,  R);
		@Approx double lu[][] = new @Approx double[N][];
		for (int i=0; i<N; i++){
			lu[i] = new @Approx double[N];
		}
		int pivot[] = new int[N];

		int cycles=100;
		for (int i=0; i<cycles; i++)
		{
			CopyMatrix(lu, A);
			LU.factor((@Approx double [][])lu, pivot);
		}

		// verify that LU is correct
		@Approx double b[] = RandomVector(N, R);
		@Approx double x[] = NewVectorCopy(b);

		LU.solve((@Approx double [][])lu, pivot, x);

		@Approx double[] y = matvec(A, x);

		System.out.print("LU vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print(Endorsements.endorse(y[i]) + " ");
		}
		System.out.println("");

		return 0.0;
	}

	private static @Approx double[] NewVectorCopy(@Approx double x[])
	{
		int N = x.length;

		@Approx double y[] = new @Approx double[N];
		for (int i=0; i<N; i++)
			y[i] = x[i];

		return y;
	}

	private static void CopyMatrix(@Approx double B[][], @Approx double A[][])
	{
		int M = A.length;
		int N = A[0].length;

		int remainder = N & 3;		 // N mod 4;

		for (int i=0; i<M; i++)
		{
			@Approx double Bi[] = B[i];
			@Approx double Ai[] = A[i];
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

	private static @Approx double[][] RandomMatrix(int M, int N, Random R)
	{
		@Approx double A[][] = new @Approx double[M][];
		for (int i=0; i<M; i++) {
			A[i] = new @Approx double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}

	private static @Approx double[] RandomVector(int N, Random R)
	{
		@Approx double A[] = new @Approx double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

	private static @Approx double[] matvec(@Approx double A[][], @Approx double x[])
	{
		int N = x.length;
		@Approx double y[] = new @Approx double[N];

		matvec(A, x, y);

		return y;
	}

	private static void matvec(@Approx double A[][], @Approx double x[], @Approx double y[])
	{
		int M = A.length;
		int N = A[0].length;

		for (int i=0; i<M; i++)
		{
			@Approx double sum = 0.0; 
			@Approx double Ai[] = A[i];
			for (int j=0; j<N; j++) {

				sum += Ai[j] * x[j]; 
			}

			y[i] = sum; 
		}
	}

}
