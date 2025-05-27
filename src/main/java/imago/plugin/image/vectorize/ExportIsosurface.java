/**
 * 
 */
package imago.plugin.image.vectorize;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.io.OffMeshWriter;
import net.sci.image.Image;
import net.sci.image.vectorize.MorphologicalMarchingCubes;

/**
 * Computes isosurface of the current image, and saves the mesh into a text file
 * in OFF format.
 * 
 * @author dlegland
 *
 */
public class ExportIsosurface implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
        
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
        dlg.showDialog();
        
        if (dlg.wasCanceled())
            return;
        // retrieve iso-surface value
        double value = dlg.getNextNumber();
        
        // create file dialog using last save path
        String fileName = String.format(Locale.ENGLISH, "%s-iso%.2f.off", image.getName(), value);
        File file = frame.getGui().chooseFileToSave(frame, "Output Mesh File", fileName);

        // Check the selected file is valid
        if (file == null)
        {
            return;
        }
        if (!file.getName().endsWith(".off"))
        {
            file = new File(file.getParent(), file.getName() + ".off");
        }
        
        // Call the morphological marching cube algorithm to compute a triangular mesh
        MorphologicalMarchingCubes mc = new MorphologicalMarchingCubes(value);
        
        // initialize algo monitoring
        mc.addAlgoListener(iFrame);
        iFrame.getStatusBar().setCurrentStepLabel("Compute Isosurface");
        
        // run process, switching to the best appropriate method depending on
        // the class of the operator
        long t0 = System.nanoTime();
        Mesh3D mesh = mc.process(scalar);
        
        // cleanup listener and status bar
        iFrame.getStatusBar().setProgressBarPercent(0);
        
        // create a writer to save the mesh
        iFrame.getStatusBar().setCurrentStepLabel("Save mesh data");
        OffMeshWriter writer = new OffMeshWriter(file);
        
        try
        {
            writer.writeMesh(mesh);
        }
        catch (IOException ex)
        {
            System.err.println(ex);
            throw new RuntimeException("Error during writing the mesh", ex);
        }
        
        // display elapsed time
        long t1 = System.nanoTime();
        iFrame.showElapsedTime("Save Isosurface", (t1 - t0) / 1_000_000.0, image);
    }
}
