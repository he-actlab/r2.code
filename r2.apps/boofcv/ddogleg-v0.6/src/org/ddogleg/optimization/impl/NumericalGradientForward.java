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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ejml.UtilEjml;

/**
 * Finite difference numerical gradient calculation using forward equation. Forward
 * difference equation, f'(x) = f(x+h)-f(x)/h.  Scaling is taken in account by h based
 * upon the magnitude of the elements in variable x.
 *
 * <p>
 * NOTE: If multiple input parameters are modified by the function when a single one is changed numerical
 * derivatives aren't reliable.
 * </p>
 *
 * @author Peter Abeles
 */
public class NumericalGradientForward implements FunctionNtoN
{
	// number of input variables
	private final int N;
	// function being differentiated
	private FunctionNtoS function;

	// scaling of the difference parameter
	private double differenceScale;

	public NumericalGradientForward(FunctionNtoS function, double differenceScale) {
		this.function = function;
		this.differenceScale = differenceScale;
		this.N = function.getNumOfInputsN();
	}

	public NumericalGradientForward(FunctionNtoS function) {
		this(function,Math.sqrt(UtilEjml.EPS));
	}

	@Override
	public int getN() {
		return N;
	}

	@Override
	public void process(double[] input, double[] output) {
		double valueOrig = function.process(input);
		
		for( int i = 0; i < N; i++ ) {
			double x = input[i];
			double h = x != 0 ? differenceScale*Math.abs(x) : differenceScale;

			// takes in account round off error
			double temp = x+h;
			h = temp-x;

			input[i] = temp;
			double perturbed = function.process(input);
			output[i] = (perturbed - valueOrig)/h;
			input[i] = x;
		}
	}
}
