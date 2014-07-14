package jnt.scimark2;

import java.util.Properties;

/**
SciMark2: A Java numerical benchmark measuring performance
of computational kernels for FFTs, Monte Carlo simulation,
sparse matrix computations, Jacobi SOR, and dense LU matrix
factorizations.  
 */

public class commandline
{

	public static void main(String args[])
	{
		double min_time = Constants.RESOLUTION_DEFAULT;	// st: 1: MOVE_D R1, DConst: 2.0

		String benchSel = null;
		String seed = null;

		if (args.length > 0)
		{

			if (args[0].equalsIgnoreCase("-h") || 
					args[0].equalsIgnoreCase("-help"))
			{
				System.out.println("Usage: [-large] [minimum_time]");
				return;
			}

			int current_arg = 0; 
			if (args.length > current_arg) {
				benchSel = args[current_arg++];
			}

			if (args.length > current_arg) {
				seed = args[current_arg];
			}
		}

		// run the benchmark
		Random R = new Random(Integer.parseInt(seed));	// op: 25: NEW T36, jnt.scimark2.Random
		if (benchSel == null || benchSel.equals("mc"))
			kernel.measureMonteCarlo(min_time, R, seed);
	}

}
