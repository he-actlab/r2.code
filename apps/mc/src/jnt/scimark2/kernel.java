package jnt.scimark2;

import chord.analyses.expax.lang.*;

public class kernel
{

	public static double measureMonteCarlo(double min_time, String seed)
	{
		int cycles=1492;
		double out = 0.0; 
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); 

		out = Accept.accept(out);

		System.out.println("MonteCarlo out: " + (out));

		out = Precise.precise(out);
		
		return 0.0;
	}

}
