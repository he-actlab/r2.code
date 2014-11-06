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

package org.ddogleg.nn.wrap;

import org.ddogleg.nn.StandardNearestNeighborTests;
import org.ddogleg.nn.alg.AxisSplitRule;
import org.ddogleg.nn.alg.AxisSplitRuleRandomK;
import org.ddogleg.nn.alg.AxisSplitterMedian;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestKdForestBbfSearch extends StandardNearestNeighborTests {

	public TestKdForestBbfSearch() {
		// set the max nodes so it that it will produce perfect results
		AxisSplitRule rule = new AxisSplitRuleRandomK(new Random(234),1);
		setAlg(new KdForestBbfSearch(5,10000,new AxisSplitterMedian(rule)));
	}
}
