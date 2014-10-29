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

package boofcv.alg.segmentation;

import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComputeRegionMeanColor {
	int w = 5, h = 4;
	ImageSInt32 segments;
	GrowQueue_I32 regionMemberCount;
	FastQueue<float[]> regionColor;


	@Before
	public void before() {
		segments = new ImageSInt32(w,h);
	    segments.data = new int[]{
				0,0,0,0,0,
				1,1,1,1,1,
				2,2,2,2,2,
				3,3,3,3,3};

		regionMemberCount = new GrowQueue_I32();
		for( int i = 0; i < 4; i++ )
			regionMemberCount.add(5);
	}

	private void createRegionColor( final int numBands ) {
		regionColor = new FastQueue<float[]>(float[].class,true) {
			@Override
			protected float[] createInstance() {
				return new float[ numBands ];
			}
		};
		regionColor.resize(4);
	}

	@Test
	public void process() {
		createRegionColor(2);
		Dummy alg = new Dummy(2);

		assertEquals(2,alg.numBands);

		ImageFloat32 image = new ImageFloat32(w,h);

		alg.process(image,segments,regionMemberCount,regionColor);

		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 2; j++ ) {
				assertEquals(2,regionColor.get(i)[j],1e-4f);
			}
		}
	}

	@Test
	public void specific_U8() {
		createRegionColor(1);
		ImageUInt8 image = new ImageUInt8(w,h);
		byte a = 1,b=2,c=3,d=10;
		byte[] expected = new byte[]{a,b,c,d};
		image.data = new byte[]{
				a,a,a,a,a,
				b,b,b,b,b,
				c,c,c,c,c,
				d,d,d,d,d};

		ComputeRegionMeanColor<ImageUInt8> alg = new ComputeRegionMeanColor.U8();

		alg.process(image,segments,regionMemberCount,regionColor);

		for( int i = 0; i < 4; i++ ) {
			assertEquals(expected[i],regionColor.get(i)[0],1e-4f);
		}
	}

	@Test
	public void specific_F32() {
		createRegionColor(1);
		ImageFloat32 image = new ImageFloat32(w,h);
		float a = 1.1f,b=2.2f,c=3.3f,d=10.7f;
		float[] expected = new float[]{a,b,c,d};
		image.data = new float[]{
				a,a,a,a,a,
				b,b,b,b,b,
				c,c,c,c,c,
				d,d,d,d,d};

		ComputeRegionMeanColor<ImageFloat32> alg = new ComputeRegionMeanColor.F32();

		alg.process(image,segments,regionMemberCount,regionColor);

		for( int i = 0; i < 4; i++ ) {
			assertEquals(expected[i],regionColor.get(i)[0],1e-4f);
		}
	}

	@Test
	public void specific_MS_U8() {
		createRegionColor(2);
		ImageUInt8 band = new ImageUInt8(w,h);
		byte a = 1,b=2,c=3,d=10;
		byte[] expected = new byte[]{a,b,c,d};
		band.data = new byte[]{
				a,a,a,a,a,
				b,b,b,b,b,
				c,c,c,c,c,
				d,d,d,d,d};

		MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class,w,h,2);
		image.bands[0] = band;
		image.bands[1] = band;

		ComputeRegionMeanColor<MultiSpectral<ImageUInt8>> alg = new ComputeRegionMeanColor.MS_U8(2);

		alg.process(image,segments,regionMemberCount,regionColor);

		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 2; j++ ) {
				assertEquals(expected[i],regionColor.get(i)[j],1e-4f);
			}
		}
	}

	@Test
	public void specific_MS_F32() {
		createRegionColor(2);
		ImageFloat32 band = new ImageFloat32(w,h);
		float a = 1.1f,b=2.2f,c=3.3f,d=10.7f;
		float[] expected = new float[]{a,b,c,d};
		band.data = new float[]{
				a,a,a,a,a,
				b,b,b,b,b,
				c,c,c,c,c,
				d,d,d,d,d};

		MultiSpectral<ImageFloat32> image = new MultiSpectral<ImageFloat32>(ImageFloat32.class,w,h,2);
		image.bands[0] = band;
		image.bands[1] = band;

		ComputeRegionMeanColor<MultiSpectral<ImageFloat32>> alg = new ComputeRegionMeanColor.MS_F32(2);

		alg.process(image,segments,regionMemberCount,regionColor);

		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 2; j++ ) {
				assertEquals(expected[i],regionColor.get(i)[j],1e-4f);
			}
		}
	}

	private static class Dummy extends ComputeRegionMeanColor {

		public Dummy(int numBands) {
			super(numBands);
		}

		@Override
		protected void addPixelValue(int index, float[] sum) {
			for( int i = 0; i < numBands; i++ )
				sum[i] += 2;
		}
	}
}
