package jnt.scimark2;

/**
 Evaluation for EnerJ framework
*/

import enerj.lang.*;

public class FFT {

	public static void transform (@Approx double data[]) {
		transform_internal(data, -1); 
	} 

	public static void inverse (@Approx double data[]) {
		transform_internal(data, +1); 
		int nd=data.length;
		int n =nd/2; 
		@Approx int aprN = n; 
		@Approx double norm=1.0/aprN;
		for(int i=0; i<nd; i++)
			data[i] *= norm; 
	}

	protected static int log2 (int n){
		int log = 0;
		for(int k=1; k < n; k *= 2, log++);
		if (n != (1 << log))
			throw new Error("FFT: Data length is not a power of 2!: "+n);
		return log; 
	}

	protected static void transform_internal (@Approx double data[], int direction) {
		if (data.length == 0) return;    
		int n = data.length/2;
		if (n == 1) return;       
		int logn = log2(n);

		bitreverse(data) ;

		for (int bit = 0, dual = 1; bit < logn; bit++, dual *= 2) {
			@Approx double w_real = 1.0; 
			@Approx double w_imag = 0.0; 

			@Approx double theta = 2.0 * direction * Math.PI / (2.0 * (double) dual);
			@Approx double s = Math.sin(Endorsements.endorse(theta));
			@Approx double t = Math.sin(Endorsements.endorse(theta) / 2.0); 
			@Approx double s2 = 2.0 * t * t; 

			for (int b = 0; b < n; b += 2 * dual) {
				int i = 2*b ;
				int j = 2*(b + dual);

				@Approx double wd_real = data[j] ; 
				@Approx double wd_imag = data[j+1] ; 

				data[j]   = data[i]   - wd_real; 
				data[j+1] = data[i+1] - wd_imag; 
				data[i]  += wd_real; 
				data[i+1]+= wd_imag; 
			}

			for (int a = 1; a < dual; a++) {
				{
					@Approx double tmp_real = w_real - s * w_imag - s2 * w_real; 
					@Approx double tmp_imag = w_imag + s * w_real - s2 * w_imag; 
					w_real = tmp_real; 
					w_imag = tmp_imag; 
				}
				for (int b = 0; b < n; b += 2 * dual) {
					int i = 2*(b + a);
					int j = 2*(b + a + dual);

					@Approx double z1_real = data[j]; 
					@Approx double z1_imag = data[j+1]; 

					@Approx double wd_real = w_real * z1_real - w_imag * z1_imag; 
					@Approx double wd_imag = w_real * z1_imag + w_imag * z1_real; 

					data[j]   = data[i]   - wd_real; 
					data[j+1] = data[i+1] - wd_imag; 
					data[i]  += wd_real; 
					data[i+1]+= wd_imag; 
				}
			}
		}
	}

	protected static void bitreverse(@Approx double data[]) {
		int n=data.length/2;
		int nm1 = n-1;
		int i=0; 
		int j=0;
		for (; i < nm1; i++) {

			int ii = i << 1;
			int jj = j << 1;
			int k = n >> 1;

			if (i < j) {
				@Approx double tmp_real    = data[ii]; 
				@Approx double tmp_imag    = data[ii+1]; 
				data[ii]   = data[jj]; 
				data[ii+1] = data[jj+1]; 
				data[jj]   = tmp_real; 
				data[jj+1] = tmp_imag; 
			} 

			while (k <= j) {
				j -= k;
				k >>= 1 ; 
			}
			j += k ;
		}
	}

	private static @Approx double[] RandomVector(int N, Random R) {
		@Approx double A[] = new double[N];

		for (int i=0; i<N; i++)
			A[i] = R.nextDouble(); 
		return A;
	}

	public static void main (String args[]) {
		Random R = new Random(Integer.parseInt(args[0]));
		@Approx double x[] = RandomVector(32, R);

		for (int i=0; i<100; i++) {
			FFT.transform(x);	
			FFT.inverse(x);		
		}

		System.out.print("FFT vector: ");
		for (int i = 0; i < 16; ++i) {
			@Approx double x_i = x[i];

			
			System.out.print(Endorsements.endorse(x_i) + " ");
		}
		System.out.println("");
	}
}
