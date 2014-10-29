/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.misc;

import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageSingleBand;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestGImageMiscOps extends BaseGClassChecksInMisc{

	long randomSeed = 2345;

	Random rand = new Random(234);

	public TestGImageMiscOps() {
		super(GImageMiscOps.class, ImageMiscOps.class);
	}

	@Test
	public void compareToPixelMath() {
		performTests(12);
	}

	@Override
	protected Object[][] createInputParam(Method candidate, Method validation) {
		Class<?> param[] = validation.getParameterTypes();
		String name = candidate.getName();

		ImageBase inputA = null;

		for( int i = 0; i < param.length; i++ ) {
			if( ImageBase.class.isAssignableFrom(param[i]) ) {
				if( ImageSingleBand.class.isAssignableFrom(param[i]))
					inputA = GeneralizedImageOps.createSingleBand((Class) param[i], width, height);
				else
					inputA = GeneralizedImageOps.createInterleaved((Class) param[i], width, height, numBands);
			}
		}
		if( inputA == null )
			throw new RuntimeException("Invalid funciton");

		Object[][] ret = new Object[1][param.length];

		if( name.equals("copy")) {
			ImageBase inputB = inputA._createNew(width,height);

			GImageMiscOps.fillUniform(inputA,rand,0,10);
			GImageMiscOps.fillUniform(inputB,rand,0,10);

			ret[0][0] = 10;
			ret[0][1] = 15;
			ret[0][2] = 12;
			ret[0][3] = 8;
			ret[0][4] = 5;
			ret[0][5] = 6;
			ret[0][6] = inputA;
			ret[0][7] = inputB;

		} else if( name.equals("fill")) {
			ret[0][0] = inputA;
			ret[0][1] = 3;
		} else if( name.equals("fillBorder")) {
			ret[0][0] = inputA;
			ret[0][1] = 3;
			ret[0][2] = 2;
		} else if( name.equals("fillRectangle")) {
			ret[0][0] = inputA;
			ret[0][1] = 3;
			ret[0][2] = 5;
			ret[0][3] = 6;
			ret[0][4] = 3;
			ret[0][5] = 4;
		} else if( name.equals("fillGaussian")) {
			ret[0][0] = inputA;
			ret[0][1] = new Random(randomSeed);
			ret[0][2] = 5;
			ret[0][3] = 3;
			ret[0][4] = 1;
			ret[0][5] = 12;
		} else if( name.equals("fillUniform")) {
			ret[0][0] = inputA;
			ret[0][1] = new Random(randomSeed);
			ret[0][2] = 5;
			ret[0][3] = 30;
		} else if( name.equals("addGaussian")) {
			ret[0][0] = inputA;
			ret[0][1] = new Random(randomSeed);
			ret[0][2] = 5;
			ret[0][3] = 1;
			ret[0][4] = 10;
		} else if( name.equals("addUniform")) {
			ret[0][0] = inputA;
			ret[0][1] = new Random(randomSeed);
			ret[0][2] = 1;
			ret[0][3] = 10;
		} else if( name.equals("flipVertical")) {
			ret[0][0] = inputA;
		} else if( name.equals("flipHorizontal")) {
			ret[0][0] = inputA;
		} else if( name.equals("rotateCW")) {
			ret[0][0] = inputA;
			ret[0][1] = inputA._createNew(height,width);
		} else if( name.equals("rotateCCW")) {
			ret[0][0] = inputA;
			ret[0][1] = inputA._createNew(height,width);
		} else {
			throw new RuntimeException("Unknown function: "+name);
		}

		fillRandom(inputA);

		return ret;
	}

	@Override
	protected Object[] reformatForValidation(Method m, Object[] targetParam) {
		Object[] ret = new Object[targetParam.length];

		for( int i = 0; i < ret.length; i++ ) {
			if( targetParam[i] instanceof ImageBase) {
				ImageBase img = (ImageBase)targetParam[i];
				ret[i] = ((ImageBase)targetParam[i])._createNew(img.width,img.height);
				((ImageBase)ret[i]).setTo((ImageBase)targetParam[i]);
			} else if( targetParam[i] instanceof Random ) {
				ret[i] = new Random(randomSeed);
			} else {
				ret[i] = targetParam[i];
			}
		}

		return ret;
	}

	@Override
	protected void compareResults(Object targetResult, Object[] targetParam, Object validationResult, Object[] validationParam) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
