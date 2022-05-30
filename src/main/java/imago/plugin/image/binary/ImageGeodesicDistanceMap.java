/**
 * 
 */
package imago.plugin.image.binary;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Arrays;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16;
import net.sci.array.scalar.UInt16Array;
import net.sci.image.Image;
import net.sci.image.binary.distmap.ChamferMask2D;
import net.sci.image.binary.distmap.ChamferMask3D;
import net.sci.image.binary.distmap.ChamferMasks2D;
import net.sci.image.binary.distmap.ChamferMasks3D;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform2D;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform2DFloat32Hybrid;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform2DUInt16Hybrid;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform3D;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform3DFloat32Hybrid;
import net.sci.image.binary.geoddist.GeodesicDistanceTransform3DUInt16Hybrid;

/**
 * Computes geodesic distance map of a marker image constrained by a mask image.
 * 
 * @author David Legland
 *
 */
public class ImageGeodesicDistanceMap implements FramePlugin
{
	public ImageGeodesicDistanceMap()
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
		System.out.println("geodesic distance map");

		ImagoGui gui = frame.getGui();
		ImagoApp app = gui.getAppli();
		Collection<String> imageNames = app.getImageHandleNames();

		// Case of no open document with image
		if (imageNames.size() == 0)
		{
			return;
		}
		
        // retrieve list of images
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = imageNameArray[0];
                
		// choose dimensionality: either the one from current image, or a default one
		int nd = 2;
		ImageFrame iFrame = null;
		if (frame instanceof ImageFrame)
		{
		    iFrame = (ImageFrame) frame;
		    nd = iFrame.getImageHandle().getImage().getDimension();
		}
		
		// Creates the dialog
		GenericDialog gd = new GenericDialog(frame, "Geod. Dist. Map");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
        if (nd == 2)
        {
            gd.addChoice("Chamfer Mask: ", ChamferMasks2D.getAllLabels(), ChamferMasks2D.CHESSKNIGHT.toString());
        }
        else if (nd == 3)
        {
            gd.addChoice("Chamfer Mask: ", ChamferMasks3D.getAllLabels(), ChamferMasks3D.SVENSSON_3_4_5_7.toString());
        }
        gd.addChoice("Output Type: ", new String[]{"16-bits integer", "32-bits float"}, "16-bits integer");
        gd.addCheckBox("Normalize ", true);
        gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse user choices
		Image markerImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		Image maskImage = app.getImageHandleFromName(gd.getNextChoice()).getImage();
		String weightsName = gd.getNextChoice();
		int bitDepthIndex = gd.getNextChoiceIndex();
		boolean normalize = gd.getNextBoolean();

        // check input type 
		if (!(markerImage.getData() instanceof BinaryArray) || !(maskImage.getData() instanceof BinaryArray) )
		{
			frame.showErrorDialog("Both arrays must be binary", "Image Type Error");
			return;
		}
        BinaryArray marker = BinaryArray.wrap(markerImage.getData());
        BinaryArray mask = BinaryArray.wrap(maskImage.getData());
        
		// check inputs dimensionality and size 
		if (!Arrays.isSameDimensionality(marker, mask))
		{
			frame.showErrorDialog("Both arrays must have same dimensionality", "Dimensionality Error");
			return;
		}
		
		if (!Arrays.isSameSize(marker, mask))
		{
			frame.showErrorDialog("Both arrays must have same size", "Image Size Error");
			return;
		}
		if (marker.dimensionality() != nd)
		{
            frame.showErrorDialog("Chamfer mask dimensionality must match that of input images", "Dimensionality Error");
            return;
		}
		
		// declare result variables
		ScalarArray<?> result;
		double timeInMillis = 0;
		
		// dispatch process depending on dimensionality
		if (nd == 2)
		{
		    // cast dimensions
            BinaryArray2D marker2d = BinaryArray2D.wrap(marker);
            BinaryArray2D mask2d = BinaryArray2D.wrap(mask);
            ChamferMask2D chamferMask = ChamferMasks2D.fromLabel(weightsName).getMask();
            
            // create algorithm depending on output data type
            GeodesicDistanceTransform2D algo;
            if (bitDepthIndex == 0)
            {
                // compute using 16-bits integers
                algo = new GeodesicDistanceTransform2DUInt16Hybrid(chamferMask, normalize);
            }
            else
            {
                // compute using 32-bits floating-points
                algo = new GeodesicDistanceTransform2DFloat32Hybrid(chamferMask, normalize);
            }
            
            // run algorithm using progress monitoring
            if (iFrame != null)
            {
                algo.addAlgoListener(iFrame);
            }
            long t0 = System.nanoTime();
            result = algo.process2d(marker2d, mask2d);
            timeInMillis = (System.nanoTime() - t0) / 1_000_000.0;
		}
		else if (nd == 3)
		{
            // cast dimensions
            BinaryArray3D marker3d = BinaryArray3D.wrap(marker);
            BinaryArray3D mask3d = BinaryArray3D.wrap(mask);
            ChamferMask3D chamferMask = ChamferMasks3D.fromLabel(weightsName).getMask();
            
            // create algorithm depending on output data type
            GeodesicDistanceTransform3D algo;
            if (bitDepthIndex == 0)
            {
             // compute using 16-bits integer
                algo = new GeodesicDistanceTransform3DUInt16Hybrid(chamferMask, normalize);
            }
            else
            {
                // compute using 32-bits floating-points
                algo = new GeodesicDistanceTransform3DFloat32Hybrid(chamferMask, normalize);
            }
            
            // run algorithm using progress monitoring
            if (iFrame != null)
            {
                algo.addAlgoListener(iFrame);
            }
            long t0 = System.nanoTime();
            result = algo.process3d(marker3d, mask3d);
            timeInMillis = (System.nanoTime() - t0) / 1_000_000.0;
		}
		else
		{
            frame.showErrorDialog("Input images dimension must be either 2 or 3", "Dimensionality Error");
            return;
		}
		
        // run algorithm using progress monitoring
        if (iFrame != null)
        {
            iFrame.showElapsedTime("Geod. Dist. Map", timeInMillis, markerImage); 
        }
        
		// Create result image
		Image resultImage = new Image(result, markerImage);
		resultImage.setName(markerImage.getName() + "-geodDist");
		
		// compute maximum (finite) distance value within mask image
		double maxDist = 0;
		if (bitDepthIndex == 0)
		{
		    for (int dist : mask.selectInts((UInt16Array) result))
		    {
                if (dist < UInt16.MAX_VALUE)
                {
                    maxDist = Math.max(dist, maxDist);
                }		        
		    }
		}
		else
		{
		    for (double dist : mask.selectValues(result))
		    {
                if (Double.isFinite(dist))
                {
                    maxDist = Math.max(dist, maxDist);
                }
		    }
		}
		resultImage.getDisplaySettings().setDisplayRange(new double[] {0, maxDist});
		
        // add the image document to GUI
        frame.createImageFrame(resultImage);
	}
}
