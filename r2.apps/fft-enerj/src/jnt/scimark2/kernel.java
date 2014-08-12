package jnt.scimark2;

import enerj.lang.*;

public class kernel
{

	public static double measureFFT(int N, double mintime, Random R)
	{
		System.out.println("measureFFT");
		@Approx double x[] = RandomVector(2*N, R);
		long cycles = 100;

		for (int i=0; i<cycles; i++)
		{
			FFT.transform(x);	// forward transform
			FFT.inverse(x);		// backward transform
			System.out.println("loop = " + i);
		}

		System.out.print("FFT vector: ");
		for (int i = 0; i < N; ++i) {
			System.out.print(Endorsements.endorse(x[i]) + " ");
		}
		System.out.println("");

		return 0.0;
	}

	private static @Approx double[] RandomVector(int N, Random R)
	{
		@Approx double A[] = new @Approx double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

}
