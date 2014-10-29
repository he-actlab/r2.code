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

package boofcv.alg.transform.pyramid;

import boofcv.abst.filter.FilterImageInterface;
import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.abst.filter.derivative.ImageHessian;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.pyramid.ImagePyramid;

import java.lang.reflect.Array;


/**
 * Various operations related to image pyramids.
 *
 * @author Peter Abeles
 */
public class PyramidOps {

	/**
	 * Creates an array of single band images for each layer in the provided pyramid.  Each image will
	 * be the same size as the corresponding layer in the pyramid.
	 *
	 * @param pyramid (Input) Image pyramid
	 * @param outputType (Input) Output image type
	 * @param <O> Output image type
	 * @return An array of images
	 */
	public static <O extends ImageSingleBand>
	O[] declareOutput( ImagePyramid<?> pyramid , Class<O> outputType ) {
		O[] ret = (O[])Array.newInstance(outputType,pyramid.getNumLayers());

		for( int i = 0; i < ret.length; i++ ) {
			int w = pyramid.getWidth(i);
			int h = pyramid.getHeight(i);
			ret[i] = GeneralizedImageOps.createSingleBand(outputType,w,h);
		}

		return ret;
	}

	/**
	 * Reshapes each image in the array to match the layers in the pyramid
	 * @param pyramid (Input) Image pyramid
	 * @param output (Output) List of images which is to be resized
	 * @param <O> Image type
	 */
	public static <O extends ImageSingleBand>
	void reshapeOutput( ImagePyramid<?> pyramid , O[] output ) {

		for( int i = 0; i < output.length; i++ ) {
			int w = pyramid.getWidth(i);
			int h = pyramid.getHeight(i);
			output[i].reshape(w, h);
		}
	}

	/**
	 * <p>
	 * Runs an image filter through each layer in the pyramid.
	 * </p>
	 *
	 * <p>
	 * It is assumed that the output has the same scales as the input.  If not
	 * initialized then it will be initialized.  If already initialized it is
	 * assumed to be setup for the same input image size.
	 * </p>
	 *
	 * @param input Input pyramid.
	 * @param filter Filter being applied to the pyramid.
	 * @param output Output pyramid where filter results are saved.
	 */
	public static <I extends ImageSingleBand, O extends ImageSingleBand>
	void filter(ImagePyramid<I> input, FilterImageInterface<I, O> filter, O[] output )
	{
		for( int i = 0; i < input.getNumLayers(); i++ ) {
			I imageIn = input.getLayer(i);
			filter.process(imageIn,output[i]);
		}
	}

	/**
	 * <p>
	 * Computes the gradient for each image the pyramid.
	 * </p>
	 *
	 * <p>
	 * It is assumed that the gradient has the same scales as the input.  If not
	 * initialized then it will be initialized.  If already initialized it is
	 * assumed to be setup for the same input image size.
	 * </p>
	 * 
	 * @param input Input pyramid.
	 * @param gradient Computes image gradient
	 * @param derivX Pyramid where x-derivative is stored.
	 * @param derivY Pyramid where y-derivative is stored.
	 */
	public static <I extends ImageSingleBand, O extends ImageSingleBand>
	void gradient(ImagePyramid<I> input, ImageGradient<I, O> gradient, O[] derivX, O[] derivY )
	{
		for( int i = 0; i < input.getNumLayers(); i++ ) {
			I imageIn = input.getLayer(i);
			gradient.process(imageIn,derivX[i],derivY[i]);
		}
	}

	/**
	 * <p>
	 * Computes the hessian (2nd order derivative) for each image the pyramid.
	 * </p>
	 *
	 * @param derivX (Input) Pyramid where x-derivative is stored.
	 * @param derivY (Input) Pyramid where y-derivative is stored.
	 * @param hessian (Input) Computes hessian from gradient
	 * @param derivXX (Output) Second derivative XX
	 * @param derivYY (Output) Second derivative YY
	 * @param derivXY (Output) Second derivative XY
	 */
	public static <I extends ImageSingleBand, O extends ImageSingleBand>
	void hessian(O[] derivX, O[] derivY , ImageHessian<O> hessian , O[] derivXX, O[] derivYY , O[] derivXY )
	{
		for( int i = 0; i < derivX.length; i++ ) {
			hessian.process(derivX[i],derivY[i],derivXX[i],derivYY[i],derivXY[i]);
		}
	}
}
