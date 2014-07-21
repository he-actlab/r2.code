/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.qrcode.detector;

import com.google.zxing.ApproxMath;
import com.google.zxing.ResultPoint;



/**
 * <p>Encapsulates a finder pattern, which are the three square patterns found in
 * the corners of QR Codes. It also encapsulates a count of similar finder patterns,
 * as a convenience to the finder's bookkeeping.</p>
 *
 * @author Sean Owen
 */
public final class FinderPattern extends ResultPoint {

  private final float estimatedModuleSize;
  private int count;

  FinderPattern(float posX, float posY,  float estimatedModuleSize) {
    super(posX, posY);
    this.estimatedModuleSize = estimatedModuleSize;
    this.count = 1;
  }

  public  float getEstimatedModuleSize() {
    return estimatedModuleSize;
  }

  int getCount() {
    return count;
  }

  void incrementCount() {
    this.count++;
  }

  /**
   * <p>Determines if this finder pattern "about equals" a finder pattern at the stated
   * position and size -- meaning, it is at nearly the same center with nearly the same size.</p>
   */
   boolean aboutEquals( float moduleSize, float i, float j) {
	boolean cond = (ApproxMath.abs(i - getY()) <= moduleSize && ApproxMath.abs(j - getX()) <= moduleSize);
	//additional accept
	cond = accept(cond);
    if (cond) {
      float moduleSizeDiff = ApproxMath.abs(moduleSize - estimatedModuleSize);
      //additional accept
      moduleSizeDiff = accept(moduleSizeDiff);
      float temp = estimatedModuleSize;
      //additional accept
      temp = accept(temp);
      return moduleSizeDiff <= 1.0f || moduleSizeDiff / temp <= 1.0f;
    }
    return false;
  }
   
  public static float accept(float f){return f;}
  public static boolean accept(boolean f){return f;}
   
}
