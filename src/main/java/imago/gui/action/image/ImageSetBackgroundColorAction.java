/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImageViewer;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.type.Color;
import net.sci.array.type.CommonColors;
import net.sci.image.Image;

/**
 * Changes the background color (used for display of labels) for this image.
 * 
 * @author David Legland
 *
 */
public class ImageSetBackgroundColorAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageSetBackgroundColorAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("image set background color");

		// get current image data
		ImagoDocViewer viewer = (ImagoDocViewer) this.frame;
		ImagoDoc doc = viewer.getDocument();
		Image image	= doc.getImage();

		GenericDialog gd = new GenericDialog(this.frame, "Set Image Scale");
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
