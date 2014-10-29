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
import boofcv.abst.feature.tracker.PointTrackerTwoPass;
import boofcv.alg.sfm.DepthSparse3D;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTrackerTwoPass;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt16;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
public class TestVisOdomPixelDepthPnP_to_DepthVisualOdometry extends CheckVisualOdometryDepthSim<ImageUInt8,ImageUInt16> {

	public TestVisOdomPixelDepthPnP_to_DepthVisualOdometry() {
		super(ImageUInt8.class,ImageUInt16.class);

		setAlgorithm(createAlgorithm());
	}

	protected DepthVisualOdometry<ImageUInt8,ImageUInt16> createAlgorithm() {

		PkltConfig config = new PkltConfig();
		config.pyramidScaling = new int[]{1,2,4,8};
		config.templateRadius = 3;
		ConfigGeneralDetector configDetector = new ConfigGeneralDetector(600,3,1);

		PointTrackerTwoPass<ImageUInt8> tracker = FactoryPointTrackerTwoPass.klt(config, configDetector,
				ImageUInt8.class, ImageSInt16.class);

		DepthSparse3D<ImageUInt16> sparseDepth = new DepthSparse3D.I<ImageUInt16>(depthUnits);

		return FactoryVisualOdometry.
				depthDepthPnP(1.5, 120, 2, 200, 50, false, sparseDepth, tracker, ImageUInt8.class, ImageUInt16.class);

	}
}
