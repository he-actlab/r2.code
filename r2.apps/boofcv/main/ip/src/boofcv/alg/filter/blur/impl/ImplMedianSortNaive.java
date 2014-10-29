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

package boofcv.alg.filter.blur.impl;

import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageInteger;
import org.ddogleg.sorting.QuickSelect;

/**
 * <p>
 * Median filter which uses quick select to find the local median value.  It is naive because the sort operation is started
 * from scratch for each pixel, discarding any information learned previously.
 * </p>
 *
 * <p>
 * NOTE: Do not modify.  Automatically generated by {@link GenerateImplMedianSortEdgeNaive}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplMedianSortNaive {

	/**
	 * Performs a median filter.
	 *
	 * @param input Raw input image.
	 * @param output Filtered image.
	 * @param radius Size of the filter's region.
	 * @param storage Array used for storage.  If null a new array is declared internally.
	 */
	public static void process( ImageInteger input, ImageInteger output, int radius , int[] storage ) {

		int w = 2*radius+1;
		if( storage == null ) {
			storage = new int[ w*w ];
		} else if( storage.length < w*w ) {
			throw new IllegalArgumentException("'storage' must be at least of length "+(w*w));
		}

		for( int y = 0; y < input.height; y++ ) {
			int minI = y - radius;
			int maxI = y + radius+1;

			// bound the y-axius inside the image
			if( minI < 0 ) minI = 0;
			if( maxI > input.height ) maxI = input.height;

			for( int x = 0; x < input.width; x++ ) {
				int minJ = x - radius;
				int maxJ = x + radius+1;

				// bound the x-axis to be inside the image
				if( minJ < 0 ) minJ = 0;
				if( maxJ > input.width ) maxJ = input.width;

				int index = 0;

				for( int i = minI; i < maxI; i++ ) {
					for( int j = minJ; j < maxJ; j++ ) {
						storage[index++] = input.get(j,i);
					}
				}
				
				// use quick select to avoid sorting the whole list
				int median = QuickSelect.select(storage, index / 2, index);
				output.set(x,y, median );
			}
		}
	}

	/**
	 * Performs a median filter.
	 *
	 * @param input Raw input image.
	 * @param output Filtered image.
	 * @param radius Size of the filter's region.
	 * @param storage Array used for storage.  If null a new array is declared internally.
	 */
	public static void process(ImageFloat32 input, ImageFloat32 output, int radius , float[] storage ) {

		int w = 2*radius+1;
		if( storage == null ) {
			storage = new float[ w*w ];
		} else if( storage.length < w*w ) {
			throw new IllegalArgumentException("'storage' must be at least of length "+(w*w));
		}

		for( int y = 0; y < input.height; y++ ) {
			int minI = y - radius;
			int maxI = y + radius+1;

			// bound the y-axius inside the image
			if( minI < 0 ) minI = 0;
			if( maxI > input.height ) maxI = input.height;

			for( int x = 0; x < input.width; x++ ) {
				int minJ = x - radius;
				int maxJ = x + radius+1;

				// bound the x-axis to be inside the image
				if( minJ < 0 ) minJ = 0;
				if( maxJ > input.width ) maxJ = input.width;

				int index = 0;

				for( int i = minI; i < maxI; i++ ) {
					for( int j = minJ; j < maxJ; j++ ) {
						storage[index++] = input.get(j,i);
					}
				}

				// use quick select to avoid sorting the whole list
				float median = QuickSelect.select(storage,index/2,index);
				output.set(x,y, median );
			}
		}
	}
}
