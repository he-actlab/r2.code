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

package boofcv.alg.sfm.robust;


import boofcv.alg.geo.h.HomographyLinear4;
import boofcv.struct.geo.AssociatedPair;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.homography.UtilHomography;
import org.ddogleg.fitting.modelset.ModelFitter;
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ejml.data.DenseMatrix64F;

import java.util.List;

/**
 * Fits a homography to the observed points using linear algebra.  This provides an approximate solution.
 *
 * @author Peter Abeles
 */
public class GenerateHomographyLinear implements
		ModelGenerator<Homography2D_F64,AssociatedPair> ,
		ModelFitter<Homography2D_F64,AssociatedPair>
{

	HomographyLinear4 alg;
	DenseMatrix64F H = new DenseMatrix64F(3,3);

	public GenerateHomographyLinear( boolean normalizeInput ) {
		alg = new HomographyLinear4(normalizeInput);
	}

	@Override
	public boolean fitModel(List<AssociatedPair> dataSet, Homography2D_F64 initial, Homography2D_F64 found) {
		if( !alg.process(dataSet,H) )
			return false;

		UtilHomography.convert(H,found);
		return true;
	}

	@Override
	public boolean generate(List<AssociatedPair> dataSet, Homography2D_F64 model ) {

		if( !alg.process(dataSet,H) )
			return false;

		UtilHomography.convert(H,model);

		return true;
	}

	@Override
	public int getMinimumPoints() {
		return 4;
	}
}
