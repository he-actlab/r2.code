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
				double sum = 0.0;	// approx: move to sum 
				int rowR = row[r];
				int rowRp1 = row[r+1];
				for (int i=rowR; i<rowRp1; i++)
					sum += x[ col[i] ] * val[i]; // approx: 1 add, 1 mul, 2 aloads 
				y[r] = sum; // approx: astore to y[r] 
			}
		}
	}

}
