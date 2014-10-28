package jnt.scimark2;

/**
  Evaluation for R2 Framework
*/

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.*;

public class MonteCarlo
{

	public static final double integrate(int Num_samples, int SEED)
	{
		Random R = new Random(SEED);

		int under_curve = 0; 
		for (int count=0; count<Num_samples; count++)
		{
			double x= R.nextDouble(); 
			double y= R.nextDouble();	

			double sum = x*x + y*y; 

			if (sum <= 1.0)
				under_curve ++; 
		}
		
		return ((double) under_curve / Num_samples) * 4.0; 
	}

	public static void main(String args[])
	{
		int SEED = Integer.parseInt(args[0]);
		double out = MonteCarlo.integrate(1492, SEED); 

		System.out.println("MonteCarlo out: " + out);

		Restrict.restrict(out);
	}

}
