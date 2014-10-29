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

package boofcv.alg.filter.convolve;

import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestConvolveImageNoBorder {

	@Test
	public void compareToStandard_symmetric() {
		CompareToStandardConvolution a = new CompareToStandardConvolution(ConvolveImageNoBorder.class);
		a.setKernelRadius(2);
		a.setOffset(2);
		a.performTests(22);
	}

	@Test
	public void compareToStandard_UNsymmetric() {
		CompareToStandardConvolution a = new CompareToStandardConvolution(ConvolveImageNoBorder.class);
		a.setKernelRadius(2);
		a.setOffset(1);
		a.performTests(22);
	}
}
