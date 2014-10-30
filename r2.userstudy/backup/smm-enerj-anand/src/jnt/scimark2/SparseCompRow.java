package jnt.scimark2;

/**
 Evaluation for EnerJ framework
*/

import enerj.lang.*;

public class SparseCompRow
{
	public static void matmult(@Approx double y[],  @Approx double val[], int row[], 
								int col[],  @Approx double x[], int NUM_ITERATIONS) {
		int M = row.length - 1;

		for (int reps=0; reps<NUM_ITERATIONS; reps++)
		{

			for (int r=0; r<M; r++)
			{
				double sum = 0.0;	
				int rowR = row[r];
				int rowRp1 = row[r+1];
				for (int i=rowR; i<rowRp1; i++)
					sum += x[ col[i] ] * val[i]; 
				y[r] = sum; 
			}
		}
	}

	private static @Approx double[] RandomVector(int N, Random R) {
		@Approx double A[] = new double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

	public static void main(String[] args){
		Random R = new Random(Integer.parseInt(args[0]));
	
		int N = Integer.parseInt(args[1]);
		int nz = Integer.parseInt(args[2]);

		@Approx double x[] = RandomVector(N, R);
		@Approx double y[] = new double[N];

	 int nr = nz/N; 	
	 int anz = nr *N; 

		@Approx double val[] = RandomVector(anz, R);
		int col[] = new int[Endorsements.endorse(anz)];
		int row[] = new int[Endorsements.endorse(N+1)];

		row[0] = 0;	
		for (int r=0; r<N; r++)
		{
			int rowr = row[r];
			row[r+1] = rowr + nr;
			int step = r/ nr;
			if (Endorsements.endorse(step) < 1) 
				step = 1;   

			for (int i=0; i<nr; i++)
				col[rowr+i] = i*step;
		}

		SparseCompRow.matmult(y, val, row, col, x, 100);

		System.out.print("SparseMatMult vector: ");
		for (int i = 0; i < N; ++i) {
			@Approx double y_i = y[i];
			System.out.print(Endorsements.endorse(y_i) + " ");
		}
		System.out.println("");
	}

}
