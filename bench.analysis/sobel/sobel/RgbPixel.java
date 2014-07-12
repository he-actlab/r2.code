package Sobel;

public class RgbPixel {
	public int r = 0;
	public int g = 0;
	public int b = 0;
	
	public RgbPixel() {
	}
	
	public String toString() {
		return "(" + r + "," + g + "," + b + ")"; 
	}
	
	public int luminance() {
		double rC = 0.30;
		double gC = 0.59;
		double bC = 0.11;
		
		return (int)(rC * r + gC * g + bC * b + 0.5) % 256;
	}
	
}
