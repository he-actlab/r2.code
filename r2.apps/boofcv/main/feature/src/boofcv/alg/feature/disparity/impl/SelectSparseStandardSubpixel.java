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

/**
 * <p>
 * Subpixel accuracy for disparity.  See {@link SelectRectSubpixel} for more details on the
 * mathematics.
 * </p>
 *
 * @author Peter Abeles
 */
public class SelectSparseStandardSubpixel {

	public static class S32 extends ImplSelectSparseStandardWta_S32 {
		public S32(int maxError, double texture) {
			super(maxError, texture);
		}

		@Override
		public boolean select(int[] scores, int maxDisparity) {
			if( super.select(scores, maxDisparity) ) {

				int disparityValue = (int)disparity;

				if( disparityValue == 0 || disparityValue == maxDisparity-1) {
					return true;
				} else {
					int c0 = scores[disparityValue-1];
					int c1 = scores[disparityValue];
					int c2 = scores[disparityValue+1];

					double offset = (double)(c0-c2)/(double)(2*(c0-2*c1+c2));

					disparity += offset;
					return true;
				}

			} else {
				return false;
			}
		}

	}

	public static class F32 extends ImplSelectSparseStandardWta_F32 {
		public F32(int maxError, double texture) {
			super(maxError, texture);
		}

		@Override
		public boolean select(float[] scores, int maxDisparity) {
			if( super.select(scores, maxDisparity) ) {

				int disparityValue = (int)disparity;

				if( disparityValue == 0 || disparityValue == maxDisparity-1) {
					return true;
				} else {
					float c0 = scores[disparityValue-1];
					float c1 = scores[disparityValue];
					float c2 = scores[disparityValue+1];

					float offset = (c0-c2)/(2f*(c0-2*c1+c2));

					disparity += offset;
					return true;
				}

			} else {
				return false;
			}
		}

	}
}
