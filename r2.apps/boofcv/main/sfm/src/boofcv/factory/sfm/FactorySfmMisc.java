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

package boofcv.factory.sfm;

import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.alg.sfm.overhead.CreateSyntheticOverheadView;
import boofcv.alg.sfm.overhead.CreateSyntheticOverheadViewMS;
import boofcv.alg.sfm.overhead.CreateSyntheticOverheadViewS;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * Factory for creating classes which don't go anywhere else.
 *
 * @author Peter Abeles
 */
public class FactorySfmMisc {

	public static <T extends ImageBase> CreateSyntheticOverheadView<T> createOverhead( ImageType<T> imageType ) {

		Class classType = imageType.getImageClass();

		switch( imageType.getFamily() ) {
			case SINGLE_BAND:
			{
				InterpolatePixelS interp = FactoryInterpolation.bilinearPixelS(classType);
				return new CreateSyntheticOverheadViewS(interp);
			}

			case MULTI_SPECTRAL:
				return new CreateSyntheticOverheadViewMS(TypeInterpolate.BILINEAR,imageType.getNumBands(),classType);

			default:
				throw new IllegalArgumentException(imageType.getFamily()+" is not supported");
		}
	}
}
