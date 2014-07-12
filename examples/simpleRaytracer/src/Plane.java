/*
 * Stupid simple Raytracer. 
 */

import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class Plane {
	int w, h;
	float k; 
	Image buffer;
	int pixels[];
	int texture, light;
	float lcoff;
	float sng; 
	int numIterations = 0;

	public void init(String[] args)
	{
		Dimension dd=new Dimension(400, 256);
		w=dd.width;
		h=dd.height;
		texture=Integer.parseInt(args[0]);
		light=Integer.parseInt(args[1]);
		alloc_TAG1();
		pixels=new int[w*h];
		int index,x,y; 
		float xe,ye,ze,xd,yd,zd;
		float ix,iy,iz;
		float nx,ny,nz;
		float lx,ly,lz;
		float lly; 
		lly=Integer.parseInt(args[2]);	// approx: 27: MOVE_F R11, T56
		ye=Integer.parseInt(args[3]);	// approx: 31: MOVE_F R60, T59

		nx=0;	// approx: 32: MOVE_F R13, FConst: 0.0
		ny=1;	// approx: 33: MOVE_F R14, FConst: 1.0
		nz=0;	// approx: 34: MOVE_F R15, FConst: 0.0
		int bl=(255<<24);	// approx: 35: MOVE_I R16, IConst: -16777216
		float t; 
		float l; 
		float w1,h1; 
		w1=w/2;	// approx: 36: GETFIELD_I T61, R0, .w	// approx: 37: DIV_I T62, T61, IConst: 2	// approx: 39: MOVE_F R17, T63
		h1=h/2;	// approx: 40: GETFIELD_I T64, R0, .h	// approx: 41: DIV_I T65, T64, IConst: 2	// approx: 43: MOVE_F R18, T66

		xe=0;	// approx: 44: MOVE_F R19, FConst: 0.0

		ze=0;	// approx: 45: MOVE_F R20, FConst: 0.0
		k=-1;	// approx: 46: PUTFIELD_F R0, .k, FConst: -1.0

		for(y=0;y<h;y++)
		{
			for(x=0;x<w;x++)
			{
				t=-1;		// approx: 110: MOVE_F R75, FConst: -1.0
				xd=(x-w1)/w1;	// approx: 112: SUB_F T77, T76, R17	// approx: 113: DIV_F T78, T77, R17	// approx: 114: MOVE_F R79, T78
				yd=(h1-y)/h1;	// approx: 118: MOVE_F R83, T82	// approx: 117: DIV_F T82, T81, R18	// approx: 116: SUB_F T81, R18, T80
				zd=-1;	// approx: 119: MOVE_F R84, FConst: -1.0
				l=xd*xd+yd*yd+zd*zd;	// approx: 125: MOVE_F R30, T89	// approx: 124: ADD_F T89, T87, T88	// approx: 123: MUL_F T88, R84, R84	// approx: 122: ADD_F T87, T85, T86	// approx: 121: MUL_F T86, R83, R83	// approx: 120: MUL_F T85, R79, R79
				xd/=l;	// approx: 126: DIV_F T90, R79, R30	// approx: 127: MOVE_F R91, T90
				yd/=l;	// approx: 129: MOVE_F R93, T92	// approx: 128: DIV_F T92, R83, R30
				zd/=l;	// approx: 130: DIV_F T94, R84, R30	// approx: 131: MOVE_F R95, T94

				// additional accept
				yd = accept(yd);
				ye = accept(ye);
				accept_all_FIELD1_TAG2(this);

				if(((k-ye)*yd)<=0) {
					t=-1;	// approx: 146: MOVE_F R105, FConst: -1.0
				} else {
					t=(k-ye)/yd;	// approx: 142: GETFIELD_F T106, R0, .k	// approx: 145: MOVE_F R109, T108	// approx: 143: SUB_F T107, T106, R99	// approx: 144: DIV_F T108, T107, R97
				}
				// additional accept
				t = accept(t);

				index=y*w+x;
				if((t)>=0)
				{
					ix=xe+t*xd;	// approx: 158: MUL_F T117, R112, R91	// approx: 160: MOVE_F R32, T118	// approx: 159: ADD_F T118, R19, T117
					iy=ye+t*yd;	// approx: 161: MUL_F T119, R112, R97	// approx: 162: ADD_F T120, R99, T119	// approx: 163: MOVE_F R33, T120
					iz=ze+t*zd;		  		// approx: 165: ADD_F T122, R20, T121	// approx: 166: MOVE_F R34, T122	// approx: 164: MUL_F T121, R112, R95
					lx=0;	// approx: 167: MOVE_F R123, FConst: 0.0
					ly=lly;	// approx: 168: MOVE_F R124, R11
					lz=0;	// approx: 169: MOVE_F R125, FConst: 0.0
					lx=lx-ix;	// approx: 170: SUB_F T126, R123, R32	// approx: 171: MOVE_F R127, T126
					ly=ly-iy;	// approx: 173: MOVE_F R129, T128	// approx: 172: SUB_F T128, R124, R33
					lz=lz-iz;	// approx: 174: SUB_F T130, R125, R34	// approx: 175: MOVE_F R131, T130
					sng=(float)Math.sqrt((lx*lx+ly*ly+lz*lz));		// approx: 176: MUL_F T132, R127, R127	// approx: 184: PUTFIELD_F R0, .sng, T139	// approx: 180: ADD_F T136, T134, T135	// approx: 179: MUL_F T135, R131, R131	// approx: 178: ADD_F T134, T132, T133	// approx: 177: MUL_F T133, R129, R129
					sng=1.0f/sng;	// approx: 188: PUTFIELD_F T140, .sng, T142	// approx: 187: DIV_F T142, FConst: 1.0, T141	// approx: 186: GETFIELD_F T141, R0, .sng
					lcoff=(lx*nx+ly*ny+lz*nz)*sng;	// approx: 192: MUL_F T146, R131, R15	// approx: 191: ADD_F T145, T143, T144	// approx: 190: MUL_F T144, R129, R14	// approx: 189: MUL_F T143, R127, R13	// approx: 197: PUTFIELD_F T148, .lcoff, T150	// approx: 195: GETFIELD_F T149, R0, .sng	// approx: 196: MUL_F T150, T147, T149	// approx: 193: ADD_F T147, T145, T146
					pixels[index]=texture(ix,iy,iz);	// approx: 200: ASTORE_I T152, T151, R31
				} else {
					pixels[index]=bl;	// approx: 157: ASTORE_I R16, T153, R31
				}
				numIterations++;
			}
		}
		
		pixels = accept_all_FIELD2_TAG1(pixels);
		
		for (int i = 0; i < pixels.length; i++) {
			System.out.println((pixels[i] & 0xff)+"\n");
			System.out.println(((pixels[i] >> 8) & 0xff)+"\n");
			System.out.println(((pixels[i] >> 16) & 0xff)+"\n");
		}

		pixels = precise_all_FIELD2_TAG1(pixels);
	}

	public  int texture( float x, float y,  float z) {
		int v;
		int col;
		int r,g,b;
		r=255;	// approx: 1: MOVE_I R12, IConst: 255
		b=0;		// approx: 2: MOVE_I R13, IConst: 0
		col=0;	// approx: 3: MOVE_I R14, IConst: 0
		if(light!=0) {
			r=( int)(255*lcoff);	// approx: 6: GETFIELD_F T8, R0, .lcoff	// approx: 9: MOVE_I R18, T17	// approx: 7: MUL_F T16, FConst: 255.0, T8
		}
		b=r;	// approx: 10: MOVE_I R20, R19

		// additional accept
		x = accept(x);
		z = accept(z);

		if(texture==1) {
			col=(255<<24)|(255<<16);	// approx: 33: MOVE_I R24, IConst: -65536
		} else if(texture==2) {
			v=(Math.round((x))+Math.round((z))) %2;	// approx: 23: MOVE_I R30, T29	// approx: 22: REM_I T29, T28, IConst: 2	// approx: 21: ADD_I T28, T26, T27
			// additional accept
			v = accept(v);
			if(v==0) {
				col=(255<<24)|b;	// approx: 30: OR_I T33, IConst: -16777216, R20	// approx: 31: MOVE_I R34, T33
			} else {
				col=(255<<24)|(r<<16);	// approx: 27: SHL_I T35, R19, IConst: 16	// approx: 28: OR_I T36, IConst: -16777216, T35	// approx: 29: MOVE_I R37, T36
			}
		}

		if(numIterations == 25) {
			System.gc(); 
		}
		return col;
	}

	public int accept(int i){return i;}
	public float accept(float i){return i;}
	public int[] accept_all_FIELD2_TAG1(int[] p){return p;}
	public int[] precise_all_FIELD2_TAG1(int[] p){return p;}
	public Plane accept_all_FIELD1_TAG2(Plane p){return p;}
	public void alloc_TAG1(){}
	public static void alloc_TAG2(){}

	public static void main(String[] args) {
		alloc_TAG2();
		Plane p = new Plane();
		p.init(args);
	}

}
