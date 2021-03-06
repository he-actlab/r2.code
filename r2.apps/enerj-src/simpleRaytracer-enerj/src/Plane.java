/*
 * Stupid simple Raytracer. 
 */
import enerj.lang.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;


public class Plane
{
	int w,h;
	float k; // what the hell is this variable for?
	MemoryImageSource idx;
	Image buffer;
	@Approx int pixels[];
	int texture,light;
	@Approx float lcoff;
	float sng; // could maybe make approximate
	int numIterations =0;

	public void init(String[] args)
	{
		Dimension dd=new Dimension(400, 256);
		w=dd.width;
		h=dd.height;
		texture=Integer.parseInt(args[0]);//getParameter("texture"));
		light=Integer.parseInt(args[1]);//getParameter("light"));
		pixels=new @Approx int[w*h];
		int index,x,y; //not approx --> for loops and array indexing.
		@Approx float xe,ye,ze,xd,yd,zd;
		@Approx float ix,iy,iz;
		@Approx float nx,ny,nz;
		@Approx float lx,ly,lz;
		float lly; 
		lly=Integer.parseInt(args[2]);//getParameter("lighty"));
		ye=Integer.parseInt(args[3]); //getParameter("viewy"));

		nx=0;
		ny=1;
		nz=0;
		int bl=(255<<24); // this stands for black, a constant, maybe?
		@Approx float t; //who knows
		@Approx float l; //who knows
		float w1,h1; //positioning in image? so don't make approx?
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

				if(Endorsements.endorse((k-ye)*yd)<=0) {
					t=-1;
				} else {
					t=(k-ye)/yd;
				}

				index=y*w+x;
				if(Endorsements.endorse(t)>=0)
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
					sng=(float)Math.sqrt(Endorsements.endorse(lx*lx+ly*ly+lz*lz));
					// sng=1.7f/sng;
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
			System.out.println(Endorsements.endorse(pixels[i] & 0xff)+"\n");
			System.out.println(Endorsements.endorse((pixels[i] >> 8) & 0xff)+"\n");
			System.out.println(Endorsements.endorse((pixels[i] >> 16) & 0xff)+"\n");
		}
	}

	public @Approx int texture(@Approx float x,@Approx float y, @Approx float z) {
		int v;
		@Approx int col;
		@Approx int r,g,b;
		r=255;
		b=0;
		col=0;
		if(light!=0) {
			r=(@Approx int)(255*lcoff);
		}
		b=r;

		if(texture==1) {
			col=(255<<24)|(255<<16);
		} else if(texture==2) {
			v=(Math.round(Endorsements.endorse(x))+Math.round(Endorsements.endorse(z))) %2;
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
