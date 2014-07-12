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

package com.google.zxing;

/**
 * <p>Encapsulates a point of interest in an image containing a barcode. Typically, this
 * would be the location of a finder pattern or the corner of the barcode, for example.</p>
 *
 * @author Sean Owen
 */
public class ResultPoint {

  public final float x;
  public final float y;

  public ResultPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public final float getX() {
    return x;
  }

  public final float getY() {
    return y;
  }

  public boolean equals(Object other) {
    if (other instanceof ResultPoint) {
      ResultPoint otherPoint = (ResultPoint) other;
      return x == otherPoint.x && y == otherPoint.y;
    }
    return false;
  }

  public int hashCode() {
    return 31 * Float.floatToIntBits(x) + Float.floatToIntBits(y);	// approx: 1: GETFIELD_F T4, R0, .x	// approx: 4: GETFIELD_F T7, R0, .y
  }

  public String toString() {
    StringBuffer result = new StringBuffer(25);
    result.append('(');
    result.append(x);	// approx: 8: GETFIELD_F T11, R0, .x
    result.append(',');
    result.append(y);	// approx: 12: GETFIELD_F T15, R0, .y
    result.append(')');
    return result.toString();
  }

  /**
   * <p>Orders an array of three ResultPoints in an order [A,B,C] such that AB < AC and
   * BC < AC and the angle between BC and BA is less than 180 degrees.
   */
  public static void orderBestPatterns(ResultPoint[] patterns) {

    // Find distances between pattern centers
    float zeroOneDistance = distance(patterns[0], patterns[1]);
    float oneTwoDistance = distance(patterns[1], patterns[2]);
    float zeroTwoDistance = distance(patterns[0], patterns[2]);

    ResultPoint pointA, pointB, pointC;
    // Assume one closest to other two is B; A and C will just be guesses at first
    if (oneTwoDistance >= zeroOneDistance && oneTwoDistance >= zeroTwoDistance) {
      pointB = patterns[0];
      pointA = patterns[1];
      pointC = patterns[2];
    } else if (zeroTwoDistance >= oneTwoDistance && zeroTwoDistance >= zeroOneDistance) {
      pointB = patterns[1];
      pointA = patterns[0];
      pointC = patterns[2];
    } else {
      pointB = patterns[2];
      pointA = patterns[0];
      pointC = patterns[1];
    }

    // Use cross product to figure out whether A and C are correct or flipped.
    // This asks whether BC x BA has a positive z component, which is the arrangement
    // we want for A, B, C. If it's negative, then we've got it flipped around and
    // should swap A and C.
    if (crossProductZ(pointA, pointB, pointC) < 0.0f) {
      ResultPoint temp = pointA;
      pointA = pointC;
      pointC = temp;
    }

    patterns[0] = pointA;
    patterns[1] = pointB;
    patterns[2] = pointC;
  }


  /**
   * @return distance between two points
   */
  public static float distance(ResultPoint pattern1, ResultPoint pattern2) {
    float xDiff = pattern1.getX() - pattern2.getX();	// approx: 4: MOVE_F R4, T9	// approx: 3: SUB_F T9, T7, T8
    float yDiff = pattern1.getY() - pattern2.getY();	// approx: 7: SUB_F T12, T10, T11	// approx: 8: MOVE_F R5, T12
    return (float) Math.sqrt((double) (xDiff * xDiff + yDiff * yDiff));	// approx: 11: ADD_F T15, T13, T14	// approx: 10: MUL_F T14, R5, R5	// approx: 9: MUL_F T13, R4, R4
  }

  /**
   * Returns the z component of the cross product between vectors BC and BA.
   */
  private static float crossProductZ(ResultPoint pointA, ResultPoint pointB, ResultPoint pointC) {
    float bX = pointB.x;
    float bY = pointB.y;
    return ((pointC.x - bX) * (pointA.y - bY)) - ((pointC.y - bY) * (pointA.x - bX));
  }


}
