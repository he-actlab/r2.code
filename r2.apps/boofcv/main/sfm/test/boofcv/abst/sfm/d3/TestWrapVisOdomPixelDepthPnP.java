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

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.disparity.StereoDisparitySparse;
import boofcv.abst.feature.tracker.PointTrackerTwoPass;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.disparity.FactoryStereoDisparity;
import boofcv.factory.feature.tracker.FactoryPointTrackerTwoPass;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.image.ImageFloat32;

/**
 * @author Peter Abeles
 */
public class TestWrapVisOdomPixelDepthPnP extends CheckVisualOdometryStereoSim<ImageFloat32> {

	public TestWrapVisOdomPixelDepthPnP() {
		super(ImageFloat32.class);
	}

	@Override
	public StereoVisualOdometry<ImageFloat32> createAlgorithm() {
		StereoDisparitySparse<ImageFloat32> disparity =
				FactoryStereoDisparity.regionSparseWta(2, 150, 3, 3, 30, -1, true, ImageFloat32.class);

		PkltConfig config = new PkltConfig();
		config.pyramidScaling = new int[]{1,2,4,8};
		config.templateRadius = 3;

		ConfigGeneralDetector configDetector = new ConfigGeneralDetector(600,3,1);

		PointTrackerTwoPass<ImageFloat32> tracker = FactoryPointTrackerTwoPass.klt(config, configDetector,
				ImageFloat32.class, ImageFloat32.class);

		return FactoryVisualOdometry.stereoDepth(1.5,40,2,200,50,false,disparity,tracker,ImageFloat32.class);
	}

}
