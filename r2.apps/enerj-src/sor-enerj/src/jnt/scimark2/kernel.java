package jnt.scimark2;
import enerj.lang.*;

public class kernel
{

	public static double measureSOR(int N, double min_time, Random R)
	{
		@Approx double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); 

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				System.out.print(Endorsements.endorse(G[i][j]) + " ");
			}
		}
		System.out.println("");

		return 0.0;
	}

	private static @Approx double[][] RandomMatrix(int M, int N, Random R)
	{
		@Approx double A[][] = new @Approx double[M][];
		for (int i=0; i<M; i++) {
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}
}
