package chord.analyses.r2.lang.math;

import chord.analyses.r2.lang.Relax;

public class ApproxMath {
	public static float abs(float f){
		f = Relax.relax(f);
		return Math.abs(f); 
	}
	
	public static double abs(double d){
		d = Relax.relax(d);
		return Math.abs(d); 
	}
	
	public static int max(int lhs, int rhs){
		lhs = Relax.relax(lhs);
		rhs = Relax.relax(rhs);
		return Math.max(lhs, rhs);
	}
	
	public static int min(int lhs, int rhs){
		lhs = Relax.relax(lhs);
		rhs = Relax.relax(rhs);
		return Math.min(lhs, rhs);
	}
	
	public static boolean isNaN(float f){
		f = Relax.relax(f);
		return Float.isNaN(f);
	}
	
	public static double sqrt(double d){
		d = Relax.relax(d);
		return Math.sqrt(d);
	}
}
