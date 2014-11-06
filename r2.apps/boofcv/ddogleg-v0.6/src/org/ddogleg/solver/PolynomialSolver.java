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

package org.ddogleg.solver;

import org.ddogleg.complex.ComplexMath64F;
import org.ddogleg.solver.impl.FindRealRootsSturm;
import org.ddogleg.solver.impl.RootFinderCompanion;
import org.ddogleg.solver.impl.WrapRealRootsSturm;
import org.ejml.data.Complex64F;

import java.util.List;

/**
 * Provides functions for finding the roots of polynomials
 *
 * @author Peter Abeles
 */
public class PolynomialSolver {

	/**
	 * Creates a generic polynomial root finding class which will return all real or all real and complex roots
	 * depending on the algorithm selected.
	 *
	 * @param type Which algorithm is to be returned.
	 * @param maxDegree Maximum degree of the polynomial being considered.
	 * @return Root finding algorihtm.
	 */
	public static PolynomialRoots createRootFinder( RootFinderType type , int maxDegree ) {
		switch ( type ) {
			case EVD:
				return new RootFinderCompanion();

			case STURM:
				FindRealRootsSturm sturm = new FindRealRootsSturm(maxDegree,-1,1e-10,30,20);
				return new WrapRealRootsSturm(sturm);
		}

		throw new IllegalArgumentException("Unknown type");
	}


	/**
	 * Finds real and imaginary roots in a polynomial using the companion matrix and
	 * Eigenvalue decomposition.  The coefficients order is specified from smallest to largest.
	 * Example, 5 + 6*x + 7*x^2 + 8*x^3 = [5,6,7,8]
	 *
	 * @param coefficients Polynomial coefficients from smallest to largest.
	 * @return The found roots.
	 */
	@SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
	public static Complex64F[] polynomialRootsEVD(double... coefficients) {

		PolynomialRoots alg = new RootFinderCompanion();

		if( !alg.process( Polynomial.wrap(coefficients)) )
			throw new IllegalArgumentException("Algorithm failed, was the input bad?");

		List<Complex64F> coefs = alg.getRoots();

		return coefs.toArray(new Complex64F[0]);
	}

	/**
	 * <p>
	 * A cubic polynomial of the form "f(x) =  a + b*x + c*x<sup>2</sup> + d*x<sup>3</sup>" has
	 * three roots.  These roots will either be all real or one real and two imaginary.  This function
	 * will return a root which is always real.
	 * </p>
	 *
	 * <p>
	 * WARNING: Not as numerically stable as {@link #polynomialRootsEVD(double...)}, but still fairly stable.
	 * </p>
	 *
	 * @param a polynomial coefficient.
	 * @param b polynomial coefficient.
	 * @param c polynomial coefficient.
	 * @param d polynomial coefficient.
	 * @return A real root of the cubic polynomial
	 */
	public static double cubicRootReal(double a, double b, double c, double d)
	{
		// normalize for numerical stability
		double norm = Math.max(Math.abs(a), Math.abs(b));
		norm = Math.max(norm,Math.abs(c));
		norm = Math.max(norm, Math.abs(d));

		a /= norm;
		b /= norm;
		c /= norm;
		d /= norm;

		// proceed with standard algorithm
		double insideLeft = 2*c*c*c - 9*d*c*b + 27*d*d*a;
		double temp = c*c-3*d*b;
		double insideOfSqrt = insideLeft*insideLeft - 4*temp*temp*temp;

		if( insideOfSqrt >= 0 ) {
			double insideRight = Math.sqrt(insideOfSqrt );

			double ret = c +
					root3(0.5*(insideLeft+insideRight)) +
					root3(0.5*(insideLeft-insideRight));

			return -ret/(3.0*d);
		} else {
			Complex64F inside = new Complex64F(0.5*insideLeft,0.5*Math.sqrt(-insideOfSqrt ));
			Complex64F root = new Complex64F();

			ComplexMath64F.root(inside, 3, 2, root);

			// imaginary components cancel out
			double ret = c + 2*root.getReal();

			return -ret/(3.0*d);
		}
	}

	private static double root3( double val ) {
		if( val < 0 )
			return -Math.pow(-val,1.0/3.0);
		else
			return Math.pow(val,1.0/3.0);
	}

	/**
	 * <p>
	 * The cubic discriminant is used to determine the type of roots.
	 * <ul>
	 * <li>if d > 0, then three distinct real roots</li>
	 * <li>if d = 0, then it has a multiple root and all will be real</li>
	 * <li>if d < 0, then one real and two non-real complex conjugate roots</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * From http://en.wikipedia.org/wiki/Cubic_function Novemeber 17, 2011
	 * </p>
	 *
	 * @param a polynomial coefficient.
	 * @param b polynomial coefficient.
	 * @param c polynomial coefficient.
	 * @param d polynomial coefficient.
	 * @return Cubic discriminant
	 */
	public static double cubicDiscriminant(double a, double b, double c, double d) {
		return 18.0*d*c*b*a -4*c*c*c*a + c*c*b*b -4*d*b*b*b - 27*d*d*a*a;
	}
}
