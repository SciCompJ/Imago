/**
 * 
 */
package imago.image.plugin.binary;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.image.Image;
import net.sci.image.morphology.reconstruction.BinaryKillBorders;


/**
 * Kill borders within a binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImageKillBorders implements FramePlugin
{
	public BinaryImageKillBorders() 
	{
	}
	
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageFrame imageFrame = (ImageFrame) frame; 
		Image image = imageFrame.getImageHandle().getImage();

		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray))
		{
			return;
		}
		
		BinaryKillBorders algo = new BinaryKillBorders();
		Image resultImage = imageFrame.runOperator(algo, image);
		
        resultImage.setName(image.getName() + "-killBorders");
        
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}

    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
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

        return image.isBinaryImage();
    }
}
