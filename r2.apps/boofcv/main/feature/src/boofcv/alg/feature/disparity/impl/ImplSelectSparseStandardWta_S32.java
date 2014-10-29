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

import boofcv.alg.feature.disparity.SelectSparseStandardWta;

/**
 * <p>
 * Implementation of {@link SelectSparseStandardWta} for score arrays of type S32.
 * </p>
 *
 * <p>
 * DO NOT MODIFY. Generated by {@link GenerateSelectSparseStandardWta}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplSelectSparseStandardWta_S32 extends SelectSparseStandardWta<int[]> {

	// texture threshold, use an integer value for speed.
	protected int textureThreshold;
	protected static final int discretizer = 10000;

	public ImplSelectSparseStandardWta_S32(int maxError, double texture) {
		super(maxError,texture);
	}

	@Override
	protected void setTexture( double texture ) {
		this.textureThreshold = (int)(discretizer *texture);
	}

	@Override
	public boolean select(int[] scores, int maxDisparity) {
		int disparity = 0;
		int best = scores[0];

		for( int i = 1; i < maxDisparity; i++ ) {
			if( scores[i] < best ) {
				best = scores[i];
				disparity = i;
			}
		}

		if( best > maxError ) {
			return false;
		} else if( textureThreshold > 0 ) {
			// find the second best disparity value and exclude its neighbors
			int secondBest = Integer.MAX_VALUE;
			for( int i = 0; i < disparity-1; i++ ) {
				if( scores[i] < secondBest )
					secondBest = scores[i];
			}
			for( int i = disparity+2; i < maxDisparity; i++ ) {
				if( scores[i] < secondBest )
					secondBest = scores[i];
			}

			// similar scores indicate lack of texture
			// C = (C2-C1)/C1
			if( discretizer*(secondBest-best) <= textureThreshold*best )
				return false;
		}

		this.disparity = disparity;

		return true;
	}

}
