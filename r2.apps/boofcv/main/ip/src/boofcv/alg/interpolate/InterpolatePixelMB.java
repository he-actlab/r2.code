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

package boofcv.alg.interpolate;

import boofcv.struct.image.ImageBase;


/**
 * Interface for interpolation between pixels on a per-pixel basis for a multi-band image.
 *
 * @author Peter Abeles
 */
public interface InterpolatePixelMB<T extends ImageBase> extends InterpolatePixel<T>{

	/**
	 * Returns the interpolated pixel values at the specified location while taking in account
	 * the image border. Bounds checking is done to ensure that the coordinate is inside the image
	 * and to see if the interpolation technique needs to be adjusted for the image border.
	 *
	 * @param x Point's x-coordinate. x >= 0 && x < image.width
	 * @param y Point's y-coordinate. y >= 0 && y < image.height
	 * @param values Interpolated value across all bands.
	 */
	public void get(float x, float y, float []values );

	/**
	 * Returns the interpolated pixel values at the specified location while assuming it is inside
	 * the image far away from the border.  For any input point {@link #isInFastBounds} should return true.
	 *
	 * @param x Point's x-coordinate.
	 * @param y Point's y-coordinate.
	 * @param values Interpolated value across all bands.
	 */
	public void get_fast(float x, float y, float []values );

}
