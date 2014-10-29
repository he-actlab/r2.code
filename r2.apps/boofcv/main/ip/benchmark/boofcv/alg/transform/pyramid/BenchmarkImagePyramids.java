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

package boofcv.alg.transform.pyramid;

import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.factory.transform.pyramid.FactoryPyramid;
import boofcv.misc.PerformerBase;
import boofcv.misc.ProfileOperation;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.pyramid.PyramidDiscrete;
import boofcv.struct.pyramid.PyramidFloat;

import java.util.Random;


/**
 * Shows runtime performance difference of each type of image pyramid given similar configurations.
 *
 * @author Peter Abeles
 */
public class BenchmarkImagePyramids {
	static int width = 640;
	static int height = 480;
	static long TEST_TIME = 1000;

	static ImageFloat32 input = new ImageFloat32(width,height);

	static int scalesD[] = new int[]{1,2,4,8};
	static double scalesF[] = new double[]{1,2,4,8};

	static PyramidDiscrete<ImageFloat32> pyramidD;
	static PyramidFloat<ImageFloat32> pyramidF;

	static Class<ImageFloat32> imageType = ImageFloat32.class;

	public static class Float_F32 extends PerformerBase {

		@Override
		public void process() {
			pyramidF.process(input);
		}
	}

	public static class Discrete_F32 extends PerformerBase {

		@Override
		public void process() {
			pyramidD.process(input);
		}
	}

	private static void createUpdate() {
		Kernel1D_F32 kernel = FactoryKernelGaussian.gaussian(Kernel1D_F32.class,-1.0,2);
		pyramidD = new PyramidDiscreteSampleBlur<ImageFloat32>(kernel,2,ImageFloat32.class,true,scalesD);

		InterpolatePixelS<ImageFloat32> interp = FactoryInterpolation.bilinearPixelS(ImageFloat32.class);
		pyramidF = FactoryPyramid.scaleSpacePyramid(scalesF, ImageFloat32.class);

	}

	public static void main(String args[]) {

		Random rand = new Random(234);
		ImageMiscOps.fillUniform(input, rand, 0, 100);
		createUpdate();

		System.out.println("=========  Profile Image Size " + width + " x " + height + " ==========");
		System.out.println();

		ProfileOperation.printOpsPerSec(new Float_F32(), TEST_TIME);
		ProfileOperation.printOpsPerSec(new Discrete_F32(), TEST_TIME);
	}
}
