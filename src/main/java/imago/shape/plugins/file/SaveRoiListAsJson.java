/**
 * 
 */
package imago.shape.plugins.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import imago.app.shape.io.JsonGeometryWriter;
import imago.gui.ImagoFrame;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;

/**
 * Saves the selected geometries as a text file in JSON format.
 */
public class SaveRoiListAsJson implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        // open a dialog to read a .json file
        String defaultFileName = "data.rois";
        File file = frame.getGui().chooseFileToSave(frame, "Save Roi List", defaultFileName,
                ShapeManager.roisFileFilter, ShapeManager.jsonFileFilter);
        if (file == null)
        {
            return;
        }
    
        try 
        {
            // create a json Geometry writer from the file
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            JsonGeometryWriter writer = new JsonGeometryWriter(new PrintWriter(fileWriter));
            // configure JSON
            writer.setIndent("  ");
            
            // write ROI List top-lvel node
            writer.beginObject();
            writer.name("type");
            writer.value("RoiList");
            writer.name("items");
            writer.beginArray();

            // choose selected geometries, or all geometries
            boolean hasSelection = sm.getJTable().getSelectionModel().getSelectedItemsCount() > 0;
            Collection<GeometryHandle> handles = hasSelection ? sm.getSelectedHandles() : sm.getAllHandles();
            
            // iterate over geometries
            for (GeometryHandle handle : handles)
            {
                writer.beginObject();
                writer.name("name");
                writer.value(handle.getName());
                writer.name("geometry");
                writer.writeGeometry(handle.getGeometry());
                writer.endObject();
            }
            
            writer.endArray();
            writer.endObject();
            writer.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        sm.updateInfoTable();
    }
}
