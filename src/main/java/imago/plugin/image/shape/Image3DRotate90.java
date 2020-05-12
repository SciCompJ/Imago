/**
 * 
 */
package imago.plugin.image.shape;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.process.shape.Rotate3D90;
import net.sci.image.Image;

/**
 * Apply a 3D rotation around one of the main axes to a given image.
 * 
 * @author David Legland
 *
 */
public class Image3DRotate90 implements Plugin
{
	public Image3DRotate90()
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
		System.out.println("Apply 3D rotation to 3D image");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();

		GenericDialog gd = new GenericDialog(frame, "Rotate 3D by 90°");
        gd.addNumericField("Rotation Axis ",  0, 0);
        gd.addNumericField("Rotation number ", 1, 0);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
        int axisIndex = (int) gd.getNextNumber();
        int rotationNumber = (int) gd.getNextNumber();

        Rotate3D90 algo = new Rotate3D90(axisIndex, rotationNumber); 
        Array<?> res = algo.process(image.getData());
        
        Image result = new Image(res, image);
		result.setName(image.getName() + "-rot90");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(result);
	}
}
