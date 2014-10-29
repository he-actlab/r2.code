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

package boofcv.io;

import boofcv.io.image.SimpleImageSequence;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

import java.awt.image.BufferedImage;
import java.io.Reader;

/**
 * Abstract interface for accessing files, images, and videos.  Intended to help
 * handle regular applications and applets
 * 
 * @author Peter Abeles
 */
public interface MediaManager {
	
	public Reader openFile( String fileName );
	
	public BufferedImage openImage( String fileName );
	
	public <T extends ImageBase>
	SimpleImageSequence<T> openVideo( String fileName , ImageType<T> imageInfo );


	public <T extends ImageBase>
	boolean openCamera( String device , int width , int height , VideoCallBack<T> callback );
}
