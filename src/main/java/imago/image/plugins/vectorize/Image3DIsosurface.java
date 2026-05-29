/**
 * 
 */
package imago.image.plugins.vectorize;

import java.awt.Color;
import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.shape.Style;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.geom.mesh3d.DefaultTriMesh3D;
import net.sci.image.Image;
import net.sci.image.vectorize.MorphologicalMarchingCubes;

/**
 * Computes isosurface of the current 3D scalar image. Result can be dispatched
 * on image XY slices, or added into the ShapeManager.
 * 
 * @author dlegland
 *
 */
public class Image3DIsosurface implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();
        Image image = handle.getImage();
        ImagoApp app = frame.getGui().getAppli();
        
        // check input data type and dimensions
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            frame.showErrorDialog("Requires a scalar image input", "Data Type Error");
            return;
        }
        int nd = array.dimensionality();
        if (nd != 3)
        {
            frame.showErrorDialog("Requires a 3D image", "Dimensionality Error");
            return;
        }

        // wrap array into a 3D scalar array
        ScalarArray3D<?> scalar = ScalarArray3D.wrap((ScalarArray<?>) array);
        

        // Open a dialog to choose the iso-surface value
        GenericDialog dlg = new GenericDialog(frame, "Isosurface");
        double[] extent = image.getDisplaySettings().getDisplayRange(); 
        dlg.addSlider("Isosurface Value", extent[0], extent[1], (extent[0] + extent[1]) / 2);
        dlg.addCheckBox("Add to Image shapes", true);
        Collection<String> imageNames = ImageHandle.getAllNames(app);
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = handle.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.addCheckBox("Add to Shape Manager", false);
        dlg.showDialog();
        
        if (dlg.wasCanceled())
            return;
        
        // retrieve user choices
        double value = dlg.getNextNumber();
        boolean addToImage = dlg.getNextBoolean();
        String imageToOverlayName = dlg.getNextChoice();
        boolean addToShapeManager = dlg.getNextBoolean();

        
        // Call the morphological marching cube algorithm to compute a triangular mesh
        MorphologicalMarchingCubes mc = new MorphologicalMarchingCubes(value);
        
        // initialize algo monitoring
        mc.addAlgoListener(frame);
        iFrame.getStatusBar().setCurrentStepLabel("Compute Isosurface");
        
        // run isosurface computation
        long t0 = System.nanoTime();
        DefaultTriMesh3D mesh = DefaultTriMesh3D.convert(mc.process(scalar));
        
        // cleanup listener and status bar
        iFrame.getStatusBar().setProgressBarPercent(0);

        if (addToImage)
        {
            iFrame.getStatusBar().setCurrentStepLabel("Add to image shape tree");
            ImageHandle targetHandle = ImageHandle.findFromName(app, imageToOverlayName);

            Style style = new Style().setLineWidth(2.5).setLineColor(Color.MAGENTA);
            targetHandle.addGeometryNode("isosurface", mesh, style);
            targetHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
        }
        
        // display elapsed time
        long t1 = System.nanoTime();
        iFrame.getStatusBar().setCurrentStepLabel("");
        iFrame.showElapsedTime("Compute Isosurface", (t1 - t0) / 1_000_000.0, image);
        
        if (addToShapeManager)
        {
            GeometryHandle geomHandle = GeometryHandle.create(app, mesh);
            
            // opens a dialog to choose name
            String name = geomHandle.getName();
            name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
    }
}
