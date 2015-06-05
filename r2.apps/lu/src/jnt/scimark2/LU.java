package jnt.scimark2;

/**
 Evaluation for FlexJava framework
*/

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.*;

public class LU 
{
	public static int factor( double[][] A, int pivot[])
	{
		int N = A.length;
		int M = A[0].length;

		boolean done = false;
		int ret = -1;
		int minMN = ApproxMath.min(M,N);

		for (int j=0; j<minMN; j++)
		{
			int jp=j;

			double t = ApproxMath.abs(A[j][j]);
			for (int i=j+1; i<M; i++)
			{
				double ab = ApproxMath.abs(A[i][j]);
				if (ab > t)
				{
					jp = i;
					t = ab;
				}
			}

			pivot[j] = jp;

			if (A[jp][j] == 0) {             
				ret = 1;
				done = true;
			}

			if(!done) {
				if (jp != j)
				{
					// swap rows j and jp
					double tA[] = A[j];
					A[j] = A[jp];
					A[jp] = tA;
				}
	
				if (j<M-1)  
				{
					double recp =  1.0 / A[j][j];
	
					for (int k=j+1; k<M; k++)
						A[k][j] *= recp;
				}
	
				if (j < minMN-1)
				{
					for (int ii=j+1; ii<M; ii++)
					{
						for (int jj=j+1; jj<N; jj++) {
							A[ii][jj] -= A[ii][j] * A[j][jj];
							Loosen.loosen(A[ii][jj]);
						}
					}
				}
			}
		}
		if(!done)
			ret = 0;

		Loosen.loosen(ret);

		return ret;
	}

	public static void solve( double[][] LU, int pvt[],  double b[])
	{
		int M = LU.length;
		int N = LU[0].length;
		int ii=0;

		for (int i=0; i<M; i++)
		{
			int ip = pvt[i];
			double sum = b[ip];

			b[ip] = b[i];
			if (ii==0)
				for (int j=ii; j<i; j++) {
					sum -= LU[i][j] * b[j];
				}
			else 
				if (sum == 0.0)
					ii = i;
			b[i] = sum;
		}

		for (int i=N-1; i>=0; i--)
		{
			double sum = b[i]; 
			for (int j=i+1; j<N; j++)
				sum -= LU[i][j] * b[j]; 
			b[i] = sum / LU[i][i]; 
			Loosen.loosen(b[i]);
		}
	}   

	private static  double[] NewVectorCopy( double x[])
	{
		int N = x.length;

		double y[] = new  double[N];
		for (int i=0; i<N; i++) {
			y[i] = x[i];
			Loosen.loosen(y[i]);
		}

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
			for (int j=0; j<N; j++) { 
				A[i][j] = R.nextDouble();
				Loosen.loosen(A[i][j]);
			}
		return A;
	}

	private static double[] RandomVector(int N, Random R)
	{
		double A[] = new double[N];

		for (int i=0; i<N; i++) {
			A[i] = R.nextDouble(); 
			Loosen.loosen(A[i]);
		}
		return A;
	}

	private static double[] matvec( double A[][],  double x[])
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
			double sum = 0.0; 
			double Ai[] = A[i];
			for (int j=0; j<N; j++) {

				sum += Ai[j] * x[j]; 
			}

			y[i] = sum; 
			Loosen.loosen(y[i]);
		}
	}


	public static void main(String[] args) {
		int N = 10;
		double min_time = 2.0;
		Random R = new Random(Integer.parseInt(args[0]));

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
			double y_i = y[i];

			Loosen.loosen(y_i);

			System.out.print(y_i + " ");

			Tighten.tighten(y_i);
		}
		System.out.println("");
	}
}
