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

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.wrap.Individual_to_CoupledJacobian;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionLeastSquares {

	/**
	 * See if it immediately exits on a perfect initial guess
	 */
	@Test
	public void perfectInitial() {

		double a=2,b=0.1;

		TrustRegionLeastSquares alg = createAlg(a,b,new DummyStep());

		alg.initialize(new double[]{2,0.1});

		assertTrue(alg.iterate());

		double found[] = alg.getParameters();

		assertEquals(a, found[0], 1e-4);
		assertEquals(b, found[1], 1e-4);
	}

	private TrustRegionLeastSquares createAlg( double a, double b , TrustRegionStep stepAlg ) {

		FunctionNtoM residual = new TrivialLeastSquaresResidual(a,b);
		FunctionNtoMxN jacobian = new NumericalJacobianForward(residual);

		TrustRegionLeastSquares alg = new TrustRegionLeastSquares(2,stepAlg);

		alg.setConvergence(1e-6,1e-6);
		alg.setFunction(new Individual_to_CoupledJacobian(residual,jacobian));

		return alg;
	}

	/**
	 * Basic test that is easily solved using the CauchyStep
	 */
	@Test
	public void basicTest_Cauchy() {
		basicTest(new CauchyStep());
	}

	/**
	 * Basic test that is easily solved using the CauchyStep
	 */
	@Test
	public void basicTest_DoglegFtF() {
		basicTest(new DoglegStepFtF());
	}

	/**
	 * Basic test that is easily solved using the CauchyStep
	 */
	@Test
	public void basicTest_DoglegF() {
		basicTest(new DoglegStepF());
	}


	protected void basicTest( TrustRegionStep step ) {
		double a=2,b=0.1;

		TrustRegionLeastSquares alg = createAlg(a,b,step);

		alg.initialize(new double[]{1,0.5});
		alg.setConvergence(1e-8,1e-8);

		int i;
		for( i = 0; i < 200 && !alg.iterate(); i++ ) { }

		// should converge way before this
		assertTrue(i != 200);
		assertTrue(alg.isConverged());

		double found[] = alg.getParameters();

		assertEquals(a, found[0], 1e-4);
		assertEquals(b, found[1], 1e-4);
	}
	

	static class DummyStep implements TrustRegionStep {

		@Override
		public void init(int numParam, int numFunctions) {}

		@Override
		public void setInputs(DenseMatrix64F x, DenseMatrix64F residuals, DenseMatrix64F J,
							  DenseMatrix64F gradient , double fx)
		{}

		@Override
		public void computeStep(double regionRadius, DenseMatrix64F step) {}

		@Override
		public double predictedReduction() {
			return 0;
		}

		@Override
		public boolean isMaxStep() {
			return false;
		}
	}

}
