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

package boofcv.factory.feature.describe;

import boofcv.abst.feature.describe.ConfigSiftDescribe;
import boofcv.abst.feature.describe.ConfigSurfDescribe;
import boofcv.abst.filter.blur.BlurFilter;
import boofcv.alg.feature.describe.*;
import boofcv.alg.feature.describe.brief.BinaryCompareDefinition_I32;
import boofcv.alg.feature.describe.impl.*;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;


/**
 * Creates algorithms for describing point features.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class FactoryDescribePointAlgs {

	public static <T extends ImageSingleBand>
	DescribePointSurf<T> surfSpeed(ConfigSurfDescribe.Speed config, Class<T> imageType) {
		if( config == null )
			config = new ConfigSurfDescribe.Speed();
		config.checkValidity();


		return new DescribePointSurf<T>(config.widthLargeGrid,config.widthSubRegion,config.widthSample,
				config.weightSigma,config.useHaar,imageType);
	}

	public static <T extends ImageSingleBand>
	DescribePointSurfMod<T> surfStability(ConfigSurfDescribe.Stability config, Class<T> imageType) {
		if( config == null )
			config = new ConfigSurfDescribe.Stability();
		config.checkValidity();

		return new DescribePointSurfMod<T>(config.widthLargeGrid,config.widthSubRegion,config.widthSample,
				config.overLap,config.sigmaLargeGrid,config.sigmaSubRegion,config.useHaar,imageType);
	}

	public static <T extends ImageSingleBand>
	DescribePointSurfMultiSpectral<T> surfColor(DescribePointSurf<T> describe , int numBands ) {

		return new DescribePointSurfMultiSpectral<T>(describe,numBands);
	}

	public static <T extends ImageSingleBand>
	DescribePointBrief<T> brief(BinaryCompareDefinition_I32 definition, BlurFilter<T> filterBlur ) {
		Class<T> imageType = filterBlur.getInputType().getImageClass();

		DescribePointBinaryCompare<T> compare;

		if( imageType == ImageFloat32.class ) {
			compare = (DescribePointBinaryCompare<T> )new ImplDescribeBinaryCompare_F32(definition);
		} else if( imageType == ImageUInt8.class ) {
			compare = (DescribePointBinaryCompare<T> )new ImplDescribeBinaryCompare_U8(definition);
		} else {
			throw new IllegalArgumentException("Unknown image type: "+imageType.getSimpleName());
		}

		return new DescribePointBrief<T>(compare,filterBlur);
	}

	// todo remove filterBlur for all BRIEF change to radius,sigma,type
	public static <T extends ImageSingleBand>
	DescribePointBriefSO<T> briefso(BinaryCompareDefinition_I32 definition, BlurFilter<T> filterBlur) {
		Class<T> imageType = filterBlur.getInputType().getImageClass();

		InterpolatePixelS<T> interp = FactoryInterpolation.bilinearPixelS(imageType);

		return new DescribePointBriefSO<T>(definition,filterBlur,interp);
	}

	public static DescribePointSift sift( ConfigSiftDescribe config )
	{
	    if( config == null )
			config = new ConfigSiftDescribe();
		config.checkValidity();

		return new DescribePointSift(config.gridWidth,config.numSamples,config.numHistBins
				,config.weightSigma, config.sigmaToRadius);
	}

	public static <T extends ImageSingleBand, D extends TupleDesc>
	DescribePointPixelRegion<T,D> pixelRegion( int regionWidth , int regionHeight , Class<T> imageType )
	{
		if( imageType == ImageFloat32.class ) {
			return (DescribePointPixelRegion<T,D>)new ImplDescribePointPixelRegion_F32(regionWidth,regionHeight);
		} else if( imageType == ImageUInt8.class ) {
			return (DescribePointPixelRegion<T,D>)new ImplDescribePointPixelRegion_U8(regionWidth,regionHeight);
		} else {
			throw new IllegalArgumentException("Unsupported image type");
		}
	}

	public static <T extends ImageSingleBand>
	DescribePointPixelRegionNCC<T> pixelRegionNCC( int regionWidth , int regionHeight , Class<T> imageType )
	{
		if( imageType == ImageFloat32.class ) {
			return (DescribePointPixelRegionNCC<T>)new ImplDescribePointPixelRegionNCC_F32(regionWidth,regionHeight);
		} else if( imageType == ImageUInt8.class ) {
			return (DescribePointPixelRegionNCC<T>)new ImplDescribePointPixelRegionNCC_U8(regionWidth,regionHeight);
		} else {
			throw new IllegalArgumentException("Unsupported image type");
		}
	}
}
