/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.process.RotationAroundCenter;

/**
 * Rotate image around its center by an angle given in degrees.
 * 
 * @author David Legland
 *
 */
public class ImageRotateAroundCenter implements Plugin
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
		System.out.println("rotate around center");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
		frame.getGui().addNewDocument(result);
	}
	
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImagoDocViewer))
            return false;
        
        // check image
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.getData().dimensionality() == 2;
    }
}