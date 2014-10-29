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

package boofcv.factory.feature.disparity;

import boofcv.alg.feature.disparity.*;
import boofcv.alg.feature.disparity.impl.*;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

/**
 * Algorithms related to computing the disparity between two rectified stereo images.
 *
 * @author Peter Abeles
 */
public class FactoryStereoDisparityAlgs {

	public static DisparitySelect<int[],ImageUInt8> selectDisparity_S32( int maxError , int tolR2L , double texture) {
		if( maxError < 0 && tolR2L < 0  & texture <= 0 )
			return new ImplSelectRectBasicWta_S32_U8();
		else
			return new ImplSelectRectStandard_S32_U8(maxError,tolR2L,texture);
	}

	public static DisparitySelect<float[],ImageUInt8> selectDisparity_F32( int maxError , int tolR2L , double texture) {
		if( maxError < 0 && tolR2L < 0  & texture <= 0 )
			return new ImplSelectRectBasicWta_F32_U8();
		else
			return new ImplSelectRectStandard_F32_U8(maxError,tolR2L,texture);
	}

	public static DisparitySelect<int[],ImageFloat32>
	selectDisparitySubpixel_S32( int maxError , int tolR2L , double texture) {
		return new SelectRectSubpixel.S32_F32(maxError,tolR2L,texture);
	}

	public static DisparitySelect<float[],ImageFloat32>
	selectDisparitySubpixel_F32( int maxError , int tolR2L , double texture) {
		return new SelectRectSubpixel.F32_F32(maxError,tolR2L,texture);
	}

	public static DisparitySparseSelect<int[]>
	selectDisparitySparse_S32( int maxError , double texture) {
		if( maxError < 0 && texture <= 0 )
			return new ImplSelectSparseBasicWta_S32();
		else
			return new ImplSelectSparseStandardWta_S32(maxError,texture);
	}

	public static DisparitySparseSelect<float[]>
	selectDisparitySparse_F32( int maxError , double texture) {
		if( maxError < 0 && texture <= 0 )
			return new ImplSelectSparseBasicWta_F32();
		else
			return new ImplSelectSparseStandardWta_F32(maxError,texture);
	}

	public static DisparitySparseSelect<int[]>
	selectDisparitySparseSubpixel_S32( int maxError , double texture) {
		return new SelectSparseStandardSubpixel.S32(maxError,texture);
	}

	public static DisparitySparseSelect<float[]>
	selectDisparitySparseSubpixel_F32( int maxError , double texture) {
		return new SelectSparseStandardSubpixel.F32(maxError,texture);
	}

	public static <T extends ImageSingleBand> DisparityScoreSadRect<ImageUInt8,T>
	scoreDisparitySadRect_U8( int minDisparity , int maxDisparity,
						   int regionRadiusX, int regionRadiusY,
						   DisparitySelect<int[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRect_U8<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static <T extends ImageSingleBand> DisparityScoreSadRect<ImageSInt16,T>
	scoreDisparitySadRect_S16( int minDisparity , int maxDisparity,
							  int regionRadiusX, int regionRadiusY,
							  DisparitySelect<int[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRect_S16<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static <T extends ImageSingleBand> DisparityScoreSadRect<ImageFloat32,T>
	scoreDisparitySadRect_F32( int minDisparity , int maxDisparity,
							  int regionRadiusX, int regionRadiusY,
							  DisparitySelect<float[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRect_F32<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static <T extends ImageSingleBand> DisparityScoreWindowFive<ImageUInt8,T>
	scoreDisparitySadRectFive_U8( int minDisparity , int maxDisparity,
								  int regionRadiusX, int regionRadiusY,
								  DisparitySelect<int[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRectFive_U8<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static <T extends ImageSingleBand> DisparityScoreWindowFive<ImageSInt16,T>
	scoreDisparitySadRectFive_S16( int minDisparity , int maxDisparity,
								  int regionRadiusX, int regionRadiusY,
								  DisparitySelect<int[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRectFive_S16<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static <T extends ImageSingleBand> DisparityScoreWindowFive<ImageFloat32,T>
	scoreDisparitySadRectFive_F32( int minDisparity , int maxDisparity,
								   int regionRadiusX, int regionRadiusY,
								   DisparitySelect<float[],T> computeDisparity)
	{
		return new ImplDisparityScoreSadRectFive_F32<T>(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY,computeDisparity);
	}

	public static DisparitySparseScoreSadRect<int[],ImageUInt8>
	scoreDisparitySparseSadRect_U8( int minDisparity , int maxDisparity,
									int regionRadiusX, int regionRadiusY )
	{
		return new ImplDisparitySparseScoreSadRect_U8(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY);
	}

	public static DisparitySparseScoreSadRect<float[],ImageFloat32>
	scoreDisparitySparseSadRect_F32( int minDisparity, int maxDisparity,
									 int regionRadiusX, int regionRadiusY )
	{
		return new ImplDisparitySparseScoreSadRect_F32(minDisparity,
				maxDisparity,regionRadiusX,regionRadiusY);
	}
}
