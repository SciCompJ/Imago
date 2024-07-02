/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalReconstruction;


/**
 * Fill the holes in the current binary / grayscale image.
 * 
 * @author David Legland
 *
 */
public class ImageFillHoles implements FramePlugin 
{
	public ImageFillHoles() 
	{
	}
	
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray2D) && !(array instanceof ScalarArray3D))
		{
			return;
		}
		Image resultImage = MorphologicalReconstruction.fillHoles(image);
		
		// add the image document to GUI
		ImageFrame.create(resultImage, frame);
	}

    /**
     * Returns true if the current frame contains a scalar image.
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

        return image.isScalarImage();
    }
}
