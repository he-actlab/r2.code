package jnt.scimark2;

import chord.analyses.expax.lang.math.ApproxMath;
import chord.analyses.expax.lang.Accept;

/**
	LU matrix factorization. (Based on TNT implementation.)
	Decomposes a matrix A  into a triangular lower triangular
	factor (L) and an upper triangular factor (U) such that
	A = L*U.  By convnetion, the main diagonal of L consists
	of 1's so that L and U can be stored compactly in
	a NxN matrix.


 */
public class LU 
{
	/**
		LU factorization (in place).

		@param A (in/out) On input, the matrix to be factored.
		On output, the compact LU factorization.

		@param pivit (out) The pivot vector records the
		reordering of the rows of A during factorization.

		@return 0, if OK, nozero value, othewise.
	 */
	public static  int factor( double[][] A, int pivot[])
	{
		int N = A.length;
		int M = A[0].length;

		int minMN = Math.min(M,N);

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

			//additional accept
			double d = A[jp][j];
			d = Accept.accept(d);
			if (d == 0)                 
				return 1;       

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
					double Aii[] = A[ii];
					double Aj[] = A[j];
					double AiiJ = Aii[j];
					for (int jj=j+1; jj<N; jj++)
						Aii[jj] -= AiiJ * Aj[jj];
				}
			}
		}

		return 0;
	}

	/**
		Solve a linear system, using a prefactored matrix
		in LU form.


		@param LU (in) the factored matrix in LU form. 
		@param pivot (in) the pivot vector which lists
		the reordering used during the factorization
		stage.
		@param b    (in/out) On input, the right-hand side.
		On output, the solution vector.
	 */
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
		}
	}   
}
