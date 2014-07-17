package com.jme.math;

public class ApproxMath {
	public static float abs(float f) {
		accept(f);
		float ret = Math.abs(f);
		accept(ret);
		return ret;
	}
	
	private static float accept(float f){return f;}
}
