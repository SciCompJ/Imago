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
import net.sci.array.scalar.IntArray;
import net.sci.array.scalar.ScalarArray;
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
		
		// default values for chamfer masks in 2D and 3D
        ChamferMasks2D mask2d = ChamferMasks2D.CHESSKNIGHT;
        ChamferMasks3D mask3d = ChamferMasks3D.SVENSSON_3_4_5_7;
		
		// build dialog for choosing options
		GenericDialog gd = new GenericDialog(frame, "Distance Map");
		if (nd == 2)
		{
            gd.addEnumChoice("Chamfer Mask: ", ChamferMasks2D.class, mask2d);
		}
		else if (nd == 3)
		{
            gd.addEnumChoice("Chamfer Mask: ", ChamferMasks3D.class, mask3d);
		}
        gd.addEnumChoice("Output Type: ", DistanceMapDataType.class, DistanceMapDataType.UINT16);
        gd.addCheckBox("Normalize", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
		{
			return;
		}
		
		// parse dialog results
		switch (nd)
		{
		    case 2 -> mask2d = (ChamferMasks2D) gd.getNextEnumChoice();
		    case 3 -> mask3d = (ChamferMasks3D) gd.getNextEnumChoice();
		};
		DistanceMapDataType outputType = (DistanceMapDataType) gd.getNextEnumChoice();
		boolean normalize = gd.getNextBoolean();
		
		// Compute distance map
		ScalarArray<?> result;
		if (nd == 2)
		{
            // Process 3D case
            DistanceTransform2D op = createAlgorithm(mask2d.getMask(), outputType, normalize);
		    op.addAlgoListener(imageFrame);
		    result = op.process2d((BinaryArray2D) image.getData());
		}
		else
		{
		    // Process 3D case
            DistanceTransform3D op = createAlgorithm(mask3d.getMask(), outputType, normalize);
            op.addAlgoListener(imageFrame);
            result = op.process3d((BinaryArray3D) image.getData());
		}
		Image resultImage = new Image(result, ImageType.DISTANCE, image);
		resultImage.setName(image.getName() + "-dist");
		
		// add the image document to GUI
		imageFrame.createImageFrame(resultImage);
	}
	
    private static final DistanceTransform2D createAlgorithm(ChamferMask2D mask, DistanceMapDataType outputType, boolean normalize)
    {
        if (!outputType.isIntType())
        {
            return new ChamferDistanceTransform2DFloat32(mask, normalize);
        }
        ChamferDistanceTransform2DInt op = new ChamferDistanceTransform2DInt(mask, normalize); 
        op.setFactory((IntArray.Factory<?>) outputType.factory());
        return op;
    }
    
	private static final DistanceTransform3D createAlgorithm(ChamferMask3D mask, DistanceMapDataType outputType, boolean normalize)
	{
	    if (!outputType.isIntType())
	    {
	        return new ChamferDistanceTransform3DFloat32(mask, normalize);
	    }
	    ChamferDistanceTransform3DInt op = new ChamferDistanceTransform3DInt(mask, normalize); 
        op.setFactory((IntArray.Factory<?>) outputType.factory());
        return op;
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
