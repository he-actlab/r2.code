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

package boofcv.alg.feature.describe;

import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.feature.TupleDesc_S8;
import boofcv.struct.feature.TupleDesc_U8;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestConvertTupleDescOps {

	/**
	 * General test with a known output
	 */
	@Test
	public void positive_F64() {
		TupleDesc_F64 input = new TupleDesc_F64(4);
		input.value = new double[]{1,2,3,4};

		TupleDesc_U8 output = new TupleDesc_U8(4);

		ConvertTupleDescOps.positive(input,output);

		assertEquals((int)(1*255.0/4.0),output.value[0] & 0xFF);
		assertEquals((int)(2*255.0/4.0),output.value[1] & 0xFF);
		assertEquals((int)(3*255.0/4.0),output.value[2] & 0xFF);
		assertEquals((int)(4*255.0/4.0),output.value[3] & 0xFF);
	}

	/**
	 * Test pathological case where the input is all zeros
	 */
	@Test
	public void positive_F64_zeros() {
		TupleDesc_F64 input = new TupleDesc_F64(4);

		TupleDesc_U8 output = new TupleDesc_U8(4);

		ConvertTupleDescOps.positive(input,output);

		for( int i = 0; i < 4; i++ )
			assertEquals(0,output.value[0]);
	}

	/**
	 * General test with a known output
	 */
	@Test
	public void real_F64() {
		TupleDesc_F64 input = new TupleDesc_F64(4);
		input.value = new double[]{1,-2,3,-4};

		TupleDesc_S8 output = new TupleDesc_S8(4);

		ConvertTupleDescOps.real(input, output);

		assertEquals((int)( 1*127.0/4.0),output.value[0]);
		assertEquals((int)(-2*127.0/4.0),output.value[1]);
		assertEquals((int)( 3*127.0/4.0),output.value[2]);
		assertEquals((int)(-4*127.0/4.0),output.value[3]);
	}

	/**
	 * Test pathological case where the input is all zeros
	 */
	@Test
	public void real_F64_zeros() {
		TupleDesc_F64 input = new TupleDesc_F64(4);

		TupleDesc_S8 output = new TupleDesc_S8(4);

		ConvertTupleDescOps.real(input, output);

		for( int i = 0; i < 4; i++ )
			assertEquals(0,output.value[0]);
	}
}
