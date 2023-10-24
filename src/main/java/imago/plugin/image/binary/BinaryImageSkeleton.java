/**
 * 
 */
package imago.plugin.image.binary;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray2D;
import net.sci.image.Image;
import net.sci.image.binary.skeleton.ImageJSkeleton;


/**
 * Compute the skeleton of the current biary image.
 * 
 * Uses ImageJ's algorithm.
 * 
 * @author David Legland
 *
 */
public class BinaryImageSkeleton implements FramePlugin
{
	public BinaryImageSkeleton() 
	{
	}
	
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray2D))
		{
			return;
		}
		
		ImageJSkeleton skel = new ImageJSkeleton();
        BinaryArray2D res = skel.process2d((BinaryArray2D) array);

        Image resultImage = new Image(res, image);
        
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
