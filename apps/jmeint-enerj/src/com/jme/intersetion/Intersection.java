package com.jme.intersection;

import java.nio.IntBuffer;

import com.jme.math.FastMath;
import com.jme.math.TransformMatrix;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;

import enerj.lang.*;

public class Intersection {

	public static final double EPSILON = 1e-12;

	private static final @Approx Vector3f tempVa = new @Approx Vector3f();
	private static final @Approx Vector3f tempVb = new @Approx Vector3f();
	private static final @Approx Vector3f tempVc = new @Approx Vector3f();
	private static final @Approx Vector3f tempVd = new @Approx Vector3f();
	private static final @Approx Vector3f tempVe = new @Approx Vector3f();
	private static final @Approx float[] tempFa = new @Approx float[2];
	private static final @Approx float[] tempFb = new @Approx float[2];
	private static final @Approx Vector2f tempV2a = new @Approx Vector2f();
	private static final @Approx Vector2f tempV2b = new @Approx Vector2f();

	public static @Approx boolean intersection(@Approx Vector3f v0, @Approx Vector3f v1, @Approx Vector3f v2,
			@Approx Vector3f u0, @Approx Vector3f u1, @Approx Vector3f u2) {
		boolean done = false;
		@Approx boolean ret = true;
		@Approx Vector3f e1 = tempVa;
		@Approx Vector3f e2 = tempVb;
		@Approx Vector3f n1 = tempVc;
		@Approx Vector3f n2 = tempVd;
		@Approx float d1, d2;
		@Approx float du0, du1, du2, dv0, dv1, dv2;
		@Approx Vector3f d = tempVe;
		@Approx float[] isect1 = tempFa;
		@Approx float[] isect2 = tempFb;
		@Approx float du0du1, du0du2, dv0dv1, dv0dv2;
		short index;
		@Approx float vp0, vp1, vp2;
		@Approx float up0, up1, up2;
		@Approx float bb, cc, max;
		@Approx float xx, yy, xxyy, tmp;

		v1.subtract(v0, e1);
		v2.subtract(v0, e2);
		e1.cross(e2, n1);
		d1 = -n1.dot(v0);

		du0 = n1.dot(u0) + d1;
		du1 = n1.dot(u1) + d1;
		du2 = n1.dot(u2) + d1;

		if (Endorsements.endorse(ApproxMath.abs(du0) < EPSILON))
			du0 = 0.0f;
		if (Endorsements.endorse(ApproxMath.abs(du1) < EPSILON))
			du1 = 0.0f;
		if (Endorsements.endorse(ApproxMath.abs(du2) < EPSILON))
			du2 = 0.0f;
		du0du1 = du0 * du1;
		du0du2 = du0 * du2;

		if (Endorsements.endorse(du0du1 > 0.0f && du0du2 > 0.0f)) {
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
	
			if (Endorsements.endorse(ApproxMath.abs(dv0) < EPSILON))
				dv0 = 0.0f;
			if (Endorsements.endorse(ApproxMath.abs(dv1) < EPSILON))
				dv1 = 0.0f;
			if (Endorsements.endorse(ApproxMath.abs(dv2) < EPSILON))
				dv2 = 0.0f;
	
			dv0dv1 = dv0 * dv1;
			dv0dv2 = dv0 * dv2;
	
			if (Endorsements.endorse(dv0dv1 > 0.0f && dv0dv2 > 0.0f)) { 
				ret = false; 
				done = true;
			}
			if(!done) {
				n1.cross(n2, d);
		
				max = ApproxMath.abs(d.x);
				index = 0;
				bb = ApproxMath.abs(d.y);
				cc = ApproxMath.abs(d.z);
				if (Endorsements.endorse(bb > max)) {
					max = bb;
					index = 1;
				}
				if (Endorsements.endorse(cc > max)) {
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
		
				@Approx Vector3f abc = tempVa;
				@Approx Vector2f x0x1 = tempV2a;
				if (newComputeIntervals(vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2,
						abc, x0x1)) {
					ret = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
					done = true;
				}
				if(!done) {
					@Approx Vector3f def = tempVb;
					@Approx Vector2f y0y1 = tempV2b;
					if (newComputeIntervals(up0, up1, up2, du0, du1, du2, du0du1, du0du2,
							def, y0y1)) {
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
				
						if (Endorsements.endorse(isect1[1] < isect2[0] || isect2[1] < isect1[0])) {
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

	private static void sort(@Approx float[] f) {
		if (Endorsements.endorse(f[0] > f[1])) {
			@Approx float c = f[0];
			f[0] = f[1];
			f[1] = c;
		}
	}

	private static boolean newComputeIntervals(@Approx float vv0, @Approx float vv1, @Approx float vv2,
			@Approx float d0, @Approx float d1, @Approx float d2, @Approx float d0d1, @Approx float d0d2, @Approx Vector3f abc,
			@Approx Vector2f x0x1) {
		
		@Approx boolean ret = false;
		if (Endorsements.endorse(d0d1 > 0.0f)) {
			abc.x = vv2;
			abc.y = (vv0 - vv2) * d2;
			abc.z = (vv1 - vv2) * d2;
			x0x1.x = d2 - d0;
			x0x1.y = d2 - d1;
		} else if (Endorsements.endorse(d0d2 > 0.0f)) {
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (Endorsements.endorse(d1 * d2 > 0.0f || d0 != 0.0f)) {
			abc.x = vv0;
			abc.y = (vv1 - vv0) * d0;
			abc.z = (vv2 - vv0) * d0;
			x0x1.x = d0 - d1;
			x0x1.y = d0 - d2;
		} else if (Endorsements.endorse(d1 != 0.0f)) {
			abc.x = vv1;
			abc.y = (vv0 - vv1) * d1;
			abc.z = (vv2 - vv1) * d1;
			x0x1.x = d1 - d0;
			x0x1.y = d1 - d2;
		} else if (Endorsements.endorse(d2 != 0.0f)) {
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

	private static @Approx boolean coplanarTriTri(@Approx Vector3f n, @Approx Vector3f v0, @Approx Vector3f v1,
			@Approx Vector3f v2, @Approx Vector3f u0, @Approx Vector3f u1, @Approx Vector3f u2) {
		
		@Approx Vector3f a = new @Approx Vector3f();
		short i0, i1;
		@Approx boolean ret = false;
		
		a.x = ApproxMath.abs(n.x);
		a.y = ApproxMath.abs(n.y);
		a.z = ApproxMath.abs(n.z);

		if (Endorsements.endorse(a.x > a.y)) {
			if (Endorsements.endorse(a.x > a.z)) {
				i0 = 1; 
				i1 = 2;
			} else {
				i0 = 0; 
				i1 = 1;
			}
		} else 
			if (Endorsements.endorse(a.z > a.y)) {
				i0 = 0; 
				i1 = 1;
			} else {
				i0 = 0; 
				i1 = 2;
			}
		}

		@Approx float[] v0f = new @Approx float[3];
		v0.toArray(v0f);
		@Approx float[] v1f = new @Approx float[3];
		v1.toArray(v1f);
		@Approx float[] v2f = new @Approx float[3];
		v2.toArray(v2f);
		@Approx float[] u0f = new @Approx float[3];
		u0.toArray(u0f);
		@Approx float[] u1f = new @Approx float[3];
		u1.toArray(u1f);
		@Approx float[] u2f = new @Approx float[3];
		u2.toArray(u2f);
		
		if (Endorsements.endorse(edgeAgainstTriEdges(v0f, v1f, u0f, u1f, u2f, i0, i1))) 
			ret = true;
		if (Endorsements.endorse(edgeAgainstTriEdges(v1f, v2f, u0f, u1f, u2f, i0, i1))) 
			ret = true;
		if (Endorsements.endorse(edgeAgainstTriEdges(v2f, v0f, u0f, u1f, u2f, i0, i1))) 
			ret = true;

		if(!ret) {
			pointInTri(v0f, u0f, u1f, u2f, i0, i1);
			pointInTri(u0f, v0f, v1f, v2f, i0, i1);
		}

		return ret;
	}

	private static @Approx boolean pointInTri(@Approx float[] V0, @Approx float[] U0, @Approx float[] U1,
			@Approx float[] U2, int i0, int i1) {
		@Approx boolean ret;
		@Approx float a, b, c, d0, d1, d2;
		
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
		if (Endorsements.endorse(d0 * d1 > 0.0 && d0 * d2 > 0.0))
			ret = true;
		else
			ret = false;
		
		return ret;
	}

	private static @Approx boolean edgeAgainstTriEdges(@Approx float[] v0, @Approx float[] v1,
			@Approx float[] u0, @Approx float[] u1, @Approx float[] u2, int i0, int i1) {
		@Approx boolean ret = false;
		@Approx float aX, aY;
		aX = v1[i0] - v0[i0];
		aY = v1[i1] - v0[i1];

		if (Endorsements.endorse(edgeEdgeTest(v0, u0, u1, i0, i1, aX, aY))) 
			ret = true;
		if (Endorsements.endorse(edgeEdgeTest(v0, u1, u2, i0, i1, aX, aY))) 
			ret = true;
		if (Endorsements.endorse(edgeEdgeTest(v0, u2, u0, i0, i1, aX, aY))) 
			ret = true;	
		return ret;
	}

	private static @Approx boolean edgeEdgeTest(@Approx float[] v0, @Approx float[] u0, @Approx float[] u1,
			int i0, int i1, @Approx float aX, @Approx float Ay) {
		@Approx boolean ret = false;
		@Approx float Bx = u0[i0] - u1[i0];
		@Approx float By = u0[i1] - u1[i1];
		@Approx float Cx = v0[i0] - u0[i0];
		@Approx float Cy = v0[i1] - u0[i1];
		@Approx float f = Ay * Bx - aX * By;
		@Approx float d = By * Cx - Bx * Cy;
		if (Endorsements.endorse((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f))) {
			@Approx float e = aX * Cy - Ay * Cx;
			if (Endorsements.endorse(f > 0)) {
				if (Endorsements.endorse(e >= 0 && e <= f))
					ret = true;
			} else {
				if (Endorsements.endorse(e <= 0 && e >= f))
					ret = true;
			}
		}
		return ret;
	}
}