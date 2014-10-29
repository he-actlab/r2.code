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

package boofcv.alg.transform.ii.impl;

import boofcv.alg.transform.ii.IntegralImageOps;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.sparse.SparseScaleSample_F64;

/**
 * Samples a square region inside an integral image
 *
 * @author Peter Abeles
 */
public class SparseIntegralSample_F32 extends SparseScaleSample_F64<ImageFloat32> {
	
	int baseR;
	int r;

	public SparseIntegralSample_F32(int baseR) {
		this.baseR = baseR;
		setScale(1);
	}

	@Override
	public void setScale(double scale) {
		r = (int)(baseR*scale + 0.5);
		if( r <= 0 )
			r = 1;
		
		x0 = y0 = -r-1;
		x1 = y1 = r;
	}

	@Override
	public double compute(int x, int y) {
		return IntegralImageOps.block_unsafe(input,x+x0,y+y0,x+x1,y+y1);
	}
}
