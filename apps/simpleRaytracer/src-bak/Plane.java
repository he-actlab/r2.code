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

				// additional accept
				yd = accept(yd);
				ye = accept(ye);
				accept_all_FIELD1_TAG2(this);

				float cond = ((k-ye)*yd);
				cond = accept(cond);
				if(cond<=0) {
					t=-1;	
				} else {
					t=(k-ye)/yd;
				}
				// additional accept
				t = accept(t);	

				index=y*w+x;
				if((t)>=0)
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
					param = accept(param);	
					sng=(float)Math.sqrt(param);	
					sng = accept(sng);
					sng=1.0f/sng;
					lcoff=(lx*nx+ly*ny+lz*nz)*sng;	
					pixels[index]=texture(ix,iy,iz);
				} else {
					pixels[index]=bl;	
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
		r=255;	
		b=0;		
		col=0;	
		if(light!=0) {
			r=( int)(255*lcoff);
		}
		b=r;	

		if(texture==1) {
			col=(255<<24)|(255<<16);
		} else if(texture==2) {
			x = accept(x);	
			z = accept(z);	
			v=(Math.round((x))+Math.round((z))) %2;	
			// additional accept
			v = accept(v);	
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
