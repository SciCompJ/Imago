/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalReconstruction;


/**
 * Kills the borders of the current binary / grayscale image.
 * 
 * @author David Legland
 *
 */
public class ImageKillBorders implements FramePlugin
{
	public ImageKillBorders()
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
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
		Image resultImage = MorphologicalReconstruction.killBorders(image);
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
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
