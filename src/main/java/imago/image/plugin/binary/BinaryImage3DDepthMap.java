/**
 * 
 */
package imago.image.plugin.binary;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.image.binary.distmap.DepthMap3D;

/**
 * Distance map to nearest foreground voxel, in one of the three main image
 * directions.
 * 
 * @author David Legland
 *
 */
public class BinaryImage3DDepthMap implements FramePlugin
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * imago.gui.Plugin#run(ImagoFrame, String)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // retrieve current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();
		
		// check type of input
		if (!(array instanceof BinaryArray))
		{
			frame.showErrorDialog("Requires a binary image input", "Data Type Error");
			return;
		}

        // also check dimensionality
		int nd = array.dimensionality();
		if (nd != 3)
		{
			frame.showErrorDialog("Can process only 3D images", "Dimensionality Error");
			return;
		}
		
		// build dialog for choosing options
		GenericDialog gd = new GenericDialog(frame, "Depth Map 3D");
		gd.addChoice("Direction: ", new String[] {"X", "Y", "Z"}, "Z");
		gd.showDialog();
		
		if (gd.wasCanceled())
		{
			return;
		}
		
		// parse dialog results
		int dirIndex = gd.getNextChoiceIndex();
		
		// Compute depth map
		DepthMap3D algo = new DepthMap3D(dirIndex);
		algo.addAlgoListener(imageFrame);
        long t0 = System.currentTimeMillis();
		ScalarArray<?> result = algo.process(image.getData());
		long t1 = System.currentTimeMillis();
		imageFrame.showElapsedTime("Depth Map 3D", (t1 - t0), image);
		
		Image resultImage = new Image(result, ImageType.DISTANCE, image);
		
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
