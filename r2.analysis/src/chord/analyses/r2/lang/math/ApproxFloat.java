package chord.analyses.r2.lang.math;

import chord.analyses.r2.lang.Relax;

public class ApproxFloat {
	public static int floatToIntBits(float x) {
		x = Relax.relax(x);
		return Float.floatToIntBits(x);
	}

	public static int compare(float f1, float f2) {
		f1 = Relax.relax(f1);
		f2 = Relax.relax(f2);
		return Float.compare(f1, f2);
	}

	public static boolean isNaN(float f) {
		f = Relax.relax(f);
		return Float.isNaN(f);
	}

	public static boolean isInfinite(float f) {
		f = Relax.relax(f);
		return Float.isInfinite(f);
	}
}
