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

import enerj.lang.*;

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
@Approximable
public class Vector3f implements Externalizable, Cloneable {
    private static final Logger logger = Logger.getLogger(Vector3f.class.getName());

    private static final long serialVersionUID = 1L;

	public final static Vector3f ZERO = new Vector3f(0, 0, 0);

	public final static Vector3f UNIT_X = new Vector3f(1, 0, 0);
	public final static Vector3f UNIT_Y = new Vector3f(0, 1, 0);
    public final static Vector3f UNIT_Z = new Vector3f(0, 0, 1);
    public final static Vector3f UNIT_XYZ = new Vector3f(1, 1, 1);
    
	/**
     * the x value of the vector.
     */
    public @Context float x;

    /**
     * the y value of the vector.
     */
    public @Context float y;

    /**
     * the z value of the vector.
     */
    public @Context float z;

    /**
     * Constructor instantiates a new <code>Vector3f</code> with default
     * values of (0,0,0).
     *
     */
    public Vector3f() {
        x = y = z = 0;
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
    public Vector3f(@Context float x, @Context float y, @Context float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    public @Context Vector3f set(@Context float x, @Context float y, @Context float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    public @Context float dot(@Context Vector3f vec) {
        if (null == vec) {
            logger.warning("Provided vector is null, 0 returned.");
            return 0;
        }
        return x * vec.x + y * vec.y + z * vec.z;
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
    public @Context Vector3f cross(@Context Vector3f v) {
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
    public @Context Vector3f cross(@Context Vector3f v, @Context Vector3f result) {
        return cross(v.x, v.y, v.z, result);
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
    public @Context Vector3f cross(@Context float otherX, @Context float otherY, @Context float otherZ, @Context Vector3f result) {
        if (result == null) result = new @Context Vector3f();
        @Context float resX = ((y * otherZ) - (z * otherY)); 
        @Context float resY = ((z * otherX) - (x * otherZ));
        @Context float resZ = ((x * otherY) - (y * otherX));
        result.set(resX, resY, resZ);
        return result;
    }

    /**
     * <code>length</code> calculates the magnitude of this vector.
     *
     * @return the length or magnitude of the vector.
     */
    public float length() {
        return FastMath.sqrt(Endorsements.endorse(lengthSquared())); //EnerJ TODO
    }
	public @Approx float length_APPROX() {
		return (@Approx float)ApproxMath.sqrt(lengthSquared());
	}

    /**
     * <code>lengthSquared</code> calculates the squared value of the
     * magnitude of the vector.
     *
     * @return the magnitude squared of the vector.
     */
    public @Context float lengthSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * <code>distanceSquared</code> calculates the distance squared between
     * this vector and vector v.
     *
     * @param v the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public @Context float distanceSquared(@Context Vector3f v) {
        @Context double dx = x - v.x;
        @Context double dy = y - v.y;
        @Context double dz = z - v.z;
        return (@Context float) (dx * dx + dy * dy + dz * dz);
    }

    /**
     * <code>distance</code> calculates the distance between this vector and
     * vector v.
     *
     * @param v the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public float distance(@Context Vector3f v) {
        return FastMath.sqrt(Endorsements.endorse(distanceSquared(v))); // EnerJ TODO
    }
	public @Approx float distance_APPROX(@Context Vector3f v) {
        return (@Approx float)ApproxMath.sqrt(distanceSquared(v));
    }

    /**
     * <code>mult</code> multiplies this vector by a scalar. The resultant
     * vector is returned.
     * "this" is not modified.
     *
     * @param scalar the value to multiply this vector by.
     * @return the new vector.
     */
    public @Context Vector3f mult(@Context float scalar) {
        return new @Context Vector3f(x * scalar, y * scalar, z * scalar);
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
    public @Context Vector3f mult(@Context float scalar, @Context Vector3f product) {
        if (null == product) {
            product = new @Context Vector3f();
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
    public @Context Vector3f multLocal(@Context float scalar) {
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
    public @Context Vector3f multLocal(@Context Vector3f vec) {
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
    public @Context Vector3f divide(@Context float scalar) {
        scalar = 1f/scalar;
        return new @Context Vector3f(x * scalar, y * scalar, z * scalar);
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
    public @Context Vector3f divideLocal(@Context float scalar) {
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
    public @Context Vector3f divide(@Context Vector3f scalar) {
        return new @Context Vector3f(x / scalar.x, y / scalar.y, z / scalar.z);
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
    public @Context Vector3f divideLocal(@Context Vector3f scalar) {
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
    public @Context Vector3f negate() {
        return new @Context Vector3f(-x, -y, -z);
    }

    /**
     *
     * <code>negateLocal</code> negates the internal values of this vector.
     *
     * @return this.
     */
    public @Context Vector3f negateLocal() {
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
    public @Context Vector3f subtract(@Context Vector3f vec) {
        return new @Context Vector3f(x - vec.x, y - vec.y, z - vec.z);
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
    public @Context Vector3f subtractLocal(@Context Vector3f vec) {
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
    public @Context Vector3f subtract(@Context Vector3f vec, @Context Vector3f result) {
        if(result == null) {
            result = new @Context Vector3f();
        }
        result.x = x - vec.x;
        result.y = y - vec.y;
        result.z = z - vec.z;
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
    public @Context float[] toArray(@Context float[] floats) {
        if (floats == null) {
            floats = new @Context float[3];
        }
        floats[0] = x;
        floats[1] = y;
        floats[2] = z;
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
        if (Float.compare(Endorsements.endorse(x),comp.x) != 0) return false;
        if (Float.compare(Endorsements.endorse(y),comp.y) != 0) return false;
        if (Float.compare(Endorsements.endorse(z),comp.z) != 0) return false;
        return true;
    }

    /**
     * <code>hashCode</code> returns a unique code for this vector object based
     * on it's values. If two vectors are logically equivalent, they will return
     * the same hash code value.
     * @return the hash code value of this vector.
     */
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + Float.floatToIntBits(Endorsements.endorse(x));
        hash += 37 * hash + Float.floatToIntBits(Endorsements.endorse(y));
        hash += 37 * hash + Float.floatToIntBits(Endorsements.endorse(z));
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
        return "(" + Endorsements.endorse(x) + ", " + Endorsements.endorse(y) + ", " + Endorsements.endorse(z) + ')';
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
