package jnt.scimark2;

public class SparseCompRow
{
	/* multiple iterations used to make kernel have roughly
		 same granulairty as other Scimark kernels. */

	/* computes  a matrix-vector multiply with a sparse matrix
		 held in compress-row format.  If the size of the matrix
		 in MxN with nz nonzeros, then the val[] is the nz nonzeros,
		 with its ith entry in column col[i].  The integer vector row[]
		 is of size M+1 and row[i] points to the begining of the
		 ith row in col[].  
	 */

	public static void matmult(  double y[],  double val[], int row[],
			int col[],  double x[], int NUM_ITERATIONS)
	{
		int M = row.length - 1;

		for (int reps=0; reps<NUM_ITERATIONS; reps++)
		{

			for (int r=0; r<M; r++)
			{
				double sum = 0.0;		// approx: 11: MOVE_D R25, DConst: 0.0
				int rowR = row[r];
				int rowRp1 = row[r+1];
				for (int i=rowR; i<rowRp1; i++)
					sum += x[ col[i] ] * val[i]; 	// approx: 26: ADD_D T18, R30, T32	// approx: 27: MOVE_D R33, T18	// approx: 25: MUL_D T32, T31, T17	// approx: 24: ALOAD_D T17, R1, R29	// approx: 23: ALOAD_D T31, R4, T15
				y[r] = sum; 	// approx: 19: ASTORE_D R30, R0, R24
			}
		}
	}

}
