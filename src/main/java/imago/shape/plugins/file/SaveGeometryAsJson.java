/**
 * 
 */
package imago.shape.plugins.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import imago.app.shape.io.JsonGeometryWriter;
import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Saves the selected geometry as a text file in JSON format.
 */
public class SaveGeometryAsJson implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        // open a dialog to read a .json file
        String defaultFileName = "data.geom";
        File file = frame.getGui().chooseFileToSave(frame, "Save to JSON file", defaultFileName,
                ShapeManager.geomFileFilter, ShapeManager.jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        GeometryHandle handle = sm.getSelectedHandle();
        
        try 
        {
            // create a json Geometry writer from the file
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(fileWriter));
            // configure JSON
            writer.setIndent("  ");
            
            writer.writeGeometry(handle.getGeometry());
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        sm.updateInfoTable();
    }
}
