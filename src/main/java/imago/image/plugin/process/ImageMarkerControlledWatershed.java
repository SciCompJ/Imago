/**
 * 
 */
package imago.image.plugin.process;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.Arrays;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.array.binary.BinaryArray3D;
import net.sci.array.numeric.IntArray2D;
import net.sci.array.numeric.IntArray3D;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.image.binary.BinaryImages;
import net.sci.image.connectivity.Connectivity2D;
import net.sci.image.connectivity.Connectivity3D;
import net.sci.image.morphology.watershed.MeyerMarkerControlledWatershed2D;
import net.sci.image.morphology.watershed.MeyerMarkerControlledWatershed3D;

/**
 * 
 */
public class ImageMarkerControlledWatershed implements FramePlugin
{

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
        GenericDialog gd = new GenericDialog(frame, "Marker Controlled Watershed");
        gd.addChoice("Relief Image: ", imageNameArray, firstImageName);
        gd.addChoice("Marker Image: ", imageNameArray, firstImageName);
        gd.showDialog();
        
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog results
        Image reliefImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        Image markerImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();

        // extract arrays and check dimensions
        Array<?> relief = reliefImage.getData();
        Array<?> marker = markerImage.getData();
        if (!Arrays.isSameSize(marker, relief))
        {
            ImagoGui.showErrorDialog(frame, "Both arrays must have same dimensions", "Dimension Error");
            return;
        }
        
        // check types
        if (marker.elementClass() != Binary.class)
        {
            ImagoGui.showErrorDialog(frame, "Marker image must be binary", "Type Error");
            return;
        }
        
        Image resultImage;
        if (marker.dimensionality() == 2)
        {
            // convert data to 2D
            Connectivity2D conn = Connectivity2D.C4;
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ScalarArray2D relief2d = ScalarArray2D.wrapScalar2d(ScalarArray.wrap((Array<Scalar>) relief));
            BinaryArray2D marker2d = BinaryArray2D.wrap(BinaryArray.wrap(marker));
            
            // compute labels of markers
            long t0 = System.nanoTime();
            IntArray2D<?> labelMap2d = BinaryImages.componentsLabeling(marker2d, conn, 32);
            
            // process watershed with markers using default connectivity
            MeyerMarkerControlledWatershed2D algo = new MeyerMarkerControlledWatershed2D(conn);
            algo.processInPlace(relief2d, labelMap2d);
            long t1 = System.nanoTime();
            
            if (frame instanceof ImageFrame)
            {
                ((ImageFrame) frame).showElapsedTime("Watershed", (t1 - t0) / 1_000_000.0, reliefImage);
            }
            
            resultImage = new Image(labelMap2d, ImageType.LABEL);
            resultImage.setName(markerImage.getName() + "-ws");
        }
        else if (marker.dimensionality() == 3)
        {
            // convert data to 2D
            Connectivity3D conn = Connectivity3D.C6;
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ScalarArray3D relief2d = ScalarArray3D.wrapScalar3d(ScalarArray.wrap((Array<Scalar>) relief));
            BinaryArray3D marker3d = BinaryArray3D.wrap(BinaryArray.wrap(marker));

            // compute labels of markers
            long t0 = System.nanoTime();
            IntArray3D<?> labelMap2d = BinaryImages.componentsLabeling(marker3d, conn, 32);

            // process watershed with markers using default connectivity
            MeyerMarkerControlledWatershed3D algo = new MeyerMarkerControlledWatershed3D(conn);
            algo.processInPlace(relief2d, labelMap2d);
            long t1 = System.nanoTime();
            
            if (frame instanceof ImageFrame)
            {
                ((ImageFrame) frame).showElapsedTime("Watershed", (t1 - t0) / 1_000_000.0, reliefImage);
            }
            
            resultImage = new Image(labelMap2d, ImageType.LABEL);
            resultImage.setName(markerImage.getName() + "-ws");
        }
        else
        {
            throw new RuntimeException("Only implemented for dimensions 2 and 3");
        }
        
        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

}
