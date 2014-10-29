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

package boofcv.alg.feature.detect.extract;

import boofcv.struct.QueueCorner;
import boofcv.struct.image.ImageFloat32;

/**
 * @author Peter Abeles
 */
public class TestNonMaxCandidateRelaxed extends GenericNonMaxCandidateTests{

	public TestNonMaxCandidateRelaxed() {
		super(false, true, true);
	}

	@Override
	public void findMaximums(ImageFloat32 intensity, float threshold, int radius, int border,
							 QueueCorner candidatesMin, QueueCorner candidatesMax,
							 QueueCorner foundMinimum, QueueCorner foundMaximum)
	{
		NonMaxCandidateRelaxed alg = new NonMaxCandidateRelaxed();
		alg.radius = radius;
		alg.ignoreBorder = border;
		alg.thresholdMin = -threshold;
		alg.thresholdMax = threshold;

		alg.process(intensity, candidatesMin, candidatesMax, foundMinimum, foundMaximum);
	}

}
