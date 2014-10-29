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

import boofcv.alg.feature.disparity.DisparityScoreSadRect;
import boofcv.alg.feature.disparity.DisparitySparseScoreSadRect;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
public class TestImplDisparitySparseScoreSadRect_F32 extends ChecksImplDisparitySparseScoreSadRect<ImageFloat32,float[]>{

	public TestImplDisparitySparseScoreSadRect_F32() {
		super(ImageFloat32.class);
	}

	@Override
	public DisparityScoreSadRect<ImageFloat32, ImageUInt8> createDense(int minDisparity, int maxDisparity,
																	   int radiusX, int radiusY) {
		return new ImplDisparityScoreSadRect_F32<ImageUInt8>(minDisparity,maxDisparity,radiusX,radiusY,
				new ImplSelectRectBasicWta_F32_U8());
	}

	@Override
	public DisparitySparseScoreSadRect<float[], ImageFloat32> createSparse(int minDisparity, int maxDisparity,
																		   int radiusX, int radiusY) {
		return new ImplDisparitySparseScoreSadRect_F32(minDisparity,maxDisparity,radiusX,radiusY);
	}
}
