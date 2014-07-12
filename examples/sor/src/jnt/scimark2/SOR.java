package jnt.scimark2;

public class SOR
{

	public static final void execute( double omega,
			double[][] G,
			int num_iterations)
	{
		int M = G.length;
		int N = G[0].length;

		double omega_over_four = omega * 0.25; // approx: mul, move to omega.. 	// approx: 6: MUL_D T29, R0, DConst: 0.25	// approx: 7: MOVE_D R9, T29
		double one_minus_omega = 1.0 - omega; // approx: sub, move to one_..	// approx: 8: SUB_D T30, DConst: 1.0, R0	// approx: 9: MOVE_D R10, T30

		// update interior points
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
					Gi[j] = omega_over_four * (Gim1[j] + Gip1[j] + Gi[j-1]  // approx: 4 adds, 2 muls, 5 aloads, 1 astore	// approx: 45: ADD_D T56, T53, T55	// approx: 44: MUL_D T55, R10, T54	// approx: 46: ASTORE_D T56, R15, R43	// approx: 40: ALOAD_D T51, R15, T50	// approx: 41: ADD_D T52, T49, T51	// approx: 42: MUL_D T53, R9, T52	// approx: 43: ALOAD_D T54, R15, R43	// approx: 37: ALOAD_D T48, R15, T47	// approx: 38: ADD_D T49, T46, T48	// approx: 33: ALOAD_D T44, R17, R43	// approx: 34: ALOAD_D T45, R18, R43	// approx: 35: ADD_D T46, T44, T45
							+ Gi[j+1]) + one_minus_omega * Gi[j];
			}
		}
	}
}

