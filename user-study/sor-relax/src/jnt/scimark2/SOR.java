package jnt.scimark2;

/**
 Evaluation for Relax framework
*/

import chord.analyses.expax.lang.*;
import chord.analyses.expax.lang.math.*;

public class SOR
{
	public static final void execute( double omega,
			double[][] G,
			int num_iterations) {
		int M = G.length;
		int N = G[0].length;

		double omega_over_four = omega * 0.25; 
		double one_minus_omega = 1.0 - omega; 

		int Mm1 = M-1;
		int Nm1 = N-1; 
		for (int p=0; p<num_iterations; p++)
		{
			for (int i=1; i<Mm1; i++)
			{
				double[] Gi = G[i];
				double[] Gim1 = G[i-1];
				double[] Gip1 = G[i+1];
				for (int j=1; j<Nm1; j++)
					Gi[j] = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j-1]  
							+ Gi[j+1]) + one_minus_omega * Gi[j];
			}
		}
	}

	private static double[][] RandomMatrix(int M, int N, Random R) {
		double A[][] = new double[M][];
		for (int i=0; i<M; i++) {
			A[i] = new double[N];
		}	

		for (int i=0; i<N; i++)
			for (int j=0; j<N; j++)
				A[i][j] = R.nextDouble();
		return A;
	}

	public static void main(String[] args) {
		int N = 10;
		Random R = new Random(Integer.parseInt(args[0]));

		double G[][] = RandomMatrix(N, N, R);

		int cycles=100;
		SOR.execute(1.25, G, cycles); 

		System.out.print("SOR values: ");
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				System.out.print((G[i][j]) + " ");
			}
		}
		System.out.println("");
	}
}
