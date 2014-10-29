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

package boofcv.abst.sfm.d3;

import boofcv.struct.calib.IntrinsicParameters;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;
import georegression.geometry.RotationMatrixGenerator;
import georegression.struct.se.Se3_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestStereoVisualOdometryScaleInput {

	int width = 640;
	int height = 320;

	StereoParameters parameters;
	ImageFloat32 leftImage;
	ImageFloat32 rightImage;
	ImageType<ImageFloat32> type = ImageType.single(ImageFloat32.class);
	boolean result;
	boolean resetCalled = false;


	@Test
	public void setCalibration() {
		StereoParameters p = createStereoParam();
		Dummy dummy = new Dummy();

		StereoVisualOdometryScaleInput<ImageFloat32> alg = new StereoVisualOdometryScaleInput<ImageFloat32>(dummy,0.5);
		alg.setCalibration(p);

		assertEquals(320, parameters.left.width);
		assertEquals(320,parameters.right.width);
		assertEquals(160,parameters.left.height);
		assertEquals(160,parameters.right.height);
	}

	@Test
	public void process() {
		StereoParameters p = createStereoParam();
		Dummy dummy = new Dummy();

		StereoVisualOdometryScaleInput<ImageFloat32> alg = new StereoVisualOdometryScaleInput<ImageFloat32>(dummy,0.5);
		alg.setCalibration(p);

		ImageFloat32 left = new ImageFloat32(width,height);
		ImageFloat32 right = new ImageFloat32(width,height);

		alg.process(left,right);

		assertTrue(left != leftImage);
		assertTrue(right != rightImage);

		assertEquals(320,leftImage.width);
		assertEquals(320,rightImage.width);
		assertEquals(160,leftImage.height);
		assertEquals(160,rightImage.height);
	}

	@Test
	public void getImageType() {

		StereoParameters p = createStereoParam();
		Dummy dummy = new Dummy();

		StereoVisualOdometryScaleInput<ImageFloat32> alg = new StereoVisualOdometryScaleInput<ImageFloat32>(dummy,0.5);

		assertTrue(type == alg.getImageType());
	}

	public StereoParameters createStereoParam() {
		StereoParameters ret = new StereoParameters();

		ret.setRightToLeft(new Se3_F64());
		ret.getRightToLeft().getT().set(-0.2,0.001,-0.012);
		RotationMatrixGenerator.eulerXYZ(0.001, -0.01, 0.0023, ret.getRightToLeft().getR());

		ret.left = new IntrinsicParameters(200,201,0,width/2,height/2,width,height, false, new double[]{0,0});
		ret.right = new IntrinsicParameters(199,200,0,width/2+2,height/2-6,width,height, false, new double[]{0,0});

		return ret;
	}

	protected class Dummy implements StereoVisualOdometry<ImageFloat32> {

		@Override
		public void setCalibration(StereoParameters p) {
			parameters = p;
		}

		@Override
		public boolean process(ImageFloat32 l, ImageFloat32 r) {
			leftImage = l;
			rightImage = r;
			return result;
		}

		@Override
		public ImageType<ImageFloat32> getImageType() {
			return type;
		}

		@Override
		public void reset() {
			resetCalled = true;
		}

		@Override
		public boolean isFault() {
			return false;
		}

		@Override
		public Se3_F64 getCameraToWorld() {
			return new Se3_F64();
		}
	}

}
