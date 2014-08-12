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

package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;

/**
 * @author Pablo Ordua, University of Deusto (pablo.orduna@deusto.es)
 * @author Eduardo Castillejo, University of Deusto (eduardo.castillejo@deusto.es)
 */
public abstract class AbstractExpandedDecoder {

  protected final BitArray information;
  protected final GeneralAppIdDecoder generalDecoder;

  AbstractExpandedDecoder(BitArray information){
    this.information    = information;
    this.generalDecoder = new GeneralAppIdDecoder(information);
  }

  public abstract String parseInformation() throws NotFoundException;

  public static AbstractExpandedDecoder createDecoder(BitArray information){
	AbstractExpandedDecoder ret = null;
	boolean done = false;
    if (information.get(1)) {
      ret = new AI01AndOtherAIs(information); done = true;
    } else if (!information.get(2)) {
      ret = new AnyAIDecoder(information); done = true;
    }

    if(!done) {
	    int fourBitEncodationMethod = GeneralAppIdDecoder.extractNumericValueFromBitArray(information, 1, 4);
	
	    switch(fourBitEncodationMethod){
	      case 4: ret = new AI013103decoder(information); done = true; break;
	      case 5: ret = new AI01320xDecoder(information); done = true; break;
	    }
	    
	    if(!done) {
		    int fiveBitEncodationMethod = GeneralAppIdDecoder.extractNumericValueFromBitArray(information, 1, 5);
		    switch(fiveBitEncodationMethod){
		      case 12: ret = new AI01392xDecoder(information); done = true; break;
		      case 13: ret = new AI01393xDecoder(information); done = true; break;
		    }
		    
		    if(!done) {
			    int sevenBitEncodationMethod = GeneralAppIdDecoder.extractNumericValueFromBitArray(information, 1, 7);
			    switch(sevenBitEncodationMethod){
			      case 56: ret = new AI013x0x1xDecoder(information, "310", "11"); done = true; break;
			      case 57: ret = new AI013x0x1xDecoder(information, "320", "11"); done = true; break;
			      case 58: ret = new AI013x0x1xDecoder(information, "310", "13"); done = true; break;
			      case 59: ret = new AI013x0x1xDecoder(information, "320", "13"); done = true; break;
			      case 60: ret = new AI013x0x1xDecoder(information, "310", "15"); done = true; break;
			      case 61: ret = new AI013x0x1xDecoder(information, "320", "15"); done = true; break;
			      case 62: ret = new AI013x0x1xDecoder(information, "310", "17"); done = true; break;
			      case 63: ret = new AI013x0x1xDecoder(information, "320", "17"); done = true; break;
			    }
		    }
	    }
    }
    if(ret == null)
    	throw new IllegalStateException("unknown decoder: " + information);
    else
    	return ret;
  }



}
