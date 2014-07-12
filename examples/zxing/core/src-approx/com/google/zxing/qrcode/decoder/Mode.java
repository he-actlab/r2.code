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
 * <p>See ISO 18004:2006, 6.4.1, Tables 2 and 3. This enum encapsulates the various modes in which
 * data can be encoded to bits in the QR code standard.</p>
 *
 * @author Sean Owen
 */
public final class Mode {

  // No, we can't use an enum here. J2ME doesn't support it.

  public static final Mode TERMINATOR = new Mode(new int[]{0, 0, 0}, 0x00, "TERMINATOR"); // Not really a mode...	// approx: 5: ASTORE_I IConst: 0, T9, IConst: 0	// approx: 12: MOVE_I T13, IConst: 0	// approx: 9: ASTORE_I IConst: 0, T11, IConst: 2	// approx: 7: ASTORE_I IConst: 0, T10, IConst: 1
  public static final Mode NUMERIC = new Mode(new int[]{10, 12, 14}, 0x01, "NUMERIC");	// approx: 20: ASTORE_I IConst: 12, T18, IConst: 1	// approx: 18: ASTORE_I IConst: 10, T17, IConst: 0	// approx: 22: ASTORE_I IConst: 14, T19, IConst: 2	// approx: 25: MOVE_I T21, IConst: 1
  public static final Mode ALPHANUMERIC = new Mode(new int[]{9, 11, 13}, 0x02, "ALPHANUMERIC");	// approx: 35: ASTORE_I IConst: 13, T27, IConst: 2	// approx: 38: MOVE_I T29, IConst: 2	// approx: 31: ASTORE_I IConst: 9, T25, IConst: 0	// approx: 33: ASTORE_I IConst: 11, T26, IConst: 1
  public static final Mode STRUCTURED_APPEND = new Mode(new int[]{0, 0, 0}, 0x03, "STRUCTURED_APPEND"); // Not supported	// approx: 44: ASTORE_I IConst: 0, T33, IConst: 0	// approx: 51: MOVE_I T37, IConst: 3	// approx: 46: ASTORE_I IConst: 0, T34, IConst: 1	// approx: 48: ASTORE_I IConst: 0, T35, IConst: 2
  public static final Mode BYTE = new Mode(new int[]{8, 16, 16}, 0x04, "BYTE");	// approx: 64: MOVE_I T45, IConst: 4	// approx: 57: ASTORE_I IConst: 8, T41, IConst: 0	// approx: 59: ASTORE_I IConst: 16, T42, IConst: 1	// approx: 61: ASTORE_I IConst: 16, T43, IConst: 2
  public static final Mode ECI = new Mode(null, 0x07, "ECI"); // character counts don't apply	// approx: 70: MOVE_I T49, IConst: 7
  public static final Mode KANJI = new Mode(new int[]{8, 10, 12}, 0x08, "KANJI");	// approx: 79: ASTORE_I IConst: 10, T55, IConst: 1	// approx: 81: ASTORE_I IConst: 12, T56, IConst: 2	// approx: 84: MOVE_I T58, IConst: 8	// approx: 77: ASTORE_I IConst: 8, T54, IConst: 0
  public static final Mode FNC1_FIRST_POSITION = new Mode(null, 0x05, "FNC1_FIRST_POSITION");	// approx: 90: MOVE_I T62, IConst: 5
  public static final Mode FNC1_SECOND_POSITION = new Mode(null, 0x09, "FNC1_SECOND_POSITION");	// approx: 97: MOVE_I T67, IConst: 9

  private final int[] characterCountBitsForVersions;
  private final int bits;
  private final String name;

  private Mode(int[] characterCountBitsForVersions, int bits, String name) {
    this.characterCountBitsForVersions = characterCountBitsForVersions;
    this.bits = bits;	// approx: 3: PUTFIELD_I R0, .bits, R2
    this.name = name;
  }

  /**
   * @param bits four bits encoding a QR Code data mode
   * @return {@link Mode} encoded by these bits
   * @throws IllegalArgumentException if bits do not correspond to a known mode
   */
  public static Mode forBits(int bits) {
    switch (bits) {
      case 0x0:
        return TERMINATOR;
      case 0x1:
        return NUMERIC;
      case 0x2:
        return ALPHANUMERIC;
      case 0x3:
        return STRUCTURED_APPEND;
      case 0x4:
        return BYTE;
      case 0x5:
        return FNC1_FIRST_POSITION;
      case 0x7:
        return ECI;
      case 0x8:
        return KANJI;
      case 0x9:
        return FNC1_SECOND_POSITION;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * @param version version in question
   * @return number of bits used, in this QR Code symbol {@link Version}, to encode the
   *         count of characters that will follow encoded in this {@link Mode}
   */
  public int getCharacterCountBits(Version version) {
    if (characterCountBitsForVersions == null) {
      throw new IllegalArgumentException("Character count doesn't apply to this mode");
    }
    int number = version.getVersionNumber();
    int offset;
    if (number <= 9) {
      offset = 0;
    } else if (number <= 26) {
      offset = 1;
    } else {
      offset = 2;
    }
    return characterCountBitsForVersions[offset];
  }

  public int getBits() {
    return bits;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

}
