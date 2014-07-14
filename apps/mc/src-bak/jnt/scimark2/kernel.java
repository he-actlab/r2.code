package jnt.scimark2;

public class kernel
{

	public static double measureMonteCarlo(double min_time, String seed)
	{
		int cycles=1492;
		double out = 0.0; 
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); 

		out = precise(out);

		System.out.println("MonteCarlo out: " + (out));

		return 0.0;
	}

	private static double accept(double d){
		return d;
	}

	private static double precise(double d){
		return d;
	}

}