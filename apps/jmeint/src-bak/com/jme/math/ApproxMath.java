package com.jme.math;

public class ApproxMath {
	public static float abs(float f) {
		//additional accept
		accept(f);
		float ret = Math.abs(f);
		return ret;
	}
	
	private static float accept(float f){return f;}
}
