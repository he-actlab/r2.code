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

package boofcv.alg.distort;

import boofcv.struct.distort.PixelTransform_F32;
import boofcv.struct.image.ImageBase;


/**
 * Copies an image onto another image while applying a transform to the pixel coordinates. Pixels
 * which have no corresponding mapping can either be skipped or set to a default value.
 *
 * @author Peter Abeles
 */
public interface ImageDistort<Input extends ImageBase,Output extends ImageBase> {

	/**
	 * Specifies how pixel coordinates are transformed from the destination
	 * to source images.
	 *
	 * @param dstToSrc Pixel coordinate transformation.
	 */
	public void setModel( PixelTransform_F32 dstToSrc );

	/**
	 * Applies the transform to the entire destination image.
	 *
	 * @param srcImg Input image. Not modified.
	 * @param dstImg Output image. Modified.
	 */
	public void apply( Input srcImg , Output dstImg );

	/**
	 * Applies the transform to only the specified region inside the destination image.
	 *
	 * @param srcImg Input image. Not modified.
	 * @param dstImg Output image. Modified.
	 * @param dstX0 Left most crop boundary. Inclusive.
	 * @param dstY0 Top most crop boundary. Inclusive.
	 * @param dstX1 Right most crop boundary. Exclusive.
	 * @param dstY1 Bottom most crop boundary. Exclusive.
	 */
	public void apply( Input srcImg , Output dstImg , int dstX0 , int dstY0 , int dstX1 , int dstY1 );
}
