package com.jme.intersection;

/**
 Evaluation for Relax framework
*/

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.*;

import com.jme.math.FastMath;
import com.jme.math.TransformMatrix;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;


public class Intersection {

	public static double EPSILON = 1e-12;
	private static Vector3f tempVa = new Vector3f();
	private static Vector3f tempVb = new Vector3f();
	private static Vector3f tempVc = new Vector3f();
	private static Vector3f tempVd = new Vector3f();
	private static Vector3f tempVe = new Vector3f();
	private static float[] tempFa = new float[2];
	private static float[] tempFb = new float[2];
	private static Vector2f tempV2a = new Vector2f();
	private static Vector2f tempV2b = new Vector2f();

	public static boolean intersection(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector3f u0, Vector3f u1, Vector3f u2) {
		boolean done = false;
		boolean ret = true;
		boolean cond;
		Vector3f e1 = tempVa;
		Vector3f e2 = tempVb;
		Vector3f n1 = tempVc;
		Vector3f n2 = tempVd;
		float d1, d2;
		float du0, du1, du2, dv0, dv1, dv2;
		Vector3f d = tempVe;
		float[] isect1 = tempFa;
		float[] isect2 = tempFb;
		float du0du1, du0du2, dv0dv1, dv0dv2;
		short index;
		float vp0, vp1, vp2;
		float up0, up1, up2;
		float bb, cc, max;
		float xx, yy, xxyy, tmp;

		v1.subtract(v0, e1);
		v2.subtract(v0, e2);
		e1.cross(e2, n1);
		d1 = -n1.dot(v0);

		du0 = n1.dot(u0) + d1;
		du1 = n1.dot(u1) + d1;
		du2 = n1.dot(u2) + d1;
		
		if (Math.abs(du0) < EPSILON)
			du0 = 0.0f;
		if (Math.abs(du1) < EPSILON)
			du1 = 0.0f;
		if (Math.abs(du2) < EPSILON)
			du2 = 0.0f;
		du0du1 = du0 * du1;
		du0du2 = du0 * du2;

		if (du0du1 > 0.0f && du0du2 > 0.0f) {
			ret = false;
			done = true;
		} 
		if(!done) {
			u1.subtract(u0, e1);
			u2.subtract(u0, e2);
			e1.cross(e2, n2);
			d2 = -n2.dot(u0);
	
			dv0 = n2.dot(v0) + d2;
			dv1 = n2.dot(v1) + d2;
			dv2 = n2.dot(v2) + d2;
			
			if (Math.abs(dv0) < EPSILON)
				dv0 = 0.0f;
			if (Math.abs(dv1) < EPSILON)
				dv1 = 0.0f;
			if (Math.abs(dv2) < EPSILON)
				dv2 = 0.0f;
	
			dv0dv1 = dv0 * dv1;
			dv0dv2 = dv0 * dv2;
			
			if (dv0dv1 > 0.0f && dv0dv2 > 0.0f) {
				ret = false;
				done = true;
			} 
			if(!done) {
	
				n1.cross(n2, d);
		
				max = Math.abs(d.x);
				index = 0;
				bb = Math.abs(d.y);
				cc = Math.abs(d.z);
		
				if (bb > max) {
					max = bb;
					index = 1;
				}
				if (cc > max) {
					max = cc;
					vp0 = v0.z;
					vp1 = v1.z;
					vp2 = v2.z;
		
					up0 = u0.z;
					up1 = u1.z;
					up2 = u2.z;
		
				} else if (index == 1) {
					vp0 = v0.y;
					vp1 = v1.y;
					vp2 = v2.y;
		
					up0 = u0.y;
					up1 = u1.y;
					up2 = u2.y;
				} else {
					vp0 = v0.x;
					vp1 = v1.x;
					vp2 = v2.x;
		
					up0 = u0.x;
					up1 = u1.x;
					up2 = u2.x;
				}
		
				Vector3f abc = tempVa;
				Vector2f x0x1 = tempV2a;
		
				cond = newComputeIntervals(vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2, abc, x0x1);
				if (cond) {
					ret = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
					done = true;
				} 
				if(!done){
					Vector3f def = tempVb;
					Vector2f y0y1 = tempV2b;
					cond = newComputeIntervals(up0, up1, up2, du0, du1, du2, du0du1, du0du2, def, y0y1);
					if (cond) {
						ret = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
						done = true;
					}
					if(!done) {
						xx = x0x1.x * x0x1.y;
						yy = y0y1.x * y0y1.y;
						xxyy = xx * yy;
				
						tmp = abc.x * xxyy;
						isect1[0] = tmp + abc.y * x0x1.y * yy;
						isect1[1] = tmp + abc.z * x0x1.x * yy;
				
						tmp = def.x * xxyy;
						isect2[0] = tmp + def.y * xx * y0y1.y;
						isect2[1] = tmp + def.z * xx * y0y1.x;
				
						sort(isect1);
						sort(isect2);
				
						if (isect1[1] < isect2[0] || isect2[1] < isect1[0]) {
							ret = false;
						} else {
							ret = true;
						}
					}
				}
			}
		}
		return ret;
	}

	private static void sort(float[] f) {
		if (f[0] > f[1]) {
			float c = f[0];
			f[0] = f[1];
			f[1] = c;
		}
	}

	private static boolean newComputeIntervals(float vv0, float vv1, float vv2,
			float d0, float d1, float d2, float d0d1, float d0d2, Vector3f abc,
			Vector2f x0x1) {
		
		boolean ret = false;
		if (d0d1 > 0.0f) {
			abc.x = vv2;
			abc.y = (vv0 - vv2) * d2;
			abc.z = (vv1 - vv2) * d2;
			x0x1.x = d2 - d0;
			x0x1.y = d2 - d1;
		} else if (d0d2 > 0.0f) {
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (d1 * d2 > 0.0f || d0 != 0.0f) {
			abc.x = vv0;
			abc.y = (vv1 - vv0) * d0;
			abc.z = (vv2 - vv0) * d0;
			x0x1.x = d0 - d1;
			x0x1.y = d0 - d2;
		} else if (d1 != 0.0f) {
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (d2 != 0.0f) {
			abc.x = vv2;
			abc.y = (vv0 - vv2) * d2;
			abc.z = (vv1 - vv2) * d2;
			x0x1.x = d2 - d0;
			x0x1.y = d2 - d1;
		} else {
			ret = true;
		}
		return ret;
	}

	private static boolean coplanarTriTri(Vector3f n, Vector3f v0, Vector3f v1,
			Vector3f v2, Vector3f u0, Vector3f u1, Vector3f u2) {
		Vector3f a = new Vector3f();
		short i0, i1;
		boolean ret = false;

		a.x = Math.abs(n.x);
		a.y = Math.abs(n.y);
		a.z = Math.abs(n.z);
		
		if (a.x > a.y) {
			if (a.x > a.z) {
				i0 = 1; 
				i1 = 2;
			} else {
				i0 = 0; 
				i1 = 1;
			}
		} else {
			if (a.z > a.y) {
				i0 = 0; 
				i1 = 1;
			} else {
				i0 = 0; 
				i1 = 2;
			}
		}

		float[] v0f = new float[3];
		v0.toArray(v0f);
		float[] v1f = new float[3];
		v1.toArray(v1f);
		float[] v2f = new float[3];
		v2.toArray(v2f);
		float[] u0f = new float[3];
		u0.toArray(u0f);
		float[] u1f = new float[3];
		u1.toArray(u1f);
		float[] u2f = new float[3];
		u2.toArray(u2f);
		
		if (edgeAgainstTriEdges(v0f, v1f, u0f, u1f, u2f, i0, i1)) 
			ret = true;
		if (edgeAgainstTriEdges(v1f, v2f, u0f, u1f, u2f, i0, i1)) 
			ret = true;
		if (edgeAgainstTriEdges(v2f, v0f, u0f, u1f, u2f, i0, i1)) 
			ret = true;
		
		if(!ret) {
			pointInTri(v0f, u0f, u1f, u2f, i0, i1);
			pointInTri(u0f, v0f, v1f, v2f, i0, i1);
		}

		return ret;
	}

	private static boolean pointInTri(float[] V0, float[] U0, float[] U1, float[] U2, int i0, int i1) {
		boolean ret;
		float a, b, c, d0, d1, d2;
		a = U1[i1] - U0[i1];
		b = -(U1[i0] - U0[i0]);
		c = -a * U0[i0] - b * U0[i1];
		d0 = a * V0[i0] + b * V0[i1] + c;

		a = U2[i1] - U1[i1];
		b = -(U2[i0] - U1[i0]);
		c = -a * U1[i0] - b * U1[i1];
		d1 = a * V0[i0] + b * V0[i1] + c;

		a = U0[i1] - U2[i1];
		b = -(U0[i0] - U2[i0]);
		c = -a * U2[i0] - b * U2[i1];
		d2 = a * V0[i0] + b * V0[i1] + c;

		if (d0 * d1 > 0.0 && d0 * d2 > 0.0)
			ret = true;
		else
			ret = false;
		return ret;
	}

	private static boolean edgeAgainstTriEdges(float[] v0, float[] v1,
			float[] u0, float[] u1, float[] u2, int i0, int i1) {
		boolean ret = false;
		float aX, aY;
		aX = v1[i0] - v0[i0];
		aY = v1[i1] - v0[i1];
		
		if (edgeEdgeTest(v0, u0, u1, i0, i1, aX, aY)) 
			ret = true;
		if (edgeEdgeTest(v0, u1, u2, i0, i1, aX, aY)) 
			ret = true;
		if (edgeEdgeTest(v0, u2, u0, i0, i1, aX, aY)) 
			ret = true;
		return ret;
	}

	private static boolean edgeEdgeTest(float[] v0, float[] u0, float[] u1,
			int i0, int i1, float aX, float Ay) {
		boolean ret = false;
		float Bx = u0[i0] - u1[i0];
		float By = u0[i1] - u1[i1];
		float Cx = v0[i0] - u0[i0];
		float Cy = v0[i1] - u0[i1];
		float f = Ay * Bx - aX * By;
		float d = By * Cx - Bx * Cy;
		
		if ((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f)) {
			float e = aX * Cy - Ay * Cx;
			if (f > 0) {
				if (e >= 0 && e <= f)
					ret = true;
			} else {
				if (e <= 0 && e >= f)
					ret = true;
			}
		}
		return ret;
	}
}
