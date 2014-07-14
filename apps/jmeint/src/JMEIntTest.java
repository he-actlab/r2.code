import com.jme.intersection.Intersection;
import com.jme.math.Vector3f;

import java.util.Random;


public class JMEIntTest {
	public static Random rand;
	public static  Vector3f randvec() {
		return new  Vector3f(
				rand.nextFloat(), rand.nextFloat(), rand.nextFloat()
				);
	}
	public static void main(String[] argv) {

		// Use a constant seed so we operate deterministically.
		rand = new Random(Integer.parseInt(argv[0])); 

		for (int i = 0; i <= 100; ++i) {
			boolean isec = Intersection.intersection(	// approx: 21: MOVE_I R21, T20
					randvec(), randvec(), randvec(),
					randvec(), randvec(), randvec()
					);
			isec = accept(isec);
			if ((isec)) {
				System.out.print("1 ");
			} else {
				System.out.print("0 ");
			}
			isec = precise(isec);	// approx: 33: MOVE_I R29, T28
		}

		System.out.println("");

	}

	public static boolean accept(boolean i){return i;}
	public static boolean precise(boolean i){return i;}

}
