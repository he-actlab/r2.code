/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
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

package boofcv.struct.feature;

/**
 * Base class for tuple based feature descriptors
 *
 * @author Peter Abeles
 */
public interface TupleDesc<T extends TupleDesc> {

	public void setTo( T source );

	/**
	 * Returns the value of a tuple's element as a double.  In general this function should not be used
	 * because of how inefficient it is.
	 *
	 * @param index Which element
	 * @return Element's value as a double
	 */
	public double getDouble( int index );

	/**
	 * Number of elements in the tuple.
	 *
	 * @return Number of elements in the tuple
	 */
	public int size();

	/**
	 * Creates a copy of this description
	 *
	 * @return Copy
	 */
	public T copy();
}
