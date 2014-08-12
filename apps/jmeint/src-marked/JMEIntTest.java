import chord.analyses.expax.lang.Accept;

import com.jme.intersection.Intersection;
import com.jme.math.Vector3f;

import java.util.Random;


public class JMEIntTest {
	public static Random rand;
	public static Vector3f randvec() {
		Vector3f vec = new Vector3f(	// st: 1: NEW T0, com.jme.math.Vector3f	// st: 1: NEW T0, com.jme.math.Vector3f	// st: 1: NEW T0, com.jme.math.Vector3f	// st: 1: NEW T0, com.jme.math.Vector3f
				rand.nextFloat(), rand.nextFloat(), rand.nextFloat()
				);
		return vec;
	}
	public static void main(String[] argv) {

		// Use a constant seed so we operate deterministically.
		rand = new Random(Integer.parseInt(argv[0])); 	// op: 5: INT_2LONG T5, T4

		for (int i = 0; i <= 100; ++i) {
			boolean isec = Intersection.intersection(	// op: 45: MOVE_I R22, T21
					randvec(), randvec(), randvec(),
					randvec(), randvec(), randvec()
					);
			isec = Accept.accept(isec);	// op: 46: MOVE_I R23, R22
			if (isec) {
				System.out.print("1 ");
			} else {
				System.out.print("0 ");
			}
		}
		System.out.println("");
	}

}
