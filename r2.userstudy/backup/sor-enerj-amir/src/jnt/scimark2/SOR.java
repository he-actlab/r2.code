package jnt.scimark2;

/**
 Evaluation for EnerJ framework
*/

import enerj.lang.*;

public class SOR
{
	public static final void execute(double omega,
			@Approx double[][] G,
			int num_iterations) {
		@Approx int M = G.length;
		@Approx int N = G[0].length;

		@Approx double omega_over_four = omega * 0.25; 
		@Approx double one_minus_omega = 1.0 - omega; 

		int Mm1 = Endorsements.endorse(M)-1;
		int Nm1 = Endorsements.endorse(N)-1; 
		for (int p=0; p<num_iterations; p++)
		{
			for (int i=1; i<Mm1; i++)
			{
				@Approx double[] Gi = G[i];
				@Approx double[] Gim1 = G[i-1];
				@Approx double[] Gip1 = G[i+1];
				for (int j=1; j<Nm1; j++)
					Gi[j] = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j-1]  
							+ Gi[j+1]) + one_minus_omega * Gi[j];
			}
		}
	}

	private static @Approx double[][] RandomMatrix(int M, int N, Random R) {
		@Approx double A[][] = new double[M][];
		for (int i=0; i<M; i++) {
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}

	public static void main(String[] args) {
		Random R = new Random(Integer.parseInt(args[0]));

		@Approx double G[][] = RandomMatrix(10, 10, R);

		SOR.execute(1.25, G, 100); 

		System.out.print("SOR values: ");
		for (int i = 0; i < 10; ++i) {
			for (int j = 0; j < 10; ++j) {
				@Approx double g_i_j = G[i][j];

				System.out.print(Endorsements.endorse(g_i_j) + " ");
			}
		}
		System.out.println("");
	}
}

