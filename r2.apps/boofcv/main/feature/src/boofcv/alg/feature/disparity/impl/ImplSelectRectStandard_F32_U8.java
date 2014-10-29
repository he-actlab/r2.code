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

import boofcv.struct.image.ImageUInt8;

/**
 * <p>
 * Implementation of {@link boofcv.alg.feature.disparity.impl.ImplSelectRectStandardBase_F32} for {@link boofcv.struct.image.ImageUInt8}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplSelectRectStandard_F32_U8 extends ImplSelectRectStandardBase_F32<ImageUInt8>
{
	public ImplSelectRectStandard_F32_U8(int maxError, int rightToLeftTolerance, double texture) {
		super(maxError, rightToLeftTolerance, texture);
	}

	@Override
	public void configure(ImageUInt8 imageDisparity, int minDisparity, int maxDisparity, int radiusX) {
		super.configure(imageDisparity, minDisparity, maxDisparity, radiusX);

		if( rangeDisparity > 254 )
			throw new IllegalArgumentException("(max - min) disparity must be <= 254");
	}

	protected void setDisparity( int index , int value ) {
		imageDisparity.data[index] = (byte)value;
	}

	@Override
	public Class<ImageUInt8> getDisparityType() {
		return ImageUInt8.class;
	}
}
