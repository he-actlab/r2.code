package jnt.scimark2;

public class kernel
{

	public static double measureFFT(int N, double mintime, Random R)
	{
		double x[] = RandomVector(2*N, R);
		double oldx[] = NewVectorCopy(x);
		long cycles = 100;

		for (int i=0; i<cycles; i++)
		{
			FFT.transform(x);	// forward transform
			FFT.inverse(x);		// backward transform
		}

		x = accept_all_FIELD1_TAG1(x);
		System.out.print("FFT vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print((x[i]) + " ");	// approx: 28: ALOAD_D T12, R22, R26
		}
		System.out.println("");
		x = precise_all_FIELD1_TAG1(x);

		return 0.0;
	}

	private static  double[] NewVectorCopy( double x[])
	{
		int N = x.length;

		double y[] = new  double[N];
		for (int i=0; i<N; i++)
			y[i] = x[i];	// approx: 8: ALOAD_D T6, R0, R9	// approx: 9: ASTORE_D T6, R4, R9

		return y;
	}

	private static  double[] RandomVector(int N, Random R)
	{
		alloc_TAG1();
		double A[] = new  double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 	// approx: 8: ASTORE_D T5, R3, R8
		return A;
	}

	private static void alloc_TAG1(){};
	private static double[] accept_all_FIELD1_TAG1(double[] d){return d;}
	private static double[] precise_all_FIELD1_TAG1(double[] d){return d;}
	
}
