/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.image.Image;
import net.sci.image.morphology.reconstruction.RunLengthBinaryReconstruction2D;
import net.sci.image.morphology.reconstruction.RunLengthBinaryReconstruction3D;

/**
 * Applies binary morphological reconstruction of a marker image constrained by a mask image.
 * 
 * Works for 2D pairs of images.
 * 
 * @author David Legland
 *
 */
public class BinaryImageMorphologicalReconstruction implements FramePlugin
{
    
    private final static String[] connNameArray = new String[] {"Min", "Full"};
    private final static int[] conn3dArray = new int[] {6, 26};
    private final static int[] conn2dArray = new int[] {4, 8};
    
	public BinaryImageMorphologicalReconstruction()
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
		ImagoGui gui = frame.getGui();
		ImagoApp app = gui.getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Morphological Reconstruction");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.addChoice("Connectivity: ", connNameArray, connNameArray[0]);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// parse dialog results
		
		Image markerImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
		Image maskImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
		int connIndex = gd.getNextChoiceIndex();

		// extract arrays and check dimensions
		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
        if (!Arrays.isSameSize(marker, mask))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays should have same dimensions", "Dimension Error");
            return;
        }
		
        // check arrays are binary
        if (!(marker instanceof BinaryArray) || !(mask instanceof BinaryArray))
        {
            ImagoGui.showErrorDialog(frame, "Input arrays must be binary", "Array Type Error");
            return;
        }
        BinaryArray result;
        
        long t0 = System.nanoTime();
        if (marker.dimensionality() == 2 && mask.dimensionality() == 2)
        {
            int conn = conn2dArray[connIndex];
            RunLengthBinaryReconstruction2D algo = new RunLengthBinaryReconstruction2D(conn);
            result = algo.processBinary2d(BinaryArray2D.wrap((BinaryArray) marker), BinaryArray2D.wrap((BinaryArray) mask));
        }
        else if (marker.dimensionality() == 3 && mask.dimensionality() == 3)
        {
            int conn = conn3dArray[connIndex];
            RunLengthBinaryReconstruction3D algo = new RunLengthBinaryReconstruction3D(conn);
            result = algo.processBinary3d(BinaryArray3D.wrap((BinaryArray) marker), BinaryArray3D.wrap((BinaryArray) mask));
        }
        else
        {
            throw new RuntimeException("Can process only dimensions 2 or 3.");
        }
        long t1 = System.nanoTime();
        double dt = (t1 - t0) / 1_000_000.0;
        if (frame instanceof ImageFrame)
        {
            ((ImageFrame) frame).showElapsedTime("Binary Morpho Rec.", dt, maskImage);
        }
        
        // encapsulate into image
		Image resultImage = new Image(result, maskImage);
		resultImage.setName(markerImage.getName() + "-morphoRec");
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
