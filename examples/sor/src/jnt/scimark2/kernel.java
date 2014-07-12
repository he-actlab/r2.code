package jnt.scimark2;

public class kernel
{

	public static double measureSOR(int N, double min_time, Random R)
	{
		double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); 	// approx: 5: MOVE_D T6, DConst: 1.25

		G[0] = accept_all_FIELD1_TAG1(G[0]);

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				System.out.print((G[i][j]) + " ");	// approx: 32: ALOAD_D T11, T25, R21
			}
		}
		System.out.println("");

		G[0] = precise_all_FIELD1_TAG1(G[0]);

		return 0.0;
	}

	private static  double[][] RandomMatrix(int M, int N, Random R)
	{
		double A[][] = new double[M][];
		for (int i=0; i<M; i++) {
			alloc_TAG1();
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();	// approx: 14: ASTORE_D T8, T18, R17
		return A;
	}

	private static void alloc_TAG1(){}
	private static double[] accept_all_FIELD1_TAG1(double[] d){return d;}
	private static double[] precise_all_FIELD1_TAG1(double[] d){return d;}

}
