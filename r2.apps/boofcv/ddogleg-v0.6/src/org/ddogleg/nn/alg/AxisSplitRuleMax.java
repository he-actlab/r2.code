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

package org.ddogleg.nn.alg;

/**
 * Selects the axis with the largest variance to split.
 *
 * @author Peter Abeles
 */
public class AxisSplitRuleMax implements AxisSplitRule {

	int N;

	@Override
	public void setDimension(int N) {
		this.N = N;
	}

	@Override
	public int select(double[] var) {
		int split = -1;
		double bestVar = -1;
		for( int i = 0; i < N; i++ ) {
			if( var[i] > bestVar ) {
				split = i;
				bestVar = var[i];
			}
		}

		return split;
	}
}
