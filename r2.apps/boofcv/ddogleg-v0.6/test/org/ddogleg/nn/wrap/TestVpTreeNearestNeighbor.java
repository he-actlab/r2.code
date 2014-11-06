/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.StandardNearestNeighborTests;

/**
 * @author Karel Petránek
 */
public class TestVpTreeNearestNeighbor extends StandardNearestNeighborTests {
	public TestVpTreeNearestNeighbor() {
		setAlg(FactoryNearestNeighbor.<Double>vptree(345345));
	}
}
