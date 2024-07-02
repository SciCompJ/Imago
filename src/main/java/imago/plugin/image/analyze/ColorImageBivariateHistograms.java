/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.Int32Array2D;
import net.sci.image.Image;

/**
 * Computes the three bivariate histograms of a RGB8 image.
 * 
 * @author David Legland
 *
 */
public class ColorImageBivariateHistograms implements FramePlugin
{
	public ColorImageBivariateHistograms()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (!(array instanceof RGB8Array))
		{
			return;
		}
		
		Int32Array2D RGHist = Int32Array2D.create(256, 256);
		Int32Array2D RBHist = Int32Array2D.create(256, 256);
		Int32Array2D GBHist = Int32Array2D.create(256, 256);
		
		RGB8Array.Iterator iterRGB = ((RGB8Array) array).iterator();
		
		while (iterRGB.hasNext())
		{
			int[] rgb = iterRGB.next().getSamples();
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			RGHist.setInt(RGHist.getInt(r, g) + 1, r, g);		
			RBHist.setInt(RBHist.getInt(r, b) + 1, r, b);		
			GBHist.setInt(GBHist.getInt(g, b) + 1, g, b);		
		}
		
		// apply operator on current image
		String name = image.getName();
		Image rgImage = new Image(RGHist, image);
		rgImage.setName(name + "-RG");
		Image rbImage = new Image(RBHist, image);
		rbImage.setName(name + "-RB");
		Image gbImage = new Image(GBHist, image);
		gbImage.setName(name + "-GB");
		
		// add the image documents to GUI
        ImageFrame.create(rgImage, frame);
        ImageFrame.create(rbImage, frame);
        ImageFrame.create(gbImage, frame);
	}

}
