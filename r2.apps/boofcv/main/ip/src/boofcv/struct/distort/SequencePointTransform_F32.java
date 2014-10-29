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

package boofcv.struct.distort;

import georegression.struct.point.Point2D_F32;

/**
 * Combines together multiple {@link PointTransform_F32} as a sequence into a single transform.
 *
 * @author Peter Abeles
 */
public class SequencePointTransform_F32 implements PointTransform_F32 {
	PointTransform_F32[] sequence;

	/**
	 * Specifies the sequence of transforms.  Lower indexes are applied first.
	 *
	 * @param sequence Sequence of transforms.
	 */
	public SequencePointTransform_F32( PointTransform_F32 ...sequence ) {
		this.sequence = sequence;
	}

	@Override
	public void compute(float x, float y, Point2D_F32 out) {
		sequence[0].compute(x,y,out);
		for( int i = 1; i < sequence.length; i++ ) {
			sequence[i].compute(out.x,out.y,out);
		}
	}
}
