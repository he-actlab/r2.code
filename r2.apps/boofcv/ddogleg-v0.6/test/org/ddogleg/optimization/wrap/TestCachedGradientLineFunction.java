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

import org.ddogleg.optimization.CallCounterNtoN;
import org.ddogleg.optimization.CallCounterNtoS;
import org.ddogleg.optimization.impl.NumericalGradientForward;
import org.ddogleg.optimization.impl.TrivialFunctionNtoS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCachedGradientLineFunction {

	/**
	 * Use setInput(double[]) and see if it has the expected behavior
	 */
	@Test
	public void testSetInput_vector() {
		CallCounterNtoS func = new CallCounterNtoS(new TrivialFunctionNtoS());
		CallCounterNtoN gradient = new CallCounterNtoN(new NumericalGradientForward(new TrivialFunctionNtoS()));

		CachedGradientLineFunction alg = new CachedGradientLineFunction(func,gradient);
		
		double x[] = new double[]{1,2,3};
		alg.setInput(x);
		
		assertEquals(0, func.count);
		assertEquals(0, gradient.count);

		double foundG[] = new double[x.length];
		double expectedG[] = new double[x.length];
		
		alg.computeFunction();
		alg.computeFunction();
		
		alg.computeGradient(foundG);
		alg.computeGradient(foundG);

		// functions should have only been evaluated once
		assertEquals(1, func.count);
		assertEquals(1, gradient.count);

		// switch the input and see if things change
		x = new double[]{2,3,1};
		alg.setInput(x);

		double foundF = alg.computeFunction();
		alg.computeGradient(foundG);

		// see if the process counter went up one
		assertEquals(2, func.count);
		assertEquals(2, gradient.count);

		// make sure the value is as expected
		gradient.process(x,expectedG);
		assertEquals(func.process(x), foundF, 1e-8);
		for( int i = 0; i < 3; i++ )
			assertEquals(expectedG[i], foundG[i], 1e-8);
	}

	/**
	 * Use line search interface and see if it has the expected behavior
	 */
	@Test
	public void testSetInput_step() {
		CallCounterNtoS func = new CallCounterNtoS(new TrivialFunctionNtoS());
		CallCounterNtoN gradient = new CallCounterNtoN(new NumericalGradientForward(new TrivialFunctionNtoS()));

		CachedGradientLineFunction alg = new CachedGradientLineFunction(func,gradient);

		double x[] = new double[]{1,2,3};
		double d[] = new double[]{1,1,1};
		alg.setLine(x,d);
		alg.setInput(1);

		assertEquals(0, func.count);
		assertEquals(0, gradient.count);

		double foundG[] = new double[x.length];

		alg.computeFunction();
		alg.computeFunction();

		// gradient should use the same answer
		alg.computeGradient(foundG);
		alg.computeGradient(foundG);

		alg.computeDerivative();
		alg.computeDerivative();

		// functions should have only been evaluated once
		assertEquals(1, func.count);
		assertEquals(1, gradient.count);

		// switch the input and see if things change
		alg.setInput(2);

		double foundF = alg.computeFunction();
		alg.computeDerivative();

		// see if the process counter went up one
		assertEquals(2, func.count);
		assertEquals(2, gradient.count);

		// make sure the value is as expected
		x = new double[]{1+2,2+2,3+2};
		assertEquals(func.process(x), foundF, 1e-8);
	}
}
