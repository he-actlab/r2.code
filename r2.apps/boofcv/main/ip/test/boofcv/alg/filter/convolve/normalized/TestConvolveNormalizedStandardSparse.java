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

package boofcv.alg.filter.convolve.normalized;

import boofcv.alg.filter.convolve.ConvolveNormalized;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.core.image.ConvertImage;
import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestConvolveNormalizedStandardSparse {

	Random rand = new Random(0xFF);

	static int width = 10;
	static int height = 12;
	static int kernelRadius = 2;
	static int targetX = 0;
	static int targetY = 5;

	static Kernel1D_F32 kernelF32;
	static Kernel1D_I32 kernelI32;
	static float expectedOutput;

	/**
	 * Automatically compares all the box filters against a generalize convolution
	 */
	@Test
	public void compareToGeneral() {
		// try different edges in the image as test points
		for( int i = 0; i < 2; i++ ) {
			targetX = i; targetY = 5;
			performComparision();
			targetX = 5; targetY = i;
			performComparision();
			targetX = width-1-i; targetY = 5;
			performComparision();
			targetX = 5; targetY = height-1-i;
			performComparision();
			targetX = 5; targetY = 5;
			performComparision();
		}
	}

	private void performComparision() {
		Method methods[] = ConvolveNormalizedStandardSparse.class.getMethods();

		// sanity check to make sure the functions are being found
		int numFound = 0;
		for (Method m : methods) {
			// search for methods which have equivalent in the other class
			Class<?> paramTypes[] = m.getParameterTypes();
			if (paramTypes.length < 3) {
				continue;
			}
			System.out.println("Checking "+m.getName()+"  type "+paramTypes[2].getSimpleName());

			checkMethod(m, width, height, kernelRadius, rand);
			numFound++;
		}

		// update this as needed when new functions are added
		assertEquals(3, numFound);
	}

	private void checkMethod(Method method, int width, int height, int kernelRadius, Random rand) {
		ImageUInt8 seedImage = new ImageUInt8(width,height);
		ImageMiscOps.fillUniform(seedImage,rand,0,255);

		// creates a floating point image with integer elements
		ImageFloat32 floatImage = new ImageFloat32(width,height);
		ConvertImage.convert(seedImage,floatImage);
		ImageSInt16 shortImage = new ImageSInt16(width,height);
		ConvertImage.convert(seedImage,shortImage);

		kernelI32 = FactoryKernelGaussian.gaussian(Kernel1D_I32.class,-1,kernelRadius);
		kernelF32 = FactoryKernelGaussian.gaussian(Kernel1D_F32.class,-1,kernelRadius);

		boolean isFloatingKernel = method.getParameterTypes()[0] == Kernel1D_F32.class;

		Class<?> imageType = method.getParameterTypes()[2];
		ImageSingleBand<?> inputImage;

		if( imageType == ImageFloat32.class) {
			inputImage = floatImage;
			expectedOutput = computeExpected(floatImage);
		} else if( imageType == ImageUInt8.class ){
			inputImage = seedImage;
			expectedOutput = computeExpected(seedImage);
		} else {
			inputImage = shortImage;
			expectedOutput = computeExpected(shortImage);
		}

		Object inputKernel = isFloatingKernel ? kernelF32 : kernelI32;
		Object inputStorage = isFloatingKernel ? new float[kernelI32.width] : new int[ kernelI32.width];

		checkResults(method,inputKernel,inputImage,inputStorage);
	}

	private void checkResults(Method method, Object inputKernel, ImageSingleBand<?> inputImage, Object inputStorage) {
		try {
			Number result = (Number)method.invoke(null,inputKernel,inputKernel,inputImage,targetX,targetY,inputStorage);
			assertEquals(expectedOutput,result.floatValue(),1e-4);
		} catch (IllegalAccessException e) {                                                         
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private float computeExpected( ImageFloat32 image ) {
		ImageFloat32 temp = new ImageFloat32(image.width,image.height);
		ImageFloat32 temp2 = new ImageFloat32(image.width,image.height);

		ConvolveNormalized.horizontal(kernelF32,image,temp);
		ConvolveNormalized.vertical(kernelF32,temp,temp2);

		return temp2.get(targetX,targetY);
	}

	private float computeExpected( ImageUInt8 image ) {
		ImageUInt8 temp = new ImageUInt8(image.width,image.height);
		ImageUInt8 temp2 = new ImageUInt8(image.width,image.height);

		ConvolveNormalized.horizontal(kernelI32,image,temp);
		ConvolveNormalized.vertical(kernelI32,temp,temp2);

		return temp2.get(targetX,targetY);
	}

	private float computeExpected( ImageSInt16 image ) {
		ImageSInt16 temp = new ImageSInt16(image.width,image.height);
		ImageSInt16 temp2 = new ImageSInt16(image.width,image.height);

		ConvolveNormalized.horizontal(kernelI32,image,temp);
		ConvolveNormalized.vertical(kernelI32,temp,temp2);

		return temp2.get(targetX,targetY);
	}
}
