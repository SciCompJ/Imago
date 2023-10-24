/**
 * 
 */
package imago.plugin.image.shape;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.process.shape.Rotate3D90;
import net.sci.image.Image;

/**
 * Apply a 3D rotation around one of the main axes to a given image.
 * 
 * @author David Legland
 *
 */
public class Image3DRotate90 implements FramePlugin
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
		// get current image data
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image	= imageFrame.getImageHandle().getImage();

		GenericDialog gd = new GenericDialog(frame, "Rotate 3D by 90 degrees");
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
        Image result = imageFrame.runOperator(algo, image);
		result.setName(image.getName() + "-rot90");
		
		// add the image document to GUI
		imageFrame.createImageFrame(result);
	}
}
