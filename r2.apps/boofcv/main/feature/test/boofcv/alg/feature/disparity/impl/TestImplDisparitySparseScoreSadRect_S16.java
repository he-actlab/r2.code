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
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
public class TestImplDisparitySparseScoreSadRect_S16 extends ChecksImplDisparitySparseScoreSadRect<ImageSInt16,int[]>{

	public TestImplDisparitySparseScoreSadRect_S16() {
		super(ImageSInt16.class);
	}

	@Override
	public DisparityScoreSadRect<ImageSInt16, ImageUInt8> createDense(int minDisparity, int maxDisparity,
																	   int radiusX, int radiusY) {
		return new ImplDisparityScoreSadRect_S16<ImageUInt8>(minDisparity,maxDisparity,radiusX,radiusY,
				new ImplSelectRectBasicWta_S32_U8());
	}

	@Override
	public DisparitySparseScoreSadRect<int[], ImageSInt16> createSparse(int minDisparity, int maxDisparity,
																		   int radiusX, int radiusY) {
		return new ImplDisparitySparseScoreSadRect_S16(minDisparity,maxDisparity,radiusX,radiusY);
	}
}
