package chord.analyses.r2.lang.math;

import chord.analyses.r2.lang.Loosen;

public class ApproxFloat {
	public static int floatToIntBits(float x) {
		x = Loosen.loosen(x);
		return Float.floatToIntBits(x);
	}

	public static int compare(float f1, float f2) {
		f1 = Loosen.loosen(f1);
		f2 = Loosen.loosen(f2);
		return Float.compare(f1, f2);
	}

	public static boolean isNaN(float f) {
		f = Loosen.loosen(f);
		return Float.isNaN(f);
	}

	public static boolean isInfinite(float f) {
		f = Loosen.loosen(f);
		return Float.isInfinite(f);
	}
}
