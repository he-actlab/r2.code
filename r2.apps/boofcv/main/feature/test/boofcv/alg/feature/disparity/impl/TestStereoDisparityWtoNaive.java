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

package boofcv.alg.feature.disparity.impl;

import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestStereoDisparityWtoNaive {

	@Test
	public void basicTest() {
		BasicDisparityTests<ImageUInt8,ImageFloat32> alg =
				new BasicDisparityTests<ImageUInt8,ImageFloat32>(ImageUInt8.class) {

					StereoDisparityWtoNaive<ImageUInt8> alg;

					@Override
					public ImageFloat32 computeDisparity(ImageUInt8 left, ImageUInt8 right ) {
						ImageFloat32 ret = new ImageFloat32(left.width,left.height);

						alg.process(left,right,ret);

						return ret;
					}

					@Override
					public void initialize(int minDisparity , int maxDisparity) {
						alg = new StereoDisparityWtoNaive<ImageUInt8>(minDisparity,maxDisparity,2,3);
					}

					@Override public int getBorderX() { return 2; }

					@Override public int getBorderY() { return 3; }
				};

		alg.allChecks();
	}
}
