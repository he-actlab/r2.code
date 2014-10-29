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

package boofcv.gui.image;

import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays images in a new window.
 *
 * @author Peter Abeles
 */
public class ShowImages {

	/**
	 * Creates a dialog window showing the specified image.  The function will not
	 * exit until the user clicks ok
	 */
	public static void showDialog(BufferedImage img) {
		ImageIcon icon = new ImageIcon();
		icon.setImage(img);
		JOptionPane.showMessageDialog(null, icon);
	}

	/**
	 * Shows a set of images in a grid pattern.
	 *
	 * @param numColumns How many columns are in the grid
	 * @param title Number of the window
	 * @param images List of images to show
	 * @return Display panel
	 */
	public static ImageGridPanel showGrid( int numColumns , String title , BufferedImage ...images ) {
		JFrame frame = new JFrame(title);

		ImageGridPanel panel = new ImageGridPanel(images.length/numColumns,numColumns,images);

		frame.add(panel, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);

		return panel;
	}

	/**
	 * Creates a window showing the specified image.
	 */
	public static ImagePanel showWindow(BufferedImage img, String title) {
		JFrame frame = new JFrame(title);

		ImagePanel panel = new ImagePanel(img);

		frame.add(panel, BorderLayout.CENTER);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		return panel;
	}

	public static ImagePanel showWindow( ImageUInt8 img , String title ) {
		BufferedImage buff = ConvertBufferedImage.convertTo(img,null);

		return showWindow(buff,title);
	}

	public static ImagePanel showWindow( ImageSInt16 img , String title ) {
		int max = ImageStatistics.maxAbs(img);
		BufferedImage buff;
		if( img.getDataType().isSigned() )
			buff = VisualizeImageData.colorizeSign(img,null,max);
		else
			buff = VisualizeImageData.grayUnsigned(img,null,max);

		return showWindow(buff,title);
	}

	public static ImagePanel showWindow( ImageFloat32 img , String title , boolean showMagnitude) {
		float max = ImageStatistics.maxAbs(img);
		BufferedImage buff;
		if( showMagnitude )
			buff = VisualizeImageData.grayMagnitude(img,null,max);
		else
			buff = VisualizeImageData.colorizeSign(img,null,max);

		return showWindow(buff,title);
	}

	public static JFrame showWindow( final JComponent component , String title ) {
		final JFrame frame = new JFrame(title);
		frame.add(component, BorderLayout.CENTER);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});

		return frame;
	}
}
