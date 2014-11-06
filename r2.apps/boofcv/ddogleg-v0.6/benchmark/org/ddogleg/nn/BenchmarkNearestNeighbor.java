/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
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

package org.ddogleg.nn;

import org.ddogleg.Performer;
import org.ddogleg.ProfileOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
// TODO spawn new JVM for each benchmark...
public class BenchmarkNearestNeighbor {

	int dimen;
	List<double[]> cloud;
	List<double[]> searchSet;
	double maxDistance;
	NnData result = new NnData();

	public class SetAndSearch implements Performer {

		NearestNeighbor alg;
		String name;

		public SetAndSearch(NearestNeighbor alg, String name) {
			this.alg = alg;
			this.name = name;

//			alg.init(dimen);
//			alg.setPoints(cloud,null);
		}

		@Override
		public void process() {
			alg.init(dimen);
			alg.setPoints(cloud,null);

			for( double[]p : searchSet ) {
				alg.findNearest(p,maxDistance,result);
			}
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public List<Performer> createAlg() {
		List<Performer> ret = new ArrayList<Performer>();

		ret.add( new SetAndSearch(FactoryNearestNeighbor.exhaustive(),"Exhaustive"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.kdtree(),"kdtree"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.kdtree(1000),"kdtree P"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.kdRandomForest(200,20,5,23423432),"K-D Random Forest"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.vptree(0xDEADBEEF),"VP-Tree"));

		return ret;
	}

	public void evaluateDataSet( int dimen , int cloudSize , int searchSize ) {
		Random rand = new Random(234);

		this.dimen = dimen;
		this.maxDistance = 10;
		this.searchSet = createData(rand,searchSize,dimen);

		System.out.println("Uniform data");
		this.cloud = createData(rand, cloudSize, dimen);
		System.out.println("K = "+dimen+"  cloud = "+cloudSize+"  search = "+searchSize);
		for( Performer alg : createAlg() ) {
			ProfileOperation.printOpsPerSec(alg,100);
		}

		System.out.println();
		System.out.println("Linear data");
		this.cloud = createLinearData(rand, cloudSize, dimen);
		System.out.println("K = "+dimen+"  cloud = "+cloudSize+"  search = "+searchSize);
		for( Performer alg : createAlg() ) {
			ProfileOperation.printOpsPerSec(alg,100);
		}
	}

	public static List<double[]> createData( Random rand , int size , int k ) {
		List<double[]> ret = new ArrayList<double[]>();

		for( int i = 0; i < size; i++ ) {
			double []d = new double[ k ];
			for( int j = 0; j < k; j++ ) {
				d[j] = rand.nextDouble()*3;
			}
			ret.add(d);
		}
		return ret;
	}

	public static List<double[]> createLinearData(Random rand, int size, int k) {
		List<double[]> ret = new ArrayList<double[]>();

		double v[] = new double[k];
		for( int j = 0; j < k; j++ ) {
			v[j] = (rand.nextDouble()-0.5)*2;
		}

		for( int i = 0; i < size; i++ ) {
			double []d = new double[ k ];
			double l =  rand.nextDouble()*3;
			for( int j = 0; j < k; j++ ) {
				d[j] = v[j]*l;
			}
			ret.add(d);
		}
		return ret;
	}


	public static void main( String args[] ) {
		BenchmarkNearestNeighbor app = new BenchmarkNearestNeighbor();

//		app.evaluateDataSet(3,30,20);
//		app.evaluateDataSet(3,300,200);
//		app.evaluateDataSet(3,600,500);
//		app.evaluateDataSet(5,10000,10000);
//		app.evaluateDataSet(10,10000,10000);
//		app.evaluateDataSet(20,10000,10000);
		app.evaluateDataSet(60,10000,10000);
//		app.evaluateDataSet(120,10000,10000);
	}
}
