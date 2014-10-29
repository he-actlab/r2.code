/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
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

package boofcv.struct.image;

import boofcv.core.image.FactoryGImageSingleBand;
import boofcv.core.image.GImageSingleBand;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Standard tests for children of {@link ImageSingleBand}.  Ensures that they contain
 * all the expected functions and that they have the expected behavior.  This is done
 * through extensive use of reflections.
 *
 * @author Peter Abeles
 */
public abstract class StandardSingleBandTests {

	public Random rand = new Random(234);

	public abstract ImageSingleBand createImage(int width, int height);

	public abstract Number randomNumber();

	/**
	 * Sets each element in the image to a random value.
	 */
	public void setRandom(ImageSingleBand img) {
		Object data = img._getData();

		int N = Array.getLength(data);
		for (int i = 0; i < N; i++) {
			Array.set(data, i, randomNumber());
		}
	}

	/**
	 * Check for a positive case of get() and set()
	 */
	@Test
	public void get_set() {
		ImageSingleBand img = createImage(10, 20);
		setRandom(img);

		Number expected = randomNumber();
		Number orig = (Number) call(img, "get", 0, null, 1, 1);

		// make sure the two are not equal
		assertFalse(expected.equals(orig));

		// set the expected to the point in the image
		call(img, "set", 1, expected, 1, 1);
		Number found = (Number) call(img, "get", 0, null, 1, 1);
		if (!img.getDataType().isInteger())
			assertEquals(expected.doubleValue(), found.doubleValue(), 1e-4);
		else {
			if( img.getDataType().isSigned() )
				assertTrue(expected.intValue() == found.intValue());
			else
				assertTrue((expected.intValue() & 0xFFFF) == found.intValue());
		}
	}

	/**
	 * Check for a positive case of get() and set()
	 */
	@Test
	public void unsafe_get_set() {
		ImageSingleBand img = createImage(10, 20);
		setRandom(img);

		Number expected = randomNumber();
		Number orig = (Number) call(img, "unsafe_get", 0, null, 1, 1);

		// make sure the two are not equal
		assertFalse(expected.equals(orig));

		// set the expected to the point in the image
		call(img, "unsafe_set", 1, expected, 1, 1);
		Number found = (Number) call(img, "unsafe_get", 0, null, 1, 1);
		if (!img.getDataType().isInteger())
			assertEquals(expected.doubleValue(), found.doubleValue(), 1e-4);
		else {
			if( img.getDataType().isSigned() )
				assertTrue(expected.intValue() == found.intValue());
			else
				assertTrue((expected.intValue() & 0xFFFF) == found.intValue());
		}
	}

	/**
	 * Makes sure all the accessors do proper bounds checking
	 */
	@Test
	public void accessorBounds() {
		ImageSingleBand img = createImage(10, 20);

		checkBound(img, "get", 0, null);
		checkBound(img, "set", 1, randomNumber());
	}

	private void checkBound(ImageSingleBand img, String method,
							int type, Object typeData) {
		checkException(img, method, type, typeData, -1, 0);
		checkException(img, method, type, typeData, 0, -1);
		checkException(img, method, type, typeData, img.getWidth(), 0);
		checkException(img, method, type, typeData, 0, img.getHeight());
	}

	private void checkException(ImageSingleBand img, String method,
								int type, Object typeData, int... where) {
		boolean found = false;
		try {
			call(img, method, type, typeData, where);
		} catch (ImageAccessException e) {
			found = true;
		}

		assertTrue("No exception was thrown", found);
	}

	private Object call(ImageSingleBand img, String method,
						int type, Object typeData, int... where) {
		try {
			Class<?>[] paramTypes = type == 0 ?
					new Class<?>[where.length] : new Class<?>[where.length + 1];
			Object[] args = new Object[paramTypes.length];

			int index;

			for (index = 0; index < where.length; index++) {
				paramTypes[index] = int.class;
				args[index] = where[index];
			}
			if (type == 1) {
				paramTypes[index] = img.getDataType().getSumType();
				args[index] = typeData;
			} else if (type == 2) {
				String name = "[" + img.getDataType().getDataType().getName().toUpperCase().charAt(0);
				paramTypes[index] = Class.forName(name);
				args[index] = typeData;
			}

			Method m = img.getClass().getMethod(method, paramTypes);

			return m.invoke(img, args);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			fail("The method " + method + " needs to be implemented");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw (RuntimeException) e.getCause();
		}
		throw new RuntimeException("Shouldn't be here");
	}
	
	@Test
	public void subimage() {
		ImageSingleBand img = createImage(10, 20);
		setRandom(img);
		
		ImageSingleBand sub = img.subimage(2,3,3,5, null);
		
		assertEquals(1,sub.getWidth());
		assertEquals(2,sub.getHeight());

		GImageSingleBand a = FactoryGImageSingleBand.wrap(img);
		GImageSingleBand b = FactoryGImageSingleBand.wrap(sub);
		
		assertEquals(a.get(2,3),b.get(0,0));
		assertEquals(a.get(2,4),b.get(0,1));
	}
	
	@Test
	public void reshape() {
		ImageSingleBand img = createImage(10, 20);
		
		// reshape to something smaller
		img.reshape(5,4);
		assertEquals(5,img.getWidth());
		assertEquals(4,img.getHeight());

		// reshape to something larger
		img.reshape(15,21);
		assertEquals(15,img.getWidth());
		assertEquals(21,img.getHeight());
	}
}
