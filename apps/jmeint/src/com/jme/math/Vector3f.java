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

	public final static Vector3f ZERO = new Vector3f(0, 0, 0);	// approx: 10: MOVE_F T12, FConst: 0.0	// approx: 9: MOVE_F T11, FConst: 0.0	// approx: 8: MOVE_F T10, FConst: 0.0

	public final static Vector3f UNIT_X = new Vector3f(1, 0, 0);	// approx: 17: MOVE_F T17, FConst: 1.0	// approx: 16: MOVE_F T16, FConst: 0.0	// approx: 15: MOVE_F T15, FConst: 0.0
	public final static Vector3f UNIT_Y = new Vector3f(0, 1, 0);	// approx: 23: MOVE_F T21, FConst: 1.0	// approx: 24: MOVE_F T22, FConst: 0.0	// approx: 22: MOVE_F T20, FConst: 0.0
    public final static Vector3f UNIT_Z = new Vector3f(0, 0, 1);	// approx: 31: MOVE_F T27, FConst: 0.0	// approx: 29: MOVE_F T25, FConst: 1.0	// approx: 30: MOVE_F T26, FConst: 0.0
    public final static Vector3f UNIT_XYZ = new Vector3f(1, 1, 1);	// approx: 37: MOVE_F T31, FConst: 1.0	// approx: 38: MOVE_F T32, FConst: 1.0	// approx: 36: MOVE_F T30, FConst: 1.0
    
	/**
     * the x value of the vector.
     */
    public  float x;

    /**
     * the y value of the vector.
     */
    public  float y;

    /**
     * the z value of the vector.
     */
    public  float z;

    /**
     * Constructor instantiates a new <code>Vector3f</code> with default
     * values of (0,0,0).
     *
     */
    public Vector3f() {
        x = y = z = 0;	// approx: 9: PUTFIELD_F T5, .y, T4	// approx: 8: MOVE_F T6, T4	// approx: 10: PUTFIELD_F R0, .x, T6	// approx: 5: PUTFIELD_F T2, .z, T1	// approx: 4: MOVE_F T3, T1	// approx: 6: MOVE_F T4, T3	// approx: 2: MOVE_F T1, FConst: 0.0
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
    public Vector3f( float x,  float y,  float z) {
        this.x = x;	// approx: 2: PUTFIELD_F R0, .x, R1
        this.y = y;	// approx: 3: PUTFIELD_F R0, .y, R2
        this.z = z;	// approx: 4: PUTFIELD_F R0, .z, R3
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
    public  Vector3f set( float x,  float y,  float z) {
        this.x = x;	// approx: 1: PUTFIELD_F R0, .x, R1
        this.y = y;	// approx: 2: PUTFIELD_F R0, .y, R2
        this.z = z;	// approx: 3: PUTFIELD_F R0, .z, R3
        return this;
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
    public  float dot( Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, 0 returned.");
            return 0;
        }
        return x * vec.x + y * vec.y + z * vec.z;	// approx: 6: GETFIELD_F T11, R1, .y	// approx: 5: GETFIELD_F T10, R0, .y	// approx: 8: ADD_F T13, T9, T12	// approx: 7: MUL_F T12, T10, T11	// approx: 10: GETFIELD_F T15, R1, .z	// approx: 9: GETFIELD_F T14, R0, .z	// approx: 12: ADD_F T17, T13, T16	// approx: 11: MUL_F T16, T14, T15	// approx: 2: GETFIELD_F T7, R0, .x	// approx: 4: MUL_F T9, T7, T8	// approx: 3: GETFIELD_F T8, R1, .x
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
    public  Vector3f cross( Vector3f v) {
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
    public  Vector3f cross( Vector3f v,  Vector3f result) {
        return cross(v.x, v.y, v.z, result);	// approx: 2: GETFIELD_F T4, R1, .y	// approx: 3: GETFIELD_F T5, R1, .z	// approx: 1: GETFIELD_F T3, R1, .x
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
    public  Vector3f cross( float otherX,  float otherY,  float otherZ,  Vector3f result) {
        if (result == null) result = new  Vector3f();
         float resX = ((y * otherZ) - (z * otherY)); 	// approx: 10: SUB_F T17, T14, T16	// approx: 9: MUL_F T16, T15, R2	// approx: 8: GETFIELD_F T15, R0, .z	// approx: 7: MUL_F T14, T13, R3	// approx: 11: MOVE_F R9, T17	// approx: 6: GETFIELD_F T13, R0, .y
         float resY = ((z * otherX) - (x * otherZ));	// approx: 17: MOVE_F R10, T22	// approx: 15: MUL_F T21, T20, R3	// approx: 16: SUB_F T22, T19, T21	// approx: 14: GETFIELD_F T20, R0, .x	// approx: 13: MUL_F T19, T18, R1	// approx: 12: GETFIELD_F T18, R0, .z
         float resZ = ((x * otherY) - (y * otherX));	// approx: 23: MOVE_F R11, T27	// approx: 18: GETFIELD_F T23, R0, .x	// approx: 21: MUL_F T26, T25, R1	// approx: 22: SUB_F T27, T24, T26	// approx: 19: MUL_F T24, T23, R2	// approx: 20: GETFIELD_F T25, R0, .y
        result.set(resX, resY, resZ);
        return result;
    }

    /**
     * <code>length</code> calculates the magnitude of this vector.
     *
     * @return the length or magnitude of the vector.
     */
    public float length() {
        return FastMath.sqrt((lengthSquared())); //EnerJ TODO
    }
	public  float length_APPROX() {
		return ( float)Math.sqrt(lengthSquared());
	}

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public  float lengthSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between
     * this vector and vector v.
     *
     * @param v the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public  float distanceSquared( Vector3f v) {
         double dx = x - v.x;
         double dy = y - v.y;
         double dz = z - v.z;
        return ( float) (dx * dx + dy * dy + dz * dz);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     *
     * @param v the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public float distance( Vector3f v) {
        return FastMath.sqrt((distanceSquared(v))); // EnerJ TODO
    }
	public  float distance_APPROX( Vector3f v) {
        return ( float)Math.sqrt(distanceSquared(v));
    }

    /**
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is returned.
     * "this" is not modified.
     *
     * @param scalar the value to multiply this vector by.
     * @return the new vector.
     */
    public  Vector3f mult( float scalar) {
        return new  Vector3f(x * scalar, y * scalar, z * scalar);
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
    public  Vector3f mult( float scalar,  Vector3f product) {
        if (null == product) {
            product = new  Vector3f();
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
    public  Vector3f multLocal( float scalar) {
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
    public  Vector3f multLocal( Vector3f vec) {
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
     * <code>divide</code> divides the values of this vector by a scalar and
     * returns the result. The values of this vector remain untouched.
     *
     * @param scalar the value to divide this vectors attributes by.
     * @return the result <code>Vector</code>.
     */
    public  Vector3f divide( float scalar) {
        scalar = 1f/scalar;
        return new  Vector3f(x * scalar, y * scalar, z * scalar);
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
    public  Vector3f divideLocal( float scalar) {
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
    public  Vector3f divide( Vector3f scalar) {
        return new  Vector3f(x / scalar.x, y / scalar.y, z / scalar.z);
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
    public  Vector3f divideLocal( Vector3f scalar) {
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
    public  Vector3f negate() {
        return new  Vector3f(-x, -y, -z);
    }

    /**
     *
     * <code>negateLocal</code> negates the internal values of this vector.
     *
     * @return this.
     */
    public  Vector3f negateLocal() {
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
    public  Vector3f subtract( Vector3f vec) {
        return new  Vector3f(x - vec.x, y - vec.y, z - vec.z);
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
    public  Vector3f subtractLocal( Vector3f vec) {
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
    public  Vector3f subtract( Vector3f vec,  Vector3f result) {
        if(result == null) {
            result = new  Vector3f();
        }
        result.x = x - vec.x;	// approx: 9: PUTFIELD_F R2, .x, T9	// approx: 8: SUB_F T9, T7, T8	// approx: 7: GETFIELD_F T8, R1, .x	// approx: 6: GETFIELD_F T7, R0, .x
        result.y = y - vec.y;	// approx: 13: PUTFIELD_F R2, .y, T12	// approx: 12: SUB_F T12, T10, T11	// approx: 11: GETFIELD_F T11, R1, .y	// approx: 10: GETFIELD_F T10, R0, .y
        result.z = z - vec.z;	// approx: 17: PUTFIELD_F R2, .z, T15	// approx: 16: SUB_F T15, T13, T14	// approx: 15: GETFIELD_F T14, R1, .z	// approx: 14: GETFIELD_F T13, R0, .z
        return result;
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
    public  float[] toArray( float[] floats) {
        if (floats == null) {
            floats = new  float[3];
        }
        floats[0] = x;	// approx: 4: GETFIELD_F T4, R0, .x	// approx: 5: ASTORE_F T4, R1, IConst: 0
        floats[1] = y;	// approx: 6: GETFIELD_F T5, R0, .y	// approx: 7: ASTORE_F T5, R1, IConst: 1
        floats[2] = z;	// approx: 8: GETFIELD_F T6, R0, .z
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

        Vector3f comp = (Vector3f) o;
        if (Float.compare((x),comp.x) != 0) return false;	// approx: 6: GETFIELD_F T9, R0, .x	// approx: 7: GETFIELD_F T10, R4, .x
        if (Float.compare((y),comp.y) != 0) return false;	// approx: 10: GETFIELD_F T12, R0, .y	// approx: 11: GETFIELD_F T13, R4, .y
        if (Float.compare((z),comp.z) != 0) return false;	// approx: 14: GETFIELD_F T15, R0, .z	// approx: 15: GETFIELD_F T16, R4, .z
        return true;
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on it's values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     * @return the hash code value of this vector.
     */
    public int hashCode() {
        int hash = 37;	// approx: 1: MOVE_I R6, IConst: 37
        hash += 37 * hash + Float.floatToIntBits((x));	// approx: 2: MUL_I T7, IConst: 37, R6	// approx: 3: GETFIELD_F T8, R0, .x	// approx: 6: ADD_I T11, R6, T10	// approx: 5: ADD_I T10, T7, T9	// approx: 7: MOVE_I R12, T11
        hash += 37 * hash + Float.floatToIntBits((y));	// approx: 9: GETFIELD_F T14, R0, .y	// approx: 12: ADD_I T17, R12, T16	// approx: 11: ADD_I T16, T13, T15	// approx: 13: MOVE_I R18, T17	// approx: 8: MUL_I T13, IConst: 37, R12
        hash += 37 * hash + Float.floatToIntBits((z));	// approx: 14: MUL_I T19, IConst: 37, R18	// approx: 15: GETFIELD_F T20, R0, .z	// approx: 19: MOVE_I R24, T23	// approx: 17: ADD_I T22, T19, T21	// approx: 18: ADD_I T23, R18, T22
        return hash;
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
        return "(" + (x) + ", " + (y) + ", " + (z) + ")";	// approx: 14: GETFIELD_F T16, R0, .z	// approx: 10: GETFIELD_F T12, R0, .y	// approx: 6: GETFIELD_F T8, R0, .x
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
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
    }

}
