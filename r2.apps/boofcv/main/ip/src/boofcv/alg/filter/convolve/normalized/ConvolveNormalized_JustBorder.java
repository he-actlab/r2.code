/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boofcv.alg.filter.convolve.normalized;

import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.convolve.Kernel2D_I32;
import boofcv.struct.image.*;

/**
 * <p>
 * Covolves a 1D kernel in the horizontal or vertical direction across an image's border only, while re-normalizing the
 * kernel sum to one.  The kernel MUST be smaller than the image.
 * </p>
 * 
 * <p>
 * NOTE: Do not modify.  Automatically generated by {@link GenerateConvolveNormalized_JustBorder}.
 * </p>
 * 
 * @author Peter Abeles
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class ConvolveNormalized_JustBorder {

	public static void horizontal(Kernel1D_F32 kernel, ImageFloat32 input, ImageFloat32 output ) {
		final float[] dataSrc = input.data;
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		for (int i = 0; i < height; i++) {
			int indexDest = output.startIndex + i * output.stride;
			int j = input.startIndex + i * input.stride;
			final int jStart = j;
			int jEnd = j + offsetL;

			for (; j < jEnd; j++) {
				float total = 0;
				float weight = 0;
				int indexSrc = jStart;
				for (int k = kernelWidth - (offsetR + 1 + j - jStart); k < kernelWidth; k++) {
					float w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = (total/weight);
			}

			j += width - (offsetL+offsetR);
			indexDest += width - (offsetL+offsetR);

			jEnd = jStart + width;
			for (; j < jEnd; j++) {
				float total = 0;
				float weight = 0;
				int indexSrc = j - offsetL;
				final int kEnd = jEnd - indexSrc;

				for (int k = 0; k < kEnd; k++) {
					float w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = (total/weight);
			}
		}
	}

	public static void vertical(Kernel1D_F32 kernel, ImageFloat32 input, ImageFloat32 output ) {
		final float[] dataSrc = input.data;
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - offsetR;

		for (int y = 0; y < offsetL; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetL - y;

			float weight = 0;
			for (int k = kStart; k < kernelWidth; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				float total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidth; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (total/weight);
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetL);

			float weight = 0;
			for (int k = 0; k < kEnd; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				float total = 0;
				int indexSrc = i - offsetL * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (total/weight);
			}
		}
	}

	public static void convolve(Kernel2D_F32 kernel, ImageFloat32 input, ImageFloat32 output ) {
		final float[] dataSrc = input.data;
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		// convolve across the left and right borders
		for (int y = 0; y < height; y++) {

			int minI = y >= offsetL ? -offsetL : -y;
			int maxI = y < height - offsetR ?  offsetR : height - y - 1;

			int indexDst = output.startIndex + y* output.stride;

			for( int x = 0; x < offsetL; x++ ) {

				float total = 0;
				float weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -x; j <= offsetR; j++ ) {
						float w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = (total/weight);
			}

			indexDst = output.startIndex + y* output.stride + width-offsetR;
			for( int x = width-offsetR; x < width; x++ ) {

				int maxJ = width-x-1;

				float total = 0;
				float weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= maxJ; j++ ) {
						float w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = (total/weight);
			}
		}

		// convolve across the top border while avoiding convolving the corners again
		for (int y = 0; y < offsetL; y++) {

			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				float total = 0;
				float weight = 0;

				for( int i = -y; i <= offsetR; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						float w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = (total/weight);
			}
		}

		// convolve across the bottom border
		for (int y = height-offsetR; y < height; y++) {

			int maxI = height - y - 1;
			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				float total = 0;
				float weight = 0;

				for( int i = -offsetL; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						float w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = (total/weight);
			}
		}
	}

	public static void horizontal(Kernel1D_I32 kernel, ImageUInt8 input, ImageInt8 output ) {
		final byte[] dataSrc = input.data;
		final byte[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		for (int i = 0; i < height; i++) {
			int indexDest = output.startIndex + i * output.stride;
			int j = input.startIndex + i * input.stride;
			final int jStart = j;
			int jEnd = j + offsetL;

			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = jStart;
				for (int k = kernelWidth - (offsetR + 1 + j - jStart); k < kernelWidth; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]& 0xFF) * w;
				}
				dataDst[indexDest++] = (byte)((total+weight/2)/weight);
			}

			j += width - (offsetL+offsetR);
			indexDest += width - (offsetL+offsetR);

			jEnd = jStart + width;
			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = j - offsetL;
				final int kEnd = jEnd - indexSrc;

				for (int k = 0; k < kEnd; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]& 0xFF) * w;
				}
				dataDst[indexDest++] = (byte)((total+weight/2)/weight);
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernel, ImageUInt8 input, ImageInt8 output ) {
		final byte[] dataSrc = input.data;
		final byte[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - offsetR;

		for (int y = 0; y < offsetL; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetL - y;

			int weight = 0;
			for (int k = kStart; k < kernelWidth; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidth; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFF) * dataKer[k];
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetL);

			int weight = 0;
			for (int k = 0; k < kEnd; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - offsetL * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFF) * dataKer[k];
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}
	}

	public static void convolve(Kernel2D_I32 kernel, ImageUInt8 input, ImageInt8 output ) {
		final byte[] dataSrc = input.data;
		final byte[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		// convolve across the left and right borders
		for (int y = 0; y < height; y++) {

			int minI = y >= offsetL ? -offsetL : -y;
			int maxI = y < height - offsetR ?  offsetR : height - y - 1;

			int indexDst = output.startIndex + y* output.stride;

			for( int x = 0; x < offsetL; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -x; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]& 0xFF) * w;
					}
				}

				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}

			indexDst = output.startIndex + y* output.stride + width-offsetR;
			for( int x = width-offsetR; x < width; x++ ) {

				int maxJ = width-x-1;

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= maxJ; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]& 0xFF) * w;
					}
				}

				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}

		// convolve across the top border while avoiding convolving the corners again
		for (int y = 0; y < offsetL; y++) {

			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -y; i <= offsetR; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]& 0xFF) * w;
					}
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}

		// convolve across the bottom border
		for (int y = height-offsetR; y < height; y++) {

			int maxI = height - y - 1;
			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -offsetL; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]& 0xFF) * w;
					}
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}
	}

	public static void horizontal(Kernel1D_I32 kernel, ImageSInt16 input, ImageInt16 output ) {
		final short[] dataSrc = input.data;
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		for (int i = 0; i < height; i++) {
			int indexDest = output.startIndex + i * output.stride;
			int j = input.startIndex + i * input.stride;
			final int jStart = j;
			int jEnd = j + offsetL;

			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = jStart;
				for (int k = kernelWidth - (offsetR + 1 + j - jStart); k < kernelWidth; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = (short)((total+weight/2)/weight);
			}

			j += width - (offsetL+offsetR);
			indexDest += width - (offsetL+offsetR);

			jEnd = jStart + width;
			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = j - offsetL;
				final int kEnd = jEnd - indexSrc;

				for (int k = 0; k < kEnd; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = (short)((total+weight/2)/weight);
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernel, ImageSInt16 input, ImageInt16 output ) {
		final short[] dataSrc = input.data;
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - offsetR;

		for (int y = 0; y < offsetL; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetL - y;

			int weight = 0;
			for (int k = kStart; k < kernelWidth; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidth; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetL);

			int weight = 0;
			for (int k = 0; k < kEnd; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - offsetL * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}
	}

	public static void convolve(Kernel2D_I32 kernel, ImageSInt16 input, ImageInt16 output ) {
		final short[] dataSrc = input.data;
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		// convolve across the left and right borders
		for (int y = 0; y < height; y++) {

			int minI = y >= offsetL ? -offsetL : -y;
			int maxI = y < height - offsetR ?  offsetR : height - y - 1;

			int indexDst = output.startIndex + y* output.stride;

			for( int x = 0; x < offsetL; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -x; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}

			indexDst = output.startIndex + y* output.stride + width-offsetR;
			for( int x = width-offsetR; x < width; x++ ) {

				int maxJ = width-x-1;

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= maxJ; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}

		// convolve across the top border while avoiding convolving the corners again
		for (int y = 0; y < offsetL; y++) {

			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -y; i <= offsetR; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}

		// convolve across the bottom border
		for (int y = height-offsetR; y < height; y++) {

			int maxI = height - y - 1;
			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -offsetL; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}
	}

	public static void horizontal(Kernel1D_I32 kernel, ImageSInt32 input, ImageSInt32 output ) {
		final int[] dataSrc = input.data;
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		for (int i = 0; i < height; i++) {
			int indexDest = output.startIndex + i * output.stride;
			int j = input.startIndex + i * input.stride;
			final int jStart = j;
			int jEnd = j + offsetL;

			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = jStart;
				for (int k = kernelWidth - (offsetR + 1 + j - jStart); k < kernelWidth; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = ((total+weight/2)/weight);
			}

			j += width - (offsetL+offsetR);
			indexDest += width - (offsetL+offsetR);

			jEnd = jStart + width;
			for (; j < jEnd; j++) {
				int total = 0;
				int weight = 0;
				int indexSrc = j - offsetL;
				final int kEnd = jEnd - indexSrc;

				for (int k = 0; k < kEnd; k++) {
					int w = dataKer[k];
					weight += w;
					total += (dataSrc[indexSrc++]) * w;
				}
				dataDst[indexDest++] = ((total+weight/2)/weight);
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernel, ImageSInt32 input, ImageSInt32 output ) {
		final int[] dataSrc = input.data;
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - offsetR;

		for (int y = 0; y < offsetL; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetL - y;

			int weight = 0;
			for (int k = kStart; k < kernelWidth; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidth; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = ((total+weight/2)/weight);
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetL);

			int weight = 0;
			for (int k = 0; k < kEnd; k++) {
				weight += dataKer[k];
			}

			for ( ; i < iEnd; i++) {
				int total = 0;
				int indexSrc = i - offsetL * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = ((total+weight/2)/weight);
			}
		}
	}

	public static void convolve(Kernel2D_I32 kernel, ImageSInt32 input, ImageSInt32 output ) {
		final int[] dataSrc = input.data;
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int kernelWidth = kernel.getWidth();
		final int offsetL = kernel.getOffset();
		final int offsetR = kernelWidth-offsetL-1;

		final int width = input.getWidth();
		final int height = input.getHeight();

		// convolve across the left and right borders
		for (int y = 0; y < height; y++) {

			int minI = y >= offsetL ? -offsetL : -y;
			int maxI = y < height - offsetR ?  offsetR : height - y - 1;

			int indexDst = output.startIndex + y* output.stride;

			for( int x = 0; x < offsetL; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -x; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = ((total+weight/2)/weight);
			}

			indexDst = output.startIndex + y* output.stride + width-offsetR;
			for( int x = width-offsetR; x < width; x++ ) {

				int maxJ = width-x-1;

				int total = 0;
				int weight = 0;

				for( int i = minI; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= maxJ; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}

				dataDst[indexDst++] = ((total+weight/2)/weight);
			}
		}

		// convolve across the top border while avoiding convolving the corners again
		for (int y = 0; y < offsetL; y++) {

			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -y; i <= offsetR; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = ((total+weight/2)/weight);
			}
		}

		// convolve across the bottom border
		for (int y = height-offsetR; y < height; y++) {

			int maxI = height - y - 1;
			int indexDst = output.startIndex + y* output.stride+offsetL;

			for( int x = offsetL; x < width-offsetR; x++ ) {

				int total = 0;
				int weight = 0;

				for( int i = -offsetL; i <= maxI; i++ ) {
					int indexSrc = input.startIndex + (y+i)* input.stride+x;
					int indexKer = (i+offsetL)*kernelWidth;

					for( int j = -offsetL; j <= offsetR; j++ ) {
						int w = dataKer[indexKer+j+offsetL];
						weight += w;
						total += (dataSrc[indexSrc+j]) * w;
					}
				}
				dataDst[indexDst++] = ((total+weight/2)/weight);
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernelX,Kernel1D_I32 kernelY,
								ImageUInt16 input, ImageInt8 output ) {
		final short[] dataSrc = input.data;
		final byte[] dataDst = output.data;
		final int[] dataKer = kernelY.data;

		final int offsetY = kernelY.getOffset();
		final int kernelWidthY = kernelY.getWidth();

		final int offsetX = kernelX.getOffset();
		final int kernelWidthX = kernelX.getWidth();
		final int offsetX1 = kernelWidthX-offsetX-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - (kernelWidthY-offsetY-1);

		int startWeightX = 0;
		for (int k = offsetX; k < kernelWidthX; k++) {
			startWeightX += kernelX.data[k];
		}

		for (int y = 0; y < offsetY; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetY - y;

			int weightY = 0;
			for (int k = kStart; k < kernelWidthY; k++) {
				weightY += dataKer[k];
			}
			int weightX = startWeightX;

			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFFFF) * dataKer[k];
				}
				if( x == 0 && y == 0 )
					System.out.println("normalized border "+total);
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
				if( x < offsetX ) {
					weightX += kernelX.data[offsetX-x-1];
				} else if( x >= input.width-(kernelWidthX-offsetX) ) {
					weightX -= kernelX.data[input.width-x+offsetX-1];
				}
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetY);

			int weightY = 0;
			for (int k = 0; k < kEnd; k++) {
				weightY += dataKer[k];
			}
			int weightX = startWeightX;

			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFFFF) * dataKer[k];
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
				if( x < offsetX ) {
					weightX += kernelX.data[offsetX-x-1];
				} else if( x >= input.width-(kernelWidthX-offsetX) ) {
					weightX -= kernelX.data[input.width-x+offsetX-1];
				}
			}
		}

		// left and right border
		int weightY = kernelY.computeSum();
		for (int y = offsetY; y < yEnd; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;

			// left side
			int iEnd = i + offsetY;
			int weightX = startWeightX;
			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFFFF) * dataKer[k];
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
				weightX += kernelX.data[offsetX-x-1];
			}

			// right side
			int startX = input.width-offsetX1;
			indexDst = output.startIndex + y * output.stride + startX;
			i = input.startIndex + y * input.stride + startX;
			iEnd = input.startIndex + y * input.stride + input.width;
			for ( int x = startX; i < iEnd; i++, x++ ) {
				weightX -= kernelX.data[input.width-x+offsetX];
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]& 0xFFFF) * dataKer[k];
				}
				dataDst[indexDst++] = (byte)((total+weight/2)/weight);
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernelX,Kernel1D_I32 kernelY,
								ImageSInt32 input, ImageInt16 output ) {
		final int[] dataSrc = input.data;
		final short[] dataDst = output.data;
		final int[] dataKer = kernelY.data;

		final int offsetY = kernelY.getOffset();
		final int kernelWidthY = kernelY.getWidth();

		final int offsetX = kernelX.getOffset();
		final int kernelWidthX = kernelX.getWidth();
		final int offsetX1 = kernelWidthX-offsetX-1;

		final int imgWidth = output.getWidth();
		final int imgHeight = output.getHeight();

		final int yEnd = imgHeight - (kernelWidthY-offsetY-1);

		int startWeightX = 0;
		for (int k = offsetX; k < kernelWidthX; k++) {
			startWeightX += kernelX.data[k];
		}

		for (int y = 0; y < offsetY; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kStart = offsetY - y;

			int weightY = 0;
			for (int k = kStart; k < kernelWidthY; k++) {
				weightY += dataKer[k];
			}
			int weightX = startWeightX;

			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - y * input.stride;
				for (int k = kStart; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
				if( x < offsetX ) {
					weightX += kernelX.data[offsetX-x-1];
				} else if( x >= input.width-(kernelWidthX-offsetX) ) {
					weightX -= kernelX.data[input.width-x+offsetX-1];
				}
			}
		}

		for (int y = yEnd; y < imgHeight; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;
			final int iEnd = i + imgWidth;

			int kEnd = imgHeight - (y - offsetY);

			int weightY = 0;
			for (int k = 0; k < kEnd; k++) {
				weightY += dataKer[k];
			}
			int weightX = startWeightX;

			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kEnd; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
				if( x < offsetX ) {
					weightX += kernelX.data[offsetX-x-1];
				} else if( x >= input.width-(kernelWidthX-offsetX) ) {
					weightX -= kernelX.data[input.width-x+offsetX-1];
				}
			}
		}

		// left and right border
		int weightY = kernelY.computeSum();
		for (int y = offsetY; y < yEnd; y++) {
			int indexDst = output.startIndex + y * output.stride;
			int i = input.startIndex + y * input.stride;

			// left side
			int iEnd = i + offsetY;
			int weightX = startWeightX;
			for ( int x = 0; i < iEnd; i++, x++ ) {
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
				weightX += kernelX.data[offsetX-x-1];
			}

			// right side
			int startX = input.width-offsetX1;
			indexDst = output.startIndex + y * output.stride + startX;
			i = input.startIndex + y * input.stride + startX;
			iEnd = input.startIndex + y * input.stride + input.width;
			for ( int x = startX; i < iEnd; i++, x++ ) {
				weightX -= kernelX.data[input.width-x+offsetX];
				int weight = weightX*weightY;
				int total = 0;
				int indexSrc = i - offsetY * input.stride;
				for (int k = 0; k < kernelWidthY; k++, indexSrc += input.stride) {
					total += (dataSrc[indexSrc]) * dataKer[k];
				}
				dataDst[indexDst++] = (short)((total+weight/2)/weight);
			}
		}
	}

}
