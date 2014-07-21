package chord.analyses.expax.lang.math;

import chord.analyses.expax.lang.Accept;

public class ApproxMath {
	public static float abs(float f){
		f = Accept.accept(f);
		return Math.abs(f); 
	}
	
	public static double abs(double d){
		d = Accept.accept(d);
		return Math.abs(d); 
	}
	
	public static int max(int lhs, int rhs){
		lhs = Accept.accept(lhs);
		rhs = Accept.accept(rhs);
		return Math.max(lhs, rhs);
	}
	
	public static int min(int lhs, int rhs){
		lhs = Accept.accept(lhs);
		rhs = Accept.accept(rhs);
		return Math.min(lhs, rhs);
	}
	
	public static boolean isNaN(float f){
		f = Accept.accept(f);
		return Float.isNaN(f);
	}
	
	public static double sqrt(double d){
		d = Accept.accept(d);
		return Math.sqrt(d);
	}
}
