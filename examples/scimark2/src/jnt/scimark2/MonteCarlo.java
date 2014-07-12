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
		Random R = new Random(SEED);

		int under_curve = 0; // approx: move to under_curve
		for (int count=0; count<Num_samples; count++)
		{
			double x= R.nextDouble(); // approx: move to x
			double y= R.nextDouble();	// approx: move to y

			// additional accept
			x = accept(x);
			y = accept(y);

			if ((x*x + y*y <= 1.0))
				under_curve ++; // approx: add
		}

		return ((double) under_curve / Num_samples) * 4.0; // approx: div, mul
	}

	//jspark
	public static double accept(double x){
		return x;
	}
	//krapsj

}
