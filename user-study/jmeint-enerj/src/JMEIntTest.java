
/**
 Evaluation for Relax framework
*/

import enerj.lang.*;

import com.jme.intersection.Intersection;
import com.jme.math.Vector3f;

import java.util.Random;


public class JMEIntTest {
	public static Random rand;
	public static Vector3f randvec() {
		Vector3f vec = new Vector3f(
				rand.nextFloat(), rand.nextFloat(), rand.nextFloat()
				);
		return vec;
	}
	public static void main(String[] argv) {

		rand = new Random(Integer.parseInt(argv[0])); 

		for (int i = 0; i <= 100; ++i) {
			boolean isec = Intersection.intersection(
					randvec(), randvec(), randvec(),
					randvec(), randvec(), randvec()
					);
			if (isec) {
				System.out.print("1 ");
			} else {
				System.out.print("0 ");
			}
		}
		System.out.println("");
	}

}
