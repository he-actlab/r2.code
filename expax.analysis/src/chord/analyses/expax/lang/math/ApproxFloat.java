package chord.analyses.expax.lang.math;

import chord.analyses.expax.lang.Accept;

public class ApproxFloat {
	public static int floatToIntBits(float x) {
		x = Accept.accept(x);
		return Float.floatToIntBits(x);
	}

	public static int compare(float f1, float f2) {
		f1 = Accept.accept(f1);
		f2 = Accept.accept(f2);
		return Float.compare(f1, f2);
	}

	public static boolean isNaN(float f) {
		f = Accept.accept(f);
		return Float.isNaN(f);
	}

	public static boolean isInfinite(float f) {
		f = Accept.accept(f);
		return Float.isInfinite(f);
	}
}
