/*
 * Copyright 2008 ZXing authors
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

package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;



/**
 * <p>Implements decoding of the UPC-E format.</p>
 * <p/>
 * <p><a href="http://www.barcodeisland.com/upce.phtml">This</a> is a great reference for
 * UPC-E information.</p>
 *
 * @author Sean Owen
 */
public final class UPCEReader extends UPCEANReader {

  /**
   * The pattern that marks the middle, and end, of a UPC-E pattern.
   * There is no "second half" to a UPC-E barcode.
   */
  private static final int[] MIDDLE_END_PATTERN = {1, 1, 1, 1, 1, 1};	// approx: 11: ASTORE_I IConst: 1, T9, IConst: 4	// approx: 9: ASTORE_I IConst: 1, T8, IConst: 3	// approx: 13: ASTORE_I IConst: 1, T10, IConst: 5	// approx: 3: ASTORE_I IConst: 1, T5, IConst: 0	// approx: 7: ASTORE_I IConst: 1, T7, IConst: 2	// approx: 5: ASTORE_I IConst: 1, T6, IConst: 1

  /**
   * See {@link #L_AND_G_PATTERNS}; these values similarly represent patterns of
   * even-odd parity encodings of digits that imply both the number system (0 or 1)
   * used, and the check digit.
   */
  private static final int[][] NUMSYS_AND_CHECK_DIGIT_PATTERNS = {	// approx: 60: ASTORE_I IConst: 26, T35, IConst: 9	// approx: 58: ASTORE_I IConst: 22, T34, IConst: 8	// approx: 56: ASTORE_I IConst: 21, T33, IConst: 7	// approx: 44: ASTORE_I IConst: 11, T27, IConst: 1	// approx: 46: ASTORE_I IConst: 13, T28, IConst: 2	// approx: 42: ASTORE_I IConst: 7, T26, IConst: 0	// approx: 52: ASTORE_I IConst: 25, T31, IConst: 5	// approx: 54: ASTORE_I IConst: 28, T32, IConst: 6	// approx: 48: ASTORE_I IConst: 14, T29, IConst: 3	// approx: 50: ASTORE_I IConst: 19, T30, IConst: 4	// approx: 29: ASTORE_I IConst: 38, T19, IConst: 5	// approx: 31: ASTORE_I IConst: 35, T20, IConst: 6	// approx: 25: ASTORE_I IConst: 49, T17, IConst: 3	// approx: 27: ASTORE_I IConst: 44, T18, IConst: 4	// approx: 37: ASTORE_I IConst: 37, T23, IConst: 9	// approx: 33: ASTORE_I IConst: 42, T21, IConst: 7	// approx: 35: ASTORE_I IConst: 41, T22, IConst: 8	// approx: 19: ASTORE_I IConst: 56, T14, IConst: 0	// approx: 23: ASTORE_I IConst: 50, T16, IConst: 2	// approx: 21: ASTORE_I IConst: 52, T15, IConst: 1
      {0x38, 0x34, 0x32, 0x31, 0x2C, 0x26, 0x23, 0x2A, 0x29, 0x25},
      {0x07, 0x0B, 0x0D, 0x0E, 0x13, 0x19, 0x1C, 0x15, 0x16, 0x1A}
  };

  private final  int[] decodeMiddleCounters;

  public UPCEReader() {
    decodeMiddleCounters = new  int[4];
  }

  protected int decodeMiddle(BitArray row, int[] startRange, StringBuffer result)
      throws NotFoundException {
     int[] counters = decodeMiddleCounters;
    counters[0] = 0;
    counters[1] = 0;
    counters[2] = 0;
    counters[3] = 0;
    int end = row.getSize();
    int rowOffset = startRange[1];

    int lgPatternFound = 0;

    for (int x = 0; x < 6 && rowOffset < end; x++) {
       int bestMatch = decodeDigit(row, counters, rowOffset, ( int [] [])L_AND_G_PATTERNS);
      result.append((char) ('0' + bestMatch % 10));
      for (int i = 0; i < counters.length; i++) {
        rowOffset += counters[i];
      }
      if ((bestMatch >= 10)) {
        lgPatternFound |= 1 << (5 - x);
      }
    }

    determineNumSysAndCheckDigit(result, lgPatternFound);

    return rowOffset;
  }

  protected int[] decodeEnd(BitArray row, int endStart) throws NotFoundException {
    return findGuardPattern(row, endStart, true, MIDDLE_END_PATTERN);
  }

  protected boolean checkChecksum(String s) throws FormatException, ChecksumException {
    return super.checkChecksum(convertUPCEtoUPCA(s));
  }

  private static void determineNumSysAndCheckDigit(StringBuffer resultString, int lgPatternFound)
      throws NotFoundException {

    for (int numSys = 0; numSys <= 1; numSys++) {
      for (int d = 0; d < 10; d++) {
        if (lgPatternFound == NUMSYS_AND_CHECK_DIGIT_PATTERNS[numSys][d]) {
          resultString.insert(0, (char) ('0' + numSys));
          resultString.append((char) ('0' + d));
          return;
        }
      }
    }
    throw NotFoundException.getNotFoundInstance();
  }

  BarcodeFormat getBarcodeFormat() {
    return BarcodeFormat.UPC_E;
  }

  /**
   * Expands a UPC-E value back into its full, equivalent UPC-A code value.
   *
   * @param upce UPC-E code as string of digits
   * @return equivalent UPC-A code as string of digits
   */
  public static String convertUPCEtoUPCA(String upce) {
    char[] upceChars = new char[6];
    upce.getChars(1, 7, upceChars, 0);	// approx: 4: MOVE_I T3, IConst: 0
    StringBuffer result = new StringBuffer(12);
    result.append(upce.charAt(0));
    char lastChar = upceChars[5];
    switch (lastChar) {
      case '0':
      case '1':
      case '2':
        result.append(upceChars, 0, 2);	// approx: 37: MOVE_I T22, IConst: 0
        result.append(lastChar);
        result.append("0000");
        result.append(upceChars, 2, 3);	// approx: 43: MOVE_I T28, IConst: 2
        break;
      case '3':
        result.append(upceChars, 0, 3);	// approx: 28: MOVE_I T31, IConst: 0
        result.append("00000");
        result.append(upceChars, 3, 2);	// approx: 33: MOVE_I T36, IConst: 3
        break;
      case '4':
        result.append(upceChars, 0, 4);	// approx: 20: MOVE_I T39, IConst: 0
        result.append("00000");
        result.append(upceChars[4]);
        break;
      default:
        result.append(upceChars, 0, 5);	// approx: 47: MOVE_I T46, IConst: 0
        result.append("0000");
        result.append(lastChar);
        break;
    }
    result.append(upce.charAt(7));
    return result.toString();
  }

}
