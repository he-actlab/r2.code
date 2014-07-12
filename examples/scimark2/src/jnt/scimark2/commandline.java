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
		double min_time = Constants.RESOLUTION_DEFAULT;

		int FFT_size = Constants.FFT_SIZE;
		int SOR_size =  Constants.SOR_SIZE;
		int Sparse_size_M = Constants.SPARSE_SIZE_M;
		int Sparse_size_nz = Constants.SPARSE_SIZE_nz;
		int LU_size = Constants.LU_SIZE;

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
			if (args[current_arg].equalsIgnoreCase("-large"))
			{
				FFT_size = Constants.LG_FFT_SIZE;
				SOR_size =  Constants.LG_SOR_SIZE;
				Sparse_size_M = Constants.LG_SPARSE_SIZE_M;
				Sparse_size_nz = Constants.LG_SPARSE_SIZE_nz;
				LU_size = Constants.LG_LU_SIZE;

				current_arg++;
			} else if (args[current_arg].equalsIgnoreCase("-tiny"))
			{
				FFT_size = Constants.TINY_FFT_SIZE;
				SOR_size =  Constants.TINY_SOR_SIZE;
				Sparse_size_M = Constants.TINY_SPARSE_SIZE_M;
				Sparse_size_nz = Constants.TINY_SPARSE_SIZE_nz;
				LU_size = Constants.TINY_LU_SIZE;

				current_arg++;
			}

			if (args.length > current_arg) {
				benchSel = args[current_arg++];
			}

			if (args.length > current_arg) {
				seed = args[current_arg];
			}
		}

		// run the benchmark
		Random R = new Random(Integer.parseInt(seed));
		/*
		if (benchSel == null || benchSel.equals("fft"))
			kernel.measureFFT( FFT_size, min_time, R);
		*/
		/*
		if (benchSel == null || benchSel.equals("sor"))
			kernel.measureSOR( SOR_size, min_time, R);
		*/		
		/*
		if (benchSel == null || benchSel.equals("mc"))
			kernel.measureMonteCarlo(min_time, R, seed);
		*/
		/*
		if (benchSel == null || benchSel.equals("smm"))
			kernel.measureSparseMatmult( Sparse_size_M, Sparse_size_nz, min_time, R);
		*/
		
		if (benchSel == null || benchSel.equals("lu"))
			kernel.measureLU( LU_size, min_time, R);
		
	}

}
