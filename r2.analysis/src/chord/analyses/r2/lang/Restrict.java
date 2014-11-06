package chord.analyses.r2.lang;

public class Restrict {
	public static float restrict(float f){return f;}
	public static int restrict(int i){return i;}
	public static double restrict(double d){return d;}
	public static byte restrict(byte b){return b;}
	public static char restrict(char c){return c;}
	public static short restrict(short s){return s;}
	public static long restrict(long l){return l;}
	public static boolean restrict(boolean b){return b;}
	
	//TODO generate enough possible precise_all
	public static double[] restrict_all_FIELD1_TAG1(double[] d){return d;}
	public static int[] restrict_all_FIELD2_TAG1(int[] arr) {return arr;}
}
