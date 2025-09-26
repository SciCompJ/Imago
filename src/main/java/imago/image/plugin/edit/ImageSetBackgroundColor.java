/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
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
        ImageHandle handle = viewer.getImageHandle();
        Image image = handle.getImage();

        GenericDialog gd = new GenericDialog(frame, "Choose Background Color");
        gd.addChoice("Background Color:", CommonColors.all(), CommonColors.BLACK);
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL) return;

        // parse dialog results
        Color bgColor = CommonColors.fromLabel(gd.getNextChoice()).getColor();

        // update image
        image.getDisplaySettings().setBackgroundColor(bgColor);
        
        // notify associated viewers
		handle.notifyImageHandleChange(ImageHandle.Event.LUT_MASK | ImageHandle.Event.CHANGE_MASK);
	}
}
