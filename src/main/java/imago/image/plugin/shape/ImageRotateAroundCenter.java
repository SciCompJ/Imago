/**
 * 
 */
package imago.image.plugin.shape;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.shape.RotationAroundCenter;

/**
 * Rotate image around its center by an angle given in degrees.
 * 
 * @author David Legland
 *
 */
public class ImageRotateAroundCenter implements FramePlugin
{
	public ImageRotateAroundCenter()
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

		if (array.dimensionality() != 2)
		{
		    ImagoGui.showErrorDialog(frame, "Requires an image with 2D array");
		    return;
		}
		
		GenericDialog gd = new GenericDialog(frame, "Rotate around center");
		gd.addNumericField("Rotation angle (degrees): ", 30, 1);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		double angle = gd.getNextNumber();
		
		// create operator box filtering operator
		RotationAroundCenter op = new RotationAroundCenter(angle); 
		
		// apply operator on current image
		Image result = new Image(op.process(array), image);
		result.setName(image.getName() + "-rot");
		
		// add the image document to GUI
		ImageFrame.create(result, frame);
	}
	
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.getData().dimensionality() == 2;
    }
}
