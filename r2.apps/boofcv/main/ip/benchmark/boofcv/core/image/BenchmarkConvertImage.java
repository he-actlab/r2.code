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

package boofcv.core.image;

import boofcv.misc.PerformerBase;
import boofcv.misc.ProfileOperation;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

/**
 * Benchmarks related to functions inside of ConvertImage
 * 
 * @author Peter Abeles
 */
public class BenchmarkConvertImage {
	static int imgWidth = 640;
	static int imgHeight = 480;

	static ImageFloat32 imgFloat32;
	static ImageUInt8 imgUInt8;
	static ImageSInt16 imgUInt16;
	static ImageUInt8 imgSInt8;
	static ImageSInt16 imgSInt16;

	public static class Float32toInt8 extends PerformerBase
	{
		@Override
		public void process() {
			ConvertImage.convert(imgFloat32,imgUInt8);
		}
	}

	public static class Int8ToFloat32 extends PerformerBase
	{
		ImageUInt8 img;

		public Int8ToFloat32(ImageUInt8 img) {
			this.img = img;
		}

		@Override
		public void process() {
			ConvertImage.convert(img,imgFloat32);
		}
	}

	public static class Int16ToFloat32 extends PerformerBase
	{
		ImageSInt16 img;

		public Int16ToFloat32(ImageSInt16 img) {
			this.img = img;
		}

		@Override
		public void process() {
			ConvertImage.convert(img,imgFloat32);
		}
	}

	public static class Int16ToInt8 extends PerformerBase
	{
		ImageSInt16 img;

		public Int16ToInt8(ImageSInt16 img) {
			this.img = img;
		}

		@Override
		public void process() {
			ConvertImage.convert(img,imgSInt8);
		}
	}

	public static void main( String args[] ) {
		imgSInt8 = new ImageUInt8(imgWidth,imgHeight);
		imgSInt16 = new ImageSInt16(imgWidth,imgHeight);
		imgUInt8 = new ImageUInt8(imgWidth,imgHeight);
		imgUInt16 = new ImageSInt16(imgWidth,imgHeight);
		imgFloat32 = new ImageFloat32(imgWidth,imgHeight);

		System.out.println("=========  Profile Image Size "+imgWidth+" x "+imgHeight+" ==========");
		System.out.println();

		System.out.printf("Float32 to Int8              %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Float32toInt8(),1000, false));
		System.out.printf("Int8 to Float32 signed       %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int8ToFloat32(imgSInt8),1000, false));
		System.out.printf("Int8 to Float32 unsigned     %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int8ToFloat32(imgUInt8),1000, false));
		System.out.printf("Int16 to Float32 signed       %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int16ToFloat32(imgSInt16),1000, false));
		System.out.printf("Int16 to Float32 unsigned     %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int16ToFloat32(imgUInt16),1000, false));
		System.out.printf("Int16 to Int8 signed          %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int16ToInt8(imgSInt16),1000, false));
		System.out.printf("Int16 to Int8 unsigned        %10.2f ops/sec\n",
				ProfileOperation.profileOpsPerSec(new Int16ToInt8(imgUInt16),1000, false));

	}
}
