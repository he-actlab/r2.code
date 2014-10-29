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

package boofcv.alg.feature.detect.intensity.impl;

import boofcv.alg.filter.derivative.GradientSobel;
import boofcv.alg.misc.GImageMiscOps;
import boofcv.struct.BoofDefaults;
import boofcv.struct.image.ImageFloat32;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestImplSsdCorner_F32 {

	Random rand = new Random(234);
	int width = 40;
	int height = 50;
	
	int radius=4;

	ImageFloat32 input = new ImageFloat32(width,height);

	ImageFloat32 derivX = new ImageFloat32(width,height);
	ImageFloat32 derivY = new ImageFloat32(width,height);

	ImageFloat32 derivXX = new ImageFloat32(width,height);
	ImageFloat32 derivXY = new ImageFloat32(width,height);
	ImageFloat32 derivYY = new ImageFloat32(width,height);

	/**
	 * Manually compute intensity values and see if they are the same
	 */
	@Test
	public void compareToManual() {
		GImageMiscOps.fillUniform(input, rand, 0, 100);

		GradientSobel.process(input,derivX,derivY, BoofDefaults.borderDerivative_F32());

		for( int i = 0; i < height; i++ ) {
			for( int j = 0; j < width; j++ ) {
				float x = derivX.get(j,i);
				float y = derivY.get(j,i);

				derivXX.set(j,i,x*x);
				derivXY.set(j,i,x*y);
				derivYY.set(j,i,y*y);
			}
		}

		Sdd alg = new Sdd(radius);

		alg.process(derivX,derivY, new ImageFloat32(width,height));
	}
	
	public float sum( int x , int y , ImageFloat32 img ) {
		float ret = 0;
		
		for( int i = -radius; i <= radius; i++ ) {
			float hsum = 0;
			for( int j = -radius; j <= radius; j++ ) {
				hsum += img.get(j+x,i+y);
			}
			ret += hsum;
		}
		
		return ret;
	}
	
	private class Sdd extends ImplSsdCorner_F32 {

		int count = 0;
		
		public Sdd(int radius) {
			super(radius);
		}

		@Override
		protected float computeIntensity() {

			float xx = sum(x,y,derivXX);
			float xy = sum(x,y,derivXY);
			float yy = sum(x,y,derivYY);

			// take in account rounding error
			assertEquals(x+" "+y,xx, totalXX, 1);
			assertEquals(x+" "+y,xy, totalXY, 1);
			assertEquals(x+" "+y,yy, totalYY, 1);

			count++;
			return 0;
		}
	}
}
