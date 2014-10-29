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

package boofcv.alg.feature.detect.intensity.impl;

import boofcv.alg.feature.detect.intensity.GenericCornerIntensityGradientTests;
import boofcv.alg.feature.detect.intensity.GenericCornerIntensityTests;
import boofcv.alg.filter.derivative.GradientSobel;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.core.image.border.BorderIndex1D_Extend;
import boofcv.core.image.border.ImageBorder1D_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.testing.BoofTesting;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplShiTomasiCornerWeighted_S16 {
	int width = 15;
	int height = 20;

	@Test
	public void genericTests() {
		GenericCornerIntensityTests generic = new GenericCornerIntensityGradientTests(){

			@Override
			public void computeIntensity( ImageFloat32 intensity ) {
				ImplShiTomasiCornerWeighted_S16 alg = new ImplShiTomasiCornerWeighted_S16(1);
				alg.process(derivX_I16,derivY_I16,intensity);
			}
		};

		generic.performAllTests();
	}

	/**
	 * Creates a random image and looks for corners in it.  Sees if the naive
	 * and fast algorithm produce exactly the same results.
	 */
	@Test
	public void compareToNaive() {
		ImageUInt8 img = new ImageUInt8(width, height);
		ImageMiscOps.fillUniform(img, new Random(0xfeed), 0, 100);

		ImageSInt16 derivX = new ImageSInt16(img.getWidth(), img.getHeight());
		ImageSInt16 derivY = new ImageSInt16(img.getWidth(), img.getHeight());

		GradientSobel.process(img, derivX, derivY, new ImageBorder1D_I32(BorderIndex1D_Extend.class));

		BoofTesting.checkSubImage(this, "compareToNaive", true, derivX, derivY);
	}

	public void compareToNaive(ImageSInt16 derivX, ImageSInt16 derivY) {
		ImageFloat32 expected = new ImageFloat32(derivX.width,derivX.height);
		ImageFloat32 found = new ImageFloat32(derivX.width,derivX.height);

		ImplSsdCornerNaive naive = new ImplSsdCornerNaive(width, height, 3, true);
		naive.process(derivX, derivY,expected);

		ImplShiTomasiCornerWeighted_S16 fast = new ImplShiTomasiCornerWeighted_S16(3);
		fast.process(derivX, derivY,found);

		// how the weighted is applied is different between these two verisons, which is why the error
		// tolerance is so high
		BoofTesting.assertEqualsInner(expected, found, 0.5, 3, 3, true);
	}

	@Test
	public void checkOverflow() {
		ImplShiTomasiCornerWeighted_S16 detector = new ImplShiTomasiCornerWeighted_S16(1);

		detector.totalXX = (1<<18)+10;
		detector.totalYY = (1<<20)+50;
		detector.totalXY = (1<<16)+5;

		assertTrue(detector.computeResponse() > 0);
	}
}
