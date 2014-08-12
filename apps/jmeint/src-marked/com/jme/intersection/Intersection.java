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

import chord.analyses.expax.lang.*;
import chord.analyses.expax.lang.math.*;

import com.jme.math.FastMath;
import com.jme.math.TransformMatrix;
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
	public static double EPSILON = 1e-12;	// op: 1: PUTSTATIC_D DConst: 1.0E-12, .EPSILON
	private static Vector3f tempVa = new Vector3f();	// st: 2: NEW T2, com.jme.math.Vector3f	// st: 2: NEW T2, com.jme.math.Vector3f	// st: 2: NEW T2, com.jme.math.Vector3f	// st: 2: NEW T2, com.jme.math.Vector3f
	private static Vector3f tempVb = new Vector3f();	// st: 6: NEW T4, com.jme.math.Vector3f	// st: 6: NEW T4, com.jme.math.Vector3f	// st: 6: NEW T4, com.jme.math.Vector3f	// st: 6: NEW T4, com.jme.math.Vector3f
	private static Vector3f tempVc = new Vector3f();	// st: 10: NEW T6, com.jme.math.Vector3f	// st: 10: NEW T6, com.jme.math.Vector3f	// st: 10: NEW T6, com.jme.math.Vector3f	// st: 10: NEW T6, com.jme.math.Vector3f
	private static Vector3f tempVd = new Vector3f();	// st: 14: NEW T8, com.jme.math.Vector3f	// st: 14: NEW T8, com.jme.math.Vector3f	// st: 14: NEW T8, com.jme.math.Vector3f	// st: 14: NEW T8, com.jme.math.Vector3f
	private static Vector3f tempVe = new Vector3f();	// st: 18: NEW T10, com.jme.math.Vector3f	// st: 18: NEW T10, com.jme.math.Vector3f	// st: 18: NEW T10, com.jme.math.Vector3f	// st: 18: NEW T10, com.jme.math.Vector3f
	private static float[] tempFa = new float[2];	// st: 22: NEWARRAY T12, IConst: 2, float[
	private static float[] tempFb = new float[2];	// st: 24: NEWARRAY T13, IConst: 2, float[
	private static Vector2f tempV2a = new Vector2f();	// st: 26: NEW T14, com.jme.math.Vector2f	// st: 26: NEW T14, com.jme.math.Vector2f	// st: 26: NEW T14, com.jme.math.Vector2f
	private static Vector2f tempV2b = new Vector2f();	// st: 30: NEW T16, com.jme.math.Vector2f	// st: 30: NEW T16, com.jme.math.Vector2f	// st: 30: NEW T16, com.jme.math.Vector2f

	public static boolean intersection(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector3f u0, Vector3f u1, Vector3f u2) {
		boolean done = false;
		boolean ret = true;	// op: 2: MOVE_I R61, IConst: 1
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
		d1 = -n1.dot(v0);	// op: 21: NEG_F T73, T72	// op: 22: MOVE_F R74, T73

		du0 = n1.dot(u0) + d1;	// op: 24: MOVE_F R76, R74	// op: 25: ADD_F T77, T75, R76	// op: 26: MOVE_F R78, T77
		du1 = n1.dot(u1) + d1;	// op: 29: ADD_F T81, T79, R80	// op: 30: MOVE_F R82, T81	// op: 28: MOVE_F R80, R76
		du2 = n1.dot(u2) + d1;	// op: 32: MOVE_F R84, R80	// op: 33: ADD_F T85, T83, R84	// op: 34: MOVE_F R86, T85
		
		if (ApproxMath.abs(du0) < EPSILON)	// op: 35: MOVE_F R87, R78	// op: 38: GETSTATIC_D T90, .EPSILON	// op: 37: FLOAT_2DOUBLE T89, T88	// op: 39: CMP_DG T91, T89, T90
			du0 = 0.0f;	// op: 41: MOVE_F R92, FConst: 0.0
		if (ApproxMath.abs(du1) < EPSILON)	// op: 44: FLOAT_2DOUBLE T96, T95	// op: 46: CMP_DG T98, T96, T97	// op: 45: GETSTATIC_D T97, .EPSILON	// op: 42: MOVE_F R94, R82
			du1 = 0.0f;	// op: 48: MOVE_F R99, FConst: 0.0
		if (ApproxMath.abs(du2) < EPSILON)	// op: 49: MOVE_F R101, R86	// op: 51: FLOAT_2DOUBLE T103, T102	// op: 52: GETSTATIC_D T104, .EPSILON	// op: 53: CMP_DG T105, T103, T104
			du2 = 0.0f;	// op: 55: MOVE_F R106, FConst: 0.0
		du0du1 = du0 * du1;	// op: 56: MOVE_F R108, R93	// op: 57: MOVE_F R109, R100	// op: 58: MUL_F T110, R108, R109	// op: 59: MOVE_F R111, T110
		du0du2 = du0 * du2;	// op: 60: MOVE_F R112, R108	// op: 61: MOVE_F R113, R107	// op: 62: MUL_F T114, R112, R113	// op: 63: MOVE_F R115, T114

		if (du0du1 > 0.0f && du0du2 > 0.0f) {	// op: 68: CMP_FL T119, R118, FConst: 0.0	// op: 67: MOVE_F R118, R115	// op: 65: CMP_FL T117, R116, FConst: 0.0	// op: 64: MOVE_F R116, R111
			ret = false;	// op: 70: MOVE_I R120, IConst: 0
			done = true;
		} 
		if(!done) {
			u1.subtract(u0, e1);
			u2.subtract(u0, e2);
			e1.cross(e2, n2);
			d2 = -n2.dot(u0);	// op: 79: MOVE_F R131, T130	// op: 78: NEG_F T130, T129
	
			dv0 = n2.dot(v0) + d2;	// op: 83: MOVE_F R135, T134	// op: 81: MOVE_F R133, R131	// op: 82: ADD_F T134, T132, R133
			dv1 = n2.dot(v1) + d2;	// op: 86: ADD_F T138, T136, R137	// op: 85: MOVE_F R137, R133	// op: 87: MOVE_F R139, T138
			dv2 = n2.dot(v2) + d2;	// op: 90: ADD_F T142, T140, R141	// op: 89: MOVE_F R141, R137	// op: 91: MOVE_F R143, T142
			
			if (ApproxMath.abs(dv0) < EPSILON)	// op: 94: FLOAT_2DOUBLE T146, T145	// op: 96: CMP_DG T148, T146, T147	// op: 95: GETSTATIC_D T147, .EPSILON	// op: 92: MOVE_F R144, R135
				dv0 = 0.0f;	// op: 98: MOVE_F R149, FConst: 0.0
			if (ApproxMath.abs(dv1) < EPSILON)	// op: 103: CMP_DG T155, T153, T154	// op: 99: MOVE_F R151, R139	// op: 101: FLOAT_2DOUBLE T153, T152	// op: 102: GETSTATIC_D T154, .EPSILON
				dv1 = 0.0f;	// op: 105: MOVE_F R156, FConst: 0.0
			if (ApproxMath.abs(dv2) < EPSILON)	// op: 109: GETSTATIC_D T161, .EPSILON	// op: 110: CMP_DG T162, T160, T161	// op: 106: MOVE_F R158, R143	// op: 108: FLOAT_2DOUBLE T160, T159
				dv2 = 0.0f;	// op: 112: MOVE_F R163, FConst: 0.0
	
			dv0dv1 = dv0 * dv1;	// op: 115: MUL_F T167, R165, R166	// op: 116: MOVE_F R168, T167	// op: 113: MOVE_F R165, R150	// op: 114: MOVE_F R166, R157
			dv0dv2 = dv0 * dv2;	// op: 117: MOVE_F R169, R165	// op: 118: MOVE_F R170, R164	// op: 119: MUL_F T171, R169, R170	// op: 120: MOVE_F R172, T171
			
			if (dv0dv1 > 0.0f && dv0dv2 > 0.0f) {	// op: 121: MOVE_F R173, R168	// op: 122: CMP_FL T174, R173, FConst: 0.0	// op: 125: CMP_FL T176, R175, FConst: 0.0	// op: 124: MOVE_F R175, R172
				ret = false;	// op: 127: MOVE_I R177, IConst: 0
				done = true;
			} 
			if(!done) {
	
				n1.cross(n2, d);
		
				max = ApproxMath.abs(d.x);	// op: 134: MOVE_F R186, T185	// op: 132: GETFIELD_F T184, R13, .x
				index = 0;	// op: 135: MOVE_I R187, IConst: 0
				bb = ApproxMath.abs(d.y);	// op: 138: MOVE_F R190, T189	// op: 136: GETFIELD_F T188, R13, .y
				cc = ApproxMath.abs(d.z);	// op: 139: GETFIELD_F T191, R13, .z	// op: 141: MOVE_F R193, T192
		
				if (bb > max) {	// op: 143: MOVE_F R195, R186	// op: 144: CMP_FL T196, R194, R195	// op: 142: MOVE_F R194, R190
					max = bb;	// op: 146: MOVE_F R197, R194	// op: 147: MOVE_F R198, R197
					index = 1;	// op: 148: MOVE_I R199, IConst: 1
				}
				if (cc > max) {	// op: 151: CMP_FL T204, R202, R203	// op: 150: MOVE_F R203, R201	// op: 149: MOVE_F R202, R193
					max = cc;	// op: 181: MOVE_F R206, R205	// op: 180: MOVE_F R205, R202
					vp0 = v0.z;	// op: 183: MOVE_F R208, T207	// op: 182: GETFIELD_F T207, R0, .z
					vp1 = v1.z;	// op: 184: GETFIELD_F T209, R1, .z	// op: 185: MOVE_F R210, T209
					vp2 = v2.z;	// op: 187: MOVE_F R212, T211	// op: 186: GETFIELD_F T211, R2, .z
		
					up0 = u0.z;	// op: 188: GETFIELD_F T213, R3, .z	// op: 189: MOVE_F R214, T213
					up1 = u1.z;	// op: 191: MOVE_F R216, T215	// op: 190: GETFIELD_F T215, R4, .z
					up2 = u2.z;	// op: 192: GETFIELD_F T217, R5, .z	// op: 193: MOVE_F R218, T217
		
				} else if (index == 1) {	// op: 153: MOVE_I R219, R200
					vp0 = v0.y;	// op: 167: GETFIELD_F T220, R0, .y	// op: 168: MOVE_F R221, T220
					vp1 = v1.y;	// op: 169: GETFIELD_F T222, R1, .y	// op: 170: MOVE_F R223, T222
					vp2 = v2.y;	// op: 172: MOVE_F R225, T224	// op: 171: GETFIELD_F T224, R2, .y
		
					up0 = u0.y;	// op: 174: MOVE_F R227, T226	// op: 173: GETFIELD_F T226, R3, .y
					up1 = u1.y;	// op: 176: MOVE_F R229, T228	// op: 175: GETFIELD_F T228, R4, .y
					up2 = u2.y;	// op: 178: MOVE_F R231, T230	// op: 177: GETFIELD_F T230, R5, .y
				} else {
					vp0 = v0.x;	// op: 156: MOVE_F R233, T232	// op: 155: GETFIELD_F T232, R0, .x
					vp1 = v1.x;	// op: 158: MOVE_F R235, T234	// op: 157: GETFIELD_F T234, R1, .x
					vp2 = v2.x;	// op: 160: MOVE_F R237, T236	// op: 159: GETFIELD_F T236, R2, .x
		
					up0 = u0.x;	// op: 161: GETFIELD_F T238, R3, .x	// op: 162: MOVE_F R239, T238
					up1 = u1.x;	// op: 163: GETFIELD_F T240, R4, .x	// op: 164: MOVE_F R241, T240
					up2 = u2.x;	// op: 165: GETFIELD_F T242, R5, .x	// op: 166: MOVE_F R243, T242
				}
		
				Vector3f abc = tempVa;
				Vector2f x0x1 = tempV2a;
		
				cond = newComputeIntervals(vp0, vp1, vp2, dv0, dv1, dv2, dv0dv1, dv0dv2, abc, x0x1);	// op: 202: MOVE_F R255, R169	// op: 201: MOVE_F R254, R247	// op: 200: MOVE_F R253, R248	// op: 199: MOVE_F R252, R249	// op: 206: MOVE_F R259, R179	// op: 205: MOVE_F R258, R173	// op: 204: MOVE_F R257, R170	// op: 203: MOVE_F R256, R166	// op: 208: MOVE_I R261, T260
				cond = Accept.accept(cond);	// op: 209: MOVE_I R262, R261
				if (cond) {
					ret = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);	// op: 215: MOVE_I R267, T266
					done = true;
				} 
				if(!done){
					Vector3f def = tempVb;
					Vector2f y0y1 = tempV2b;
					cond = newComputeIntervals(up0, up1, up2, du0, du1, du2, du0du1, du0du2, def, y0y1);	// op: 230: MOVE_F R281, R122	// op: 232: MOVE_I R283, T282	// op: 226: MOVE_F R277, R112	// op: 227: MOVE_F R278, R109	// op: 228: MOVE_F R279, R113	// op: 229: MOVE_F R280, R116	// op: 223: MOVE_F R274, R246	// op: 224: MOVE_F R275, R245	// op: 225: MOVE_F R276, R244
					cond = Accept.accept(cond);	// op: 233: MOVE_I R284, R283
					if (cond) {
						ret = coplanarTriTri(n1, v0, v1, v2, u0, u1, u2);	// op: 239: MOVE_I R289, T288
						done = true;
					}
					if(!done) {
						xx = x0x1.x * x0x1.y;	// op: 244: GETFIELD_F T295, R44, .y	// op: 243: GETFIELD_F T294, R44, .x	// op: 245: MUL_F T296, T294, T295	// op: 246: MOVE_F R297, T296
						yy = y0y1.x * y0y1.y;	// op: 249: MUL_F T300, T298, T299	// op: 250: MOVE_F R301, T300	// op: 247: GETFIELD_F T298, R47, .x	// op: 248: GETFIELD_F T299, R47, .y
						xxyy = xx * yy;	// op: 253: MUL_F T304, R302, R303	// op: 254: MOVE_F R305, T304	// op: 251: MOVE_F R302, R297	// op: 252: MOVE_F R303, R301
				
						tmp = abc.x * xxyy;	// op: 257: MUL_F T308, T306, R307	// op: 258: MOVE_F R309, T308	// op: 255: GETFIELD_F T306, R43, .x	// op: 256: MOVE_F R307, R305
						isect1[0] = tmp + abc.y * x0x1.y * yy;	// op: 259: MOVE_F R310, R309	// op: 260: GETFIELD_F T311, R43, .y	// op: 262: MUL_F T313, T311, T312	// op: 261: GETFIELD_F T312, R44, .y	// op: 264: MUL_F T315, T313, R314	// op: 263: MOVE_F R314, R303	// op: 266: ASTORE_F T316, R14, IConst: 0	// op: 265: ADD_F T316, R310, T315
						isect1[1] = tmp + abc.z * x0x1.x * yy;	// op: 270: MUL_F T320, T318, T319	// op: 269: GETFIELD_F T319, R44, .x	// op: 272: MUL_F T322, T320, R321	// op: 271: MOVE_F R321, R314	// op: 274: ASTORE_F T323, R14, IConst: 1	// op: 273: ADD_F T323, R317, T322	// op: 268: GETFIELD_F T318, R43, .z	// op: 267: MOVE_F R317, R310
				
						tmp = def.x * xxyy;	// op: 276: MOVE_F R325, R307	// op: 275: GETFIELD_F T324, R46, .x	// op: 277: MUL_F T326, T324, R325	// op: 278: MOVE_F R327, T326
						isect2[0] = tmp + def.y * xx * y0y1.y;	// op: 285: ADD_F T334, R328, T333	// op: 286: ASTORE_F T334, R15, IConst: 0	// op: 279: MOVE_F R328, R327	// op: 280: GETFIELD_F T329, R46, .y	// op: 281: MOVE_F R330, R302	// op: 282: MUL_F T331, T329, R330	// op: 283: GETFIELD_F T332, R47, .y	// op: 284: MUL_F T333, T331, T332
						isect2[1] = tmp + def.z * xx * y0y1.x;	// op: 294: ASTORE_F T341, R15, IConst: 1	// op: 293: ADD_F T341, R335, T340	// op: 287: MOVE_F R335, R328	// op: 288: GETFIELD_F T336, R46, .z	// op: 289: MOVE_F R337, R330	// op: 290: MUL_F T338, T336, R337	// op: 291: GETFIELD_F T339, R47, .x	// op: 292: MUL_F T340, T338, T339
				
						sort(isect1);
						sort(isect2);
				
						if (isect1[1] < isect2[0] || isect2[1] < isect1[0]) {	// op: 303: CMP_FG T347, T345, T346	// op: 302: ALOAD_F T346, R14, IConst: 0	// op: 301: ALOAD_F T345, R15, IConst: 1	// op: 299: CMP_FG T344, T342, T343	// op: 298: ALOAD_F T343, R15, IConst: 0	// op: 297: ALOAD_F T342, R14, IConst: 1
							ret = false;	// op: 306: MOVE_I R349, IConst: 0
						} else {
							ret = true;	// op: 305: MOVE_I R348, IConst: 1
						}
					}
				}
			}
		}
		return ret;	// op: 308: MOVE_I R351, R350
	}

	private static void sort(float[] f) {
		if (f[0] > f[1]) {	// op: 1: ALOAD_F T7, R0, IConst: 0	// op: 3: CMP_FL T3, T7, T2	// op: 2: ALOAD_F T2, R0, IConst: 1
			float c = f[0];	// op: 5: ALOAD_F T8, R0, IConst: 0	// op: 6: MOVE_F R9, T8
			f[0] = f[1];	// op: 9: ASTORE_F T6, T5, IConst: 0	// op: 8: ALOAD_F T6, R0, IConst: 1
			f[1] = c;	// op: 11: ASTORE_F R10, R0, IConst: 1	// op: 10: MOVE_F R10, R9
		}
	}

	private static boolean newComputeIntervals(float vv0, float vv1, float vv2,
			float d0, float d1, float d2, float d0d1, float d0d2, Vector3f abc,
			Vector2f x0x1) {

		boolean ret;

		if (d0d1 > 0.0f) {	// op: 2: CMP_FL T14, R6, FConst: 0.0	// op: 1: MOVE_F R6, R6
			abc.x = vv2;	// op: 118: MOVE_F R15, R2	// op: 119: PUTFIELD_F R8, .x, R15
			abc.y = (vv0 - vv2) * d2;	// op: 120: MOVE_F R16, R0	// op: 122: SUB_F T18, R16, R17	// op: 121: MOVE_F R17, R15	// op: 124: MUL_F T20, T18, R19	// op: 123: MOVE_F R19, R5	// op: 125: PUTFIELD_F R8, .y, T20
			abc.z = (vv1 - vv2) * d2;	// op: 126: MOVE_F R21, R1	// op: 128: SUB_F T23, R21, R22	// op: 127: MOVE_F R22, R17	// op: 130: MUL_F T25, T23, R24	// op: 129: MOVE_F R24, R19	// op: 131: PUTFIELD_F R8, .z, T25
			x0x1.x = d2 - d0;	// op: 132: MOVE_F R26, R24	// op: 135: PUTFIELD_F R9, .x, T28	// op: 134: SUB_F T28, R26, R27	// op: 133: MOVE_F R27, R3
			x0x1.y = d2 - d1;	// op: 139: PUTFIELD_F R9, .y, T31	// op: 138: SUB_F T31, R29, R30	// op: 137: MOVE_F R30, R4	// op: 136: MOVE_F R29, R26
			ret = false;	// op: 140: MOVE_I R32, IConst: 0
		} else if (d0d2 > 0.0f) {	// op: 4: MOVE_F R7, R7	// op: 5: CMP_FL T33, R7, FConst: 0.0
			abc.x = vv1;	// op: 95: PUTFIELD_F R8, .x, R34	// op: 94: MOVE_F R34, R1
			abc.y = (vv0 - vv1) * d1;	// op: 97: MOVE_F R36, R34	// op: 96: MOVE_F R35, R0	// op: 101: PUTFIELD_F R8, .y, T39	// op: 100: MUL_F T39, T37, R38	// op: 99: MOVE_F R38, R4	// op: 98: SUB_F T37, R35, R36
			abc.z = (vv2 - vv1) * d1;	// op: 102: MOVE_F R40, R2	// op: 103: MOVE_F R41, R36	// op: 104: SUB_F T42, R40, R41	// op: 105: MOVE_F R43, R38	// op: 106: MUL_F T44, T42, R43	// op: 107: PUTFIELD_F R8, .z, T44
			x0x1.x = d1 - d0;	// op: 108: MOVE_F R45, R43	// op: 109: MOVE_F R46, R3	// op: 110: SUB_F T47, R45, R46	// op: 111: PUTFIELD_F R9, .x, T47
			x0x1.y = d1 - d2;	// op: 112: MOVE_F R48, R45	// op: 113: MOVE_F R49, R5	// op: 114: SUB_F T50, R48, R49	// op: 115: PUTFIELD_F R9, .y, T50
			ret = false;	// op: 116: MOVE_I R51, IConst: 0
		} else if (d1 * d2 > 0.0f || d0 != 0.0f) {	// op: 7: MOVE_F R52, R4	// op: 10: CMP_FL T54, T11, FConst: 0.0	// op: 8: MOVE_F R53, R5	// op: 9: MUL_F T11, R52, R53	// op: 13: CMP_FL T56, R55, FConst: 0.0	// op: 12: MOVE_F R55, R3
			abc.x = vv0;	// op: 70: MOVE_F R99, R0	// op: 71: PUTFIELD_F R8, .x, R99
			abc.y = (vv1 - vv0) * d0;	// op: 73: MOVE_F R101, R99	// op: 74: SUB_F T102, R100, R101	// op: 72: MOVE_F R100, R1	// op: 77: PUTFIELD_F R8, .y, T104	// op: 75: MOVE_F R103, R98	// op: 76: MUL_F T104, T102, R103
			abc.z = (vv2 - vv0) * d0;	// op: 78: MOVE_F R105, R2	// op: 81: MOVE_F R108, R103	// op: 82: MUL_F T109, T107, R108	// op: 79: MOVE_F R106, R101	// op: 80: SUB_F T107, R105, R106	// op: 83: PUTFIELD_F R8, .z, T109
			x0x1.x = d0 - d1;	// op: 87: PUTFIELD_F R9, .x, T112	// op: 85: MOVE_F R111, R52	// op: 86: SUB_F T112, R110, R111	// op: 84: MOVE_F R110, R108
			x0x1.y = d0 - d2;	// op: 90: SUB_F T115, R113, R114	// op: 89: MOVE_F R114, R53	// op: 88: MOVE_F R113, R110	// op: 91: PUTFIELD_F R9, .y, T115
			ret = false;	// op: 92: MOVE_I R116, IConst: 0
		} else if (d1 != 0.0f) {	// op: 16: CMP_FL T58, R57, FConst: 0.0	// op: 15: MOVE_F R57, R52
			abc.x = vv1;	// op: 46: MOVE_F R59, R1	// op: 47: PUTFIELD_F R8, .x, R59
			abc.y = (vv0 - vv1) * d1;	// op: 52: MUL_F T64, T62, R63	// op: 53: PUTFIELD_F R8, .y, T64	// op: 48: MOVE_F R60, R0	// op: 49: MOVE_F R61, R59	// op: 50: SUB_F T62, R60, R61	// op: 51: MOVE_F R63, R57
			abc.z = (vv2 - vv1) * d1;	// op: 57: MOVE_F R68, R63	// op: 56: SUB_F T67, R65, R66	// op: 59: PUTFIELD_F R8, .z, T69	// op: 58: MUL_F T69, T67, R68	// op: 54: MOVE_F R65, R2	// op: 55: MOVE_F R66, R61
			x0x1.x = d1 - d0;	// op: 61: MOVE_F R71, R55	// op: 60: MOVE_F R70, R68	// op: 63: PUTFIELD_F R9, .x, T72	// op: 62: SUB_F T72, R70, R71
			x0x1.y = d1 - d2;	// op: 65: MOVE_F R74, R53	// op: 64: MOVE_F R73, R70	// op: 67: PUTFIELD_F R9, .y, T75	// op: 66: SUB_F T75, R73, R74
			ret = false;	// op: 68: MOVE_I R76, IConst: 0
		} else if (d2 != 0.0f) {	// op: 18: MOVE_F R77, R53	// op: 19: CMP_FL T78, R77, FConst: 0.0
			abc.x = vv2;	// op: 23: PUTFIELD_F R8, .x, R79	// op: 22: MOVE_F R79, R2
			abc.y = (vv0 - vv2) * d2;	// op: 29: PUTFIELD_F R8, .y, T84	// op: 28: MUL_F T84, T82, R83	// op: 27: MOVE_F R83, R77	// op: 26: SUB_F T82, R80, R81	// op: 25: MOVE_F R81, R79	// op: 24: MOVE_F R80, R0
			abc.z = (vv1 - vv2) * d2;	// op: 32: SUB_F T87, R85, R86	// op: 31: MOVE_F R86, R81	// op: 30: MOVE_F R85, R1	// op: 35: PUTFIELD_F R8, .z, T89	// op: 34: MUL_F T89, T87, R88	// op: 33: MOVE_F R88, R83
			x0x1.x = d2 - d0;	// op: 39: PUTFIELD_F R9, .x, T92	// op: 38: SUB_F T92, R90, R91	// op: 37: MOVE_F R91, R55	// op: 36: MOVE_F R90, R88
			x0x1.y = d2 - d1;	// op: 41: MOVE_F R94, R57	// op: 42: SUB_F T95, R93, R94	// op: 43: PUTFIELD_F R9, .y, T95	// op: 40: MOVE_F R93, R90
			ret = false;	// op: 44: MOVE_I R96, IConst: 0
		} else {
			ret = true;	// op: 21: MOVE_I R97, IConst: 1
		}
		return ret;	// op: 142: MOVE_I R118, R117
	}

	private static boolean coplanarTriTri(Vector3f n, Vector3f v0, Vector3f v1,
			Vector3f v2, Vector3f u0, Vector3f u1, Vector3f u2) {
		Vector3f a = new Vector3f();	// st: 1: NEW T23, com.jme.math.Vector3f	// st: 1: NEW T23, com.jme.math.Vector3f	// st: 1: NEW T23, com.jme.math.Vector3f	// st: 1: NEW T23, com.jme.math.Vector3f
		short i0, i1;
		boolean ret = false;

		a.x = ApproxMath.abs(n.x);	// op: 8: PUTFIELD_F R9, .x, T26	// op: 6: GETFIELD_F T25, R0, .x
		a.y = ApproxMath.abs(n.y);	// op: 9: GETFIELD_F T27, R0, .y	// op: 11: PUTFIELD_F R9, .y, T28
		a.z = ApproxMath.abs(n.z);	// op: 12: GETFIELD_F T29, R0, .z	// op: 14: PUTFIELD_F R9, .z, T30
		
		if (a.x > a.y) {	// op: 15: GETFIELD_F T31, R9, .x	// op: 16: GETFIELD_F T32, R9, .y	// op: 17: CMP_FL T33, T31, T32
			if (a.x > a.z) {	// op: 30: CMP_FL T36, T34, T35	// op: 29: GETFIELD_F T35, R9, .z	// op: 28: GETFIELD_F T34, R9, .x
				i0 = 1; 
				i1 = 2;
			} else {
				i0 = 0; 
				i1 = 1;
			}
		} else {
			if (a.z > a.y) {	// op: 19: GETFIELD_F T41, R9, .z	// op: 20: GETFIELD_F T42, R9, .y	// op: 21: CMP_FL T43, T41, T42
				i0 = 0; 
				i1 = 1;
			} else {
				i0 = 0; 
				i1 = 2;
			}
		}

		float[] v0f = new float[3];	// st: 38: NEWARRAY T50, IConst: 3, float[
		v0.toArray(v0f);
		float[] v1f = new float[3];	// st: 41: NEWARRAY T52, IConst: 3, float[
		v1.toArray(v1f);
		float[] v2f = new float[3];	// st: 44: NEWARRAY T54, IConst: 3, float[
		v2.toArray(v2f);
		float[] u0f = new float[3];	// st: 47: NEWARRAY T56, IConst: 3, float[
		u0.toArray(u0f);
		float[] u1f = new float[3];	// st: 50: NEWARRAY T58, IConst: 3, float[
		u1.toArray(u1f);
		float[] u2f = new float[3];	// st: 53: NEWARRAY T60, IConst: 3, float[
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

		return ret;	// op: 79: MOVE_I R84, R77
	}

	private static boolean pointInTri(float[] V0, float[] U0, float[] U1, float[] U2, int i0, int i1) {
		boolean ret;
		float a, b, c, d0, d1, d2;
		a = U1[i1] - U0[i1];	// op: 5: SUB_F T22, T19, T21	// op: 6: MOVE_F R23, T22	// op: 4: ALOAD_F T21, R1, R20	// op: 2: ALOAD_F T19, R2, R18
		b = -(U1[i0] - U0[i0]);	// op: 13: MOVE_F R30, T29	// op: 11: SUB_F T28, T25, T27	// op: 12: NEG_F T29, T28	// op: 10: ALOAD_F T27, R1, R26	// op: 8: ALOAD_F T25, R2, R24
		c = -a * U0[i0] - b * U0[i1];	// op: 24: MOVE_F R41, T40	// op: 23: SUB_F T40, T35, T39	// op: 19: MOVE_F R36, R30	// op: 22: MUL_F T39, R36, T38	// op: 21: ALOAD_F T38, R1, R37	// op: 17: ALOAD_F T34, R1, R33	// op: 18: MUL_F T35, T32, T34	// op: 15: NEG_F T32, R31	// op: 14: MOVE_F R31, R23
		d0 = a * V0[i0] + b * V0[i1] + c;	// op: 35: ADD_F T52, T50, R51	// op: 36: MOVE_F R53, T52	// op: 32: MUL_F T49, R46, T48	// op: 31: ALOAD_F T48, R0, R47	// op: 34: MOVE_F R51, R41	// op: 33: ADD_F T50, T45, T49	// op: 28: MUL_F T45, R42, T44	// op: 27: ALOAD_F T44, R0, R43	// op: 29: MOVE_F R46, R36	// op: 25: MOVE_F R42, R31

		a = U2[i1] - U1[i1];	// op: 40: ALOAD_F T57, R2, R56	// op: 41: SUB_F T58, T55, T57	// op: 42: MOVE_F R59, T58	// op: 38: ALOAD_F T55, R3, R54
		b = -(U2[i0] - U1[i0]);	// op: 47: SUB_F T64, T61, T63	// op: 48: NEG_F T65, T64	// op: 49: MOVE_F R66, T65	// op: 44: ALOAD_F T61, R3, R60	// op: 46: ALOAD_F T63, R2, R62
		c = -a * U1[i0] - b * U1[i1];	// op: 50: MOVE_F R67, R59	// op: 60: MOVE_F R77, T76	// op: 59: SUB_F T76, T71, T75	// op: 54: MUL_F T71, T68, T70	// op: 53: ALOAD_F T70, R2, R69	// op: 51: NEG_F T68, R67	// op: 58: MUL_F T75, R72, T74	// op: 57: ALOAD_F T74, R2, R73	// op: 55: MOVE_F R72, R66
		d1 = a * V0[i0] + b * V0[i1] + c;	// op: 69: ADD_F T86, T81, T85	// op: 70: MOVE_F R87, R77	// op: 67: ALOAD_F T84, R0, R83	// op: 68: MUL_F T85, R82, T84	// op: 71: ADD_F T88, T86, R87	// op: 72: MOVE_F R89, T88	// op: 61: MOVE_F R78, R67	// op: 65: MOVE_F R82, R72	// op: 64: MUL_F T81, R78, T80	// op: 63: ALOAD_F T80, R0, R79

		a = U0[i1] - U2[i1];	// op: 77: SUB_F T94, T91, T93	// op: 78: MOVE_F R95, T94	// op: 76: ALOAD_F T93, R3, R92	// op: 74: ALOAD_F T91, R1, R90
		b = -(U0[i0] - U2[i0]);	// op: 84: NEG_F T101, T100	// op: 83: SUB_F T100, T97, T99	// op: 85: MOVE_F R102, T101	// op: 82: ALOAD_F T99, R3, R98	// op: 80: ALOAD_F T97, R1, R96
		c = -a * U2[i0] - b * U2[i1];	// op: 91: MOVE_F R108, R102	// op: 94: MUL_F T111, R108, T110	// op: 93: ALOAD_F T110, R3, R109	// op: 96: MOVE_F R113, T112	// op: 95: SUB_F T112, T107, T111	// op: 86: MOVE_F R103, R95	// op: 87: NEG_F T104, R103	// op: 90: MUL_F T107, T104, T106	// op: 89: ALOAD_F T106, R3, R105
		d2 = a * V0[i0] + b * V0[i1] + c;	// op: 107: ADD_F T124, T122, R123	// op: 108: MOVE_F R125, T124	// op: 99: ALOAD_F T116, R0, R115	// op: 100: MUL_F T117, R114, T116	// op: 101: MOVE_F R118, R108	// op: 103: ALOAD_F T120, R0, R119	// op: 104: MUL_F T121, R118, T120	// op: 105: ADD_F T122, T117, T121	// op: 106: MOVE_F R123, R113	// op: 97: MOVE_F R114, R103

		if (d0 * d1 > 0.0 && d0 * d2 > 0.0)	// op: 109: MOVE_F R126, R53	// op: 110: MOVE_F R127, R89	// op: 111: MUL_F T128, R126, R127	// op: 112: FLOAT_2DOUBLE T129, T128	// op: 113: CMP_DL T130, T129, DConst: 0.0	// op: 115: MOVE_F R131, R126	// op: 116: MOVE_F R132, R125	// op: 117: MUL_F T133, R131, R132	// op: 118: FLOAT_2DOUBLE T134, T133	// op: 119: CMP_DL T135, T134, DConst: 0.0
			ret = true;	// op: 122: MOVE_I R136, IConst: 1
		else
			ret = false;	// op: 121: MOVE_I R137, IConst: 0
		return ret;	// op: 124: MOVE_I R139, R138
	}

	private static boolean edgeAgainstTriEdges(float[] v0, float[] v1,
			float[] u0, float[] u1, float[] u2, int i0, int i1) {
		boolean ret = false;	// op: 1: MOVE_I R13, IConst: 0
		float aX, aY;
		aX = v1[i0] - v0[i0];	// op: 7: MOVE_F R19, T18	// op: 6: SUB_F T18, T15, T17	// op: 5: ALOAD_F T17, R0, R16	// op: 3: ALOAD_F T15, R1, R14
		aY = v1[i1] - v0[i1];	// op: 13: MOVE_F R25, T24	// op: 12: SUB_F T24, T21, T23	// op: 11: ALOAD_F T23, R0, R22	// op: 9: ALOAD_F T21, R1, R20
		
		if (edgeEdgeTest(v0, u0, u1, i0, i1, aX, aY)) 	// op: 17: MOVE_F R29, R25	// op: 16: MOVE_F R28, R19
			ret = true;	// op: 20: MOVE_I R31, IConst: 1
		if (edgeEdgeTest(v0, u1, u2, i0, i1, aX, aY)) 	// op: 23: MOVE_F R35, R28	// op: 24: MOVE_F R36, R29
			ret = true;	// op: 27: MOVE_I R38, IConst: 1
		if (edgeEdgeTest(v0, u2, u0, i0, i1, aX, aY)) 	// op: 30: MOVE_F R42, R35	// op: 31: MOVE_F R43, R36
			ret = true;	// op: 34: MOVE_I R45, IConst: 1
		return ret;	// op: 35: MOVE_I R47, R46
	}

	private static boolean edgeEdgeTest(float[] v0, float[] u0, float[] u1,
			int i0, int i1, float aX, float Ay) {
		boolean ret = false;	// op: 1: MOVE_I R18, IConst: 0
		float Bx = u0[i0] - u1[i0];	// op: 6: SUB_F T23, T20, T22	// op: 5: ALOAD_F T22, R2, R21	// op: 3: ALOAD_F T20, R1, R19	// op: 7: MOVE_F R24, T23
		float By = u0[i1] - u1[i1];	// op: 9: ALOAD_F T26, R1, R25	// op: 13: MOVE_F R30, T29	// op: 12: SUB_F T29, T26, T28	// op: 11: ALOAD_F T28, R2, R27
		float Cx = v0[i0] - u0[i0];	// op: 18: SUB_F T35, T32, T34	// op: 17: ALOAD_F T34, R1, R33	// op: 15: ALOAD_F T32, R0, R31	// op: 19: MOVE_F R36, T35
		float Cy = v0[i1] - u0[i1];	// op: 21: ALOAD_F T38, R0, R37	// op: 23: ALOAD_F T40, R1, R39	// op: 24: SUB_F T41, T38, T40	// op: 25: MOVE_F R42, T41
		float f = Ay * Bx - aX * By;	// op: 26: MOVE_F R43, R6	// op: 27: MOVE_F R44, R24	// op: 28: MUL_F T45, R43, R44	// op: 29: MOVE_F R46, R5	// op: 30: MOVE_F R47, R30	// op: 31: MUL_F T48, R46, R47	// op: 32: SUB_F T49, T45, T48	// op: 33: MOVE_F R50, T49
		float d = By * Cx - Bx * Cy;	// op: 36: MUL_F T53, R51, R52	// op: 35: MOVE_F R52, R36	// op: 38: MOVE_F R55, R42	// op: 37: MOVE_F R54, R44	// op: 40: SUB_F T57, T53, T56	// op: 39: MUL_F T56, R54, R55	// op: 41: MOVE_F R58, T57	// op: 34: MOVE_F R51, R47
		
		if ((f > 0 && d >= 0 && d <= f) || (f < 0 && d <= 0 && d >= f)) {	// op: 42: MOVE_F R59, R50	// op: 43: CMP_FL T60, R59, FConst: 0.0	// op: 46: CMP_FL T62, R61, FConst: 0.0	// op: 45: MOVE_F R61, R58	// op: 60: CMP_FL T74, R72, R73	// op: 59: MOVE_F R73, R68	// op: 58: MOVE_F R72, R70	// op: 49: MOVE_F R64, R59	// op: 50: CMP_FG T65, R63, R64	// op: 48: MOVE_F R63, R61	// op: 55: MOVE_F R70, R66	// op: 56: CMP_FG T71, R70, FConst: 0.0	// op: 52: MOVE_F R68, R67	// op: 53: CMP_FG T69, R68, FConst: 0.0
			float e = aX * Cy - Ay * Cx;	// op: 63: MOVE_F R77, R55	// op: 62: MOVE_F R76, R46	// op: 69: MOVE_F R83, T82	// op: 68: SUB_F T82, T78, T81	// op: 67: MUL_F T81, R79, R80	// op: 66: MOVE_F R80, R52	// op: 65: MOVE_F R79, R43	// op: 64: MUL_F T78, R76, R77
			if (f > 0) {	// op: 71: CMP_FL T85, R84, FConst: 0.0	// op: 70: MOVE_F R84, R75
				if (e >= 0 && e <= f)	// op: 85: MOVE_F R89, R84	// op: 84: MOVE_F R88, R86	// op: 86: CMP_FG T90, R88, R89	// op: 81: MOVE_F R86, R83	// op: 82: CMP_FL T87, R86, FConst: 0.0
					ret = true;	// op: 88: MOVE_I R91, IConst: 1
			} else {
				if (e <= 0 && e >= f)	// op: 76: MOVE_F R94, R92	// op: 77: MOVE_F R95, R84	// op: 78: CMP_FL T96, R94, R95	// op: 73: MOVE_F R92, R83	// op: 74: CMP_FG T93, R92, FConst: 0.0
					ret = true;	// op: 80: MOVE_I R97, IConst: 1
			}
		}
		return ret;	// op: 90: MOVE_I R99, R98
	}
}
