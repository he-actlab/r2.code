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
 */package boofcv.alg.filter.convolve.border;

import boofcv.core.image.border.ImageBorder_F32;
import boofcv.core.image.border.ImageBorder_I32;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.convolve.Kernel2D_F32;
import boofcv.struct.convolve.Kernel2D_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageInt16;
import boofcv.struct.image.ImageSInt32;

/**
 * <p>
 * Convolves just the image's border.  How the border condition is handled is specified by the {@link boofcv.core.image.border.ImageBorder}
 * passed in.  For 1D kernels only the horizontal or vertical borders are processed.
 * </p>
 * 
 * <p>
 * WARNING: Do not modify.  Automatically generated by {@link GenerateConvolveJustBorder_General}.
 * </p>
 * 
 * @author Peter Abeles
 */
public class ConvolveJustBorder_General {

	public static void horizontal(Kernel1D_F32 kernel, ImageBorder_F32 input, ImageFloat32 output ) {
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderRight = kernelWidth-offset-1;

		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offset; x++ ) {
				float total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = total;
			}

			indexDest = output.startIndex + y * output.stride + width-borderRight;
			for ( int x = width-borderRight; x < width; x++ ) {
				float total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = total;
			}
		}
	}

	public static void vertical(Kernel1D_F32 kernel, ImageBorder_F32 input, ImageFloat32 output ) {
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderBottom = kernelWidth-offset-1;

		for ( int x = 0; x < width; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offset; y++, indexDest += output.stride) {
				float total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = total;
			}

			indexDest = output.startIndex + (height-borderBottom) * output.stride + x;
			for (int y = height-borderBottom; y < height; y++, indexDest += output.stride) {
				float total = 0;
				for (int k = 0; k < kernelWidth; k++ ) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = total;
			}
		}
	}

	public static void convolve(Kernel2D_F32 kernel, ImageBorder_F32 input, ImageFloat32 output ) {
		final float[] dataDst = output.data;
		final float[] dataKer = kernel.data;

		final int offsetL = kernel.getOffset();
		final int offsetR = kernel.getWidth()-offsetL-1;
		final int width = output.getWidth();
		final int height = output.getHeight();

		// convolve along the left and right borders
		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offsetL; x++ ) {
				float total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = total;
			}

			indexDest = output.startIndex + y * output.stride + width-offsetR;
			for ( int x = width-offsetR; x < width; x++ ) {
				float total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = total;
			}
		}

		// convolve along the top and bottom borders
		for ( int x = offsetL; x < width-offsetR; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offsetL; y++, indexDest += output.stride) {
				float total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = total;
			}

			indexDest = output.startIndex + (height-offsetR) * output.stride + x;
			for (int y = height-offsetR; y < height; y++, indexDest += output.stride) {
				float total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = total;
			}
		}
	}

	public static void horizontal(Kernel1D_I32 kernel, ImageBorder_I32 input, ImageInt16 output ) {
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderRight = kernelWidth-offset-1;

		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offset; x++ ) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = (short)total;
			}

			indexDest = output.startIndex + y * output.stride + width-borderRight;
			for ( int x = width-borderRight; x < width; x++ ) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = (short)total;
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernel, ImageBorder_I32 input, ImageInt16 output ) {
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderBottom = kernelWidth-offset-1;

		for ( int x = 0; x < width; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offset; y++, indexDest += output.stride) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = (short)total;
			}

			indexDest = output.startIndex + (height-borderBottom) * output.stride + x;
			for (int y = height-borderBottom; y < height; y++, indexDest += output.stride) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++ ) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = (short)total;
			}
		}
	}

	public static void convolve(Kernel2D_I32 kernel, ImageBorder_I32 input, ImageInt16 output ) {
		final short[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offsetL = kernel.getOffset();
		final int offsetR = kernel.getWidth()-offsetL-1;
		final int width = output.getWidth();
		final int height = output.getHeight();

		// convolve along the left and right borders
		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offsetL; x++ ) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = (short)total;
			}

			indexDest = output.startIndex + y * output.stride + width-offsetR;
			for ( int x = width-offsetR; x < width; x++ ) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = (short)total;
			}
		}

		// convolve along the top and bottom borders
		for ( int x = offsetL; x < width-offsetR; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offsetL; y++, indexDest += output.stride) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = (short)total;
			}

			indexDest = output.startIndex + (height-offsetR) * output.stride + x;
			for (int y = height-offsetR; y < height; y++, indexDest += output.stride) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = (short)total;
			}
		}
	}

	public static void horizontal(Kernel1D_I32 kernel, ImageBorder_I32 input, ImageSInt32 output ) {
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderRight = kernelWidth-offset-1;

		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offset; x++ ) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = total;
			}

			indexDest = output.startIndex + y * output.stride + width-borderRight;
			for ( int x = width-borderRight; x < width; x++ ) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x+k-offset,y) * dataKer[k];
				}
				dataDst[indexDest++] = total;
			}
		}
	}

	public static void vertical(Kernel1D_I32 kernel, ImageBorder_I32 input, ImageSInt32 output ) {
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offset = kernel.getOffset();
		final int kernelWidth = kernel.getWidth();
		final int width = output.getWidth();
		final int height = output.getHeight();
		final int borderBottom = kernelWidth-offset-1;

		for ( int x = 0; x < width; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offset; y++, indexDest += output.stride) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = total;
			}

			indexDest = output.startIndex + (height-borderBottom) * output.stride + x;
			for (int y = height-borderBottom; y < height; y++, indexDest += output.stride) {
				int total = 0;
				for (int k = 0; k < kernelWidth; k++ ) {
					total += input.get(x,y+k-offset) * dataKer[k];
				}
				dataDst[indexDest] = total;
			}
		}
	}

	public static void convolve(Kernel2D_I32 kernel, ImageBorder_I32 input, ImageSInt32 output ) {
		final int[] dataDst = output.data;
		final int[] dataKer = kernel.data;

		final int offsetL = kernel.getOffset();
		final int offsetR = kernel.getWidth()-offsetL-1;
		final int width = output.getWidth();
		final int height = output.getHeight();

		// convolve along the left and right borders
		for (int y = 0; y < height; y++) {
			int indexDest = output.startIndex + y * output.stride;

			for ( int x = 0; x < offsetL; x++ ) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = total;
			}

			indexDest = output.startIndex + y * output.stride + width-offsetR;
			for ( int x = width-offsetR; x < width; x++ ) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest++] = total;
			}
		}

		// convolve along the top and bottom borders
		for ( int x = offsetL; x < width-offsetR; x++ ) {
			int indexDest = output.startIndex + x;

			for (int y = 0; y < offsetL; y++, indexDest += output.stride) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = total;
			}

			indexDest = output.startIndex + (height-offsetR) * output.stride + x;
			for (int y = height-offsetR; y < height; y++, indexDest += output.stride) {
				int total = 0;
				int indexKer = 0;
				for( int i = -offsetL; i <= offsetR; i++ ) {
					for (int j = -offsetL; j <= offsetR; j++) {
						total += input.get(x+j,y+i) * dataKer[indexKer++];
					}
				}
				dataDst[indexDest] = total;
			}
		}
	}

}
