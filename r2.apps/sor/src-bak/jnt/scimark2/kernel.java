package jnt.scimark2;

import chord.analyses.expax.lang.*;

public class kernel
{

	public static double measureSOR(int N, Random R)
	{
		double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); 

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				G[0] = Accept.accept_all_FIELD1_TAG1(G[0]);
				System.out.print((G[i][j]) + " ");
				G[0] = Precise.precise_all_FIELD1_TAG1(G[0]);
			}
		}
		System.out.println("");

		return 0.0;
	}

	private static  double[][] RandomMatrix(int M, int N, Random R)
	{
		double A[][] = new double[M][];
		for (int i=0; i<M; i++) {
			Alloc.alloc_TAG1();
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}

}