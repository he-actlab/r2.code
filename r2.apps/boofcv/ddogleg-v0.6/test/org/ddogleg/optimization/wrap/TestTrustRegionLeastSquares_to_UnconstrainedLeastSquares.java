/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.RegionStepType;
import org.ddogleg.optimization.UnconstrainedLeastSquares;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionLeastSquares_to_UnconstrainedLeastSquares extends GenericUnconstrainedLeastSquaresTests {

	@Override
	public UnconstrainedLeastSquares createAlgorithm() {
		return FactoryOptimization.leastSquaresTrustRegion(0.1, RegionStepType.DOG_LEG_F,true);
	}
}
