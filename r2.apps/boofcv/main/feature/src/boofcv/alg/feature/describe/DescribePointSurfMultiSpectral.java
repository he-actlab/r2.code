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

import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;

/**
 * Computes a color SURF descriptor from a {@link boofcv.struct.image.MultiSpectral} image.  Each band in the
 * input image is used to compute its own descriptor, which are then combined together into a single one. The
 * laplacian sign is computed from a gray-scale image.  The descriptor from each band are not individually
 * normalized.  The whole combined descriptor is normalized.
 *
 * @see DescribePointSurf
 * @see DescribePointSurfMod
 *
 * @param <II> Type of integral image
 *
 * @author Peter Abeles
 */
public class DescribePointSurfMultiSpectral<II extends ImageSingleBand>
{
	// SURF algorithms
	private DescribePointSurf<II> describe;

	// number of elements in the feature
	private int descriptorLength;

	// integral of gray image
	private II grayII;
	// integral of multi-band image
	private MultiSpectral<II> ii;

	// storage for feature compute in each band
	private TupleDesc_F64 bandDesc;

	// number of bands in the input image
	private int numBands;

	public DescribePointSurfMultiSpectral(DescribePointSurf<II> describe,
										  int numBands )
	{
		this.describe = describe;
		this.numBands = numBands;

		bandDesc = new TupleDesc_F64(describe.getDescriptionLength());
		descriptorLength = describe.getDescriptionLength()*numBands;
	}

	public SurfFeature createDescription() {
		return new SurfFeature(descriptorLength);
	}

	public int getDescriptorLength() {
		return descriptorLength;
	}

	public Class<SurfFeature> getDescriptionType() {
		return SurfFeature.class;
	}

	public void setImage( II grayII , MultiSpectral<II> integralImage ) {
		this.grayII = grayII;
		ii = integralImage;
	}

	public void describe(double x, double y, double angle, double scale, SurfFeature desc)
	{
		int featureIndex = 0;
		for( int band = 0; band < ii.getNumBands(); band++ ) {
			describe.setImage(ii.getBand(band));
			describe.describe(x,y, angle, scale, bandDesc);
			System.arraycopy(bandDesc.value,0,desc.value,featureIndex,bandDesc.size());
			featureIndex += bandDesc.size();
		}

		SurfDescribeOps.normalizeFeatures(desc.value);

		describe.setImage(grayII);
		desc.laplacianPositive = describe.computeLaplaceSign((int)(x+0.5),(int)(y+0.5),scale);
	}

	public DescribePointSurf<II> getDescribe() {
		return describe;
	}

	public int getNumBands() {
		return numBands;
	}
}
