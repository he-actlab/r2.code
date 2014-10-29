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

package boofcv.abst.filter.binary;

import boofcv.alg.filter.binary.impl.ThresholdSauvola;
import boofcv.core.image.GConvertImage;
import boofcv.struct.image.*;

/**
 * Adaptive/local threshold using a Sauvola calculation
 *
 * @see ThresholdSauvola
 *
 * @author Peter Abeles
 */
public class AdaptiveSauvolaBinaryFilter<T extends ImageSingleBand> implements InputToBinary<T> {

	ImageType<T> inputType;

	ThresholdSauvola alg;
	ImageFloat32 input;

	public AdaptiveSauvolaBinaryFilter(int radius, float k, boolean down,
									   ImageType<T> inputType) {

		this.inputType = inputType;

		if( inputType.getDataType() != ImageDataType.F32 ) {
			input = new ImageFloat32(1,1);
		}

		alg = new ThresholdSauvola(radius,k, down);
	}

	@Override
	public void process(T input, ImageUInt8 output) {
		if( this.input == null )
			alg.process((ImageFloat32)input,output);
		else {
			this.input.reshape(input.width,input.height);
			GConvertImage.convert(input,this.input);
			alg.process(this.input,output);
		}
	}

	@Override
	public int getHorizontalBorder() {
		return 0;
	}

	@Override
	public int getVerticalBorder() {
		return 0;
	}

	@Override
	public ImageType<T> getInputType() {
		return inputType;
	}

	@Override
	public ImageType<ImageUInt8> getOutputType() {
		return ImageType.single(ImageUInt8.class);
	}
}
