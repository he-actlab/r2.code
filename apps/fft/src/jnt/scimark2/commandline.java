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
		double min_time = Constants.RESOLUTION_DEFAULT; // approx: move to min_time

		int FFT_size = Constants.TINY_FFT_SIZE;

		String benchSel = null;
		String seed = null;

		if (args.length != 1)
			throw new RuntimeException("Error! Seed should be provided as an argument");

		seed = args[0];

		// run the benchmark
		Random R = new Random(Integer.parseInt(seed));
		kernel.measureFFT( FFT_size, min_time, R);
				
	}

}
