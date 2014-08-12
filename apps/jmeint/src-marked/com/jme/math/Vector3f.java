/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

import chord.analyses.expax.lang.math.*;

/*
 * -- Added *Local methods to cut down on object creation - JS
 */

/**
 * <code>Vector3f</code> defines a Vector for a three float value tuple.
 * <code>Vector3f</code> can represent any three dimensional value, such as a
 * vertex, a normal, etc. Utility methods are also included to aid in
 * mathematical calculations.
 *
 * @author Mark Powell
 * @author Joshua Slack
 */
public class Vector3f implements Externalizable, Cloneable {
    private static final Logger logger = Logger.getLogger(Vector3f.class.getName());

    private static final long serialVersionUID = 1L;

	public final static Vector3f ZERO = new Vector3f(0, 0, 0);	// op: 10: MOVE_F T12, FConst: 0.0	// op: 9: MOVE_F T11, FConst: 0.0	// op: 8: MOVE_F T10, FConst: 0.0	// st: 5: NEW T8, com.jme.math.Vector3f	// st: 5: NEW T8, com.jme.math.Vector3f	// st: 5: NEW T8, com.jme.math.Vector3f	// st: 5: NEW T8, com.jme.math.Vector3f

	public final static Vector3f UNIT_X = new Vector3f(1, 0, 0);	// op: 15: MOVE_F T15, FConst: 0.0	// op: 17: MOVE_F T17, FConst: 1.0	// op: 16: MOVE_F T16, FConst: 0.0	// st: 12: NEW T13, com.jme.math.Vector3f	// st: 12: NEW T13, com.jme.math.Vector3f	// st: 12: NEW T13, com.jme.math.Vector3f	// st: 12: NEW T13, com.jme.math.Vector3f
	public final static Vector3f UNIT_Y = new Vector3f(0, 1, 0);	// op: 23: MOVE_F T21, FConst: 1.0	// op: 22: MOVE_F T20, FConst: 0.0	// op: 24: MOVE_F T22, FConst: 0.0	// st: 19: NEW T18, com.jme.math.Vector3f	// st: 19: NEW T18, com.jme.math.Vector3f	// st: 19: NEW T18, com.jme.math.Vector3f	// st: 19: NEW T18, com.jme.math.Vector3f
    public final static Vector3f UNIT_Z = new Vector3f(0, 0, 1);	// op: 29: MOVE_F T25, FConst: 1.0	// op: 30: MOVE_F T26, FConst: 0.0	// op: 31: MOVE_F T27, FConst: 0.0	// st: 26: NEW T23, com.jme.math.Vector3f	// st: 26: NEW T23, com.jme.math.Vector3f	// st: 26: NEW T23, com.jme.math.Vector3f	// st: 26: NEW T23, com.jme.math.Vector3f
    public final static Vector3f UNIT_XYZ = new Vector3f(1, 1, 1);	// op: 36: MOVE_F T30, FConst: 1.0	// op: 37: MOVE_F T31, FConst: 1.0	// op: 38: MOVE_F T32, FConst: 1.0	// st: 33: NEW T28, com.jme.math.Vector3f	// st: 33: NEW T28, com.jme.math.Vector3f	// st: 33: NEW T28, com.jme.math.Vector3f	// st: 33: NEW T28, com.jme.math.Vector3f
    
	/**
     * the x value of the vector.
     */
    public float x;

    /**
     * the y value of the vector.
     */
    public float y;

    /**
     * the z value of the vector.
     */
    public float z;

    /**
     * Constructor instantiates a new <code>Vector3f</code> with default
     * values of (0,0,0).
     *
     */
    public Vector3f() {
        x = y = z = 0;	// op: 2: MOVE_F T1, FConst: 0.0	// op: 6: MOVE_F T4, T3	// op: 5: PUTFIELD_F T2, .z, T1	// op: 4: MOVE_F T3, T1	// op: 10: PUTFIELD_F R0, .x, T6	// op: 9: PUTFIELD_F T5, .y, T4	// op: 8: MOVE_F T6, T4
    }

    /**
     * Constructor instantiates a new <code>Vector3f</code> with provides
     * values.
     *
     * @param x
     *            the x value of the vector.
     * @param y
     *            the y value of the vector.
     * @param z
     *            the z value of the vector.
     */
    public Vector3f(float x, float y, float z) {
        this.x = x;	// op: 3: PUTFIELD_F R0, .x, R1	// op: 2: MOVE_F R1, R1
        this.y = y;	// op: 5: PUTFIELD_F R0, .y, R2	// op: 4: MOVE_F R2, R2
        this.z = z;	// op: 6: MOVE_F R3, R3	// op: 7: PUTFIELD_F R0, .z, R3
    }

    /**
     * Constructor instantiates a new <code>Vector3f</code> that is a copy
     * of the provided vector
     * @param copy The Vector3f to copy
     */
    public Vector3f(Vector3f copy) {
        this.set(copy);
    }

    /**
     * <code>set</code> sets the x,y,z values of the vector based on passed
     * parameters.
     *
     * @param x
     *            the x value of the vector.
     * @param y
     *            the y value of the vector.
     * @param z
     *            the z value of the vector.
     * @return this vector
     */
    public Vector3f set(float x, float y, float z) {
        this.x = x;	// op: 2: PUTFIELD_F R0, .x, R1	// op: 1: MOVE_F R1, R1
        this.y = y;	// op: 4: PUTFIELD_F R0, .y, R2	// op: 3: MOVE_F R2, R2
        this.z = z;	// op: 6: PUTFIELD_F R0, .z, R3	// op: 5: MOVE_F R3, R3
        return this;
    }

    /**
     * <code>set</code> sets the x,y,z values of the vector by copying the
     * supplied vector.
     *
     * @param vect
     *            the vector to copy.
     * @return this vector
     */
    public Vector3f set(Vector3f vect) {
        this.x = vect.x;
        this.y = vect.y;
        this.z = vect.z;
        return this;
    }

    /**
     *
     * <code>add</code> adds a provided vector to this vector creating a
     * resultant vector which is returned. If the provided vector is null, null
     * is returned.
     *
     * Neither 'this' nor 'vec' are modified.
     *
     * @param vec the vector to add to this.
     * @return the resultant vector.
     */
    public Vector3f add(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        return new Vector3f(x + vec.x, y + vec.y, z + vec.z);
    }

    /**
     *
     * <code>add</code> adds the values of a provided vector storing the
     * values in the supplied vector.
     *
     * @param vec
     *            the vector to add to this
     * @param result
     *            the vector to store the result in
     * @return result returns the supplied result vector.
     */
    public Vector3f add(Vector3f vec, Vector3f result) {
        result.x = x + vec.x;
        result.y = y + vec.y;
        result.z = z + vec.z;
        return result;
    }

    /**
     * <code>addLocal</code> adds a provided vector to this vector internally,
     * and returns a handle to this vector for easy chaining of calls. If the
     * provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to add to this vector.
     * @return this
     */
    public Vector3f addLocal(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    /**
     *
     * <code>add</code> adds the provided values to this vector, creating a
     * new vector that is then returned.
     *
     * @param addX
     *            the x value to add.
     * @param addY
     *            the y value to add.
     * @param addZ
     *            the z value to add.
     * @return the result vector.
     */
    public Vector3f add(float addX, float addY, float addZ) {
        return new Vector3f(x + addX, y + addY, z + addZ);
    }

    /**
     * <code>addLocal</code> adds the provided values to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     *
     * @param addX
     *            value to add to x
     * @param addY
     *            value to add to y
     * @param addZ
     *            value to add to z
     * @return this
     */
    public Vector3f addLocal(float addX, float addY, float addZ) {
        x += addX;
        y += addY;
        z += addZ;
        return this;
    }

    /**
     *
     * <code>scaleAdd</code> multiplies this vector by a scalar then adds the
     * given Vector3f.
     *
     * @param scalar
     *            the value to multiply this vector by.
     * @param add
     *            the value to add
     */
    public void scaleAdd(float scalar, Vector3f add) {
        x = x * scalar + add.x;
        y = y * scalar + add.y;
        z = z * scalar + add.z;
    }

    /**
     *
     * <code>scaleAdd</code> multiplies the given vector by a scalar then adds
     * the given vector.
     *
     * @param scalar
     *            the value to multiply this vector by.
     * @param mult
     *            the value to multiply the scalar by
     * @param add
     *            the value to add
     */
    public void scaleAdd(float scalar, Vector3f mult, Vector3f add) {
        this.x = mult.x * scalar + add.x;
        this.y = mult.y * scalar + add.y;
        this.z = mult.z * scalar + add.z;
    }

    /**
     *
     * <code>dot</code> calculates the dot product of this vector with a
     * provided vector. If the provided vector is null, 0 is returned.
     *
     * @param vec
     *            the vector to dot with this vector.
     * @return the resultant dot product of this vector and a given vector.
     */
    public float dot(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, 0 returned.");
            return 0;
        }
        return x * vec.x + y * vec.y + z * vec.z;	// op: 7: MUL_F T12, T10, T11	// op: 6: GETFIELD_F T11, R1, .y	// op: 5: GETFIELD_F T10, R0, .y	// op: 4: MUL_F T9, T7, T8	// op: 3: GETFIELD_F T8, R1, .x	// op: 2: GETFIELD_F T7, R0, .x	// op: 12: ADD_F T17, T13, T16	// op: 10: GETFIELD_F T15, R1, .z	// op: 11: MUL_F T16, T14, T15	// op: 8: ADD_F T13, T9, T12	// op: 9: GETFIELD_F T14, R0, .z
    }

    /**
     * Returns a new vector which is the cross product of this vector with
     * the specified vector.
     * <P>
     * Neither 'this' nor v are modified.  The starting value of 'result'
     * </P>
     *
     * @param v the vector to take the cross product of with this.
     * @return the cross product vector.
     */
    public Vector3f cross(Vector3f v) {
        return cross(v, null);
    }

    /**
     * <code>cross</code> calculates the cross product of this vector with a
     * parameter vector v.  The result is stored in <code>result</code>
     * <P>
     * Neither 'this' nor v are modified.  The starting value of 'result'
     * (if any) is ignored.
     * </P>
     *
     * @param v the vector to take the cross product of with this.
     * @param result the vector to store the cross product result.
     * @return result, after recieving the cross product vector.
     */
    public Vector3f cross(Vector3f v, Vector3f result) {
        return cross(v.x, v.y, v.z, result);	// op: 3: GETFIELD_F T5, R1, .z	// op: 2: GETFIELD_F T4, R1, .y	// op: 1: GETFIELD_F T3, R1, .x
    }

    /**
     * <code>cross</code> calculates the cross product of this vector with a
     * Vector comprised of the specified other* elements.
     * The result is stored in <code>result</code>, without modifying either
     * 'this' or the 'other*' values.
     *
     * @param otherX
     *            x component of the vector to take the cross product of with this.
     * @param otherY
     *            y component of the vector to take the cross product of with this.
     * @param otherZ
     *            z component of the vector to take the cross product of with this.
     * @param result the vector to store the cross product result.
     * @return result, after recieving the cross product vector.
     */
    public Vector3f cross(float otherX, float otherY, float otherZ, Vector3f result) {
        if (result == null) result = new Vector3f();	// st: 2: NEW T12, com.jme.math.Vector3f	// st: 2: NEW T12, com.jme.math.Vector3f	// st: 2: NEW T12, com.jme.math.Vector3f	// st: 2: NEW T12, com.jme.math.Vector3f
        float resX = ((y * otherZ) - (z * otherY)); 	// op: 8: MUL_F T15, T13, R14	// op: 7: MOVE_F R14, R3	// op: 10: MOVE_F R17, R2	// op: 9: GETFIELD_F T16, R0, .z	// op: 6: GETFIELD_F T13, R0, .y	// op: 12: SUB_F T19, T15, T18	// op: 11: MUL_F T18, T16, R17	// op: 13: MOVE_F R20, T19
        float resY = ((z * otherX) - (x * otherZ));	// op: 16: MUL_F T23, T21, R22	// op: 15: MOVE_F R22, R1	// op: 18: MOVE_F R25, R14	// op: 17: GETFIELD_F T24, R0, .x	// op: 14: GETFIELD_F T21, R0, .z	// op: 19: MUL_F T26, T24, R25	// op: 20: SUB_F T27, T23, T26	// op: 21: MOVE_F R28, T27
        float resZ = ((x * otherY) - (y * otherX));	// op: 23: MOVE_F R30, R17	// op: 24: MUL_F T31, T29, R30	// op: 25: GETFIELD_F T32, R0, .y	// op: 26: MOVE_F R33, R22	// op: 22: GETFIELD_F T29, R0, .x	// op: 27: MUL_F T34, T32, R33	// op: 28: SUB_F T35, T31, T34	// op: 29: MOVE_F R36, T35
        result.set(resX, resY, resZ);	// op: 31: MOVE_F R38, R28	// op: 32: MOVE_F R39, R36	// op: 30: MOVE_F R37, R20
        return result;
    }

    /**
     * <code>crossLocal</code> calculates the cross product of this vector
     * with a parameter vector v.
     *
     * @param v
     *            the vector to take the cross product of with this.
     * @return this.
     */
    public Vector3f crossLocal(Vector3f v) {
        return crossLocal(v.x, v.y, v.z);
    }

    /**
     * <code>crossLocal</code> calculates the cross product of this vector
     * with a parameter vector v.
     *
     * @param otherX
     *            x component of the vector to take the cross product of with this.
     * @param otherY
     *            y component of the vector to take the cross product of with this.
     * @param otherZ
     *            z component of the vector to take the cross product of with this.
     * @return this.
     */
    public Vector3f crossLocal(float otherX, float otherY, float otherZ) {
        float tempx = ( y * otherZ ) - ( z * otherY );
        float tempy = ( z * otherX ) - ( x * otherZ );
        z = (x * otherY) - (y * otherX);
        x = tempx;
        y = tempy;
        return this;
    }

    /**
     * <code>length</code> calculates the magnitude of this vector.
     *
     * @return the length or magnitude of the vector.
     */
    public float length() {
        return ApproxMath.sqrt(lengthSquared()); 
    }
	public float length_APPROX() {
		return (float)ApproxMath.sqrt(lengthSquared());
	}

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between
     * this vector and vector v.
     *
     * @param v the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public float distanceSquared(Vector3f v) {
        double dx = x - v.x;
        double dy = y - v.y;
        double dz = z - v.z;
        return (float) (dx * dx + dy * dy + dz * dz);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     *
     * @param v the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public float distance(Vector3f v) {
        return ApproxMath.sqrt(distanceSquared(v));
    }
	public float distance_APPROX(Vector3f v) {
        return (float)ApproxMath.sqrt(distanceSquared(v));
    }

    /**
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is returned.
     * "this" is not modified.
     *
     * @param scalar the value to multiply this vector by.
     * @return the new vector.
     */
    public Vector3f mult(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    /**
     *
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is supplied as the second parameter and returned.
     * "this" is not modified.
     *
     * @param scalar the scalar to multiply this vector by.
     * @param product the product to store the result in.
     * @return product
     */
    public Vector3f mult(float scalar, Vector3f product) {
        if (null == product) {
            product = new Vector3f();
        }

        product.x = x * scalar;
        product.y = y * scalar;
        product.z = z * scalar;
        return product;
    }

    /**
     * <code>multLocal</code> multiplies this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls.
     *
     * @param scalar
     *            the value to multiply this vector by.
     * @return this
     */
    public Vector3f multLocal(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    /**
     * <code>multLocal</code> multiplies a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     * The provided 'vec' is not modified.
     *
     * @param vec the vector to mult to this vector.
     * @return this
     */
    public Vector3f multLocal(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }


    /**
     * Returns a new Vector instance comprised of elements which are the
     * product of the corresponding vector elements.
     * (N.b. this is not a cross product).
     * <P>
     * Neither 'this' nor 'vec' are modified.
     * </P>
     *
     * @param vec the vector to mult to this vector.
     */
    public Vector3f mult(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        return mult(vec, null);
    }

    /**
     * Multiplies a provided 'vec' vector with this vector.
     * If the specified 'store' is null, then a new Vector instance is returned.
     * Otherwise, 'store' with replaced values will be returned, to facilitate
     * chaining.
     * </P> <P>
     * 'This' is not modified; and the starting value of 'store' (if any) is
     * ignored (and over-written).
     * <P>
     * The resultant Vector is comprised of elements which are the
     * product of the corresponding vector elements.
     * (N.b. this is not a cross product).
     * </P>
     *
     * @param vec the vector to mult to this vector.
     * @param store result vector (null to create a new vector)
     * @return 'store', or a new Vector3f
     */
    public Vector3f mult(Vector3f vec, Vector3f store) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        if (store == null) store = new Vector3f();
        return store.set(x * vec.x, y * vec.y, z * vec.z);
    }


    /**
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     *
     * @param scalar the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector3f divide(float scalar) {
        scalar = 1f/scalar;
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector3f divideLocal(float scalar) {
        scalar = 1f/scalar;
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }


    /**
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     *
     * @param scalar
     *            the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public Vector3f divide(Vector3f scalar) {
        return new Vector3f(x / scalar.x, y / scalar.y, z / scalar.z);
    }

    /**
     * <code>divideLocal</code> divides this vector by a scalar internally,
     * and returns a handle to this vector for easy chaining of calls. Dividing
     * by zero will result in an exception.
     *
     * @param scalar
     *            the value to divides this vector by.
     * @return this
     */
    public Vector3f divideLocal(Vector3f scalar) {
        x /= scalar.x;
        y /= scalar.y;
        z /= scalar.z;
        return this;
    }

    /**
     *
     * <code>negate</code> returns the negative of this vector. All values are
     * negated and set to a new vector.
     *
     * @return the negated vector.
     */
    public Vector3f negate() {
        return new Vector3f(-x, -y, -z);
    }

    /**
     *
     * <code>negateLocal</code> negates the internal values of this vector.
     *
     * @return this.
     */
    public Vector3f negateLocal() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     *
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector creating a new vector object. If the provided vector is
     * null, null is returned.
     *
     * @param vec
     *            the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector3f subtract(Vector3f vec) {
        return new Vector3f(x - vec.x, y - vec.y, z - vec.z);
    }

    /**
     * <code>subtractLocal</code> subtracts a provided vector to this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls. If the provided vector is null, null is returned.
     *
     * @param vec
     *            the vector to subtract
     * @return this
     */
    public Vector3f subtractLocal(Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, null returned.");
            return null;
        }
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    /**
     *
     * <code>subtract</code>
     *
     * @param vec
     *            the vector to subtract from this
     * @param result
     *            the vector to store the result in
     * @return result
     */
    public Vector3f subtract(Vector3f vec, Vector3f result) {
        if(result == null) {
            result = new Vector3f();	// st: 2: NEW T3, com.jme.math.Vector3f	// st: 2: NEW T3, com.jme.math.Vector3f	// st: 2: NEW T3, com.jme.math.Vector3f	// st: 2: NEW T3, com.jme.math.Vector3f
        }
        result.x = x - vec.x;	// op: 8: SUB_F T9, T7, T8	// op: 9: PUTFIELD_F R2, .x, T9	// op: 6: GETFIELD_F T7, R0, .x	// op: 7: GETFIELD_F T8, R1, .x
        result.y = y - vec.y;	// op: 12: SUB_F T12, T10, T11	// op: 13: PUTFIELD_F R2, .y, T12	// op: 10: GETFIELD_F T10, R0, .y	// op: 11: GETFIELD_F T11, R1, .y
        result.z = z - vec.z;	// op: 17: PUTFIELD_F R2, .z, T15	// op: 16: SUB_F T15, T13, T14	// op: 14: GETFIELD_F T13, R0, .z	// op: 15: GETFIELD_F T14, R1, .z
        return result;
    }

    /**
     *
     * <code>subtract</code> subtracts the provided values from this vector,
     * creating a new vector that is then returned.
     *
     * @param subtractX
     *            the x value to subtract.
     * @param subtractY
     *            the y value to subtract.
     * @param subtractZ
     *            the z value to subtract.
     * @return the result vector.
     */
    public Vector3f subtract(float subtractX, float subtractY, float subtractZ) {
        return new Vector3f(x - subtractX, y - subtractY, z - subtractZ);
    }

    /**
     * <code>subtractLocal</code> subtracts the provided values from this vector
     * internally, and returns a handle to this vector for easy chaining of
     * calls.
     *
     * @param subtractX
     *            the x value to subtract.
     * @param subtractY
     *            the y value to subtract.
     * @param subtractZ
     *            the z value to subtract.
     * @return this
     */
    public Vector3f subtractLocal(float subtractX, float subtractY, float subtractZ) {
        x -= subtractX;
        y -= subtractY;
        z -= subtractZ;
        return this;
    }

    /**
     * <code>normalize</code> returns the unit vector of this vector.
     *
     * @return unit vector of this vector.
     */
    public Vector3f normalize() {    	
    	Vector3f vec;
        float length = length();
        if (length != 0) {
        	length = 1f/length;
            vec = new Vector3f(x * length, y * length, z * length);
        } else {
        	vec = new Vector3f(x, y, z);
        }
        return vec;        
    }

    /**
     * <code>normalizeLocal</code> makes this vector into a unit vector of
     * itself.
     *
     * @return this.
     */
    public Vector3f normalizeLocal() {
        float length = length();
        if (length != 0) {
        	length = 1f/length;
            x *= length;
            y *= length;
            z *= length;
        }
        return this;        
    }

    /**
     * <code>zero</code> resets this vector's data to zero internally.
     */
    public void zero() {
        x = y = z = 0;
    }

    /**
     * <code>angleBetween</code> returns (in radians) the angle between two vectors.
     * It is assumed that both this vector and the given vector are unit vectors (iow, normalized).
     * 
     * @param otherVector a unit vector to find the angle against
     * @return the angle in radians.
     */
    public float angleBetween(Vector3f otherVector) {
        float dotProduct = dot(otherVector);
        float angle = ApproxMath.acos(dotProduct); 
        return angle;
    }
    
    /**
     * Sets this vector to the interpolation by changeAmnt from this to the finalVec
     * this=(1-changeAmnt)*this + changeAmnt * finalVec
     * @param finalVec The final vector to interpolate towards
     * @param changeAmnt An amount between 0.0 - 1.0 representing a precentage
     *  change from this towards finalVec
     */
    public void interpolate(Vector3f finalVec, float changeAmnt) {
        this.x=(1-changeAmnt)*this.x + changeAmnt*finalVec.x;
        this.y=(1-changeAmnt)*this.y + changeAmnt*finalVec.y;
        this.z=(1-changeAmnt)*this.z + changeAmnt*finalVec.z;
    }

    /**
     * Sets this vector to the interpolation by changeAmnt from beginVec to finalVec
     * this=(1-changeAmnt)*beginVec + changeAmnt * finalVec
     * @param beginVec the beging vector (changeAmnt=0)
     * @param finalVec The final vector to interpolate towards
     * @param changeAmnt An amount between 0.0 - 1.0 representing a precentage
     *  change from beginVec towards finalVec
     */
    public void interpolate(Vector3f beginVec, Vector3f finalVec, float changeAmnt) {
        this.x=(1-changeAmnt)*beginVec.x + changeAmnt*finalVec.x;
        this.y=(1-changeAmnt)*beginVec.y + changeAmnt*finalVec.y;
        this.z=(1-changeAmnt)*beginVec.z + changeAmnt*finalVec.z;
    }

    /**
     * Check a vector... if it is null or its floats are NaN or infinite,
     * return false.  Else return true.
     * @param vector the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValidVector(Vector3f vector) {
      if (vector == null) return false;
      if (ApproxFloat.isNaN(vector.x) ||
          ApproxFloat.isNaN(vector.y) ||
          ApproxFloat.isNaN(vector.z)) return false;
      if (ApproxFloat.isInfinite(vector.x) ||
    	  ApproxFloat.isInfinite(vector.y) ||
    	  ApproxFloat.isInfinite(vector.z)) return false;
      return true;
    }

    public static void generateOrthonormalBasis(Vector3f u, Vector3f v, Vector3f w) {
        w.normalizeLocal();
        generateComplementBasis(u, v, w);
    }

    public static void generateComplementBasis(Vector3f u, Vector3f v,
            Vector3f w) {
        float fInvLength;

        if (FastMath.abs(w.x) >= FastMath.abs(w.y)) {
            // w.x or w.z is the largest magnitude component, swap them
            fInvLength = FastMath.invSqrt(w.x * w.x + w.z * w.z);
            u.x = -w.z * fInvLength;
            u.y = 0.0f;
            u.z = +w.x * fInvLength;
            v.x = w.y * u.z;
            v.y = w.z * u.x - w.x * u.z;
            v.z = -w.y * u.x;
        } else {
            // w.y or w.z is the largest magnitude component, swap them
            fInvLength = FastMath.invSqrt(w.y * w.y + w.z * w.z);
            u.x = 0.0f;
            u.y = +w.z * fInvLength;
            u.z = -w.y * fInvLength;
            v.x = w.y * u.z - w.z * u.y;
            v.y = -w.x * u.z;
            v.z = w.x * u.y;
        }
    }

    @Override
    public Vector3f clone() {
        try {
            return (Vector3f) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    /**
     * Saves this Vector3f into the given float[] object.
     * 
     * @param floats
     *            The float[] to take this Vector3f. If null, a new float[3] is
     *            created.
     * @return The array, with X, Y, Z float values in that order
     */
    public float[] toArray(float[] floats) {
        if (floats == null) {
            floats = new float[3];	// st: 2: NEWARRAY T2, IConst: 3, float[
        }
        floats[0] = x;	// op: 5: ASTORE_F T4, R1, IConst: 0	// op: 4: GETFIELD_F T4, R0, .x
        floats[1] = y;	// op: 7: ASTORE_F T5, R1, IConst: 1	// op: 6: GETFIELD_F T5, R0, .y
        floats[2] = z;	// op: 9: ASTORE_F T6, R1, IConst: 2	// op: 8: GETFIELD_F T6, R0, .z
        return floats;
    }

    /**
     * are these two vectors the same? they are is they both have the same x,y,
     * and z values.
     *
     * @param o
     *            the object to compare for equality
     * @return true if they are equal
     */
    public boolean equals(Object o) {
        if (!(o instanceof Vector3f)) { return false; }

        if (this == o) { return true; }

        boolean ret = true;	// op: 4: MOVE_I R10, IConst: 1
        Vector3f comp = (Vector3f) o;
        if (ApproxFloat.compare(x,comp.x) != 0) ret = false;	// op: 7: GETFIELD_F T11, R0, .x	// op: 8: GETFIELD_F T12, R5, .x	// op: 11: MOVE_I R14, IConst: 0
        if (ApproxFloat.compare(y,comp.y) != 0) ret = false;	// op: 13: GETFIELD_F T17, R5, .y	// op: 12: GETFIELD_F T16, R0, .y	// op: 16: MOVE_I R19, IConst: 0
        if (ApproxFloat.compare(z,comp.z) != 0) ret = false;	// op: 18: GETFIELD_F T22, R5, .z	// op: 17: GETFIELD_F T21, R0, .z	// op: 21: MOVE_I R24, IConst: 0
        return ret;	// op: 22: MOVE_I R26, R25
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on it's values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     * @return the hash code value of this vector.
     */
    public int hashCode() {
        int hash = 37;	// op: 1: MOVE_I R6, IConst: 37
        hash += 37 * hash + ApproxFloat.floatToIntBits(x);	// op: 3: GETFIELD_F T8, R0, .x	// op: 2: MUL_I T7, IConst: 37, IConst: 37	// op: 5: ADD_I T10, T7, T9	// op: 6: ADD_I T11, IConst: 37, T10	// op: 7: MOVE_I R12, T11
        hash += 37 * hash + ApproxFloat.floatToIntBits(y);	// op: 13: ADD_I T18, T15, T17	// op: 14: ADD_I T19, R14, T18	// op: 15: MOVE_I R20, T19	// op: 8: MOVE_I R13, R12	// op: 9: MOVE_I R14, R13	// op: 10: MUL_I T15, IConst: 37, R14	// op: 11: GETFIELD_F T16, R0, .y
        hash += 37 * hash + ApproxFloat.floatToIntBits(z);	// op: 21: ADD_I T26, T23, T25	// op: 23: MOVE_I R28, T27	// op: 22: ADD_I T27, R22, T26	// op: 16: MOVE_I R21, R20	// op: 17: MOVE_I R22, R21	// op: 18: MUL_I T23, IConst: 37, R22	// op: 19: GETFIELD_F T24, R0, .z
        return hash;	// op: 24: MOVE_I R29, R28
    }

    /**
     * <code>toString</code> returns the string representation of this vector.
     * The format is: <code>(xx.x..., yy.y..., zz.z...)</code>
     * <p>
     * If you want to display a class name, then use
     * Vector3f.class.getName() or getClass().getName().
     * </p>
     *
     * @return the string representation of this vector.
     */
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ')';
    }


    public Class<? extends Vector3f> getClassTag() {
        return this.getClass();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
    
    /**
     * @param index
     * @return x value if index == 0, y value if index == 1 or z value if index ==
     *         2
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
     */
    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }
    
    /**
     * @param index
     *            which field index in this vector to set.
     * @param value
     *            to set to one of x, y or z.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
     */
    public void set(int index, float value) {
        switch (index) {
            case 0:
                x = value;
                return;
            case 1:
                y = value;
                return;
            case 2:
                z = value;
                return;
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param in
     *            ObjectInput
     * @throws IOException
     * @throws ClassNotFoundException
     * @see java.io.Externalizable
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
	/*
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
*/
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
/*
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
*/
    }

}
