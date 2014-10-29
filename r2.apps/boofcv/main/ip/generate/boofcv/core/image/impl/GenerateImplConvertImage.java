/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
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

package boofcv.core.image.impl;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;


/**
 * @author Peter Abeles
 */
public class GenerateImplConvertImage extends CodeGeneratorBase {

	String className = "ImplConvertImage";

	public GenerateImplConvertImage() throws FileNotFoundException {
		setOutputFile(className);
	}

	@Override
	public void generate() throws FileNotFoundException {
		printPreamble();

		for( AutoTypeImage in : AutoTypeImage.getSpecificTypes()) {
			for( AutoTypeImage out : AutoTypeImage.getGenericTypes() ) {
				if( in == out )
					continue;

				printConvert(in,out);
			}
		}

		out.print("}\n");
	}

	private void printPreamble() {
		out.print("import boofcv.struct.image.*;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Functions for converting between different primitive image types. Numerical values do not change or are closely approximated\n" +
				" * in these functions.  \n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * DO NOT MODIFY: This class was automatically generated by {@link boofcv.core.image.impl.GenerateImplConvertImage}\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	private void printConvert( AutoTypeImage imageIn , AutoTypeImage imageOut ) {

		String typeCast = "( "+imageOut.getDataType()+" )";
		String bitWise = imageIn.getBitWise();

		boolean sameTypes = imageIn.getDataType().compareTo(imageOut.getDataType()) == 0;

		if( imageIn.isInteger() && imageOut.isInteger() &&
				((imageOut.getNumBits() == 32 && imageIn.getNumBits() != 64) ||
				(imageOut.getNumBits() == 64)) )
			typeCast = "";
		else if( sameTypes && imageIn.isSigned() )
			typeCast = "";

		out.print("\tpublic static void convert( "+imageIn.getSingleBandName()+" from, "+imageOut.getSingleBandName()+" to ) {\n" +
				"\n" +
				"\t\tif (from.isSubimage() || to.isSubimage()) {\n" +
				"\n" +
				"\t\t\tfor (int y = 0; y < from.height; y++) {\n" +
				"\t\t\t\tint indexFrom = from.getIndex(0, y);\n" +
				"\t\t\t\tint indexTo = to.getIndex(0, y);\n" +
				"\n" +
				"\t\t\t\tfor (int x = 0; x < from.width; x++) {\n" +
				"\t\t\t\t\tto.data[indexTo++] = "+typeCast+"( from.data[indexFrom++] "+bitWise+");\n" +
				"\t\t\t\t}\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t} else {\n" +
				"\t\t\tfinal int N = from.width * from.height;\n" +
				"\n");

		if( sameTypes ) {
			out.print("\t\t\tSystem.arraycopy(from.data, 0, to.data, 0, N);\n");
		} else {
			out.print("\t\t\tfor (int i = 0; i < N; i++) {\n" +
					"\t\t\t\tto.data[i] = "+typeCast+"( from.data[i] "+bitWise+");\n" +
					"\t\t\t}\n");
		}
		out.print("\t\t}\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImplConvertImage app = new GenerateImplConvertImage();

		app.generate();
	}
}
