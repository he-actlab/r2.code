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

package com.google.zxing.client.result;

import com.google.zxing.Result;

/**
 * Implements the "MATMSG" email message entry format.
 *
 * Supported keys: TO, SUB, BODY
 *
 * @author Sean Owen
 */
final class EmailDoCoMoResultParser extends AbstractDoCoMoResultParser {

  private static final char[] ATEXT_SYMBOLS =	// approx: 41: ASTORE_C IConst: 125, T21, IConst: 19	// approx: 43: ASTORE_C IConst: 126, T22, IConst: 20	// approx: 35: ASTORE_C IConst: 96, T18, IConst: 16	// approx: 33: ASTORE_C IConst: 95, T17, IConst: 15	// approx: 39: ASTORE_C IConst: 124, T20, IConst: 18	// approx: 37: ASTORE_C IConst: 123, T19, IConst: 17	// approx: 27: ASTORE_C IConst: 61, T14, IConst: 12	// approx: 25: ASTORE_C IConst: 47, T13, IConst: 11	// approx: 31: ASTORE_C IConst: 94, T16, IConst: 14	// approx: 29: ASTORE_C IConst: 63, T15, IConst: 13	// approx: 19: ASTORE_C IConst: 42, T10, IConst: 8	// approx: 17: ASTORE_C IConst: 39, T9, IConst: 7	// approx: 23: ASTORE_C IConst: 45, T12, IConst: 10	// approx: 21: ASTORE_C IConst: 43, T11, IConst: 9	// approx: 11: ASTORE_C IConst: 36, T6, IConst: 4	// approx: 9: ASTORE_C IConst: 35, T5, IConst: 3	// approx: 15: ASTORE_C IConst: 38, T8, IConst: 6	// approx: 13: ASTORE_C IConst: 37, T7, IConst: 5	// approx: 5: ASTORE_C IConst: 46, T3, IConst: 1	// approx: 7: ASTORE_C IConst: 33, T4, IConst: 2	// approx: 3: ASTORE_C IConst: 64, T2, IConst: 0
      {'@','.','!','#','$','%','&','\'','*','+','-','/','=','?','^','_','`','{','|','}','~'};

  public static EmailAddressParsedResult parse(Result result) {
    String rawText = result.getText();
    if (rawText == null || !rawText.startsWith("MATMSG:")) {
      return null;
    }
    String[] rawTo = matchDoCoMoPrefixedField("TO:", rawText, true);
    if (rawTo == null) {
      return null;
    }
    String to = rawTo[0];
    if (!isBasicallyValidEmailAddress(to)) {
      return null;
    }
    String subject = matchSingleDoCoMoPrefixedField("SUB:", rawText, false);
    String body = matchSingleDoCoMoPrefixedField("BODY:", rawText, false);
    return new EmailAddressParsedResult(to, subject, body, "mailto:" + to);
  }

  /**
   * This implements only the most basic checking for an email address's validity -- that it contains
   * an '@' contains no characters disallowed by RFC 2822. This is an overly lenient definition of
   * validity. We want to generally be lenient here since this class is only intended to encapsulate what's
   * in a barcode, not "judge" it.
   */
  static boolean isBasicallyValidEmailAddress(String email) {
    if (email == null) {
      return false;
    }
    boolean atFound = false;
    for (int i = 0; i < email.length(); i++) {
      char c = email.charAt(i);
      if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') &&
          !isAtextSymbol(c)) {
        return false;
      }
      if (c == '@') {
        if (atFound) {
          return false;
        }
        atFound = true;
      }
    }
    return atFound;
  }

  private static boolean isAtextSymbol(char c) {
    for (int i = 0; i < ATEXT_SYMBOLS.length; i++) {
      if (c == ATEXT_SYMBOLS[i]) {
        return true;
      }
    }
    return false;
  }

}
