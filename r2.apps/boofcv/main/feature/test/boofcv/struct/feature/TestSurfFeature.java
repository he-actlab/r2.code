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

package boofcv.struct.feature;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSurfFeature {

	Random rand = new Random(234);

	@Test
	public void setTo() {

		SurfFeature a = new SurfFeature(10);
		a.laplacianPositive = true;
		for( int i = 0; i < a.value.length; i++ )
			a.value[i] = rand.nextDouble();

		SurfFeature b = new SurfFeature(10);

		b.setTo(a);

		checkIdentical(a, b);
	}

	@Test
	public void copy() {
		SurfFeature a = new SurfFeature(10);
		a.laplacianPositive = true;
		for( int i = 0; i < a.value.length; i++ )
			a.value[i] = rand.nextDouble();

		SurfFeature b = a.copy();

		checkIdentical(a, b);
	}

	private void checkIdentical(SurfFeature a, SurfFeature b) {
		assertTrue(a.laplacianPositive==b.laplacianPositive);
		assertEquals(a.value.length,b.value.length);
		for( int i = 0; i < a.value.length; i++ ) {
			assertTrue(a.value[i] == b.value[i]);
		}
	}


}
