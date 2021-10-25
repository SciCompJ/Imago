/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.color.RGB8;
import net.sci.image.ColorMaps;
import net.sci.image.DisplaySettings;
import net.sci.image.Image;
import net.sci.image.binary.ChamferWeights2D;
import net.sci.image.binary.ChamferWeights3D;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DUInt16;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DUInt16;
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
	public BinaryImageChamferDistanceMap()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * imago.gui.Plugin#run(ImagoFrame, String)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("Chamfer distance map");

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();
		if (!(array instanceof BinaryArray))
		{
			frame.showErrorDialog("Requires a binary image input", "Data Type Error");
			return;
		}

		int nd = array.dimensionality();
		if (nd != 2 && nd != 3)
		{
			frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
			return;
		}
		
		GenericDialog gd = new GenericDialog(frame, "Distance Map");
		if (nd == 2)
		{
            gd.addChoice("Chamfer Weights: ", ChamferWeights2D.getAllLabels(), ChamferWeights2D.CHESSKNIGHT.toString());
		}
		else if (nd == 3)
		{
            gd.addChoice("Chamfer Weights: ", ChamferWeights3D.getAllLabels(), ChamferWeights3D.WEIGHTS_3_4_5_7.toString());
		}
        gd.addChoice("Output Type: ", new String[]{"16-bits", "32-bits float"}, "16-bits");
        gd.addCheckBox("Normalize: ", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
		{
			return;
		}
		
		// parse dialog results
		String weightsName = gd.getNextChoice();
		int bitDepthIndex = gd.getNextChoiceIndex();
		boolean normalize = gd.getNextBoolean();
		
		// apply connected components labeling
		Array<?> result;
		if (nd == 2)
		{
            ChamferWeights2D weights = ChamferWeights2D.fromLabel(weightsName);
		    DistanceTransform2D op;
		    if (bitDepthIndex == 0)
		    {
		        op = new ChamferDistanceTransform2DUInt16(weights, normalize); 
		    }
		    else
		    {
                op = new ChamferDistanceTransform2DFloat32(weights, normalize); 
		    }
		    op.addAlgoListener((ImageFrame) frame);
		    result = op.process2d((BinaryArray2D) image.getData());
		}
		else
		{
		    // Process 3D case
            ChamferWeights3D weights = ChamferWeights3D.fromLabel(weightsName);
            DistanceTransform3D op;
            if (bitDepthIndex == 0)
            {
                op = new ChamferDistanceTransform3DUInt16(weights, normalize); 
            }
            else
            {
                op = new ChamferDistanceTransform3DFloat32(weights, normalize); 
            }
            op.addAlgoListener((ImageFrame) frame);
            result = op.process3d((BinaryArray3D) image.getData());
		}
		Image resultImage = new Image(result, image);
		
		// compute JET lut by default
		DisplaySettings settings = resultImage.getDisplaySettings();
		settings.setColorMap(ColorMaps.JET.createColorMap(255));
		settings.setBackgroundColor(RGB8.WHITE);
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage);
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
