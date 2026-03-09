/**
 * 
 */
package imago.transform.plugins.file;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.io.DelimitedFileAffineTransformReader;
import imago.transform.plugins.TransformManagerPlugin;
import net.sci.geom.Transform;

/**
 * Imports an affine transform by reading matrix parameters from a delimited
 * file. All parameters are stored on the first data row within the file. Works
 * for 2D or 3D affine transforms.
 */
public class ImportAffineTransformFromCoefficientsFile implements TransformManagerPlugin
{
    private static FileFilter textFileFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
        
        // open a dialog to read a .json file
        File file = frame.getGui().chooseFileToOpen(frame,
                "Import Transform coefficients file", textFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }

        Transform transfo;
        try (DelimitedFileAffineTransformReader reader = new DelimitedFileAffineTransformReader(file))
        {
            transfo = reader.readTransform();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        
        ImagoApp app = frame.getGui().getAppli();
        TransformHandle.create(app, transfo, file.getName());
        
        tm.updateInfoTable();
    }
}
