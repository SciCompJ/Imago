/**
 * 
 */
package imago.mesh3d.plugins.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.mesh3d.io.OffMeshWriter;

/**
 * Writes mesh data into a text files in "Object File format" ("OFF" extension).
 */
public class ExportMeshToOffFile implements FramePlugin
{
    private static FileFilter offFileFilter = new FileNameExtensionFilter("Object File Format files (*.off)", "off");

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (frame instanceof Mesh3DViewer meshFrame)
        {
            // open a dialog to read a .json file
            File file = frame.getGui().chooseFileToSave(frame,
                    "Import Mesh in OFF format", "mesh.off", offFileFilter);
            
            // Check the selected file is valid
            if (file == null)
            {
                return;
            }
            if (!file.getName().endsWith(".off"))
            {
                file = new File(file.getParent(), file.getName() + ".off");
            }
            
            // Create a writer with specified file
            OffMeshWriter writer = new OffMeshWriter(file);
            long t0 = System.nanoTime();
            try 
            {
                writer.addAlgoListener(meshFrame);
                writer.writeMesh(meshFrame.getMesh());
            }
            catch (Exception ex)
            {
                ex.printStackTrace(System.err);
                ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "Tiff Image Export Error");
                return;
            }
            
            long t1 = System.nanoTime();
            double dt = (t1 - t0) / 1_000_000_000.0;

            meshFrame.getStatusBar().setProgressBarPercent(0);
            String pattern = "elapsed time: %.3f seconds";
            meshFrame.getStatusBar().setToolLabel(String.format(pattern, dt));
        }
    }
}
