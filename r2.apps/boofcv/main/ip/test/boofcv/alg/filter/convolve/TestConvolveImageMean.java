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

package boofcv.alg.filter.convolve;

import boofcv.alg.misc.GImageMiscOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.filter.kernel.FactoryKernel;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.testing.BoofTesting;
import boofcv.testing.CompareEquivalentFunctions;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestConvolveImageMean extends CompareEquivalentFunctions {

	Random rand = new Random(0xFF);

	static int width = 10;
	static int height = 12;
	static int kernelRadius = 2;
	static int kernelRadius2 = 6; // kernel will be larger than the image

	public TestConvolveImageMean() {
		super(ConvolveImageMean.class, ConvolveNormalized.class);
	}

	@Test
	public void compareToStandard() {
		performTests(6);
	}

	@Override
	protected boolean isTestMethod(Method m) {
		Class<?> params[] = m.getParameterTypes();

		if( params.length != 3)
			return false;

		return ImageSingleBand.class.isAssignableFrom(params[0]);
	}

	@Override
	protected boolean isEquivalent(Method candidate, Method validation) {

		Class<?> v[] = candidate.getParameterTypes();
		Class<?> c[] = validation.getParameterTypes();

		if( !candidate.getName().equals(validation.getName()))
			return false;

		if (v.length != 3)
			return false;
		return v[1].isAssignableFrom(c[0]) && v[2].isAssignableFrom(c[1]);
	}

	@Override
	protected Object[][] createInputParam(Method candidate, Method validation) {

		Class c[] = candidate.getParameterTypes();

		ImageSingleBand input = GeneralizedImageOps.createSingleBand(c[0], width, height);
		ImageSingleBand output = GeneralizedImageOps.createSingleBand(c[1], width, height);

		GImageMiscOps.fillUniform(input, rand, 0, 100);

		Object[][] ret = new Object[2][];
		ret[0] = new Object[]{input,output,kernelRadius};
		ret[1] = new Object[]{input,output,kernelRadius2};

		return ret;
	}

	@Override
	protected Object[] reformatForValidation(Method m, Object[] targetParam) {
		Class<?> params[] = m.getParameterTypes();
		int radius = (Integer)targetParam[2];
		Object kernel = createTableKernel(params[0],radius);

		ImageSingleBand output = (ImageSingleBand)((ImageSingleBand)targetParam[1]).clone();

		return new Object[]{kernel, targetParam[0], output};
	}

	@Override
	protected void compareResults(Object targetResult, Object[] targetParam, Object validationResult, Object[] validationParam) {

		if (validationParam.length == 3) {
			ImageSingleBand expected = (ImageSingleBand) validationParam[2];
			ImageSingleBand found = (ImageSingleBand) targetParam[1];

			BoofTesting.assertEquals(expected, found, 1e-4);
		} else {
			ImageSingleBand expected = (ImageSingleBand) validationParam[3];
			ImageSingleBand found = (ImageSingleBand) targetParam[1];

			BoofTesting.assertEquals(expected, found, 1e-4);
		}
	}

	public static Object createTableKernel(Class<?> kernelType, int kernelRadius) {
		Object kernel;
		if (Kernel1D_F32.class == kernelType) {
			kernel = FactoryKernel.table1D_F32(kernelRadius,true);
		} else if (Kernel1D_I32.class == kernelType) {
			kernel = FactoryKernel.table1D_I32(kernelRadius);
		} else {
			throw new RuntimeException("Unknown kernel type");
		}
		return kernel;
	}
}
