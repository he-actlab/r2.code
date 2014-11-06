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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestKdTree {

	@Test
	public void distanceSq() {
		KdTree alg = new KdTree(2);
		KdTree.Node n = new KdTree.Node(new double[]{1,2},null);
		double p[] = new double[]{2,5};

		double expected = 1*1 + 3*3;
		assertEquals(expected,KdTree.distanceSq(n,p,2),1e-8);
	}

	@Test
	public void isLeaf() {
		KdTree.Node n = new KdTree.Node();

		n.split = -1;
		assertTrue(n.isLeaf());
		n.split = 1;
		assertFalse(n.isLeaf());
	}

}
