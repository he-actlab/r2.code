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

package boofcv.alg.transform.wavelet;

import boofcv.alg.misc.GImageMiscOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderIndex1D_Reflect;
import boofcv.core.image.border.BorderIndex1D_Wrap;
import boofcv.core.image.border.BorderType;
import boofcv.struct.image.ImageDimension;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.wavelet.WlCoef_F32;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestUtilWavelet {

	Random rand = new Random(234);

	@Test
	public void checkShape_positive() {
		ImageFloat32 orig = new ImageFloat32(10,20);
		ImageFloat32 transformed = new ImageFloat32(10,20);

		UtilWavelet.checkShape(orig,transformed);

		orig = new ImageFloat32(9,19);
		UtilWavelet.checkShape(orig,transformed);
	}

	@Test
	public void checkShape_negative() {
		ImageFloat32 orig = new ImageFloat32(9,19);
		ImageFloat32 transformed = new ImageFloat32(19,19);

		try {
		UtilWavelet.checkShape(orig,transformed);
			fail("transformed image can't be odd");
		} catch( IllegalArgumentException e ){}

		orig = new ImageFloat32(12,22);
		transformed = new ImageFloat32(10,20);

		try {
		UtilWavelet.checkShape(orig,transformed);
			fail("Image are not compatible shapes");
		} catch( IllegalArgumentException e ){}
	}

	@Test
	public void computeEnergy_F32() {
		float[] a = new float[]{-2,3,4.67f,7,-10.2f};

		assertEquals(187.8489f,UtilWavelet.computeEnergy(a),1e-4f);
	}

	@Test
	public void computeEnergy_I32() {
		int[] a = new int[]{-2,3,4,7,-10};

		assertEquals(11.125f,UtilWavelet.computeEnergy(a,4),1e-4f);
	}

	@Test
	public void sumCoefficients_F32() {
		float[] a = new float[]{-2,3,4,7,-10};

		assertEquals(2f,UtilWavelet.sumCoefficients(a),1e-4f);
	}

	@Test
	public void borderForwardUpper() {
		checkForwardUpper(0,0,2,2,10,0);
		checkForwardUpper(0,0,3,3,10,2);
		checkForwardUpper(0,0,4,4,10,2);
		checkForwardUpper(0,0,5,5,10,4);
		checkForwardUpper(-1,-1,5,5,10,2);
		checkForwardUpper(-1,-1,4,4,10,2);
		checkForwardUpper(-1,0,4,2,10,2);

		checkForwardUpper(0,0,2,2,11,1);
		checkForwardUpper(0,0,3,3,11,1);
		checkForwardUpper(0,0,4,4,11,3);
	}

	private void checkForwardUpper( int offsetA , int offsetB ,
						   int lengthA , int lengthB , int dataLength ,
						   int expected ) {
		WlCoef_F32 desc = new WlCoef_F32();
		desc.offsetScaling = offsetA;
		desc.offsetWavelet = offsetB;
		desc.scaling = new float[lengthA];
		desc.wavelet = new float[lengthB];

		assertEquals(expected,UtilWavelet.borderForwardUpper(desc,dataLength));
	}

	@Test
	public void borderForwardLower() {
		checkForwardLower(0,0,0);
		checkForwardLower(-1,0,2);
		checkForwardLower(0,-1,2);
		checkForwardLower(-2,0,2);
		checkForwardLower(0,-2,2);
		checkForwardLower(-3,0,4);
		checkForwardLower(0,-3,4);
		checkForwardLower(-3,0,4);
		checkForwardLower(-4,-4,4);
	}

	private void checkForwardLower( int offsetA , int offsetB ,
							int expected ) {
		WlCoef_F32 desc = new WlCoef_F32();
		desc.offsetScaling = offsetA;
		desc.offsetWavelet = offsetB;

		assertEquals(expected,UtilWavelet.borderForwardLower(desc));
	}

	@Test
	public void convertToType() {
		assertEquals(BorderType.REFLECT,UtilWavelet.convertToType(new BorderIndex1D_Reflect()));
		assertEquals(BorderType.WRAP,UtilWavelet.convertToType(new BorderIndex1D_Wrap()));
	}

	/**
	 * Just see if it blows up
	 */
	@Test
	public void adjustForDisplay() {
		adjustForDisplay(ImageFloat32.class);
		adjustForDisplay(ImageSInt32.class);
	}

	public <T extends ImageSingleBand>
	void adjustForDisplay( Class<T> imageType ) {
		ImageDimension d = UtilWavelet.transformDimension(320, 240, 3);
		T b = GeneralizedImageOps.createSingleBand(imageType,d.width,d.height);
		GImageMiscOps.fillUniform(b, rand, 0, 200);

		UtilWavelet.adjustForDisplay(b,3,255);
	}
}
