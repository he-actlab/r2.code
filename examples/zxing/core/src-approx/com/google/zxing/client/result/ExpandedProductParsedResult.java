/*
 * Copyright (C) 2010 ZXing authors
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

/*
 * These authors would like to acknowledge the Spanish Ministry of Industry,
 * Tourism and Trade, for the support in the project TSI020301-2008-2
 * "PIRAmIDE: Personalizable Interactions with Resources on AmI-enabled
 * Mobile Dynamic Environments", led by Treelogic
 * ( http://www.treelogic.com/ ):
 *
 *   http://www.piramidepse.com/
 */

package com.google.zxing.client.result;

import java.util.Hashtable;

/**
 * @author Antonio Manuel Benjumea Conde, Servinform, S.A.
 * @author Agustín Delgado, Servinform, S.A.
 */
public class ExpandedProductParsedResult extends ParsedResult {

  public static final String KILOGRAM = "KG";
  public static final String POUND = "LB";

  private final String productID;
  private final String sscc;
  private final String lotNumber;
  private final String productionDate;
  private final String packagingDate;
  private final String bestBeforeDate;
  private final String expirationDate;
  private final String weight;
  private final String weightType;
  private final String weightIncrement;
  private final String price;
  private final String priceIncrement;
  private final String priceCurrency;
  // For AIS that not exist in this object
  private final Hashtable uncommonAIs;

  ExpandedProductParsedResult() {
    super(ParsedResultType.PRODUCT);
    this.productID = "";
    this.sscc = "";
    this.lotNumber = "";
    this.productionDate = "";
    this.packagingDate = "";
    this.bestBeforeDate = "";
    this.expirationDate = "";
    this.weight = "";
    this.weightType = "";
    this.weightIncrement = "";
    this.price = "";
    this.priceIncrement = "";
    this.priceCurrency = "";
    this.uncommonAIs = new Hashtable();
  }

  public ExpandedProductParsedResult(String productID, String sscc,
      String lotNumber, String productionDate, String packagingDate,
      String bestBeforeDate, String expirationDate, String weight,
      String weightType, String weightIncrement, String price,
      String priceIncrement, String priceCurrency, Hashtable uncommonAIs) {
    super(ParsedResultType.PRODUCT);
    this.productID = productID;
    this.sscc = sscc;
    this.lotNumber = lotNumber;
    this.productionDate = productionDate;
    this.packagingDate = packagingDate;
    this.bestBeforeDate = bestBeforeDate;
    this.expirationDate = expirationDate;
    this.weight = weight;
    this.weightType = weightType;
    this.weightIncrement = weightIncrement;
    this.price = price;
    this.priceIncrement = priceIncrement;
    this.priceCurrency = priceCurrency;
    this.uncommonAIs = uncommonAIs;
  }

  public boolean equals(Object o){
    if (!(o instanceof ExpandedProductParsedResult)) {
      return false;
    }

    ExpandedProductParsedResult other = (ExpandedProductParsedResult)o;

    return this.productID.equals(       other.productID)	// approx: 57: MOVE_I T49, IConst: 0	// approx: 58: MOVE_I T48, IConst: 1
      && this.sscc.equals(            other.sscc)
      && this.lotNumber.equals(       other.lotNumber)
      && this.productionDate.equals(  other.productionDate)
      && this.bestBeforeDate.equals(  other.bestBeforeDate)
      && this.expirationDate.equals(  other.expirationDate)
      && this.weight.equals(          other.weight)
      && this.weightType.equals(      other.weightType)
      && this.weightIncrement.equals( other.weightIncrement)
      && this.price.equals(           other.price)
      && this.priceIncrement.equals(  other.priceIncrement)
      && this.priceCurrency.equals(   other.priceCurrency)
      && this.uncommonAIs.equals(     other.uncommonAIs);
  }

  public int hashCode(){
    int hash1 = this.productID.hashCode();	// approx: 3: MOVE_I R9, T8
    hash1 = 31 * hash1 + this.sscc.hashCode();	// approx: 4: MUL_I T10, IConst: 31, R9	// approx: 8: MOVE_I R14, T13	// approx: 7: ADD_I T13, T10, T12
    hash1 = 31 * hash1 + this.lotNumber.hashCode();	// approx: 9: MUL_I T15, IConst: 31, R14	// approx: 13: MOVE_I R19, T18	// approx: 12: ADD_I T18, T15, T17
    hash1 = 31 * hash1 + this.productionDate.hashCode();	// approx: 14: MUL_I T20, IConst: 31, R19	// approx: 17: ADD_I T23, T20, T22	// approx: 18: MOVE_I R24, T23
    hash1 = 31 * hash1 + this.bestBeforeDate.hashCode();	// approx: 22: ADD_I T28, T25, T27	// approx: 19: MUL_I T25, IConst: 31, R24	// approx: 23: MOVE_I R29, T28
    hash1 = 31 * hash1 + this.expirationDate.hashCode();	// approx: 28: MOVE_I R34, T33	// approx: 27: ADD_I T33, T30, T32	// approx: 24: MUL_I T30, IConst: 31, R29
    hash1 = 31 * hash1 + this.weight.hashCode();	// approx: 29: MUL_I T35, IConst: 31, R34	// approx: 33: MOVE_I R39, T38	// approx: 32: ADD_I T38, T35, T37

    int hash2 = this.weightType.hashCode();	// approx: 36: MOVE_I R42, T41
    hash2 = 31 * hash2 + this.weightIncrement.hashCode();	// approx: 37: MUL_I T43, IConst: 31, R42	// approx: 41: MOVE_I R47, T46	// approx: 40: ADD_I T46, T43, T45
    hash2 = 31 * hash2 + this.price.hashCode();	// approx: 42: MUL_I T48, IConst: 31, R47	// approx: 45: ADD_I T51, T48, T50	// approx: 46: MOVE_I R52, T51
    hash2 = 31 * hash2 + this.priceIncrement.hashCode();	// approx: 47: MUL_I T53, IConst: 31, R52	// approx: 50: ADD_I T56, T53, T55	// approx: 51: MOVE_I R57, T56
    hash2 = 31 * hash2 + this.priceCurrency.hashCode();	// approx: 55: ADD_I T61, T58, T60	// approx: 56: MOVE_I R62, T61	// approx: 52: MUL_I T58, IConst: 31, R57
    hash2 = 31 * hash2 + this.uncommonAIs.hashCode();	// approx: 60: ADD_I T66, T63, T65	// approx: 61: MOVE_I R67, T66	// approx: 57: MUL_I T63, IConst: 31, R62
    return hash1 ^ hash2;	// approx: 62: XOR_I T68, R39, R67
  }

  public String getProductID() {
    return productID;
  }

  public String getSscc() {
    return sscc;
  }

  public String getLotNumber() {
    return lotNumber;
  }

  public String getProductionDate() {
    return productionDate;
  }

  public String getPackagingDate() {
    return packagingDate;
  }

  public String getBestBeforeDate() {
    return bestBeforeDate;
  }

  public String getExpirationDate() {
    return expirationDate;
  }

  public String getWeight() {
    return weight;
  }

  public String getWeightType() {
    return weightType;
  }

  public String getWeightIncrement() {
    return weightIncrement;
  }

  public String getPrice() {
    return price;
  }

  public String getPriceIncrement() {
    return priceIncrement;
  }

  public String getPriceCurrency() {
    return priceCurrency;
  }

  public Hashtable getUncommonAIs() {
    return uncommonAIs;
  }

  public String getDisplayResult() {
    return productID;
  }
}
