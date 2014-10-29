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

package boofcv.abst.feature.orientation;

import boofcv.struct.image.ImageSingleBand;


/**
 * Estimates the orientation of a region directly from the image's pixels.
 *
 * @author Peter Abeles
 */
public interface OrientationImage <T extends ImageSingleBand> extends RegionOrientation {
	/**
	 * Specifies input image data for estimating orientation.
	 *
	 * @param image Input image..
	 */
	public void setImage( T image );

	/**
	 * Returns the type of image it can process.
	 *
	 * @return Type of image which can be processed
	 */
	public Class<T> getImageType();
}
