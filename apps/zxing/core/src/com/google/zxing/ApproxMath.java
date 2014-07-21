package com.google.zxing;

public class ApproxMath {
	public static float abs(float f){
		f = accept(f);
		return Math.abs(f); 
	}
	
	public static int max(int lhs, int rhs){
		return -1;
	}
	
	public static int min(int lhs, int rhs){
		return -1;
	}
	
	public static boolean isNaN(float f){
		return false;
	}
	
	public static float sqrt(double d){
		return -1.0f;
	}
	
	public static float accept(float f){return f;}
}
