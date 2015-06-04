package jnt.scimark2;
import enerj.lang.*;

public class kernel
{

	public static double measureMonteCarlo(double min_time, String seed)
	{
		int cycles=1492;
		@Approx double out = 0.0; 
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); 

		System.out.println("MonteCarlo out: " + Endorsements.endorse(out));

		return 0.0;
	}

}
