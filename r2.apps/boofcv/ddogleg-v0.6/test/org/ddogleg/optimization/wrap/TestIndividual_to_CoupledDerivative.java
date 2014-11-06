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

import org.ddogleg.optimization.impl.TrivialQuadraticStoS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestIndividual_to_CoupledDerivative {

	/**
	 * Sanity check to see if it blows up
	 */
	@Test
	public void trivial() {
		TrivialQuadraticStoS f = new TrivialQuadraticStoS(5);
		TrivialQuadraticStoS g = new TrivialQuadraticStoS(2);
		Individual_to_CoupledDerivative alg = new Individual_to_CoupledDerivative(f,g);

		double x = 2.1;
		alg.setInput(x);
		
		assertEquals(f.process(x),alg.computeFunction(),1e-8);
		assertEquals(g.process(x), alg.computeDerivative(), 1e-8);
	}
}
