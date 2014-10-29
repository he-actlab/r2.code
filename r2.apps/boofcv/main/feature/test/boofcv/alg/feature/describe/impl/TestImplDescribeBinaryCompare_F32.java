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

package boofcv.alg.feature.describe.impl;


import boofcv.alg.feature.describe.BaseTestDescribePointBinaryCompare;
import boofcv.alg.feature.describe.DescribePointBinaryCompare;
import boofcv.alg.feature.describe.brief.BinaryCompareDefinition_I32;
import boofcv.struct.image.ImageFloat32;

/**
 * @author Peter Abeles
 */
public class TestImplDescribeBinaryCompare_F32 extends BaseTestDescribePointBinaryCompare<ImageFloat32> {
	public TestImplDescribeBinaryCompare_F32() {
		super(ImageFloat32.class);
	}

	@Override
	protected DescribePointBinaryCompare<ImageFloat32> createAlg(BinaryCompareDefinition_I32 def) {
		return new ImplDescribeBinaryCompare_F32(def);
	}
}
