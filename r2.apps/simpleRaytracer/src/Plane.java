/*
 * Stupid simple Raytracer. 
 */

/**
 Evaluation for FlexJava framework
*/

import java.awt.*;

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.ApproxMath;

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
		pixels=new int[w*h];	
		int index,x,y; 
		float xe,ye,ze,xd,yd,zd;
		float ix,iy,iz;
		float nx,ny,nz;
		float lx,ly,lz;
		float lly; 
		lly=Integer.parseInt(args[2]);	
		ye=Integer.parseInt(args[3]);	

		nx=0;	
		ny=1;	
		nz=0;	
		int bl=(255<<24);
		float t; 
		float l; 
		float w1,h1; 
		w1=w/2;	
		h1=h/2;	

		xe=0;	

		ze=0;	
		k=-1;	

		for(y=0;y<h;y++)
		{
			for(x=0;x<w;x++)
			{
				t=-1;	
				xd=(x-w1)/w1;
				yd=(h1-y)/h1;
				zd=-1;	
				l=xd*xd+yd*yd+zd*zd;
				xd/=l;	
				yd/=l;	
				zd/=l;	

				float cond = (k-ye)*yd;
				if(Loosen.loosen(cond)<=0) {
					t=-1;	
				} else {
					t=(k-ye)/yd;
				}

				index=y*w+x;
				
				if(Loosen.loosen(t) >= 0)
				{
					ix=xe+t*xd;
					iy=ye+t*yd;
					iz=ze+t*zd;
					lx=0;	
					ly=lly;	
					lz=0;	
					lx=lx-ix;	
					ly=ly-iy;	
					lz=lz-iz;	
					float param = (lx*lx+ly*ly+lz*lz);	
					sng=(float)ApproxMath.sqrt(param);
					sng=1.0f/sng;
					lcoff=(lx*nx+ly*ny+lz*nz)*sng;	
					pixels[index]=texture(ix,iy,iz);
				} else {
					pixels[index]=bl;	
				}
				numIterations++;
			}
		}

		for (int i = 0; i < pixels.length; i++) {
		
			int pixels_i = pixels[i];

			Loosen.loosen(pixels_i);

			System.out.println((pixels_i & 0xff)+"\n");
			System.out.println(((pixels_i >> 8) & 0xff)+"\n");
			System.out.println(((pixels_i >> 16) & 0xff)+"\n");

			Tighten.tighten(pixels_i);
		}
	}

	public int texture( float x, float y,  float z) {
		int v;
		int col;
		int r,g,b;
		r=255;	
		b=0;		
		col=0;	
		if(light!=0) {
			r=( int)(255*lcoff);
		}
		b=r;	

		if(Loosen.loosen(texture)==1) {
			col=(255<<24)|(255<<16);
		} else if(texture==2) {
			int mrx = ApproxMath.round(x);
			int mrz = ApproxMath.round(z);
			v = (mrx + mrz) %2;	
	
			if(Loosen.loosen(v)==0) {
				col=(255<<24)|b;	
			} else {
				col=(255<<24)|(r<<16);	
			}
		}

		if(numIterations == 25) {
			System.gc(); 
		}

		Loosen.loosen(col);

		return col;	
	}

	public static void main(String[] args) {
		Plane p = new Plane();	
		p.init(args);

		for (int i = 0; i < pixels.length; i++) {
			Loosen.loosen(pixels[i]);
		}
	}

}
