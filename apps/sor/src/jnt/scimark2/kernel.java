package jnt.scimark2;

public class kernel
{

	public static double measureSOR(int N, Random R)
	{
		double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); 

		G[0] = accept_all_FIELD1_TAG1(G[0]);

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				System.out.print((G[i][j]) + " ");
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
				A[i][j] = R.nextDouble();
		return A;
	}

	private static void alloc_TAG1(){}
	private static double[] accept_all_FIELD1_TAG1(double[] d){return d;}
	private static double[] precise_all_FIELD1_TAG1(double[] d){return d;}

}
