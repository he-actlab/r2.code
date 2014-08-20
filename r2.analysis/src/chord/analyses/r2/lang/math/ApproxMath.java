package chord.analyses.r2.lang.math;

import chord.analyses.r2.lang.Relax;

public class ApproxMath {
	public static int abs(int i){
		i = Relax.relax(i);
		return Math.abs(i); 
	}
	
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

	public static double sin(double d) {	
		d = Relax.relax(d);
		return Math.sin(d);
	}
	
	public static float sqrt(float f){
		f = Relax.relax(f);
		return (float)Math.sqrt((double)f);
	}

	public static float acos(float f) {
		f = Relax.relax(f);
		return (float)Math.acos((double)f);
	}

	public static float atan2(float f1, float f2) {
		f1 = Relax.relax(f1);
		f2 = Relax.relax(f2);
		return (float)Math.atan2((double)f1, (double)f2);
	}

	public static float sin(float f) {
		f = Relax.relax(f);
		return (float)Math.sin((double)f);
	}
	
	public static float cos(float f) {
		f = Relax.relax(f);
		return (float)Math.cos((double)f);
	}

	public static int round(float x) {
		x = Relax.relax(x);
		return Math.round(x);
	}

	public static float min(float f1, float f2) {
		f1 = Relax.relax(f1);
		f2 = Relax.relax(f2);
		return Math.min(f1, f2);
	}
}
