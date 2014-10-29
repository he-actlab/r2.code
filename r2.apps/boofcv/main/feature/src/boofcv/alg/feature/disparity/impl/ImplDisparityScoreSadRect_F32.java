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

package boofcv.alg.feature.disparity.impl;

import boofcv.alg.feature.disparity.DisparityScoreSadRect;
import boofcv.alg.feature.disparity.DisparitySelect;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;

/**
 * <p>
 * Implementation of {@link boofcv.alg.feature.disparity.DisparityScoreSadRect} for processing
 * input images of type {@link ImageFloat32}.
 * </p>
 * <p>
 * DO NOT MODIFY. Generated by {@link GenerateDisparityScoreSadRect}.
 * </p>
 * 
 * @author Peter Abeles
 */
public class ImplDisparityScoreSadRect_F32<Disparity extends ImageSingleBand>
	extends DisparityScoreSadRect<ImageFloat32,Disparity>
{

	// Computes disparity from scores
	DisparitySelect<float[],Disparity> computeDisparity;

	// stores the local scores for the width of the region
	float elementScore[];
	// scores along horizontal axis for current block
	// To allow right to left validation all disparity scores are stored for the entire row
	// size = num columns * maxDisparity
	// disparity for column i is stored in elements i*maxDisparity to (i+1)*maxDisparity
	float horizontalScore[][];
	// summed scores along vertical axis
	// This is simply the sum of like elements in horizontal score
	float verticalScore[];

	public ImplDisparityScoreSadRect_F32( int minDisparity , int maxDisparity,
										int regionRadiusX, int regionRadiusY,
										DisparitySelect<float[],Disparity> computeDisparity) {
		super(minDisparity,maxDisparity,regionRadiusX,regionRadiusY);

		this.computeDisparity = computeDisparity;
	}

	@Override
	public void _process( ImageFloat32 left , ImageFloat32 right , Disparity disparity ) {
		if( horizontalScore == null || verticalScore.length < lengthHorizontal ) {
			horizontalScore = new float[regionHeight][lengthHorizontal];
			verticalScore = new float[lengthHorizontal];
			elementScore = new float[ left.width ];
		}

		computeDisparity.configure(disparity,minDisparity,maxDisparity,radiusX);

		// initialize computation
		computeFirstRow(left, right);
		// efficiently compute rest of the rows using previous results to avoid repeat computations
		computeRemainingRows(left, right);
	}

	/**
	 * Initializes disparity calculation by finding the scores for the initial block of horizontal
	 * rows.
	 */
	private void computeFirstRow(ImageFloat32 left, ImageFloat32 right ) {
		// compute horizontal scores for first row block
		for( int row = 0; row < regionHeight; row++ ) {

			float scores[] = horizontalScore[row];

			UtilDisparityScore.computeScoreRow(left, right, row, scores,
					minDisparity,maxDisparity,regionWidth,elementScore);
		}

		// compute score for the top possible row
		for( int i = 0; i < lengthHorizontal; i++ ) {
			float sum = 0;
			for( int row = 0; row < regionHeight; row++ ) {
				sum += horizontalScore[row][i];
			}
			verticalScore[i] = sum;
		}

		// compute disparity
		computeDisparity.process(radiusY, verticalScore);
	}

	/**
	 * Using previously computed results it efficiently finds the disparity in the remaining rows.
	 * When a new block is processes the last row/column is subtracted and the new row/column is
	 * added.
	 */
	private void computeRemainingRows( ImageFloat32 left, ImageFloat32 right )
	{
		for( int row = regionHeight; row < left.height; row++ ) {
			int oldRow = row%regionHeight;

			// subtract first row from vertical score
			float scores[] = horizontalScore[oldRow];
			for( int i = 0; i < lengthHorizontal; i++ ) {
				verticalScore[i] -= scores[i];
			}

			UtilDisparityScore.computeScoreRow(left, right, row, scores,
					minDisparity,maxDisparity,regionWidth,elementScore);

			// add the new score
			for( int i = 0; i < lengthHorizontal; i++ ) {
				verticalScore[i] += scores[i];
			}

			// compute disparity
			computeDisparity.process(row - regionHeight + 1 + radiusY, verticalScore);
		}
	}

	@Override
	public Class<ImageFloat32> getInputType() {
		return ImageFloat32.class;
	}

	@Override
	public Class<Disparity> getDisparityType() {
		return computeDisparity.getDisparityType();
	}

}
