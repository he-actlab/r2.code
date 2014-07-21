package jnt.scimark2;

import chord.analyses.expax.lang.*;

public class kernel
{

	public static double measureFFT(int N, double mintime, Random R)
	{
		double x[] = RandomVector(2*N, R);
		long cycles = 100;

		for (int i=0; i<cycles; i++)
		{
			FFT.transform(x);	// forward transform
			FFT.inverse(x);		// backward transform
		}

		System.out.print("FFT vector: ");
		for (int i = 0; i < N; ++i) {
			x = Accept.accept_all_FIELD1_TAG1(x);
			System.out.print((x[i]) + " ");
		}
		System.out.println("");

		return 0.0;
	}

	private static double[] RandomVector(int N, Random R)
	{
		Alloc.alloc_TAG1();
		double A[] = new  double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}
	
}
