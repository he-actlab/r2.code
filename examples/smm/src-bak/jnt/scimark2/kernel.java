package jnt.scimark2;

public class kernel
{

	public static double measureSparseMatmult(int N, int nz, double min_time, Random R)
	{
		double x[] = RandomVector(N, R);
		alloc_TAG1();
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

		y = accept_all_FIELD1_TAG1(y);

		System.out.print("SparseMatMult vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((y[i]) + " ");
		}
		System.out.println("");

		y = precise_all_FIELD1_TAG1(y);

		return 0.0;
	}

	private static  double[] RandomVector(int N, Random R) {
		double A[] = new  double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

	private static void alloc_TAG1(){}
	private static double[] accept_all_FIELD1_TAG1(double[] d){return d;}
	private static double[] precise_all_FIELD1_TAG1(double[] d){return d;}



}
