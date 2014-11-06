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


import java.util.Arrays;

/**
 * This is a queue that is composed of integers.  Elements are added and removed from the tail
 *
 * @author Peter Abeles
 */
public class GrowQueue_I64 {

	public long data[];
	public int size;

	public GrowQueue_I64(int maxSize) {
		data = new long[ maxSize ];
		this.size = 0;
	}

	public GrowQueue_I64() {
		this(10);
	}

	public void reset() {
		size = 0;
	}

	public void addAll( GrowQueue_I64 queue ) {
		if( size+queue.size > data.length ) {
			long temp[] = new long[ (size+queue.size) * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		System.arraycopy(queue.data,0,data,size,queue.size);
		size += queue.size;
	}

	public void add(int value) {
		push(value);
	}

	public void push( int val ) {
		if( size == data.length ) {
			long temp[] = new long[ size * 2];
			System.arraycopy(data,0,temp,0,size);
			data = temp;
		}
		data[size++] = val;
	}

	public long get( int index ) {
		return data[index];
	}

	public void remove( int index ) {
		for( int i = index+1; i < size; i++ ) {
			data[i-1] = data[i];
		}
		size--;
	}

	public void resize( int size ) {
		if( data.length < size ) {
			data = new long[size];
		}
		this.size = size;
	}

	public void fill( long value ) {
		Arrays.fill(data, 0, size, value);
	}

	public void setMaxSize( int size ) {
		if( data.length < size ) {
			data = new long[size];
		}
	}

	public int size() {
		return size;
	}

	public long pop() {
		return data[--size];
	}
}
