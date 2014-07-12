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

package com.google.zxing.qrcode.decoder;

/**
 * <p>Encapsulates a QR Code's format information, including the data mask used and
 * error correction level.</p>
 *
 * @author Sean Owen
 * @see DataMask
 * @see ErrorCorrectionLevel
 */
final class FormatInformation {

  private static final int FORMAT_INFO_MASK_QR = 0x5412;

  /**
   * See ISO 18004:2006, Annex C, Table C.1
   */
  private static final int[][] FORMAT_INFO_DECODE_LOOKUP = {	// approx: 75: ASTORE_I IConst: 32170, T47, IConst: 0	// approx: 77: ASTORE_I IConst: 10, T48, IConst: 1	// approx: 84: ASTORE_I IConst: 11, T52, IConst: 1	// approx: 82: ASTORE_I IConst: 30877, T51, IConst: 0	// approx: 63: ASTORE_I IConst: 8, T40, IConst: 1	// approx: 61: ASTORE_I IConst: 30660, T39, IConst: 0	// approx: 68: ASTORE_I IConst: 29427, T43, IConst: 0	// approx: 70: ASTORE_I IConst: 9, T44, IConst: 1	// approx: 42: ASTORE_I IConst: 5, T28, IConst: 1	// approx: 47: ASTORE_I IConst: 20375, T31, IConst: 0	// approx: 49: ASTORE_I IConst: 6, T32, IConst: 1	// approx: 54: ASTORE_I IConst: 19104, T35, IConst: 0	// approx: 56: ASTORE_I IConst: 7, T36, IConst: 1	// approx: 26: ASTORE_I IConst: 23371, T19, IConst: 0	// approx: 28: ASTORE_I IConst: 3, T20, IConst: 1	// approx: 33: ASTORE_I IConst: 17913, T23, IConst: 0	// approx: 35: ASTORE_I IConst: 4, T24, IConst: 1	// approx: 40: ASTORE_I IConst: 16590, T27, IConst: 0	// approx: 14: ASTORE_I IConst: 1, T12, IConst: 1	// approx: 12: ASTORE_I IConst: 20773, T11, IConst: 0	// approx: 21: ASTORE_I IConst: 2, T16, IConst: 1	// approx: 19: ASTORE_I IConst: 24188, T15, IConst: 0	// approx: 7: ASTORE_I IConst: 0, T8, IConst: 1	// approx: 5: ASTORE_I IConst: 21522, T7, IConst: 0	// approx: 210: ASTORE_I IConst: 29, T124, IConst: 1	// approx: 215: ASTORE_I IConst: 11994, T127, IConst: 0	// approx: 203: ASTORE_I IConst: 28, T120, IConst: 1	// approx: 201: ASTORE_I IConst: 9396, T119, IConst: 0	// approx: 208: ASTORE_I IConst: 8579, T123, IConst: 0	// approx: 196: ASTORE_I IConst: 27, T116, IConst: 1	// approx: 194: ASTORE_I IConst: 14854, T115, IConst: 0	// approx: 187: ASTORE_I IConst: 16177, T111, IConst: 0	// approx: 189: ASTORE_I IConst: 26, T112, IConst: 1	// approx: 180: ASTORE_I IConst: 12392, T107, IConst: 0	// approx: 182: ASTORE_I IConst: 25, T108, IConst: 1	// approx: 173: ASTORE_I IConst: 13663, T103, IConst: 0	// approx: 175: ASTORE_I IConst: 24, T104, IConst: 1
      {0x5412, 0x00},
      {0x5125, 0x01},
      {0x5E7C, 0x02},
      {0x5B4B, 0x03},
      {0x45F9, 0x04},
      {0x40CE, 0x05},
      {0x4F97, 0x06},
      {0x4AA0, 0x07},
      {0x77C4, 0x08},
      {0x72F3, 0x09},
      {0x7DAA, 0x0A},
      {0x789D, 0x0B},
      {0x662F, 0x0C},
      {0x6318, 0x0D},
      {0x6C41, 0x0E},
      {0x6976, 0x0F},
      {0x1689, 0x10},
      {0x13BE, 0x11},
      {0x1CE7, 0x12},
      {0x19D0, 0x13},
      {0x0762, 0x14},
      {0x0255, 0x15},
      {0x0D0C, 0x16},
      {0x083B, 0x17},
      {0x355F, 0x18},
      {0x3068, 0x19},
      {0x3F31, 0x1A},
      {0x3A06, 0x1B},
      {0x24B4, 0x1C},
      {0x2183, 0x1D},
      {0x2EDA, 0x1E},
      {0x2BED, 0x1F},
  };

  /**
   * Offset i holds the number of 1 bits in the binary representation of i
   */
  private static final int[] BITS_SET_IN_HALF_BYTE =	// approx: 231: ASTORE_I IConst: 1, T135, IConst: 1	// approx: 229: ASTORE_I IConst: 0, T134, IConst: 0	// approx: 243: ASTORE_I IConst: 3, T141, IConst: 7	// approx: 241: ASTORE_I IConst: 2, T140, IConst: 6	// approx: 247: ASTORE_I IConst: 2, T143, IConst: 9	// approx: 245: ASTORE_I IConst: 1, T142, IConst: 8	// approx: 235: ASTORE_I IConst: 2, T137, IConst: 3	// approx: 233: ASTORE_I IConst: 1, T136, IConst: 2	// approx: 239: ASTORE_I IConst: 2, T139, IConst: 5	// approx: 237: ASTORE_I IConst: 1, T138, IConst: 4	// approx: 257: ASTORE_I IConst: 3, T148, IConst: 14	// approx: 259: ASTORE_I IConst: 4, T149, IConst: 15	// approx: 249: ASTORE_I IConst: 2, T144, IConst: 10	// approx: 251: ASTORE_I IConst: 3, T145, IConst: 11	// approx: 253: ASTORE_I IConst: 2, T146, IConst: 12	// approx: 255: ASTORE_I IConst: 3, T147, IConst: 13
      {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};

  private final ErrorCorrectionLevel errorCorrectionLevel;
  private final byte dataMask;

  private FormatInformation(int formatInfo) {
    // Bits 3,4
    errorCorrectionLevel = ErrorCorrectionLevel.forBits((formatInfo >> 3) & 0x03);
    // Bottom 3 bits
    dataMask = (byte) (formatInfo & 0x07);
  }

  static int numBitsDiffering(int a, int b) {
    a ^= b; // a now has a 1 bit exactly where its bit differs with b's
    // Count bits set quickly with a series of lookups:
    return BITS_SET_IN_HALF_BYTE[a & 0x0F] +
        BITS_SET_IN_HALF_BYTE[(a >>> 4 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 8 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 12 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 16 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 20 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 24 & 0x0F)] +
        BITS_SET_IN_HALF_BYTE[(a >>> 28 & 0x0F)];
  }

  /**
   * @param maskedFormatInfo1 format info indicator, with mask still applied
   * @param maskedFormatInfo2 second copy of same info; both are checked at the same time
   *  to establish best match
   * @return information about the format it specifies, or <code>null</code>
   *  if doesn't seem to match any known pattern
   */
  static FormatInformation decodeFormatInformation(int maskedFormatInfo1, int maskedFormatInfo2) {
    FormatInformation formatInfo = doDecodeFormatInformation(maskedFormatInfo1, maskedFormatInfo2);
    if (formatInfo != null) {
      return formatInfo;
    }
    // Should return null, but, some QR codes apparently
    // do not mask this info. Try again by actually masking the pattern
    // first
    return doDecodeFormatInformation(maskedFormatInfo1 ^ FORMAT_INFO_MASK_QR,
                                     maskedFormatInfo2 ^ FORMAT_INFO_MASK_QR);
  }

  private static FormatInformation doDecodeFormatInformation(int maskedFormatInfo1, int maskedFormatInfo2) {
    // Find the int in FORMAT_INFO_DECODE_LOOKUP with fewest bits differing
    int bestDifference = Integer.MAX_VALUE;
    int bestFormatInfo = 0;
    for (int i = 0; i < FORMAT_INFO_DECODE_LOOKUP.length; i++) {
      int[] decodeInfo = FORMAT_INFO_DECODE_LOOKUP[i];
      int targetInfo = decodeInfo[0];
      if (targetInfo == maskedFormatInfo1 || targetInfo == maskedFormatInfo2) {
        // Found an exact match
        return new FormatInformation(decodeInfo[1]);
      }
      int bitsDifference = numBitsDiffering(maskedFormatInfo1, targetInfo);
      if (bitsDifference < bestDifference) {
        bestFormatInfo = decodeInfo[1];
        bestDifference = bitsDifference;
      }
      if (maskedFormatInfo1 != maskedFormatInfo2) {
        // also try the other option
        bitsDifference = numBitsDiffering(maskedFormatInfo2, targetInfo);
        if (bitsDifference < bestDifference) {
          bestFormatInfo = decodeInfo[1];
          bestDifference = bitsDifference;
        }
      }
    }
    // Hamming distance of the 32 masked codes is 7, by construction, so <= 3 bits
    // differing means we found a match
    if (bestDifference <= 3) {
      return new FormatInformation(bestFormatInfo);
    }
    return null;
  }

  ErrorCorrectionLevel getErrorCorrectionLevel() {
    return errorCorrectionLevel;
  }

  byte getDataMask() {
    return dataMask;
  }

  public int hashCode() {
    return (errorCorrectionLevel.ordinal() << 3) | (int) dataMask;	// approx: 3: SHL_I T5, T4, IConst: 3	// approx: 4: GETFIELD_B T3, R0, .dataMask	// approx: 5: OR_I T6, T5, T3
  }

  public boolean equals(Object o) {
    if (!(o instanceof FormatInformation)) {
      return false;
    }
    FormatInformation other = (FormatInformation) o;
    return this.errorCorrectionLevel == other.errorCorrectionLevel &&	// approx: 11: MOVE_I T13, IConst: 0	// approx: 9: GETFIELD_B T7, R4, .dataMask	// approx: 8: GETFIELD_B T11, R0, .dataMask	// approx: 12: MOVE_I T12, IConst: 1
        this.dataMask == other.dataMask;
  }

}
