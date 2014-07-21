import com.jme.intersection.Intersection;
import com.jme.math.Vector3f;

import java.util.Random;


public class JMEIntTest {
	public static Random rand;
	public static  Vector3f randvec() {
//		alloc_TAG5();
		Vector3f vec = new Vector3f(
				rand.nextFloat(), rand.nextFloat(), rand.nextFloat()
				);
//		accept_all_FIELD1_TAG5(vec);
//		accept_all_FIELD2_TAG5(vec);
//		accept_all_FIELD3_TAG5(vec);
		return vec;
	}
	private static void alloc_TAG5() {}
	private static void accept_all_FIELD1_TAG5(Vector3f vec){}
	private static void accept_all_FIELD2_TAG5(Vector3f vec){}
	private static void accept_all_FIELD3_TAG5(Vector3f vec){}
	public static void main(String[] argv) {

		// Use a constant seed so we operate deterministically.
		rand = new Random(Integer.parseInt(argv[0])); 

		for (int i = 0; i <= 100; ++i) {
			boolean isec = Intersection.intersection(
					randvec(), randvec(), randvec(),
					randvec(), randvec(), randvec()
					);
			isec = accept(isec);
			if (isec) {
				System.out.print("1 ");
			} else {
				System.out.print("0 ");
			}
		}
		System.out.println("");
	}

	public static boolean accept(boolean i){return i;}

}
