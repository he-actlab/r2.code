/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme.intersection;

import com.jme.math.ApproxMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;

/**
 * <code>Intersection</code> provides functional methods for calculating the
 * intersection of some objects. All the methods are static to allow for quick
 * and easy calls. <code>Intersection</code> relays requests to specific classes
 * to handle the actual work. By providing checks to just
 * <code>BoundingVolume</code> the client application need not worry about what
 * type of bounding volume is being used.
 * 
 * @author Mark Powell
 * @version $Id: Intersection.java 4131 2009-03-19 20:15:28Z blaine.dev $
 */
public class Intersection {

	/**
	 * EPSILON represents the error buffer used to denote a hit.
	 */
	public static  double EPSILON = 1e-12;	// approx: 1: PUTSTATIC_D DConst: 1.0E-12, .EPSILON
	private static  Vector3f tempVa = new Vector3f();
	private static  Vector3f tempVb = new Vector3f();
	private static  Vector3f tempVc;
	private static  Vector3f tempVd;
	private static  Vector3f tempVe;
	private static  float[] tempFa; 
	private static  float[] tempFb; 
	private static  Vector2f tempV2a = new Vector2f();
	private static  Vector2f tempV2b = new Vector2f();

	static {
		alloc_TAG3();
		tempVc = new Vector3f();
		alloc_TAG3();
		tempVd = new Vector3f();
		alloc_TAG1();
		tempVe = new Vector3f();
		alloc_TAG2();
		tempFa = new float[2];
		alloc_TAG2();
		tempFb = new float[2];
	}

	public static boolean intersection(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector3f u0, Vector3f u1, Vector3f u2) {
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
		d1 = -n1.dot(v0);	// approx: 20: MOVE_F R15, T71	// approx: 19: NEG_F T71, T70

		du0 = n1.dot(u0) + d1;	// approx: 23: MOVE_F R74, T73	// approx: 22: ADD_F T73, T72, R15
		du1 = n1.dot(u1) + d1;	// approx: 26: MOVE_F R77, T76	// approx: 25: ADD_F T76, T75, R15
		du2 = n1.dot(u2) + d1;	// approx: 29: MOVE_F R80, T79	// approx: 28: ADD_F T79, T78, R15

		// additional accept
		du0 = accept(du0);	// approx: 31: MOVE_F R82, T81
		du1 = accept(du1);	// approx: 33: MOVE_F R84, T83
		du2 = accept(du2);	// approx: 35: MOVE_F R86, T85

		if ((ApproxMath.abs(du0) < EPSILON))
			du0 = 0.0f;	// approx: 41: MOVE_F R91, FConst: 0.0
		if ((ApproxMath.abs(du1) < EPSILON))
			du1 = 0.0f;	// approx: 47: MOVE_F R97, FConst: 0.0
		if ((ApproxMath.abs(du2) < EPSILON))
			du2 = 0.0f;	// approx: 53: MOVE_F R103, FConst: 0.0
		du0du1 = du0 * du1;	// approx: 54: MUL_F T105, R92, R98	// approx: 55: MOVE_F R106, T105
		du0du2 = du0 * du2;	// approx: 57: MOVE_F R108, T107	// approx: 56: MUL_F T107, R92, R104

		// additional accept
		du0du1 = accept(du0du1);
		du0du2 = accept(du0du2);

		if ((du0du1 > 0.0f && du0du2 > 0.0f)) {
			return false;
		}

		u1.subtract(u0, e1);
		u2.subtract(u0, e2);
		e1.cross(e2, n2);
		d2 = -n2.dot(u0);	// approx: 71: MOVE_F R25, T119	// approx: 70: NEG_F T119, T118

		dv0 = n2.dot(v0) + d2;	// approx: 74: MOVE_F R122, T121	// approx: 73: ADD_F T121, T120, R25
		dv1 = n2.dot(v1) + d2;	// approx: 77: MOVE_F R125, T124	// approx: 76: ADD_F T124, T123, R25
		dv2 = n2.dot(v2) + d2;	// approx: 79: ADD_F T127, T126, R25	// approx: 80: MOVE_F R128, T127

		// additional accept
		dv0 = accept(dv0);	// approx: 82: MOVE_F R130, T129
		dv1 = accept(dv1);	// approx: 84: MOVE_F R132, T131
		dv2 = accept(dv2);	// approx: 86: MOVE_F R134, T133

		if ((ApproxMath.abs(dv0) < EPSILON))
			dv0 = 0.0f;	// approx: 92: MOVE_F R139, FConst: 0.0
		if ((ApproxMath.abs(dv1) < EPSILON))
			dv1 = 0.0f;	// approx: 98: MOVE_F R145, FConst: 0.0
		if ((ApproxMath.abs(dv2) < EPSILON))
			dv2 = 0.0f;

		dv0dv1 = dv0 * dv1;	// approx: 105: MUL_F T153, R140, R146	// approx: 106: MOVE_F R154, T153
		dv0dv2 = dv0 * dv2;	// approx: 107: MUL_F T155, R140, R152	// approx: 108: MOVE_F R156, T155

		// additional accept
		dv0dv1 = accept(dv0dv1);
		dv0dv2 = accept(dv0dv2);

		if ((dv0dv1 > 0.0f && dv0dv2 > 0.0f)) {
			return false;
		}

		n1.cross(n2, d);

		// additional accept
		d = accept_all_FIELD1_TAG1(d);
		d = accept_all_FIELD2_TAG1(d);
		d = accept_all_FIELD3_TAG1(d);

		max = ApproxMath.abs(d.x);	// approx: 126: MOVE_F R172, T171	// approx: 124: GETFIELD_F T170, R169, .x
		index = 0;
		bb = ApproxMath.abs(d.y);	// approx: 130: MOVE_F R176, T175	// approx: 128: GETFIELD_F T174, R169, .y
		cc = ApproxMath.abs(d.z);	// approx: 133: MOVE_F R179, T178	// approx: 131: GETFIELD_F T177, R169, .z

		// additional accept
		bb = accept(bb);
		cc = accept(cc);
		max = accept(max);

		if ((bb > max)) {
			max = bb;
			index = 1;
		}
		if ((cc > max)) {
			max = cc;	// approx: 172: MOVE_F R192, R183
			vp0 = v0.z;	// approx: 173: GETFIELD_F T193, R0, .z	// approx: 174: MOVE_F R194, T193
			vp1 = v1.z;	// approx: 175: GETFIELD_F T195, R1, .z	// approx: 176: MOVE_F R196, T195
			vp2 = v2.z;	// approx: 177: GETFIELD_F T197, R2, .z	// approx: 178: MOVE_F R198, T197

			up0 = u0.z;	// approx: 179: GETFIELD_F T199, R3, .z	// approx: 180: MOVE_F R200, T199
			up1 = u1.z;	// approx: 181: GETFIELD_F T201, R4, .z	// approx: 182: MOVE_F R202, T201
			up2 = u2.z;	// approx: 184: MOVE_F R204, T203	// approx: 183: GETFIELD_F T203, R5, .z

		} else if (index == 1) {
			vp0 = v0.y;	// approx: 159: GETFIELD_F T205, R0, .y	// approx: 160: MOVE_F R206, T205
			vp1 = v1.y;	// approx: 161: GETFIELD_F T207, R1, .y	// approx: 162: MOVE_F R208, T207
			vp2 = v2.y;	// approx: 163: GETFIELD_F T209, R2, .y	// approx: 164: MOVE_F R210, T209

			up0 = u0.y;	// approx: 165: GETFIELD_F T211, R3, .y	// approx: 166: MOVE_F R212, T211
			up1 = u1.y;	// approx: 167: GETFIELD_F T213, R4, .y	// approx: 168: MOVE_F R214, T213
			up2 = u2.y;	// approx: 169: GETFIELD_F T215, R5, .y	// approx: 170: MOVE_F R216, T215
		} else {
			vp0 = v0.x;	// approx: 148: MOVE_F R218, T217	// approx: 147: GETFIELD_F T217, R0, .x
			vp1 = v1.x;	// approx: 150: MOVE_F R220, T219	// approx: 149: GETFIELD_F T219, R1, .x
			vp2 = v2.x;	// approx: 152: MOVE_F R222, T221	// approx: 151: GETFIELD_F T221, R2, .x

			up0 = u0.x;	// approx: 154: MOVE_F R224, T223	// approx: 153: GETFIELD_F T223, R3, .x
			up1 = u1.x;	// approx: 156: MOVE_F R226, T225	// approx: 155: GETFIELD_F T225, R4, .x
			up2 = u2.x;	// approx: 158: MOVE_F R228, T227	// approx: 157: GETFIELD_F T227, R5, .x
		}

		Vector3f abc = tempVa;
		Vector2f x0x1 = tempV2a;

		if (newComputeIntervals(vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2, abc, x0x1)) {
			return coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
		}

		Vector3f def = tempVb;
		Vector2f y0y1 = tempV2b;
		if (newComputeIntervals(up0, up1, up2, du0, du1, du2, du0du1, du0du2,
				def, y0y1)) {
			return coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);
		}

		xx = x0x1.x * x0x1.y;	// approx: 199: GETFIELD_F T244, R43, .y	// approx: 200: MUL_F T245, T243, T244	// approx: 198: GETFIELD_F T243, R43, .x	// approx: 201: MOVE_F R48, T245
		yy = y0y1.x * y0y1.y;	// approx: 205: MOVE_F R50, T248	// approx: 203: GETFIELD_F T247, R45, .y	// approx: 204: MUL_F T248, T246, T247	// approx: 202: GETFIELD_F T246, R45, .x
		xxyy = xx * yy;	// approx: 207: MOVE_F R51, T249	// approx: 206: MUL_F T249, R48, R50

		tmp = abc.x * xxyy;	// approx: 208: GETFIELD_F T250, R42, .x	// approx: 210: MOVE_F R252, T251	// approx: 209: MUL_F T251, T250, R51
		isect1[0] = tmp + abc.y * x0x1.y * yy;	// approx: 216: ASTORE_F T257, R12, IConst: 0	// approx: 215: ADD_F T257, R252, T256	// approx: 214: MUL_F T256, T255, R50	// approx: 213: MUL_F T255, T253, T254	// approx: 212: GETFIELD_F T254, R43, .y	// approx: 211: GETFIELD_F T253, R42, .y
		isect1[1] = tmp + abc.z * x0x1.x * yy;	// approx: 222: ASTORE_F T262, R12, IConst: 1	// approx: 221: ADD_F T262, R252, T261	// approx: 220: MUL_F T261, T260, R50	// approx: 219: MUL_F T260, T258, T259	// approx: 218: GETFIELD_F T259, R43, .x	// approx: 217: GETFIELD_F T258, R42, .z

		tmp = def.x * xxyy;	// approx: 224: MUL_F T264, T263, R51	// approx: 223: GETFIELD_F T263, R44, .x	// approx: 225: MOVE_F R265, T264
		isect2[0] = tmp + def.y * xx * y0y1.y;	// approx: 226: GETFIELD_F T266, R44, .y	// approx: 227: MUL_F T267, T266, R48	// approx: 228: GETFIELD_F T268, R45, .y	// approx: 229: MUL_F T269, T267, T268	// approx: 230: ADD_F T270, R265, T269	// approx: 231: ASTORE_F T270, R13, IConst: 0
		isect2[1] = tmp + def.z * xx * y0y1.x;	// approx: 232: GETFIELD_F T271, R44, .z	// approx: 233: MUL_F T272, T271, R48	// approx: 234: GETFIELD_F T273, R45, .x	// approx: 235: MUL_F T274, T272, T273	// approx: 236: ADD_F T275, R265, T274	// approx: 237: ASTORE_F T275, R13, IConst: 1

		sort(isect1);
		sort(isect2);

		if ((isect1[1] < isect2[0] || isect2[1] < isect1[0])) {
			return false;
		}

		return true;
	}

	private static void sort(float[] f) {

		// additional accept
		f = accept_all_FIELD4_TAG2(f);

		if ((f[0] > f[1])) {
			float c = f[0];	// approx: 8: MOVE_F R5, T10	// approx: 7: ALOAD_F T10, R8, IConst: 0
			f[0] = f[1];	// approx: 11: ASTORE_F T6, T11, IConst: 0	// approx: 10: ALOAD_F T6, R8, IConst: 1
			f[1] = c;	// approx: 12: ASTORE_F R5, R8, IConst: 1
		}
		
		// additional accept
		f = accept_all_FIELD4_TAG2(f);
	}

	private static boolean newComputeIntervals(float vv0, float vv1, float vv2,
			float d0, float d1, float d2, float d0d1, float d0d2, Vector3f abc,
			Vector2f x0x1) {

		// additional accept
		d0d1 = accept(d0d1);
		d0d2 = accept(d0d2);
		d0 = accept(d0);
		d1 = accept(d1);
		d2 = accept(d2);

		if ((d0d1 > 0.0f)) {
			abc.x = vv2;	// approx: 73: PUTFIELD_F R8, .x, R2
			abc.y = (vv0 - vv2) * d2;	// approx: 76: PUTFIELD_F R8, .y, T20	// approx: 75: MUL_F T20, T19, R5	// approx: 74: SUB_F T19, R0, R2
			abc.z = (vv1 - vv2) * d2;	// approx: 79: PUTFIELD_F R8, .z, T22	// approx: 78: MUL_F T22, T21, R5	// approx: 77: SUB_F T21, R1, R2
			x0x1.x = d2 - d0;	// approx: 80: SUB_F T23, R5, R3	// approx: 81: PUTFIELD_F R9, .x, T23
			x0x1.y = d2 - d1;	// approx: 82: SUB_F T24, R5, R4	// approx: 83: PUTFIELD_F R9, .y, T24
		} else if ((d0d2 > 0.0f)) {
			abc.x = vv1;	// approx: 61: PUTFIELD_F R8, .x, R1
			abc.y = (vv0 - vv1) * d1;	// approx: 64: PUTFIELD_F R8, .y, T27	// approx: 62: SUB_F T26, R0, R1	// approx: 63: MUL_F T27, T26, R4
			abc.z = (vv2 - vv1) * d1;	// approx: 67: PUTFIELD_F R8, .z, T29	// approx: 66: MUL_F T29, T28, R4	// approx: 65: SUB_F T28, R2, R1
			x0x1.x = d1 - d0;	// approx: 69: PUTFIELD_F R9, .x, T30	// approx: 68: SUB_F T30, R4, R3
			x0x1.y = d1 - d2;	// approx: 71: PUTFIELD_F R9, .y, T31	// approx: 70: SUB_F T31, R4, R5
		} else if ((d1 * d2 > 0.0f || d0 != 0.0f)) {
			abc.x = vv0;	// approx: 49: PUTFIELD_F R8, .x, R0
			abc.y = (vv1 - vv0) * d0;	// approx: 52: PUTFIELD_F R8, .y, T50	// approx: 50: SUB_F T49, R1, R0	// approx: 51: MUL_F T50, T49, R3
			abc.z = (vv2 - vv0) * d0;	// approx: 53: SUB_F T51, R2, R0	// approx: 54: MUL_F T52, T51, R3	// approx: 55: PUTFIELD_F R8, .z, T52
			x0x1.x = d0 - d1;	// approx: 56: SUB_F T53, R3, R4	// approx: 57: PUTFIELD_F R9, .x, T53
			x0x1.y = d0 - d2;	// approx: 58: SUB_F T54, R3, R5	// approx: 59: PUTFIELD_F R9, .y, T54
		} else if ((d1 != 0.0f)) {
			abc.x = vv1;	// approx: 37: PUTFIELD_F R8, .x, R1
			abc.y = (vv0 - vv1) * d1;	// approx: 40: PUTFIELD_F R8, .y, T37	// approx: 39: MUL_F T37, T36, R4	// approx: 38: SUB_F T36, R0, R1
			abc.z = (vv2 - vv1) * d1;	// approx: 41: SUB_F T38, R2, R1	// approx: 43: PUTFIELD_F R8, .z, T39	// approx: 42: MUL_F T39, T38, R4
			x0x1.x = d1 - d0;	// approx: 45: PUTFIELD_F R9, .x, T40	// approx: 44: SUB_F T40, R4, R3
			x0x1.y = d1 - d2;	// approx: 47: PUTFIELD_F R9, .y, T41	// approx: 46: SUB_F T41, R4, R5
		} else if ((d2 != 0.0f)) {
			abc.x = vv2;	// approx: 25: PUTFIELD_F R8, .x, R2
			abc.y = (vv0 - vv2) * d2;	// approx: 27: MUL_F T44, T43, R5	// approx: 26: SUB_F T43, R0, R2	// approx: 28: PUTFIELD_F R8, .y, T44
			abc.z = (vv1 - vv2) * d2;	// approx: 31: PUTFIELD_F R8, .z, T46	// approx: 30: MUL_F T46, T45, R5	// approx: 29: SUB_F T45, R1, R2
			x0x1.x = d2 - d0;	// approx: 33: PUTFIELD_F R9, .x, T47	// approx: 32: SUB_F T47, R5, R3
			x0x1.y = d2 - d1;	// approx: 35: PUTFIELD_F R9, .y, T48	// approx: 34: SUB_F T48, R5, R4
		} else {
			return true;
		}
		return false;
	}

	private static boolean coplanarTriTri(Vector3f n, Vector3f v0, Vector3f v1,
			Vector3f v2, Vector3f u0, Vector3f u1, Vector3f u2) {
		alloc_TAG4();
		Vector3f a = new Vector3f();
		short i0, i1;

		// additional accept
		n = accept_all_FIELD1_TAG3(n);
		n = accept_all_FIELD2_TAG3(n);
		n = accept_all_FIELD3_TAG3(n);

		a.x = ApproxMath.abs(n.x);	// approx: 12: GETFIELD_F T30, R29, .x	// approx: 14: PUTFIELD_F R23, .x, T31
		a.y = ApproxMath.abs(n.y);	// approx: 15: GETFIELD_F T32, R29, .y	// approx: 17: PUTFIELD_F R23, .y, T33
		a.z = ApproxMath.abs(n.z);	// approx: 20: PUTFIELD_F R23, .z, T35	// approx: 18: GETFIELD_F T34, R29, .z

		// additional accept
		a = accept_all_FIELD1_TAG4(a);
		a = accept_all_FIELD2_TAG4(a);
		a = accept_all_FIELD3_TAG4(a);

		if ((a.x > a.y)) {
			if ((a.x > a.z)) {
				i0 = 1; 
				i1 = 2;
			} else {
				i0 = 0; 
				i1 = 1;
			}
		} else {
			if ((a.z > a.y)) {
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
		if ((edgeAgainstTriEdges(v0f, v1f, u0f, u1f, u2f, i0, i1))) {
			return true;
		}
		if ((edgeAgainstTriEdges(v1f, v2f, u0f, u1f, u2f, i0, i1))) {
			return true;
		}
		if ((edgeAgainstTriEdges(v2f, v0f, u0f, u1f, u2f, i0, i1))) {
			return true;
		}

		pointInTri(v0f, u0f, u1f, u2f, i0, i1);
		pointInTri(u0f, v0f, v1f, v2f, i0, i1);

		return false;
	}

	private static boolean pointInTri(float[] V0, float[] U0, float[] U1, float[] U2, int i0, int i1) {
		float a, b, c, d0, d1, d2;
		a = U1[i1] - U0[i1];	// approx: 3: SUB_F T19, T17, T18	// approx: 4: MOVE_F R20, T19	// approx: 1: ALOAD_F T17, R2, R5	// approx: 2: ALOAD_F T18, R1, R5
		b = -(U1[i0] - U0[i0]);	// approx: 7: SUB_F T23, T21, T22	// approx: 8: NEG_F T24, T23	// approx: 5: ALOAD_F T21, R2, R4	// approx: 6: ALOAD_F T22, R1, R4	// approx: 9: MOVE_F R25, T24
		c = -a * U0[i0] - b * U0[i1];	// approx: 10: NEG_F T26, R20	// approx: 12: MUL_F T28, T26, T27	// approx: 11: ALOAD_F T27, R1, R4	// approx: 14: MUL_F T30, R25, T29	// approx: 13: ALOAD_F T29, R1, R5	// approx: 16: MOVE_F R32, T31	// approx: 15: SUB_F T31, T28, T30
		d0 = a * V0[i0] + b * V0[i1] + c;	// approx: 18: MUL_F T34, R20, T33	// approx: 17: ALOAD_F T33, R0, R4	// approx: 20: MUL_F T36, R25, T35	// approx: 19: ALOAD_F T35, R0, R5	// approx: 22: ADD_F T38, T37, R32	// approx: 21: ADD_F T37, T34, T36	// approx: 23: MOVE_F R39, T38

		a = U2[i1] - U1[i1];	// approx: 24: ALOAD_F T40, R3, R5	// approx: 25: ALOAD_F T41, R2, R5	// approx: 26: SUB_F T42, T40, T41	// approx: 27: MOVE_F R43, T42
		b = -(U2[i0] - U1[i0]);	// approx: 28: ALOAD_F T44, R3, R4	// approx: 29: ALOAD_F T45, R2, R4	// approx: 30: SUB_F T46, T44, T45	// approx: 31: NEG_F T47, T46	// approx: 32: MOVE_F R48, T47
		c = -a * U1[i0] - b * U1[i1];	// approx: 33: NEG_F T49, R43	// approx: 34: ALOAD_F T50, R2, R4	// approx: 35: MUL_F T51, T49, T50	// approx: 36: ALOAD_F T52, R2, R5	// approx: 37: MUL_F T53, R48, T52	// approx: 38: SUB_F T54, T51, T53	// approx: 39: MOVE_F R55, T54
		d1 = a * V0[i0] + b * V0[i1] + c;	// approx: 44: ADD_F T60, T57, T59	// approx: 43: MUL_F T59, R48, T58	// approx: 42: ALOAD_F T58, R0, R5	// approx: 41: MUL_F T57, R43, T56	// approx: 46: MOVE_F R62, T61	// approx: 45: ADD_F T61, T60, R55	// approx: 40: ALOAD_F T56, R0, R4

		a = U0[i1] - U2[i1];	// approx: 50: MOVE_F R66, T65	// approx: 49: SUB_F T65, T63, T64	// approx: 48: ALOAD_F T64, R3, R5	// approx: 47: ALOAD_F T63, R1, R5
		b = -(U0[i0] - U2[i0]);	// approx: 52: ALOAD_F T68, R3, R4	// approx: 51: ALOAD_F T67, R1, R4	// approx: 55: MOVE_F R71, T70	// approx: 54: NEG_F T70, T69	// approx: 53: SUB_F T69, T67, T68
		c = -a * U2[i0] - b * U2[i1];	// approx: 56: NEG_F T72, R66	// approx: 59: ALOAD_F T75, R3, R5	// approx: 60: MUL_F T76, R71, T75	// approx: 57: ALOAD_F T73, R3, R4	// approx: 58: MUL_F T74, T72, T73	// approx: 61: SUB_F T77, T74, T76	// approx: 62: MOVE_F R78, T77
		d2 = a * V0[i0] + b * V0[i1] + c;	// approx: 67: ADD_F T83, T80, T82	// approx: 68: ADD_F T84, T83, R78	// approx: 65: ALOAD_F T81, R0, R5	// approx: 66: MUL_F T82, R71, T81	// approx: 69: MOVE_F R85, T84	// approx: 63: ALOAD_F T79, R0, R4	// approx: 64: MUL_F T80, R66, T79
		
		// additional accept
		d0 = accept(d0);
		d1 = accept(d1);
		d2 = accept(d2);

		if ((d0 * d1 > 0.0 && d0 * d2 > 0.0))
			return true;

		return false;
	}

	private static boolean edgeAgainstTriEdges(float[] v0, float[] v1,
			float[] u0, float[] u1, float[] u2, int i0, int i1) {
		float aX, aY;
		aX = v1[i0] - v0[i0];	// approx: 3: SUB_F T14, T12, T13	// approx: 2: ALOAD_F T13, R0, R5	// approx: 1: ALOAD_F T12, R1, R5	// approx: 4: MOVE_F R9, T14
		aY = v1[i1] - v0[i1];	// approx: 8: MOVE_F R10, T17	// approx: 6: ALOAD_F T16, R0, R6	// approx: 7: SUB_F T17, T15, T16	// approx: 5: ALOAD_F T15, R1, R6

		if ((edgeEdgeTest(v0, u0, u1, i0, i1, aX, aY))) {
			return true;
		}
		if ((edgeEdgeTest(v0, u1, u2, i0, i1, aX, aY))) {
			return true;
		}
		if ((edgeEdgeTest(v0, u2, u0, i0, i1, aX, aY))) {
			return true;
		}
		return false;
	}

	private static boolean edgeEdgeTest(float[] v0, float[] u0, float[] u1,
			int i0, int i1, float aX, float Ay) {
		float Bx = u0[i0] - u1[i0];	// approx: 2: ALOAD_F T18, R2, R3	// approx: 3: SUB_F T19, T17, T18	// approx: 1: ALOAD_F T17, R1, R3	// approx: 4: MOVE_F R9, T19
		float By = u0[i1] - u1[i1];	// approx: 6: ALOAD_F T21, R2, R4	// approx: 7: SUB_F T22, T20, T21	// approx: 5: ALOAD_F T20, R1, R4	// approx: 8: MOVE_F R10, T22
		float Cx = v0[i0] - u0[i0];	// approx: 10: ALOAD_F T24, R1, R3	// approx: 11: SUB_F T25, T23, T24	// approx: 9: ALOAD_F T23, R0, R3	// approx: 12: MOVE_F R11, T25
		float Cy = v0[i1] - u0[i1];	// approx: 16: MOVE_F R12, T28	// approx: 14: ALOAD_F T27, R1, R4	// approx: 15: SUB_F T28, T26, T27	// approx: 13: ALOAD_F T26, R0, R4
		float f = Ay * Bx - aX * By;	// approx: 19: SUB_F T31, T29, T30	// approx: 18: MUL_F T30, R5, R10	// approx: 17: MUL_F T29, R6, R9	// approx: 20: MOVE_F R32, T31
		float d = By * Cx - Bx * Cy;	// approx: 23: SUB_F T35, T33, T34	// approx: 22: MUL_F T34, R9, R12	// approx: 21: MUL_F T33, R10, R11	// approx: 24: MOVE_F R36, T35
		
		// additional accept
		f = accept(f);
		d = accept(d);

		if (((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f))) {
			float e = aX * Cy - Ay * Cx;	// approx: 41: MUL_F T47, R5, R12	// approx: 42: MUL_F T48, R6, R11	// approx: 43: SUB_F T49, T47, T48	// approx: 44: MOVE_F R50, T49
			// additional accept
			e = accept(e);
			if ((f > 0)) {
				if ((e >= 0 && e <= f))
					return true;
			} else {
				if ((e <= 0 && e >= f))
					return true;
			}
		}
		return false;
	}

	public static void alloc_TAG1() {}
	public static void alloc_TAG2() {}
	public static void alloc_TAG3() {}
	public static void alloc_TAG4() {}
	public static boolean accept(boolean b) {return b;}
	public static float accept(float f) {return f;}
	public static Vector3f accept_all_FIELD1_TAG1(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD2_TAG1(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD3_TAG1(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD1_TAG3(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD2_TAG3(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD3_TAG3(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD1_TAG4(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD2_TAG4(Vector3f d){return d;}
	public static Vector3f accept_all_FIELD3_TAG4(Vector3f d){return d;}
	public static float[] accept_all_FIELD4_TAG2(float[] d){return d;}
}
