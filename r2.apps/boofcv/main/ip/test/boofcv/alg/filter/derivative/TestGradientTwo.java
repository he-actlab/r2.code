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

package boofcv.alg.filter.derivative;

import boofcv.alg.misc.ImageMiscOps;
import boofcv.core.image.border.ImageBorder_F32;
import boofcv.core.image.border.ImageBorder_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestGradientTwo {

	Random rand = new Random(234);

	int width = 5;
	int height = 7;

	@Test
	public void testStandard() {
		StandardGradientChecks standard = new StandardGradientChecks();
		standard.secondDerivativeTest(GradientTwo.class,3);
	}

	@Test
	public void compareToConvolve_I8() throws NoSuchMethodException {
		CompareDerivativeToConvolution validator = new CompareDerivativeToConvolution();
		validator.setTarget(GradientTwo.class.getMethod("process",
				ImageUInt8.class, ImageSInt16.class, ImageSInt16.class, ImageBorder_I32.class ));

		validator.setKernel(0,GradientTwo.kernelDeriv_I32,true);
		validator.setKernel(1,GradientTwo.kernelDeriv_I32,false);

		ImageUInt8 input = new ImageUInt8(width,height);
		ImageMiscOps.fillUniform(input, rand, 0, 10);
		ImageSInt16 derivX = new ImageSInt16(width,height);
		ImageSInt16 derivY = new ImageSInt16(width,height);

		validator.compare(input,derivX,derivY);
	}

	@Test
	public void compareToConvolve_I16() throws NoSuchMethodException {
		CompareDerivativeToConvolution validator = new CompareDerivativeToConvolution();
		validator.setTarget(GradientTwo.class.getMethod("process",
				ImageSInt16.class, ImageSInt16.class, ImageSInt16.class, ImageBorder_I32.class ));

		validator.setKernel(0,GradientTwo.kernelDeriv_I32,true);
		validator.setKernel(1,GradientTwo.kernelDeriv_I32,false);

		ImageSInt16 input = new ImageSInt16(width,height);
		ImageMiscOps.fillUniform(input, rand, 0, 10);
		ImageSInt16 derivX = new ImageSInt16(width,height);
		ImageSInt16 derivY = new ImageSInt16(width,height);

		validator.compare(input,derivX,derivY);
	}

@	Test
	public void compareToConvolve_F32() throws NoSuchMethodException {
		CompareDerivativeToConvolution validator = new CompareDerivativeToConvolution();
		validator.setTarget(GradientTwo.class.getMethod("process",
				ImageFloat32.class, ImageFloat32.class, ImageFloat32.class, ImageBorder_F32.class ));

		validator.setKernel(0,GradientTwo.kernelDeriv_F32,true);
		validator.setKernel(1,GradientTwo.kernelDeriv_F32,false);

		ImageFloat32 input = new ImageFloat32(width,height);
		ImageMiscOps.fillUniform(input, rand, 0, 10);
		ImageFloat32 derivX = new ImageFloat32(width,height);
		ImageFloat32 derivY = new ImageFloat32(width,height);

		validator.compare(input,derivX,derivY);
	}

}
