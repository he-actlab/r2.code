package jnt.scimark2;

public class kernel
{

	public static double measureMonteCarlo(double min_time, Random R, String seed)
	{
		int cycles=1492;
		double out = 0.0; 	// st: 2: MOVE_D R11, DConst: 0.0
		int SEED = Integer.parseInt(seed);
		out = MonteCarlo.integrate(cycles, SEED); 	// st: 6: MOVE_D R13, T12

		out = accept(out);

		System.out.println("MonteCarlo out: " + (out));

		out = precise(out);	// st: 19: MOVE_D R23, T22

		return 0.0;
	}

	private static double accept(double d){
		return d;
	}

	private static double precise(double d){
		return d;
	}

}
