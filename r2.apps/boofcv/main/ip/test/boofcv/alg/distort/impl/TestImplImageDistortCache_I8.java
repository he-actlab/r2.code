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

package boofcv.alg.distort.impl;

import boofcv.alg.distort.ImageDistortCache;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.core.image.border.ImageBorder;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
public class TestImplImageDistortCache_I8 extends CommonImageDistortCacheTests<ImageUInt8> {

	public TestImplImageDistortCache_I8() {
		super(ImageUInt8.class);
	}

	@Override
	public ImageDistortCache<ImageUInt8,ImageUInt8> create(InterpolatePixelS<ImageUInt8> interp,
														   ImageBorder<ImageUInt8> border,
														   Class<ImageUInt8> imageType) {
		return new ImplImageDistortCache_I8<ImageUInt8,ImageUInt8>(interp,border);
	}
}
