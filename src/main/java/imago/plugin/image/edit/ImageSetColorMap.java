/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.ColorMap;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageSetColorMap implements Plugin
{
    ColorMap colorMap;
    
	public ImageSetColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
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
		System.out.println("image set colormap");

		// get current image data
		ImageFrame viewer = (ImageFrame) frame;
		ImageHandle doc = viewer.getDocument();
		Image image	= doc.getImage();

		image.getDisplaySettings().setColorMap(this.colorMap);
		//TODO: notify change ?
		
		viewer.getImageView().refreshDisplay();
		
		viewer.repaint();
	}
}
