package jnt.scimark2;

import chord.analyses.r2.lang.*;

public class kernel
{

	public static double measureMonteCarlo(double min_time, String seed)
	{
		int cycles=1492;
		double out = 0.0; 
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); 

		out = Relax.relax(out);

		System.out.println("MonteCarlo out: " + (out));
		
		return 0.0;
	}

}
