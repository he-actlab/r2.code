package jnt.scimark2;

import chord.analyses.expax.lang.*;

public class kernel
{

	public static double measureSparseMatmult(int N, int nz, double min_time, Random R)
	{
		double x[] = RandomVector(N, R);
		Alloc.alloc_TAG1();
		double y[] = new double[N];

		int nr = nz/N; 	
		int anz = nr *N; 

		double val[] = RandomVector(anz, R);
		int col[] = new int[anz];
		int row[] = new int[N+1];

		row[0] = 0;	
		for (int r=0; r<N; r++)
		{
			int rowr = row[r];
			row[r+1] = rowr + nr;
			int step = r/ nr;
			if (step < 1) 
				step = 1;   

			for (int i=0; i<nr; i++)
				col[rowr+i] = i*step;
		}

		int cycles=100;
		SparseCompRow.matmult(y, val, row, col, x, cycles);

		System.out.print("SparseMatMult vector: ");
		for (int i = 0; i < N; ++i) {
			y = Accept.accept_all_FIELD1_TAG1(y);
			System.out.print((y[i]) + " ");
		}
		System.out.println("");

		return 0.0;
	}

	private static double[] RandomVector(int N, Random R) {
		double A[] = new double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

}