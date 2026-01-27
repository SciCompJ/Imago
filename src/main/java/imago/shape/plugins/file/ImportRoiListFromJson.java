/**
 * 
 */
package imago.shape.plugins.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import imago.app.ImagoApp;
import imago.app.shape.io.JsonGeometryReader;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.Geometry;

/**
 * Import an ImageJ Roi into the workspace as a new Shape.
 */
public class ImportRoiListFromJson implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        ImagoGui gui = frame.getGui();
        
        // open a dialog to read a .json file
        File file = gui.chooseFileToOpen(frame,
                "Import Roi List", ShapeManager.roisFileFilter, ShapeManager.jsonFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }
        
        ImagoApp app = gui.getAppli();
        try (FileReader fr = new FileReader(file.getAbsoluteFile());
                JsonGeometryReader reader = new JsonGeometryReader(new BufferedReader(fr)))
        {
            reader.beginObject();
            
            @SuppressWarnings("unused")
            String typeKey = reader.nextName();
            @SuppressWarnings("unused")
            String type = reader.nextString();
            
            @SuppressWarnings("unused")
            String itemListKey = reader.nextName();
            reader.beginArray();
            while(reader.hasNext())
            {
                reader.beginObject();
                @SuppressWarnings("unused")
                String nameKey = reader.nextName();
                String name = reader.nextString();
                
                reader.nextName();
                Geometry geom = reader.readGeometry();
                
                GeometryHandle handle = GeometryHandle.create(app, geom);
                handle.setName(name);
                
                reader.endObject();
            }
            reader.endArray();
            reader.endObject();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        // update display
        sm.updateInfoTable();
    }
}
