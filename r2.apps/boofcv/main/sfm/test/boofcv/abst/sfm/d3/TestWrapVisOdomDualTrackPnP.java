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

import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.describe.FactoryDescribeRegionPoint;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.image.ImageFloat32;

/**
 * @author Peter Abeles
 */
public class TestWrapVisOdomDualTrackPnP extends CheckVisualOdometryStereoSim<ImageFloat32> {

	public TestWrapVisOdomDualTrackPnP() {
		super(ImageFloat32.class);
	}

	@Override
	public StereoVisualOdometry<ImageFloat32> createAlgorithm() {
		ConfigGeneralDetector configDetector = new ConfigGeneralDetector(600,2,1);

		PkltConfig kltConfig = new PkltConfig();
		kltConfig.templateRadius = 3;
		kltConfig.pyramidScaling =  new int[]{1, 2, 4, 8};

		PointTracker<ImageFloat32> trackerLeft = FactoryPointTracker.klt(kltConfig, configDetector,
				ImageFloat32.class,ImageFloat32.class);
		PointTracker<ImageFloat32> trackerRight = FactoryPointTracker.klt(kltConfig, configDetector,
				ImageFloat32.class,ImageFloat32.class);

		DescribeRegionPoint describe = FactoryDescribeRegionPoint.surfFast(null, ImageFloat32.class);

		return FactoryVisualOdometry.stereoDualTrackerPnP(90, 2, 1.5, 1.5, 200, 50,
				trackerLeft, trackerRight, describe,ImageFloat32.class);
	}
}
