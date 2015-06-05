package jnt.scimark2;

/**
 Evaluation for FlexJava framework
*/

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.*;

public class SparseCompRow
{
	public static void matmult(double y[],  double val[], int row[], 
								int col[],  double x[], int NUM_ITERATIONS) {
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
				Loosen.loosen(y[r]);
			}
		}
	}

	private static double[] RandomVector(int N, Random R) {
		double A[] = new double[N];

		for (int i=0; i<N; i++) {
			A[i] = R.nextDouble(); 
			Loosen.loosen(A[i]);
		}
		return A;
	}

	public static void main(String[] args) {
		Random R = new Random(Integer.parseInt(args[0]));
	
		double min_time = 2.0;
		int N = 10;
		int nz = 50;

		double x[] = RandomVector(N, R);
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
			double y_i = y[i];

			Loosen.loosen(y_i);

			System.out.print(y_i + " ");

			Tighten.tighten(y_i);
		}
		System.out.println("");
	}

}
