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

package org.ddogleg.optimization.impl;

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Base class for Levenberg type algorithms.  TODO describe in more detail
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class LevenbergBase {

	// number of parameters
	protected int N;
	// number of functions
	protected int M;

	// tolerance for termination. magnitude of gradient. absolute
	private double gtol;
	// tolerance for termination, change in function value.  relative
	private double ftol;

	// current set of parameters being considered
	private DenseMatrix64F x = new DenseMatrix64F(1,1);
	// gradient at 'x' = J'*f(x)
	private DenseMatrix64F g = new DenseMatrix64F(1,1);
	// function residuals values at x
	private DenseMatrix64F funcVals = new DenseMatrix64F(1,1);

	// Current x being considered
	private DenseMatrix64F xtest = new DenseMatrix64F(1,1);
	private DenseMatrix64F xdelta = new DenseMatrix64F(1,1);

	// function value norm at x
	private double fnorm;
	// previous value of fnorm
	private double fnormPrev;

	// levenberg marquardt dampening parameter
	private double dampParam;
	private double initialDampParam;

	// used to scale the dampening parameter
	private double nu;

	// is it searching for a new dampening parameter or setting up the next iteration
	private int mode;

	// true if x was updated in this iteration
	private boolean updatedParameters;

	// has it converged or not
	private boolean hasConverged;

	// total number of iterations
	private int iterationCount;

	// message explaining failure
	private String message;

	/**
	 * Specifies termination condition and dampening parameter
	 *
	 * @param initialDampParam Initial value of the dampening parameter.  Tune.. try 1e-3;
	 */
	public LevenbergBase(double initialDampParam ) {
		this.initialDampParam = initialDampParam;
	}

	/**
	 * Specify convergence tolerances
	 *
	 * @param gtol absolute convergence tolerance based on gradient norm. 0 <= gtol
	 */
	public void setConvergence( double ftol , double gtol ) {
		if( ftol < 0 || ftol >= 1 )
			throw new IllegalArgumentException("0 <= ftol < 1");
		if( gtol < 0 )
			throw new IllegalArgumentException("gtol < 0 ");

		this.gtol = gtol;
		this.ftol = ftol;
	}

	/**
	 * Specify the initial value of the dampening parameter.
	 *
	 * @param initialDampParam initial value
	 */
	public void setInitialDampParam(double initialDampParam) {
		this.initialDampParam = initialDampParam;
	}

	/**
	 * Initializes internal parameters.
	 */
	protected void internalInitialize( int numParameters , int numFunctions ) {
		this.N = numParameters;
		this.M = numFunctions;

		x.reshape(N, 1, false);
		g.reshape(N,1,false);
		xdelta.reshape(N, 1, false);
		xtest.reshape(N,1,false);
		funcVals.reshape(M,1,false);
	}

	/**
	 * Sets the initial parameter being searched.
	 *
	 * @param initial
	 */
	public void initialize(double[] initial) {
		if( initial.length < N)
			throw new IllegalArgumentException("Expected N="+N+" parameters");
		System.arraycopy(initial,0,x.data,0,N);

		// calculate residuals
		setFunctionParameters(x.data);
		computeResiduals(funcVals.data);

		// error at this point
		fnorm = computeError();
		fnormPrev = 0;
		
		hasConverged = false;
		mode = 0;
		dampParam = initialDampParam;
		nu = 2;
		iterationCount = 0;

		message = "";
	}

	protected abstract void setFunctionParameters( double []param );

	protected abstract void computeResiduals( double[] output );

	/**
	 * Computes the Jacobian matrix,
	 */
	protected abstract void computeJacobian( DenseMatrix64F residuals ,
											 DenseMatrix64F gradient );

	protected abstract boolean computeStep( double dampeningParam ,
											DenseMatrix64F gradientNegative ,
											DenseMatrix64F step );

	protected abstract double predictedReduction( DenseMatrix64F param, DenseMatrix64F gradientNegative , double dampeningParam );

	/**
	 * Returns the minimum allowed value for the dampening parameters.  This should be a function
	 * of the Jacobian matrix's scale.
	 *
	 * @return
	 */
	protected abstract double getMinimumDampening();

	public double[] getParameters() {
		return x.data;
	}

	/**
	 * Performs a single step in the optimization.
	 *
	 * @return true if the optimization has finished.
	 */
	public boolean iterate() {
		updatedParameters = false;
		if( mode == 0 ) {
			return initSamplePoint();
		} else {
			if( !computeStep() )
				return true;
		}

		return false;
	}

	/**
	 * Evaluates the Jacobian, computes the gradient and Hessian approximation.  Checks for convergence.
	 *
	 * @return true if it has converged
	 */
	private boolean initSamplePoint() {
		// calculate the Jacobian values at the current sample point
		computeJacobian(funcVals,g);
		CommonOps.scale(-1, g);

		// Find the derivative along the current Jacobian's direction
		double gx = CommonOps.elementMaxAbs(g);

		// check for convergence
		if( Math.abs(fnorm-fnormPrev) <= ftol*Math.max(fnorm,fnormPrev) || Math.abs(gx) <= gtol )
			return terminateSearch(true, null);

		fnormPrev = fnorm;
		
		mode = 1;
		return false;
	}

	private boolean computeStep() {
		// Solves for xdelta
		// (B + mu*I)xdelta = -g
		// where mu is dampParam
		if( !solveForXDelta() )
			return false;

		// xtest = x + delta x
		CommonOps.add(x, xdelta, xtest);
		// take in account rounding error
		CommonOps.subtract(xtest, x, xdelta);

		// compute the residuals at x
		setFunctionParameters(xtest.data);
		computeResiduals(funcVals.data);

		// actual reduction
		double ftestnorm = computeError();
		double actualReduction = fnorm - ftestnorm;

		// Predicted reduction
		double predictedReduction = predictedReduction(xdelta,g,dampParam);

		// update the dampParam depending on the results
		if( predictedReduction > 0 && actualReduction >= 0 ) {
			// set the test point to be the new point
			DenseMatrix64F temp = x;
			x = xtest; xtest = temp;
			// updated residual norm
			fnorm = ftestnorm;

			// reduction ratio
			double ratio = actualReduction/predictedReduction;
			// reduce the amount of dampening.  Magic equation from [1].  My attempts to improve
			// upon it have failed.  It is truly magical.
			dampParam *= Math.max(0.3333333,1-Math.pow(2*ratio-1,3));

			nu = 2;

			// start the iteration over again
			mode = 0;
			iterationCount++;
			updatedParameters = true;
		} else {
			//  did not improve, increase the amount of dampening
			dampParam *= nu;
			nu *= 2;
		}
		
		if( Double.isInfinite(dampParam) || Double.isNaN(dampParam))
			return false;
		
		return true;
	}

	/**
	 * Sets up the linear system to find the change in x.  If the solver fails or is nearly singular
	 * then the dampParam is increased.
	 */
	protected boolean solveForXDelta() {
//		double max = CommonOps.elementMax(Bdiag);
//
//		// if the matrix is null do a simple gradient descent search
//		if( max == 0 ) {
//			for( int i = 0; i < N; i++ ) {
//				xdelta.data[i] = g.data[i]/dampParam;
//			}
//			return true;
//		}

		// Adjust the dampening parameter until the solution can be solved
		// This is not designed to take advantage of QR decomposition, which is why cholesky is recommended
		boolean failed = true;
		for( int iter = 0; iter < 1000 && failed; iter++ ) {
			if( computeStep(dampParam,g,xdelta) ) {
				failed = false;
			}

			if( failed ) {
				dampParam = Math.max(10*dampParam, getMinimumDampening());
			}
		}
		if( failed ) {
			addToMessage("Failed to find dampParam which cold be solved");
			return false;
		}

		return true;
	}

	/**
	 * Helper function that lets converged and the final message bet set in one line
	 */
	private boolean terminateSearch( boolean converged , String message ) {
		this.hasConverged = converged;

		return true;
	}

	/**
	 * Computes the residual's error:
	 *
	 * sum_i 0.5*fi(x)^2
	 */
	private double computeError() {
		return VectorVectorMult.innerProd(funcVals,funcVals)/2.0;
	}

	public boolean isConverged() {
		return hasConverged;
	}

	public int getIterationCount() {
		return iterationCount;
	}

	public void addToMessage(String message) {
		this.message += message+"\n";
	}

	public String getMessage() {
		return message;
	}

	public double getFnorm() {
		return fnorm;
	}

	public boolean isUpdatedParameters() {
		return updatedParameters;
	}
}
