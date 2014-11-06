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

package org.ddogleg.struct;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/**
 * Growable array designed for fast access.  It can be configured to declare new instances
 * or just grow the array.
 *
 * @author Peter Abeles
 */
public class FastQueue<T> {
	public T []data;
	public int size;
	public Class<T> type;

	// if true then it will declare new instances automatically
	// if false then
	private boolean declareInstances;

	// Wrapper around this class for lists
	private FastQueueList<T> list = new FastQueueList<T>(this);

	public FastQueue(int initialMaxSize, Class<T> type, boolean declareInstances) {
		init(initialMaxSize, type, declareInstances);
	}

	public FastQueue(Class<T> type, boolean declareInstances ) {
		this(10,type,declareInstances);
	}

	protected FastQueue() {
	}

	/**
	 * Data structure initialization is done here so that child classes can declay initialization until they are ready
	 */
	protected void init(int initialMaxSize, Class<T> type, boolean declareInstances) {
		this.size = 0;
		this.type = type;
		this.declareInstances = declareInstances;

		data = (T[]) Array.newInstance(type, initialMaxSize);
		if( declareInstances ) {
			for( int i = 0; i < initialMaxSize; i++ ) {
				data[i] = createInstance();
			}
		}
	}

	/**
	 * Returns a wrapper around FastQueue that allows it to act as a read only list.
	 * There is little overhead in using this interface.
	 *
	 * NOTE: The same instead of a list is returned each time.  Be careful when writing
	 * concurrent code and create a copy.
	 *
	 * @return List wrapper.
	 */
	public List<T> toList() {
		return list;
	}

	/**
	 * Shrinks the size of the array by one and returns the element stored at the former last element.
	 *
	 * @return The last element in the list that was removed.
	 */
	public T removeTail() {
		if( size > 0 ) {
			size--;
			return data[size];
		} else
			throw new IllegalArgumentException("Size is already zero");
	}

	public T getTail() {
		return data[size-1];
	}

	public void reset() {
		size = 0;
	}

	public int getMaxSize() {
		return data.length;
	}

	public int size() {
		return size;
	}

	/**
	 * Reverse the item order in this queue.
	 */
	public void reverse() {
		for (int i = 0; i < size / 2; i++) {
			T tmp = data[i];
			data[i] = data[size - i - 1];
			data[size - i - 1] = tmp;
		}
	}

	/**
	 * Returns the element at the specified index.  Bounds checking is performed.
	 * @param index
	 * @return
	 */
	public T get( int index ) {
		if( index >= size )
			throw new IllegalArgumentException("Index out of bounds: index "+index+" size "+size);
		return data[index];
	}

	/**
	 * Returns a new element of data.  If there are new data elements available then array will
	 * automatically grow.
	 *
	 * @return A new instance.
	 */
	public T grow() {
		if( size < data.length ) {
			return data[size++];
		} else {
			growArray((data.length+1)*2);
			return data[size++];
		}
	}

	/**
	 * Removes an element from the queue by shifting elements in the array down one and placing the removed element
	 * at the old end of the list.
	 *
	 * @param index
	 */
	public void remove( int index ) {
		T removed = data[index];
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		data[size-1] = removed;
		size--;
	}

	public void add( T object ) {
		if( size >= data.length ) {
			growArray((data.length+1)*2);
		}
		data[size++] = object;
	}

	public void addAll( FastQueue<T> list ) {
		for( int i = 0; i < list.size; i++ ) {
			add( list.data[i]);
		}
	}

	/**
	 * Increases the size of the internal array without changing the shape's size. If the array
	 * is already larger than the specified length then nothing is done.  Elements previously
	 * stored in the array are copied over is a new internal array is declared.
	 *
	 * @param length Requested size of internal array.
	 */
	public void growArray( int length) {
		// now need to grow since it is already larger
		if( this.data.length >= length)
			return;

		T []data = (T[])Array.newInstance(type, length);
		System.arraycopy(this.data,0,data,0,this.data.length);

		if( declareInstances ) {
			for( int i = this.data.length; i < length; i++ ) {
				data[i] = createInstance();
			}
		}
		this.data = data;
	}

	/**
	 * Changes the size to the specified length. Equivalent to calling {@link #growArray} and this.size = N.
	 * @param length The new size of the queue
	 */
	public void resize(int length) {
		growArray(length);
		this.size = length;
	}

	public boolean contains(Object o) {
		for( int i = 0; i < size; i++ ) {
			if( data[i].equals(o) )
				return true;
		}

		return false;
	}

	protected T createInstance() {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public List<T> copyIntoList(List<T> ret) {
		if( ret == null )
			ret = new ArrayList<T>(size);
		for( int i = 0; i < size; i++ ) {
			ret.add(data[i]);
		}
		return ret;
	}

	// -------- These are only around so that it can be a java bean
	public T[] getData() {
		return data;
	}

	public void setData(T[] data) {
		this.data = data;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isDeclareInstances() {
		return declareInstances;
	}

	public void setDeclareInstances(boolean declareInstances) {
		this.declareInstances = declareInstances;
	}

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}
}
