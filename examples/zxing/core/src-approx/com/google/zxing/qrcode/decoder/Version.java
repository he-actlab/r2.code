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

import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;

/**
 * See ISO 18004:2006 Annex D
 *
 * @author Sean Owen
 */
public final class Version {

  /**
   * See ISO 18004:2006 Annex D.
   * Element i represents the raw version bits that specify version i + 7
   */
  private static final int[] VERSION_DECODE_INFO = {	// approx: 19: ASTORE_I IConst: 63784, T11, IConst: 8	// approx: 17: ASTORE_I IConst: 58893, T10, IConst: 7	// approx: 15: ASTORE_I IConst: 55367, T9, IConst: 6	// approx: 13: ASTORE_I IConst: 51042, T8, IConst: 5	// approx: 11: ASTORE_I IConst: 48118, T7, IConst: 4	// approx: 9: ASTORE_I IConst: 42195, T6, IConst: 3	// approx: 7: ASTORE_I IConst: 39577, T5, IConst: 2	// approx: 5: ASTORE_I IConst: 34236, T4, IConst: 1	// approx: 3: ASTORE_I IConst: 31892, T3, IConst: 0	// approx: 37: ASTORE_I IConst: 102084, T20, IConst: 17	// approx: 39: ASTORE_I IConst: 102881, T21, IConst: 18	// approx: 41: ASTORE_I IConst: 110507, T22, IConst: 19	// approx: 43: ASTORE_I IConst: 110734, T23, IConst: 20	// approx: 45: ASTORE_I IConst: 117786, T24, IConst: 21	// approx: 47: ASTORE_I IConst: 119615, T25, IConst: 22	// approx: 49: ASTORE_I IConst: 126325, T26, IConst: 23	// approx: 51: ASTORE_I IConst: 127568, T27, IConst: 24	// approx: 21: ASTORE_I IConst: 68472, T12, IConst: 9	// approx: 23: ASTORE_I IConst: 70749, T13, IConst: 10	// approx: 25: ASTORE_I IConst: 76311, T14, IConst: 11	// approx: 27: ASTORE_I IConst: 79154, T15, IConst: 12	// approx: 29: ASTORE_I IConst: 84390, T16, IConst: 13	// approx: 31: ASTORE_I IConst: 87683, T17, IConst: 14	// approx: 33: ASTORE_I IConst: 92361, T18, IConst: 15	// approx: 35: ASTORE_I IConst: 96236, T19, IConst: 16	// approx: 69: ASTORE_I IConst: 167017, T36, IConst: 33	// approx: 55: ASTORE_I IConst: 136944, T29, IConst: 26	// approx: 53: ASTORE_I IConst: 133589, T28, IConst: 25	// approx: 59: ASTORE_I IConst: 145311, T31, IConst: 28	// approx: 57: ASTORE_I IConst: 141498, T30, IConst: 27	// approx: 63: ASTORE_I IConst: 152622, T33, IConst: 30	// approx: 61: ASTORE_I IConst: 150283, T32, IConst: 29	// approx: 67: ASTORE_I IConst: 161089, T35, IConst: 32	// approx: 65: ASTORE_I IConst: 158308, T34, IConst: 31
      0x07C94, 0x085BC, 0x09A99, 0x0A4D3, 0x0BBF6,
      0x0C762, 0x0D847, 0x0E60D, 0x0F928, 0x10B78,
      0x1145D, 0x12A17, 0x13532, 0x149A6, 0x15683,
      0x168C9, 0x177EC, 0x18EC4, 0x191E1, 0x1AFAB,
      0x1B08E, 0x1CC1A, 0x1D33F, 0x1ED75, 0x1F250,
      0x209D5, 0x216F0, 0x228BA, 0x2379F, 0x24B0B,
      0x2542E, 0x26A64, 0x27541, 0x28C69
  };

  private static final Version[] VERSIONS = buildVersions();

  private final int versionNumber;
  private final int[] alignmentPatternCenters;
  private final ECBlocks[] ecBlocks;
  private final int totalCodewords;

  private Version(int versionNumber,
                  int[] alignmentPatternCenters,
                  ECBlocks ecBlocks1,
                  ECBlocks ecBlocks2,
                  ECBlocks ecBlocks3,
                  ECBlocks ecBlocks4) {
    this.versionNumber = versionNumber;	// approx: 2: PUTFIELD_I R0, .versionNumber, R1
    this.alignmentPatternCenters = alignmentPatternCenters;
    this.ecBlocks = new ECBlocks[]{ecBlocks1, ecBlocks2, ecBlocks3, ecBlocks4};
    int total = 0;	// approx: 14: MOVE_I R24, IConst: 0
    int ecCodewords = ecBlocks1.getECCodewordsPerBlock();	// approx: 16: MOVE_I R11, T25
    ECB[] ecbArray = ecBlocks1.getECBlocks();
    for (int i = 0; i < ecbArray.length; i++) {	// approx: 32: ADD_I R38, R28, IConst: 1	// approx: 20: ARRAYLENGTH T30, R13	// approx: 19: MOVE_I R27, IConst: 0
      ECB ecBlock = ecbArray[i];
      total += ecBlock.getCount() * (ecBlock.getDataCodewords() + ecCodewords);	// approx: 28: ADD_I T34, T33, R11	// approx: 29: MUL_I T35, T32, T34	// approx: 30: ADD_I T36, R29, T35	// approx: 31: MOVE_I R37, T36
    }
    this.totalCodewords = total;	// approx: 22: PUTFIELD_I R0, .totalCodewords, R29
  }

  public int getVersionNumber() {
    return versionNumber;
  }

  public int[] getAlignmentPatternCenters() {
    return alignmentPatternCenters;
  }

  public int getTotalCodewords() {
    return totalCodewords;
  }

  public int getDimensionForVersion() {
    return 17 + 4 * versionNumber;
  }

  public ECBlocks getECBlocksForLevel(ErrorCorrectionLevel ecLevel) {
    return ecBlocks[ecLevel.ordinal()];
  }

  /**
   * <p>Deduces version information purely from QR Code dimensions.</p>
   *
   * @param dimension dimension in modules
   * @return {@link Version} for a QR Code of that dimension
   * @throws FormatException if dimension is not 1 mod 4
   */
  public static Version getProvisionalVersionForDimension(int dimension) throws FormatException {
    if (dimension % 4 != 1) {
      throw FormatException.getFormatInstance();
    }
    try {
      return getVersionForNumber((dimension - 17) >> 2);
    } catch (IllegalArgumentException iae) {
      throw FormatException.getFormatInstance();
    }
  }

  public static Version getVersionForNumber(int versionNumber) {
    if (versionNumber < 1 || versionNumber > 40) {
      throw new IllegalArgumentException();
    }
    return VERSIONS[versionNumber - 1];
  }

  static Version decodeVersionInformation(int versionBits) {
    int bestDifference = Integer.MAX_VALUE;
    int bestVersion = 0;
    for (int i = 0; i < VERSION_DECODE_INFO.length; i++) {
      int targetVersion = VERSION_DECODE_INFO[i];
      // Do the version info bits match exactly? done.
      if (targetVersion == versionBits) {
        return getVersionForNumber(i + 7);
      }
      // Otherwise see if this is the closest to a real version info bit string
      // we have seen so far
      int bitsDifference = FormatInformation.numBitsDiffering(versionBits, targetVersion);
      if (bitsDifference < bestDifference) {
        bestVersion = i + 7;
        bestDifference = bitsDifference;
      }
    }
    // We can tolerate up to 3 bits of error since no two version info codewords will
    // differ in less than 8 bits.
    if (bestDifference <= 3) {
      return getVersionForNumber(bestVersion);
    }
    // If we didn't find a close enough match, fail
    return null;
  }

  /**
   * See ISO 18004:2006 Annex E
   */
  BitMatrix buildFunctionPattern() {
    int dimension = getDimensionForVersion();
    BitMatrix bitMatrix = new BitMatrix(dimension);

    // Top left finder pattern + separator + format
    bitMatrix.setRegion(0, 0, 9, 9);
    // Top right finder pattern + separator + format
    bitMatrix.setRegion(dimension - 8, 0, 8, 9);
    // Bottom left finder pattern + separator + format
    bitMatrix.setRegion(0, dimension - 8, 9, 8);

    // Alignment patterns
    int max = alignmentPatternCenters.length;
    for (int x = 0; x < max; x++) {
      int i = alignmentPatternCenters[x] - 2;
      for (int y = 0; y < max; y++) {
        if ((x == 0 && (y == 0 || y == max - 1)) || (x == max - 1 && y == 0)) {
          // No alignment patterns near the three finder paterns
          continue;
        }
        bitMatrix.setRegion(alignmentPatternCenters[y] - 2, i, 5, 5);
      }
    }

    // Vertical timing pattern
    bitMatrix.setRegion(6, 9, 1, dimension - 17);
    // Horizontal timing pattern
    bitMatrix.setRegion(9, 6, dimension - 17, 1);

    if (versionNumber > 6) {
      // Version info, top right
      bitMatrix.setRegion(dimension - 11, 0, 3, 6);
      // Version info, bottom left
      bitMatrix.setRegion(0, dimension - 11, 6, 3);
    }

    return bitMatrix;
  }

  /**
   * <p>Encapsulates a set of error-correction blocks in one symbol version. Most versions will
   * use blocks of differing sizes within one version, so, this encapsulates the parameters for
   * each set of blocks. It also holds the number of error-correction codewords per block since it
   * will be the same across all blocks within one version.</p>
   */
  public static final class ECBlocks {
    private final int ecCodewordsPerBlock;
    private final ECB[] ecBlocks;

    ECBlocks(int ecCodewordsPerBlock, ECB ecBlocks) {
      this.ecCodewordsPerBlock = ecCodewordsPerBlock;	// approx: 2: PUTFIELD_I R0, .ecCodewordsPerBlock, R1
      this.ecBlocks = new ECB[]{ecBlocks};
    }

    ECBlocks(int ecCodewordsPerBlock, ECB ecBlocks1, ECB ecBlocks2) {
      this.ecCodewordsPerBlock = ecCodewordsPerBlock;	// approx: 2: PUTFIELD_I R0, .ecCodewordsPerBlock, R1
      this.ecBlocks = new ECB[]{ecBlocks1, ecBlocks2};
    }

    public int getECCodewordsPerBlock() {
      return ecCodewordsPerBlock;
    }

    public int getNumBlocks() {
      int total = 0;
      for (int i = 0; i < ecBlocks.length; i++) {
        total += ecBlocks[i].getCount();
      }
      return total;
    }

    public int getTotalECCodewords() {
      return ecCodewordsPerBlock * getNumBlocks();
    }

    public ECB[] getECBlocks() {
      return ecBlocks;
    }
  }

  /**
   * <p>Encapsualtes the parameters for one error-correction block in one symbol version.
   * This includes the number of data codewords, and the number of times a block with these
   * parameters is used consecutively in the QR code version's format.</p>
   */
  public static final class ECB {
    private final int count;
    private final int dataCodewords;

    ECB(int count, int dataCodewords) {
      this.count = count;	// approx: 2: PUTFIELD_I R0, .count, R1
      this.dataCodewords = dataCodewords;	// approx: 3: PUTFIELD_I R0, .dataCodewords, R2
    }

    public int getCount() {
      return count;
    }

    public int getDataCodewords() {
      return dataCodewords;
    }
  }

  public String toString() {
    return String.valueOf(versionNumber);	// approx: 1: GETFIELD_I T1, R0, .versionNumber
  }

  /**
   * See ISO 18004:2006 6.5.1 Table 9
   */
  private static Version[] buildVersions() {
    return new Version[]{	// approx: 1228: MOVE_I T943, IConst: 15	// approx: 1229: MOVE_I T944, IConst: 15	// approx: 1233: MOVE_I T947, IConst: 16	// approx: 1234: MOVE_I T948, IConst: 10	// approx: 1220: MOVE_I T937, IConst: 5	// approx: 1222: MOVE_I T938, IConst: 30	// approx: 1215: MOVE_I T933, IConst: 15	// approx: 1214: MOVE_I T932, IConst: 24	// approx: 1219: MOVE_I T936, IConst: 25	// approx: 1205: MOVE_I T925, IConst: 42	// approx: 1206: MOVE_I T926, IConst: 13	// approx: 1208: MOVE_I T927, IConst: 26	// approx: 1200: MOVE_I T921, IConst: 41	// approx: 1201: MOVE_I T922, IConst: 3	// approx: 1191: MOVE_I T914, IConst: 108	// approx: 1194: MOVE_I T916, IConst: 28	// approx: 1192: MOVE_I T915, IConst: 5	// approx: 1180: ASTORE_I IConst: 90, T905, IConst: 3	// approx: 1187: MOVE_I T911, IConst: 3	// approx: 1186: MOVE_I T910, IConst: 107	// approx: 1174: ASTORE_I IConst: 6, T902, IConst: 0	// approx: 1178: ASTORE_I IConst: 62, T904, IConst: 2	// approx: 1176: ASTORE_I IConst: 34, T903, IConst: 1	// approx: 1165: MOVE_I T896, IConst: 26	// approx: 1167: MOVE_I T897, IConst: 19	// approx: 1162: MOVE_I T894, IConst: 14	// approx: 1163: MOVE_I T895, IConst: 16	// approx: 1157: MOVE_I T890, IConst: 13	// approx: 1158: MOVE_I T891, IConst: 9	// approx: 1149: MOVE_I T884, IConst: 4	// approx: 1148: MOVE_I T883, IConst: 22	// approx: 1151: MOVE_I T885, IConst: 26	// approx: 1144: MOVE_I T880, IConst: 17	// approx: 1143: MOVE_I T879, IConst: 21	// approx: 1137: MOVE_I T874, IConst: 26	// approx: 1134: MOVE_I T872, IConst: 45	// approx: 1135: MOVE_I T873, IConst: 11	// approx: 1130: MOVE_I T869, IConst: 3	// approx: 1129: MOVE_I T868, IConst: 44	// approx: 1123: MOVE_I T863, IConst: 28	// approx: 1121: MOVE_I T862, IConst: 4	// approx: 1120: MOVE_I T861, IConst: 114	// approx: 1116: MOVE_I T858, IConst: 3	// approx: 1115: MOVE_I T857, IConst: 113	// approx: 1109: ASTORE_I IConst: 86, T852, IConst: 3	// approx: 1107: ASTORE_I IConst: 58, T851, IConst: 2
        new Version(1, new int[]{},
            new ECBlocks(7, new ECB(1, 19)),
            new ECBlocks(10, new ECB(1, 16)),
            new ECBlocks(13, new ECB(1, 13)),
            new ECBlocks(17, new ECB(1, 9))),
        new Version(2, new int[]{6, 18},
            new ECBlocks(10, new ECB(1, 34)),
            new ECBlocks(16, new ECB(1, 28)),
            new ECBlocks(22, new ECB(1, 22)),
            new ECBlocks(28, new ECB(1, 16))),
        new Version(3, new int[]{6, 22},
            new ECBlocks(15, new ECB(1, 55)),
            new ECBlocks(26, new ECB(1, 44)),
            new ECBlocks(18, new ECB(2, 17)),
            new ECBlocks(22, new ECB(2, 13))),
        new Version(4, new int[]{6, 26},
            new ECBlocks(20, new ECB(1, 80)),
            new ECBlocks(18, new ECB(2, 32)),
            new ECBlocks(26, new ECB(2, 24)),
            new ECBlocks(16, new ECB(4, 9))),
        new Version(5, new int[]{6, 30},
            new ECBlocks(26, new ECB(1, 108)),
            new ECBlocks(24, new ECB(2, 43)),
            new ECBlocks(18, new ECB(2, 15),
                new ECB(2, 16)),
            new ECBlocks(22, new ECB(2, 11),
                new ECB(2, 12))),
        new Version(6, new int[]{6, 34},
            new ECBlocks(18, new ECB(2, 68)),
            new ECBlocks(16, new ECB(4, 27)),
            new ECBlocks(24, new ECB(4, 19)),
            new ECBlocks(28, new ECB(4, 15))),
        new Version(7, new int[]{6, 22, 38},
            new ECBlocks(20, new ECB(2, 78)),
            new ECBlocks(18, new ECB(4, 31)),
            new ECBlocks(18, new ECB(2, 14),
                new ECB(4, 15)),
            new ECBlocks(26, new ECB(4, 13),
                new ECB(1, 14))),
        new Version(8, new int[]{6, 24, 42},
            new ECBlocks(24, new ECB(2, 97)),
            new ECBlocks(22, new ECB(2, 38),
                new ECB(2, 39)),
            new ECBlocks(22, new ECB(4, 18),
                new ECB(2, 19)),
            new ECBlocks(26, new ECB(4, 14),
                new ECB(2, 15))),
        new Version(9, new int[]{6, 26, 46},
            new ECBlocks(30, new ECB(2, 116)),
            new ECBlocks(22, new ECB(3, 36),
                new ECB(2, 37)),
            new ECBlocks(20, new ECB(4, 16),
                new ECB(4, 17)),
            new ECBlocks(24, new ECB(4, 12),
                new ECB(4, 13))),
        new Version(10, new int[]{6, 28, 50},
            new ECBlocks(18, new ECB(2, 68),
                new ECB(2, 69)),
            new ECBlocks(26, new ECB(4, 43),
                new ECB(1, 44)),
            new ECBlocks(24, new ECB(6, 19),
                new ECB(2, 20)),
            new ECBlocks(28, new ECB(6, 15),
                new ECB(2, 16))),
        new Version(11, new int[]{6, 30, 54},
            new ECBlocks(20, new ECB(4, 81)),
            new ECBlocks(30, new ECB(1, 50),
                new ECB(4, 51)),
            new ECBlocks(28, new ECB(4, 22),
                new ECB(4, 23)),
            new ECBlocks(24, new ECB(3, 12),
                new ECB(8, 13))),
        new Version(12, new int[]{6, 32, 58},
            new ECBlocks(24, new ECB(2, 92),
                new ECB(2, 93)),
            new ECBlocks(22, new ECB(6, 36),
                new ECB(2, 37)),
            new ECBlocks(26, new ECB(4, 20),
                new ECB(6, 21)),
            new ECBlocks(28, new ECB(7, 14),
                new ECB(4, 15))),
        new Version(13, new int[]{6, 34, 62},
            new ECBlocks(26, new ECB(4, 107)),
            new ECBlocks(22, new ECB(8, 37),
                new ECB(1, 38)),
            new ECBlocks(24, new ECB(8, 20),
                new ECB(4, 21)),
            new ECBlocks(22, new ECB(12, 11),
                new ECB(4, 12))),
        new Version(14, new int[]{6, 26, 46, 66},
            new ECBlocks(30, new ECB(3, 115),
                new ECB(1, 116)),
            new ECBlocks(24, new ECB(4, 40),
                new ECB(5, 41)),
            new ECBlocks(20, new ECB(11, 16),
                new ECB(5, 17)),
            new ECBlocks(24, new ECB(11, 12),
                new ECB(5, 13))),
        new Version(15, new int[]{6, 26, 48, 70},
            new ECBlocks(22, new ECB(5, 87),
                new ECB(1, 88)),
            new ECBlocks(24, new ECB(5, 41),
                new ECB(5, 42)),
            new ECBlocks(30, new ECB(5, 24),
                new ECB(7, 25)),
            new ECBlocks(24, new ECB(11, 12),
                new ECB(7, 13))),
        new Version(16, new int[]{6, 26, 50, 74},
            new ECBlocks(24, new ECB(5, 98),
                new ECB(1, 99)),
            new ECBlocks(28, new ECB(7, 45),
                new ECB(3, 46)),
            new ECBlocks(24, new ECB(15, 19),
                new ECB(2, 20)),
            new ECBlocks(30, new ECB(3, 15),
                new ECB(13, 16))),
        new Version(17, new int[]{6, 30, 54, 78},
            new ECBlocks(28, new ECB(1, 107),
                new ECB(5, 108)),
            new ECBlocks(28, new ECB(10, 46),
                new ECB(1, 47)),
            new ECBlocks(28, new ECB(1, 22),
                new ECB(15, 23)),
            new ECBlocks(28, new ECB(2, 14),
                new ECB(17, 15))),
        new Version(18, new int[]{6, 30, 56, 82},
            new ECBlocks(30, new ECB(5, 120),
                new ECB(1, 121)),
            new ECBlocks(26, new ECB(9, 43),
                new ECB(4, 44)),
            new ECBlocks(28, new ECB(17, 22),
                new ECB(1, 23)),
            new ECBlocks(28, new ECB(2, 14),
                new ECB(19, 15))),
        new Version(19, new int[]{6, 30, 58, 86},
            new ECBlocks(28, new ECB(3, 113),
                new ECB(4, 114)),
            new ECBlocks(26, new ECB(3, 44),
                new ECB(11, 45)),
            new ECBlocks(26, new ECB(17, 21),
                new ECB(4, 22)),
            new ECBlocks(26, new ECB(9, 13),
                new ECB(16, 14))),
        new Version(20, new int[]{6, 34, 62, 90},
            new ECBlocks(28, new ECB(3, 107),
                new ECB(5, 108)),
            new ECBlocks(26, new ECB(3, 41),
                new ECB(13, 42)),
            new ECBlocks(30, new ECB(15, 24),
                new ECB(5, 25)),
            new ECBlocks(28, new ECB(15, 15),
                new ECB(10, 16))),
        new Version(21, new int[]{6, 28, 50, 72, 94},
            new ECBlocks(28, new ECB(4, 116),
                new ECB(4, 117)),
            new ECBlocks(26, new ECB(17, 42)),
            new ECBlocks(28, new ECB(17, 22),
                new ECB(6, 23)),
            new ECBlocks(30, new ECB(19, 16),
                new ECB(6, 17))),
        new Version(22, new int[]{6, 26, 50, 74, 98},
            new ECBlocks(28, new ECB(2, 111),
                new ECB(7, 112)),
            new ECBlocks(28, new ECB(17, 46)),
            new ECBlocks(30, new ECB(7, 24),
                new ECB(16, 25)),
            new ECBlocks(24, new ECB(34, 13))),
        new Version(23, new int[]{6, 30, 54, 74, 102},
            new ECBlocks(30, new ECB(4, 121),
                new ECB(5, 122)),
            new ECBlocks(28, new ECB(4, 47),
                new ECB(14, 48)),
            new ECBlocks(30, new ECB(11, 24),
                new ECB(14, 25)),
            new ECBlocks(30, new ECB(16, 15),
                new ECB(14, 16))),
        new Version(24, new int[]{6, 28, 54, 80, 106},
            new ECBlocks(30, new ECB(6, 117),
                new ECB(4, 118)),
            new ECBlocks(28, new ECB(6, 45),
                new ECB(14, 46)),
            new ECBlocks(30, new ECB(11, 24),
                new ECB(16, 25)),
            new ECBlocks(30, new ECB(30, 16),
                new ECB(2, 17))),
        new Version(25, new int[]{6, 32, 58, 84, 110},
            new ECBlocks(26, new ECB(8, 106),
                new ECB(4, 107)),
            new ECBlocks(28, new ECB(8, 47),
                new ECB(13, 48)),
            new ECBlocks(30, new ECB(7, 24),
                new ECB(22, 25)),
            new ECBlocks(30, new ECB(22, 15),
                new ECB(13, 16))),
        new Version(26, new int[]{6, 30, 58, 86, 114},
            new ECBlocks(28, new ECB(10, 114),
                new ECB(2, 115)),
            new ECBlocks(28, new ECB(19, 46),
                new ECB(4, 47)),
            new ECBlocks(28, new ECB(28, 22),
                new ECB(6, 23)),
            new ECBlocks(30, new ECB(33, 16),
                new ECB(4, 17))),
        new Version(27, new int[]{6, 34, 62, 90, 118},
            new ECBlocks(30, new ECB(8, 122),
                new ECB(4, 123)),
            new ECBlocks(28, new ECB(22, 45),
                new ECB(3, 46)),
            new ECBlocks(30, new ECB(8, 23),
                new ECB(26, 24)),
            new ECBlocks(30, new ECB(12, 15),
                new ECB(28, 16))),
        new Version(28, new int[]{6, 26, 50, 74, 98, 122},
            new ECBlocks(30, new ECB(3, 117),
                new ECB(10, 118)),
            new ECBlocks(28, new ECB(3, 45),
                new ECB(23, 46)),
            new ECBlocks(30, new ECB(4, 24),
                new ECB(31, 25)),
            new ECBlocks(30, new ECB(11, 15),
                new ECB(31, 16))),
        new Version(29, new int[]{6, 30, 54, 78, 102, 126},
            new ECBlocks(30, new ECB(7, 116),
                new ECB(7, 117)),
            new ECBlocks(28, new ECB(21, 45),
                new ECB(7, 46)),
            new ECBlocks(30, new ECB(1, 23),
                new ECB(37, 24)),
            new ECBlocks(30, new ECB(19, 15),
                new ECB(26, 16))),
        new Version(30, new int[]{6, 26, 52, 78, 104, 130},
            new ECBlocks(30, new ECB(5, 115),
                new ECB(10, 116)),
            new ECBlocks(28, new ECB(19, 47),
                new ECB(10, 48)),
            new ECBlocks(30, new ECB(15, 24),
                new ECB(25, 25)),
            new ECBlocks(30, new ECB(23, 15),
                new ECB(25, 16))),
        new Version(31, new int[]{6, 30, 56, 82, 108, 134},
            new ECBlocks(30, new ECB(13, 115),
                new ECB(3, 116)),
            new ECBlocks(28, new ECB(2, 46),
                new ECB(29, 47)),
            new ECBlocks(30, new ECB(42, 24),
                new ECB(1, 25)),
            new ECBlocks(30, new ECB(23, 15),
                new ECB(28, 16))),
        new Version(32, new int[]{6, 34, 60, 86, 112, 138},
            new ECBlocks(30, new ECB(17, 115)),
            new ECBlocks(28, new ECB(10, 46),
                new ECB(23, 47)),
            new ECBlocks(30, new ECB(10, 24),
                new ECB(35, 25)),
            new ECBlocks(30, new ECB(19, 15),
                new ECB(35, 16))),
        new Version(33, new int[]{6, 30, 58, 86, 114, 142},
            new ECBlocks(30, new ECB(17, 115),
                new ECB(1, 116)),
            new ECBlocks(28, new ECB(14, 46),
                new ECB(21, 47)),
            new ECBlocks(30, new ECB(29, 24),
                new ECB(19, 25)),
            new ECBlocks(30, new ECB(11, 15),
                new ECB(46, 16))),
        new Version(34, new int[]{6, 34, 62, 90, 118, 146},
            new ECBlocks(30, new ECB(13, 115),
                new ECB(6, 116)),
            new ECBlocks(28, new ECB(14, 46),
                new ECB(23, 47)),
            new ECBlocks(30, new ECB(44, 24),
                new ECB(7, 25)),
            new ECBlocks(30, new ECB(59, 16),
                new ECB(1, 17))),
        new Version(35, new int[]{6, 30, 54, 78, 102, 126, 150},
            new ECBlocks(30, new ECB(12, 121),
                new ECB(7, 122)),
            new ECBlocks(28, new ECB(12, 47),
                new ECB(26, 48)),
            new ECBlocks(30, new ECB(39, 24),
                new ECB(14, 25)),
            new ECBlocks(30, new ECB(22, 15),
                new ECB(41, 16))),
        new Version(36, new int[]{6, 24, 50, 76, 102, 128, 154},
            new ECBlocks(30, new ECB(6, 121),
                new ECB(14, 122)),
            new ECBlocks(28, new ECB(6, 47),
                new ECB(34, 48)),
            new ECBlocks(30, new ECB(46, 24),
                new ECB(10, 25)),
            new ECBlocks(30, new ECB(2, 15),
                new ECB(64, 16))),
        new Version(37, new int[]{6, 28, 54, 80, 106, 132, 158},
            new ECBlocks(30, new ECB(17, 122),
                new ECB(4, 123)),
            new ECBlocks(28, new ECB(29, 46),
                new ECB(14, 47)),
            new ECBlocks(30, new ECB(49, 24),
                new ECB(10, 25)),
            new ECBlocks(30, new ECB(24, 15),
                new ECB(46, 16))),
        new Version(38, new int[]{6, 32, 58, 84, 110, 136, 162},
            new ECBlocks(30, new ECB(4, 122),
                new ECB(18, 123)),
            new ECBlocks(28, new ECB(13, 46),
                new ECB(32, 47)),
            new ECBlocks(30, new ECB(48, 24),
                new ECB(14, 25)),
            new ECBlocks(30, new ECB(42, 15),
                new ECB(32, 16))),
        new Version(39, new int[]{6, 26, 54, 82, 110, 138, 166},
            new ECBlocks(30, new ECB(20, 117),
                new ECB(4, 118)),
            new ECBlocks(28, new ECB(40, 47),
                new ECB(7, 48)),
            new ECBlocks(30, new ECB(43, 24),
                new ECB(22, 25)),
            new ECBlocks(30, new ECB(10, 15),
                new ECB(67, 16))),
        new Version(40, new int[]{6, 30, 58, 86, 114, 142, 170},
            new ECBlocks(30, new ECB(19, 118),
                new ECB(6, 119)),
            new ECBlocks(28, new ECB(18, 47),
                new ECB(31, 48)),
            new ECBlocks(30, new ECB(34, 24),
                new ECB(34, 25)),
            new ECBlocks(30, new ECB(20, 15),
                new ECB(61, 16)))
    };
  }

}
