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

package boofcv.struct.image;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Adds tests specific to integer images
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"PointlessBooleanExpression"})
public abstract class StandardImageIntegerTests extends StandardSingleBandTests {
	boolean expectedSign;

	protected StandardImageIntegerTests(boolean expectedSign) {
		this.expectedSign = expectedSign;
	}

	@Test
	public void checkSign() {
		ImageInteger<?> img = (ImageInteger<?>)createImage(10,10);

		assertEquals(expectedSign,img.getDataType().isSigned());
	}
}
