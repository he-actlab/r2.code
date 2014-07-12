package jnt.scimark2;

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

			double t = absolute(A[j][j]);	// approx: 13: ALOAD_D T36, T35, R33
			for (int i=j+1; i<M; i++)
			{
				double ab = absolute(A[i][j]);	// approx: 81: ALOAD_D T45, T44, R33
				if ((ab > t))
				{
					jp = i;
					t = ab;
				}
			}

			pivot[j] = jp;

			//additional accept
			A[0] = accept_all_FIELD4_TAG3(A[0]);
			if (( A[jp][j] == 0 ))                 
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
				double recp =  1.0 / A[j][j];	// approx: 38: ALOAD_D T64, T63, R33	// approx: 40: MOVE_D R16, T65	// approx: 39: DIV_D T65, DConst: 1.0, T64

				for (int k=j+1; k<M; k++)
					A[k][j] *= recp;	// approx: 75: MUL_D T73, T72, R16	// approx: 76: ASTORE_D T73, T71, T70	// approx: 74: ALOAD_D T72, T69, R33
			}

			if (j < minMN-1)
			{
				for (int ii=j+1; ii<M; ii++)
				{
					double Aii[] = A[ii];
					double Aj[] = A[j];
					double AiiJ = Aii[j];	// approx: 56: MOVE_D R21, T81	// approx: 55: ALOAD_D T81, R18, R33
					for (int jj=j+1; jj<N; jj++)
						Aii[jj] -= AiiJ * Aj[jj];	// approx: 67: SUB_D T88, T87, T26	// approx: 66: MUL_D T26, R21, T25	// approx: 68: ASTORE_D T88, T86, T85	// approx: 65: ALOAD_D T25, R19, R84	// approx: 64: ALOAD_D T87, R18, R84
				}
			}
		}

		return 0;
	}

	public static  double absolute( double num) {
		double pnum = (num);	// approx: 1: MOVE_D R3, R0
		//additional accept
		pnum = accept(pnum);
		return Math.abs(pnum);
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
			double sum = b[ip];	// approx: 34: ALOAD_D T27, R2, R16	// approx: 35: MOVE_D R28, T27

			// additional accept
			sum = accept(sum);
			b[ip] = b[i];	// approx: 39: ALOAD_D T32, R2, R24	// approx: 40: ASTORE_D T32, T31, R16
			if (ii==0)
				for (int j=ii; j<i; j++) {
					sum -= LU[i][j] * b[j];	// approx: 55: SUB_D T40, R35, T39	// approx: 56: MOVE_D R41, T40	// approx: 53: ALOAD_D T38, R2, R34	// approx: 54: MUL_D T39, T37, T38	// approx: 52: ALOAD_D T37, T36, R34
				}
			else 
				if ((sum == 0.0))
					ii = i;
			b[i] = sum;	// approx: 48: ASTORE_D R45, R2, R24
		}

		for (int i=N-1; i>=0; i--)
		{
			double sum = b[i]; 	// approx: 14: MOVE_D R52, T51	// approx: 13: ALOAD_D T51, R2, R50
			for (int j=i+1; j<N; j++)
				sum -= LU[i][j] * b[j]; 	// approx: 27: MUL_D T60, T58, T59	// approx: 28: SUB_D T61, R56, T60	// approx: 29: MOVE_D R62, T61	// approx: 26: ALOAD_D T59, R2, R55	// approx: 25: ALOAD_D T58, T57, R55
			b[i] = sum / LU[i][i]; 	// approx: 21: ASTORE_D T65, R2, R50	// approx: 20: DIV_D T65, R56, T64	// approx: 19: ALOAD_D T64, T12, R50
		}
	}   
	private static double accept(double d){return d;}
	private static double[] accept_all_FIELD4_TAG3(double[] d){return d;}
}
