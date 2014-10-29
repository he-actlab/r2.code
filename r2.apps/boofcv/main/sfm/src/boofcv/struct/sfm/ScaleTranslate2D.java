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

package boofcv.struct.sfm;

/**
 * Motion model for scale and translation:
 *
 * (x',y') = (x,y)*scale + (tranX , tranY)
 *
 * @author Peter Abeles
 */
public class ScaleTranslate2D {
	/**
	 * Scaling
	 */
	public double scale;
	/**
	 * Translation along x and y axis
	 */
	public double transX,transY;

	public ScaleTranslate2D(double scale, double transX, double transY) {
		this.scale = scale;
		this.transX = transX;
		this.transY = transY;
	}

	public ScaleTranslate2D() {
	}

	public void set( ScaleTranslate2D src ) {
		this.scale = src.scale;
		this.transX = src.transX;
		this.transY = src.transY;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getTransX() {
		return transX;
	}

	public void setTransX(double transX) {
		this.transX = transX;
	}

	public double getTransY() {
		return transY;
	}

	public void setTransY(double transY) {
		this.transY = transY;
	}
}
