/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.color.Color;
import net.sci.array.color.CommonColors;
import net.sci.image.Image;

/**
 * Changes the background color (used for display of labels) for this image.
 * 
 * @author David Legland
 *
 */
public class ImageSetBackgroundColor implements FramePlugin
{
	public ImageSetBackgroundColor()
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
		ImageFrame viewer = (ImageFrame) frame;
		ImageHandle doc = viewer.getImageHandle();
		Image image	= doc.getImage();

		GenericDialog gd = new GenericDialog(frame, "Set Image Scale");
        gd.addChoice("Background Color:", CommonColors.all(), CommonColors.BLACK);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
        // parse dialog results
		Color bgColor = CommonColors.fromLabel(gd.getNextChoice()).getColor();
		image.getDisplaySettings().setBackgroundColor(bgColor);

		ImageViewer imageViewer = viewer.getImageView();
		imageViewer.refreshDisplay();
		viewer.repaint();
	}

}
