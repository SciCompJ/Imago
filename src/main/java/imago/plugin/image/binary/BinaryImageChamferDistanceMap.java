/**
 * 
 */
package imago.plugin.image.binary;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.scalar.Int32Array;
import net.sci.array.scalar.IntArray;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DInt;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DInt;
import net.sci.image.binary.distmap.ChamferMask2D;
import net.sci.image.binary.distmap.ChamferMask3D;
import net.sci.image.binary.distmap.ChamferMasks2D;
import net.sci.image.binary.distmap.ChamferMasks3D;
import net.sci.image.binary.distmap.DistanceTransform2D;
import net.sci.image.binary.distmap.DistanceTransform3D;

/**
 * Distance map to nearest background pixel/voxel, using chamfer distances.
 * 
 * @author David Legland
 *
 */
public class BinaryImageChamferDistanceMap implements FramePlugin
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
		if (nd != 2 && nd != 3)
		{
			frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
			return;
		}
		
		// build dialog for choosing options
		GenericDialog gd = new GenericDialog(frame, "Distance Map");
		if (nd == 2)
		{
            gd.addChoice("Chamfer Weights: ", ChamferMasks2D.getAllLabels(), ChamferMasks2D.CHESSKNIGHT.toString());
		}
		else if (nd == 3)
		{
            gd.addChoice("Chamfer Weights: ", ChamferMasks3D.getAllLabels(), ChamferMasks3D.SVENSSON_3_4_5_7.toString());
		}
        gd.addChoice("Output Type: ", new String[]{"8-bits", "16-bits", "32-bits", "32-bits float"}, "16-bits");
        gd.addCheckBox("Normalize", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
		{
			return;
		}
		
		// parse dialog results
		String weightsName = gd.getNextChoice();
		int bitDepthIndex = gd.getNextChoiceIndex();
		boolean normalize = gd.getNextBoolean();
		
		// Compute distance map
		ScalarArray<?> result;
		if (nd == 2)
		{
            ChamferMask2D weights = ChamferMasks2D.fromLabel(weightsName).getMask();
		    DistanceTransform2D op;
		    if (bitDepthIndex < 3)
		    {
		        op = new ChamferDistanceTransform2DInt(weights, normalize); 
		        IntArray.Factory<?> factory = chooseIntArrayFactory(bitDepthIndex);
		        ((ChamferDistanceTransform2DInt) op).setFactory(factory);
		    }
		    else
		    {
                op = new ChamferDistanceTransform2DFloat32(weights, normalize); 
		    }
		    op.addAlgoListener(imageFrame);
		    result = op.process2d((BinaryArray2D) image.getData());
		}
		else
		{
		    // Process 3D case
            ChamferMask3D weights = ChamferMasks3D.fromLabel(weightsName).getMask();
            DistanceTransform3D op;
            if (bitDepthIndex < 3)
            {
                op = new ChamferDistanceTransform3DInt(weights, normalize); 
                IntArray.Factory<?> factory = chooseIntArrayFactory(bitDepthIndex);
                ((ChamferDistanceTransform3DInt) op).setFactory(factory);
            }
            else
            {
                op = new ChamferDistanceTransform3DFloat32(weights, normalize); 
            }
            op.addAlgoListener(imageFrame);
            result = op.process3d((BinaryArray3D) image.getData());
		}
		Image resultImage = new Image(result, ImageType.DISTANCE, image);
		
		// add the image document to GUI
		imageFrame.createImageFrame(resultImage);
	}
	
	private IntArray.Factory<?> chooseIntArrayFactory(int bitDepthIndex)
	{
        if (bitDepthIndex == 0) return UInt8Array.defaultFactory;
        if (bitDepthIndex == 1) return UInt16Array.defaultFactory;
        if (bitDepthIndex == 2) return Int32Array.defaultFactory;
        throw new RuntimeException("Unknown IntArray factory index");
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
