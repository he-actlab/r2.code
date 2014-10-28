/*
 * Stupid simple Raytracer. 
 */

import java.awt.*;

/**
 Evaluation for R2 framework
*/

import chord.analyses.r2.lang.*;
import chord.analyses.r2.lang.math.*;

public class Plane {
	int w, h;
	float k; 
	Image buffer;
	int pixels[];
	int texture, light;
	float lcoff;
	float sng; 
	int numIterations = 0;
	
	int index,x,y; 
	float ix,iy,iz;
	float lly; 
	float t; 
	float xe,ye,ze,xd,yd,zd;
	float lx,ly,lz;
	float nx,ny,nz;
	int r,g,b;
	int bl;

	public void init(String[] args)
	{
		Dimension dd=new Dimension(400, 256);
		w=dd.width;
		h=dd.height;
		texture=Integer.parseInt(args[0]);
		light=Integer.parseInt(args[1]);
		pixels=new int[w*h];	
		lly=Integer.parseInt(args[2]);	
		ye=Integer.parseInt(args[3]);	

		nx=0;	
		ny=1;	
		nz=0;	
		bl=(255<<24);

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

				if((k-ye)*yd <= 0) {
					t=-1;	
				} else {
					//t=(k-ye)/yd;
					t = -1;
				}

				index=y*w+x;
				
				float tmpt = t; 
				Relax.relax(tmpt);
				if(tmpt >= 0)
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
			int pixel_i = pixels[i];	

			Relax.relax(pixel_i);

			System.out.println((pixel_i & 0xff)+"\n");
			System.out.println(((pixel_i >> 8) & 0xff)+"\n");
			System.out.println(((pixel_i >> 16) & 0xff)+"\n");

			Restrict.restrict(pixel_i);
		}
	}

	public int texture( float x, float y,  float z) {
		int v;
		int col;
		r=255;	
		g = (int)y;
		b=0;		
		col=0;	
		if(light!=0) {
			r=( int)(255*lcoff);
		}
		b=r;	
		while (g < 0) break;

		if(texture==1) {
			col=(255<<24)|(255<<16);
		} else if(texture==2) {
			int mrx = ApproxMath.round(x);
			int mrz = ApproxMath.round(z);
			v = (mrx + mrz) %2;	
	
			if(v==0) {
				col=(255<<24)|b;	
			} else {
				col=(255<<24)|(r<<16);	
			}
		}

		if(numIterations == 25) {
			System.gc(); 
		}
		return col;	
	}

	public static void main(String[] args) {
		Plane p = new Plane();	
		p.init(args);
	}

}
