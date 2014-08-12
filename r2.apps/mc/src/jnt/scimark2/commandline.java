package jnt.scimark2;

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
		double min_time = Constants.RESOLUTION_DEFAULT;

		String seed = null;

		if (args.length != 1)
			throw new RuntimeException("Error! Seed should be provided as an argument");
		seed = args[0];

		// run the benchmark
		kernel.measureMonteCarlo(min_time, seed);
	}

}
