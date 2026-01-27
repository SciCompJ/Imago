/**
 * 
 */
package imago.shape.plugins.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import imago.util.imagej.ImagejRoi;
import imago.util.imagej.ImagejRoiDecoder;
import net.sci.geom.Geometry;

/**
 * Import an ImageJ Roi into the workspace as a new Shape.
 */
public class ImportImagejRoi implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        ImagoGui gui = frame.getGui();
        
        // open a dialog to read a .json file
        File file = gui.chooseFileToOpen(frame,
                "Import ImageJ Roi", ShapeManager.ijroiFileFilter);
        if (file == null)
        {
            return;
        }
        // Check the chosen file exists
        if (!file.exists())
        {
            return;
        }
        
        // import ROI Geometry from file
        Geometry geom;
        try 
        {
            byte[] array = Files.readAllBytes(file.toPath());
            ImagejRoi roi = ImagejRoiDecoder.decode(array);
            geom = roi.asShape().getGeometry();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        
        // create handle
        ImagoApp app = gui.getAppli();
        GeometryHandle handle = GeometryHandle.create(app, geom);
        
        // setup metadata
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        handle.setName(file.getName());
        
        // update display
        sm.updateInfoTable();
    }
}
