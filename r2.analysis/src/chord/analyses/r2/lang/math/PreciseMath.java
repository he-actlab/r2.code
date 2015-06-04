package chord.analyses.r2.lang.math;

import chord.analyses.r2.lang.Tighten;

public class PreciseMath {
	public static int abs(int i){
		i = Tighten.tighten(i);
		return java.lang.Math.abs(i); 
	}
	
	public static float abs(float f){
		f = Tighten.tighten(f);
		return java.lang.Math.abs(f); 
	}
	
	public static double abs(double d){
		d = Tighten.tighten(d);
		return java.lang.Math.abs(d); 
	}
	
	public static int max(int lhs, int rhs){
		lhs = Tighten.tighten(lhs);
		rhs = Tighten.tighten(rhs);
		return java.lang.Math.max(lhs, rhs);
	}
	
	public static int min(int lhs, int rhs){
		lhs = Tighten.tighten(lhs);
		rhs = Tighten.tighten(rhs);
		return java.lang.Math.min(lhs, rhs);
	}
	
	public static boolean isNaN(float f){
		f = Tighten.tighten(f);
		return Float.isNaN(f);
	}
	
	public static double sqrt(double d){
		d = Tighten.tighten(d);
		return java.lang.Math.sqrt(d);
	}

	public static double sin(double d) {	
		d = Tighten.tighten(d);
		return java.lang.Math.sin(d);
	}
	
	public static float sqrt(float f){
		f = Tighten.tighten(f);
		return (float)java.lang.Math.sqrt((double)f);
	}

	public static float acos(float f) {
		f = Tighten.tighten(f);
		return (float)java.lang.Math.acos((double)f);
	}

	public static float atan2(float f1, float f2) {
		f1 = Tighten.tighten(f1);
		f2 = Tighten.tighten(f2);
		return (float)java.lang.Math.atan2((double)f1, (double)f2);
	}

	public static float sin(float f) {
		f = Tighten.tighten(f);
		return (float)java.lang.Math.sin((double)f);
	}
	
	public static float cos(float f) {
		f = Tighten.tighten(f);
		return (float)java.lang.Math.cos((double)f);
	}

	public static int round(float x) {
		x = Tighten.tighten(x);
		return java.lang.Math.round(x);
	}

	public static float min(float f1, float f2) {
		f1 = Tighten.tighten(f1);
		f2 = Tighten.tighten(f2);
		return java.lang.Math.min(f1, f2);
	}
}
