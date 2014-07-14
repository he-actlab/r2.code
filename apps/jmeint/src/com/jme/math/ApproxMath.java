package com.jme.math;

public class ApproxMath {
	public static float abs(float f) {
		accept(f);
		float ret = Math.abs(f);	// approx: 3: MOVE_F R2, T4
		accept(ret);
		return ret;
	}
	
	private static float accept(float f){return f;}
}
