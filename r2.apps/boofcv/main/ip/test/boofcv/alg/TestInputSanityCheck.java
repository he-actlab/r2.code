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

package boofcv.alg;

import boofcv.struct.image.ImageUInt8;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestInputSanityCheck {

	int imgWidth = 10;
	int imgHeight = 20;

	@Test
	public void checkShape_two() {
		ImageUInt8 a = new ImageUInt8(imgWidth, imgHeight);
		ImageUInt8 b = new ImageUInt8(imgWidth, imgHeight);

		// InputSanityCheck test
		InputSanityCheck.checkSameShape(a, b);

		// negative test
		try {
			b = new ImageUInt8(imgWidth + 1, imgHeight);
			InputSanityCheck.checkSameShape(a, b);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}

		try {
			b = new ImageUInt8(imgWidth, imgHeight + 1);
			InputSanityCheck.checkSameShape(a, b);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void checkShape_three() {
		ImageUInt8 a = new ImageUInt8(imgWidth, imgHeight);
		ImageUInt8 b = new ImageUInt8(imgWidth, imgHeight);
		ImageUInt8 c = new ImageUInt8(imgWidth, imgHeight);

		// InputSanityCheck test
		InputSanityCheck.checkSameShape(a, b, c);

		// negative test
		try {
			b = new ImageUInt8(imgWidth + 1, imgHeight);
			InputSanityCheck.checkSameShape(a, b, c);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}

		try {
			b = new ImageUInt8(imgWidth, imgHeight + 1);
			InputSanityCheck.checkSameShape(a, b, c);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}
		b = new ImageUInt8(imgWidth, imgHeight);
		try {
			c = new ImageUInt8(imgWidth + 1, imgHeight);
			InputSanityCheck.checkSameShape(a, b, c);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}

		try {
			c = new ImageUInt8(imgWidth, imgHeight + 1);
			InputSanityCheck.checkSameShape(a, b, c);
			fail("Didn't throw an exception");
		} catch (IllegalArgumentException e) {
		}
	}
}
