package com.google.zxing;

public class ApproxMath {
	public static float abs(float f){
		f = accept(f);
		return Math.abs(f); 
	}
	
	public static int max(int lhs, int rhs){
		//additional accept
		lhs = accept(lhs);
		//additional accept
		rhs = accept(rhs);
		return Math.max(lhs, rhs);
	}
	
	public static int min(int lhs, int rhs){
		//additional accept
		lhs = accept(lhs);
		//additional accept
		rhs = accept(rhs);
		return Math.min(lhs, rhs);
	}
	
	public static boolean isNaN(float f){
		//additional accept
		f = accept(f);
		return Float.isNaN(f);
	}
	
	public static double sqrt(double d){
		//additional accept
		d = accept(d);
		return Math.sqrt(d);
	}
	
	public static float accept(float f){return f;}
	public static int accept(int i){return i;}
	public static double accept(double d){return d;}
	
}
