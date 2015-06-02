//import enerj.lang.*;

import chord.analyses.r2.lang.*;

public class Simple{
	
	public static void main(String[] args) {
		int c, d;
		c = 10;
		d = foo (c);
	}

	public static int foo (int a) {
		int b; 
		b = a + 1;
		Relax.relax(b);
		return b;
	}
}

