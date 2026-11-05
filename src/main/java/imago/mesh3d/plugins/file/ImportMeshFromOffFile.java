/**
 * 
 */
package imago.mesh3d.plugins.file;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.TriMesh3D;
import net.sci.geom.mesh3d.io.OffMeshReader;

/**
 * Opens a file to read a mesh in "Object File format" ("OFF" extension).
 */
public class ImportMeshFromOffFile implements FramePlugin
{
    private static FileFilter offFileFilter = new FileNameExtensionFilter("Object File Format files (*.off)", "off");

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        if (frame instanceof Mesh3DViewer meshFrame)
        {
            // open a dialog to read a .json file
            File file = frame.getGui().chooseFileToOpen(frame,
                    "Import Mesh in OFF format", offFileFilter);
            if (file == null)
            {
                return;
            }
            // Check the chosen file exists
            if (!file.exists())
            {
                return;
            }

            long t0 = System.nanoTime();
            Mesh3D mesh;
            try
            {
                OffMeshReader reader = new OffMeshReader(file);
                reader.addAlgoListener(meshFrame);
                mesh = reader.readMesh();
            }
            catch (IOException ex)
            {
                System.out.println("IO Exception");
                throw new RuntimeException(ex);
            }
            catch (Exception ex)
            {
                System.out.println("Exception");
                throw new RuntimeException(ex);
            }

            // ensure mesh is a triangle mesh
            if (!(mesh instanceof TriMesh3D))
            {
                mesh = Meshes3D.triangulate(mesh);
            }
            long t1 = System.nanoTime();
            double dt = (t1 - t0) / 1_000_000_000.0;

            meshFrame.getStatusBar().setProgressBarPercent(0);
            String msg = String.format("Read OFF Mesh: %.3f seconds", dt);
            meshFrame.getStatusBar().setCurrentStepLabel(msg);

            System.out.println(String.format("read mesh with %d vertices and %d faces", mesh.vertexCount(), mesh.faceCount()));
            System.out.println(msg);

            // replace mesh in current frame
            meshFrame.setMesh((TriMesh3D) mesh);
            meshFrame.repaint();
        }
    }
}
