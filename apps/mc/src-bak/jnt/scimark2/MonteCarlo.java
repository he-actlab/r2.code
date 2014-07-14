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

		int under_curve = 0; 
		for (int count=0; count<Num_samples; count++)
		{
			double x= R.nextDouble(); 
			double y= R.nextDouble();	

			double sum = x*x + y*y; 
			
			// additional accept
			sum = accept(sum);

			if (sum <= 1.0)
				under_curve ++; 
		}
		
		precise_all_FIELD2_TAG(R);
		precise_all_FIELD3_TAG(R);
		precise_all_FIELD4_TAG(R);
		precise_all_FIELD1_TAG1(R);
		precise_all_FIELD6_TAG(R);
		precise_all_FIELD7_TAG(R);
		precise_all_FIELD8_TAG(R);
		precise_all_FIELD9_TAG(R);
		precise_all_FIELD10_TAG(R);
		precise_all_FIELD11_TAG(R);
		precise_all_FIELD12_TAG(R);

		return ((double) under_curve / Num_samples) * 4.0; 
	}

	public static void precise_all_FIELD2_TAG(Random R){}
	public static void precise_all_FIELD3_TAG(Random R){}
	public static void precise_all_FIELD4_TAG(Random R){}
	public static void precise_all_FIELD1_TAG1(Random R){}
	public static void precise_all_FIELD6_TAG(Random R){}
	public static void precise_all_FIELD7_TAG(Random R){}
	public static void precise_all_FIELD8_TAG(Random R){}
	public static void precise_all_FIELD9_TAG(Random R){}
	public static void precise_all_FIELD10_TAG(Random R){}
	public static void precise_all_FIELD11_TAG(Random R){}
	public static void precise_all_FIELD12_TAG(Random R){}
	public static double accept(double i){return i;}
}
