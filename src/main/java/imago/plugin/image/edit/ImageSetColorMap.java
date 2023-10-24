/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.color.ColorMap;
import net.sci.image.Image;

/**
 * Choose the colormap of the current scalar image.
 * 
 * @author David Legland
 *
 */
public class ImageSetColorMap implements FramePlugin
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
		// get current image data
		ImageFrame viewer = (ImageFrame) frame;
		ImageHandle doc = viewer.getImageHandle();
		Image image	= doc.getImage();

		image.getDisplaySettings().setColorMap(this.colorMap);
		//TODO: notify change ?
		
		viewer.getImageView().refreshDisplay();
		
		viewer.repaint();
	}
}
