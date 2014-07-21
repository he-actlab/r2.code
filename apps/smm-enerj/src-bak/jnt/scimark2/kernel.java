package jnt.scimark2;
import enerj.lang.*;

public class kernel
{

	public static double measureSparseMatmult(int N, int nz, double min_time, Random R)
	{
		@Approx double x[] = RandomVector(N, R);
		@Approx double y[] = new @Approx double[N];

		int nr = nz/N; 		// average number of nonzeros per row
		int anz = nr *N;   // _actual_ number of nonzeros


		@Approx double val[] = RandomVector(anz, R);
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

		System.out.print("SparseMatMult vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print(Endorsements.endorse(y[i]) + " ");
		}
		System.out.println("");

		return 0.0;
	}

	private static @Approx double[] RandomVector(int N, Random R) {
		@Approx double A[] = new @Approx double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

}
