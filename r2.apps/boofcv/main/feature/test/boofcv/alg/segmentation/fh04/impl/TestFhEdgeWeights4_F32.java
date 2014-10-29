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

package boofcv.alg.segmentation.fh04.impl;

import boofcv.alg.segmentation.fh04.FhEdgeWeights;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;

import java.util.Random;

/**
* @author Peter Abeles
*/
public class TestFhEdgeWeights4_F32 extends GenericFhEdgeWeightsChecks<ImageFloat32> {

	Random rand = new Random(234);

	public TestFhEdgeWeights4_F32() {
		super(ImageType.single(ImageFloat32.class), ConnectRule.FOUR);
	}

	@Override
	public FhEdgeWeights<ImageFloat32> createAlg() {
		return new FhEdgeWeights4_F32();
	}

	@Override
	public float weight( ImageFloat32 input , int indexA, int indexB) {
		return Math.abs(input.data[indexA] - input.data[indexB]);
	}
}
