package jnt.scimark2;

/**
 Estimate Pi by approximating the area of a circle.

 How: generate N random numbers in the unit square, (0,0) to (1,1)
 and see how are within a radius of 1 or less, i.e.
 <pre>  

 sqrt(x^2 + y^2) < r

 </pre>
  since the radius is 1.0, we can square both sides
  and avoid a sqrt() computation:
  <pre>

    x^2 + y^2 <= 1.0

  </pre>
  this area under the curve is (Pi * r^2)/ 4.0,
  and the area of the unit of square is 1.0,
  so Pi can be approximated by 
  <pre>
		        # points with x^2+y^2 < 1
     Pi =~ 		--------------------------  * 4.0
		             total # points

  </pre>

*/

public class MonteCarlo
{

	public static final  double integrate(int Num_samples, int SEED)
	{
		Random R = new Random(SEED);	// op: 1: NEW T2, jnt.scimark2.Random

		int under_curve = 0; 	// st: 5: MOVE_I R13, IConst: 0
		for (int count=0; count<Num_samples; count++)
		{
			double x= R.nextDouble(); 	// st: 14: MOVE_D R18, T17
			double y= R.nextDouble();		// st: 16: MOVE_D R20, T19

			// additional accept
			x = accept(x);
			y = accept(y);

			if ((x*x + y*y <= 1.0))
				under_curve ++; 	// st: 26: ADD_I R28, R16, IConst: 1
		}

		return ((double) under_curve / Num_samples) * 4.0; 	// st: 9: INT_2DOUBLE T32, R0	// st: 8: INT_2DOUBLE T31, R16	// st: 11: MUL_D T34, T33, DConst: 4.0	// st: 10: DIV_D T33, T31, T32
	}

	//jspark
	public static double accept(double x){return x;}
	//krapsj

}
