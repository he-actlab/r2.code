/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.transform.ii;

import boofcv.alg.InputSanityCheck;
import boofcv.alg.transform.ii.impl.ImplIntegralImageOps;
import boofcv.struct.ImageRectangle;
import boofcv.struct.image.*;


/**
 * <p>
 * Common operations for dealing with integral images.
 * </p>
 *
 * @author Peter Abeles
 */
public class IntegralImageOps {

	/**
	 * Converts a regular image into an integral image.
	 *
	 * @param input Regular image. Not modified.
	 * @param transformed Integral image. If null a new image will be created. Modified.
	 * @return Integral image.
	 */
	public static ImageFloat32 transform( ImageFloat32 input , ImageFloat32 transformed ) {
		transformed = InputSanityCheck.checkDeclare(input,transformed);

		ImplIntegralImageOps.transform(input,transformed);

		return transformed;
	}

	/**
	 * Converts a regular image into an integral image.
	 *
	 * @param input Regular image. Not modified.
	 * @param transformed Integral image. If null a new image will be created. Modified.
	 * @return Integral image.
	 */
	public static ImageFloat64 transform( ImageFloat64 input , ImageFloat64 transformed ) {
		transformed = InputSanityCheck.checkDeclare(input,transformed);

		ImplIntegralImageOps.transform(input,transformed);

		return transformed;
	}

	/**
	 * Converts a regular image into an integral image.
	 *
	 * @param input Regular image. Not modified.
	 * @param transformed Integral image. If null a new image will be created. Modified.
	 * @return Integral image.
	 */
	public static ImageSInt32 transform( ImageUInt8 input , ImageSInt32 transformed ) {
		transformed = InputSanityCheck.checkDeclare(input,transformed,ImageSInt32.class);

		ImplIntegralImageOps.transform(input,transformed);

		return transformed;
	}

	/**
	 * Converts a regular image into an integral image.
	 *
	 * @param input Regular image. Not modified.
	 * @param transformed Integral image. If null a new image will be created. Modified.
	 * @return Integral image.
	 */
	public static ImageSInt32 transform( ImageSInt32 input , ImageSInt32 transformed ) {
		transformed = InputSanityCheck.checkDeclare(input,transformed,ImageSInt32.class);

		ImplIntegralImageOps.transform(input, transformed);

		return transformed;
	}

	/**
	 * Converts a regular image into an integral image.
	 *
	 * @param input Regular image. Not modified.
	 * @param transformed Integral image. If null a new image will be created. Modified.
	 * @return Integral image.
	 */
	public static ImageSInt64 transform( ImageSInt64 input , ImageSInt64 transformed ) {
		transformed = InputSanityCheck.checkDeclare(input,transformed,ImageSInt64.class);

		ImplIntegralImageOps.transform(input, transformed);

		return transformed;
	}

	/**
	 * General code for convolving a box filter across an image using the integral image.
	 *
	 * @param integral Integral image.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be declared and returned. Modified.
	 * @return Convolved image.
	 */
	public static ImageFloat32 convolve( ImageFloat32 integral ,
										 IntegralKernel kernel ,
										 ImageFloat32 output )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolve(integral, kernel, output);

		return output;
	}

	/**
	 * General code for convolving a box filter across an image using the integral image.
	 *
	 * @param integral Integral image.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be declared and returned. Modified.
	 * @return Convolved image.
	 */
	public static ImageFloat64 convolve( ImageFloat64 integral ,
										 IntegralKernel kernel ,
										 ImageFloat64 output )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolve(integral,kernel,output);

		return output;
	}

	/**
	 * General code for convolving a box filter across an image using the integral image.
	 *
	 * @param integral Integral image.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be declared and returned. Modified.
	 * @return Convolved image.
	 */
	public static ImageSInt32 convolve( ImageSInt32 integral ,
										IntegralKernel kernel ,
										ImageSInt32 output )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolve(integral, kernel, output);

		return output;
	}

	/**
	 * General code for convolving a box filter across an image using the integral image.
	 *
	 * @param integral Integral image.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be declared and returned. Modified.
	 * @return Convolved image.
	 */
	public static ImageSInt64 convolve( ImageSInt64 integral ,
										IntegralKernel kernel ,
										ImageSInt64 output )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolve(integral,kernel,output);

		return output;
	}

	/**
	 * Convolves the kernel only across the image's border.
	 *
	 * @param integral Integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be created. Modified.
	 * @param borderX Size of the image border along the horizontal axis.
	 * @param borderY size of the image border along the vertical axis.
	 */
	public static ImageFloat32 convolveBorder( ImageFloat32 integral ,
											   IntegralKernel kernel ,
											   ImageFloat32 output , int borderX , int borderY )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolveBorder(integral,kernel,output,borderX,borderY);

		return output;
	}

	/**
	 * Convolves the kernel only across the image's border.
	 *
	 * @param integral Integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be created. Modified.
	 * @param borderX Size of the image border along the horizontal axis.
	 * @param borderY size of the image border along the vertical axis.
	 */
	public static ImageFloat64 convolveBorder( ImageFloat64 integral ,
											   IntegralKernel kernel ,
											   ImageFloat64 output , int borderX , int borderY )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolveBorder(integral,kernel,output,borderX,borderY);

		return output;
	}

	/**
	 * Convolves the kernel only across the image's border.
	 *
	 * @param integral Integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be created. Modified.
	 * @param borderX Size of the image border along the horizontal axis.
	 * @param borderY size of the image border along the vertical axis.
	 */
	public static ImageSInt32 convolveBorder( ImageSInt32 integral ,
											  IntegralKernel kernel ,
											  ImageSInt32 output , int borderX , int borderY )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolveBorder(integral,kernel,output,borderX,borderY);

		return output;
	}

	/**
	 * Convolves the kernel only across the image's border.
	 *
	 * @param integral Integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param output The convolved image. If null a new image will be created. Modified.
	 * @param borderX Size of the image border along the horizontal axis.
	 * @param borderY size of the image border along the vertical axis.
	 */
	public static ImageSInt64 convolveBorder( ImageSInt64 integral ,
											  IntegralKernel kernel ,
											  ImageSInt64 output , int borderX , int borderY )
	{
		output = InputSanityCheck.checkDeclare(integral,output);

		ImplIntegralImageOps.convolveBorder(integral,kernel,output,borderX,borderY);

		return output;
	}

	/**
	 * Convolves a kernel around a single point in the integral image.
	 *
	 * @param integral Input integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param x Pixel the convolution is performed at.
	 * @param y Pixel the convolution is performed at.
	 * @return Value of the convolution
	 */
	public static float convolveSparse( ImageFloat32 integral , IntegralKernel kernel , int x , int y )
	{
		return ImplIntegralImageOps.convolveSparse(integral, kernel, x, y);
	}

	/**
	 * Convolves a kernel around a single point in the integral image.
	 *
	 * @param integral Input integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param x Pixel the convolution is performed at.
	 * @param y Pixel the convolution is performed at.
	 * @return Value of the convolution
	 */
	public static double convolveSparse( ImageFloat64 integral , IntegralKernel kernel , int x , int y )
	{
		return ImplIntegralImageOps.convolveSparse(integral,kernel,x,y);
	}

	/**
	 * Convolves a kernel around a single point in the integral image.
	 *
	 * @param integral Input integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param x Pixel the convolution is performed at.
	 * @param y Pixel the convolution is performed at.
	 * @return Value of the convolution
	 */
	public static int convolveSparse( ImageSInt32 integral , IntegralKernel kernel , int x , int y )
	{
		return ImplIntegralImageOps.convolveSparse(integral, kernel, x, y);
	}

	/**
	 * Convolves a kernel around a single point in the integral image.
	 *
	 * @param integral Input integral image. Not modified.
	 * @param kernel Convolution kernel.
	 * @param x Pixel the convolution is performed at.
	 * @param y Pixel the convolution is performed at.
	 * @return Value of the convolution
	 */
	public static long convolveSparse( ImageSInt64 integral , IntegralKernel kernel , int x , int y )
	{
		return ImplIntegralImageOps.convolveSparse(integral,kernel,x,y);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image without bounds checking.  The block is
	 * defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static double block_unsafe( ImageFloat64 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_unsafe(integral,x0,y0,x1,y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image without bounds checking.  The block is
	 * defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static float block_unsafe( ImageFloat32 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_unsafe(integral,x0,y0,x1,y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image without bounds checking.  The block is
	 * defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static int block_unsafe( ImageSInt32 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_unsafe(integral, x0, y0, x1, y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image without bounds checking.  The block is
	 * defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static long block_unsafe( ImageSInt64 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_unsafe(integral, x0, y0, x1, y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image and treats pixels outside of the
	 * image as zero.  The block is defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static float block_zero( ImageFloat32 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_zero(integral,x0,y0,x1,y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image and treats pixels outside of the
	 * image as zero.  The block is defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static double block_zero( ImageFloat64 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_zero(integral,x0,y0,x1,y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image and treats pixels outside of the
	 * image as zero.  The block is defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static int block_zero( ImageSInt32 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_zero(integral,x0,y0,x1,y1);
	}

	/**
	 * <p>
	 * Computes the value of a block inside an integral image and treats pixels outside of the
	 * image as zero.  The block is defined as follows: x0 < x <= x1 and y0 < y <= y1.
	 * </p>
	 *
	 * @param integral Integral image.
	 * @param x0 Lower bound of the block.  Exclusive.
	 * @param y0 Lower bound of the block.  Exclusive.
	 * @param x1 Upper bound of the block.  Inclusive.
	 * @param y1 Upper bound of the block.  Inclusive.
	 * @return Value inside the block.
	 */
	public static long block_zero( ImageSInt64 integral , int x0 , int y0 , int x1 , int y1 )
	{
		return ImplIntegralImageOps.block_zero(integral,x0,y0,x1,y1);
	}

	/**
	 * Prints out the kernel.
	 *
	 * @param kernel THe kernel which is to be printed.
	 */
	public static void print( IntegralKernel kernel )
	{
		int x0 = 0,x1=0,y0=0,y1=0;
		for( ImageRectangle k : kernel.blocks) {
			if( k.x0 < x0 )
				x0 = k.x0;
			if( k.y0 < y0 )
				y0 = k.y0;
			if( k.x1 > x1 )
				x1 = k.x1;
			if( k.y1 > y1 )
				y1 = k.y1;
		}

		int w = x1-x0;
		int h = y1-y0;

		int sum[] = new int[ w*h ];

		for( int i = 0; i < kernel.blocks.length; i++ ) {
			ImageRectangle r = kernel.blocks[i];
			int value = kernel.scales[i];

			for( int y = r.y0; y < r.y1; y++ ) {
				int yy = y-y0;
				for( int x = r.x0; x < r.x1; x++ ) {
					int xx = x - x0;
					sum[ yy*w + xx ] += value;
				}
			}
		}

		System.out.println("IntegralKernel: TL = ("+(x0+1)+","+(y0+1)+") BR=("+x1+","+y1+")");
		for( int y = 0; y < h; y++ ) {
			for( int x = 0; x < w; x++ ) {
				System.out.printf("%4d ",sum[y*w+x]);
			}
			System.out.println();
		}
	}

	/**
	 * Checks to see if the kernel is applied at this specific spot if all the pixels
	 * would be inside the image bounds or not
	 *
	 * @param x location where the kernel is applied. x-axis
	 * @param y location where the kernel is applied. y-axis
	 * @param kernel The kernel
	 * @param width Image's width
	 * @param height Image's height
	 * @return true if in bounds and false if out of bounds
	 */
	public static boolean isInBounds( int x , int y , IntegralKernel kernel , int width , int height )
	{
		for(ImageRectangle r : kernel.blocks ) {
			if( x+r.x0 < 0 || y+r.y0 < 0 )
				return false;
			if( x+r.x1 >= width || y+r.y1 >= height )
				return false;
		}

		return true;
	}
}
