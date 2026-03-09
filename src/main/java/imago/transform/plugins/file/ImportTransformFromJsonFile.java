/**
 * 
 */
package imago.transform.plugins.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.io.JsonTransformReader;
import imago.transform.plugins.TransformManagerPlugin;
import net.sci.geom.Transform;

/**
 * 
 */
public class ImportTransformFromJsonFile implements TransformManagerPlugin
{
    private static FileFilter jsonFileFilter = new FileNameExtensionFilter("All JSON files (*.json)", "json");

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
        
        // open a dialog to read a .json file
        File file = frame.getGui().chooseFileToOpen(frame,
                "Import Transform coefficients file", jsonFileFilter);
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
        try (JsonTransformReader reader = new JsonTransformReader(new FileReader(file));)
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
