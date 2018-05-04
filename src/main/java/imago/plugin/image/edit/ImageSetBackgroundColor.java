/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.type.Color;
import net.sci.array.type.CommonColors;
import net.sci.image.Image;

/**
 * Changes the background color (used for display of labels) for this image.
 * 
 * @author David Legland
 *
 */
public class ImageSetBackgroundColor implements Plugin
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
		System.out.println("image set background color");

		// get current image data
		ImagoDocViewer viewer = (ImagoDocViewer) frame;
		ImagoDoc doc = viewer.getDocument();
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
		image.setBackgroundColor(bgColor);

		ImageViewer imageViewer = viewer.getImageView();
		imageViewer.refreshDisplay();
		viewer.repaint();
	}

}
