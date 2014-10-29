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

package boofcv.alg.feature.detdesc;

import boofcv.abst.feature.orientation.OrientationIntegral;
import boofcv.alg.feature.describe.DescribePointSurfMultiSpectral;
import boofcv.alg.feature.detect.interest.FastHessianFeatureDetector;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.SurfFeatureQueue;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;
import georegression.struct.point.Point2D_F64;
import org.ddogleg.struct.GrowQueue_F64;

import java.util.List;

/**
 * Computes a color SURF descriptor from a {@link boofcv.struct.image.MultiSpectral} image.  Features are detected,
 * orientation estimated, and laplacian sign computed using a gray scale image.  The gray scale image is found by
 * computing the average across all bands for each pixel.  A descriptor is computed inside band individually
 * and stored in a descriptor which is N*length long.  N = number of bands and length = number of
 * elements in normal descriptor.
 *
 * @see boofcv.alg.feature.describe.DescribePointSurfMultiSpectral
 *
 * @param <II> Type of integral image
 *
 * @author Peter Abeles
 */
public class DetectDescribeSurfMultiSpectral<II extends ImageSingleBand>
{
	// SURF algorithms
	private FastHessianFeatureDetector<II> detector;
	private OrientationIntegral<II> orientation;
	private DescribePointSurfMultiSpectral<II> describe;


	// storage for computed features
	private SurfFeatureQueue descriptions;
	// detected scale points
	private List<ScalePoint> foundPoints;
	// orientation of features
	private GrowQueue_F64 featureAngles = new GrowQueue_F64(10);

	public DetectDescribeSurfMultiSpectral(FastHessianFeatureDetector<II> detector,
										   OrientationIntegral<II> orientation,
										   DescribePointSurfMultiSpectral<II> describe )
	{
		this.detector = detector;
		this.orientation = orientation;
		this.describe = describe;

		descriptions = new SurfFeatureQueue(describe.getDescriptorLength());
	}

	public SurfFeature createDescription() {
		return describe.createDescription();
	}

	public SurfFeature getDescription(int index) {
		return descriptions.get(index);
	}

	/**
	 * Detects and describes features inside provide images.  All images are integral images.
	 *
	 * @param grayII Gray-scale integral image
	 * @param colorII Color integral image
	 */
	public void detect( II grayII , MultiSpectral<II> colorII ) {

		orientation.setImage(grayII);
		describe.setImage(grayII,colorII);

		descriptions.reset();
		featureAngles.reset();

		// detect features
		detector.detect(grayII);

		// describe the found interest points
		foundPoints = detector.getFoundPoints();

		for( int i = 0; i < foundPoints.size(); i++ ) {
			ScalePoint p = foundPoints.get(i);
			orientation.setScale(p.scale);
			double angle = orientation.compute(p.x,p.y);

			describe.describe(p.x, p.y, angle, p.scale, descriptions.grow());

			featureAngles.push(angle);
		}
	}

	public DescribePointSurfMultiSpectral<II> getDescribe() {
		return describe;
	}

	public int getNumberOfFeatures() {
		return foundPoints.size();
	}

	public Point2D_F64 getLocation(int featureIndex) {
		return foundPoints.get(featureIndex);
	}

	public double getScale(int featureIndex) {
		return foundPoints.get(featureIndex).scale;
	}

	public double getOrientation(int featureIndex) {
		return featureAngles.get(featureIndex);
	}
}
