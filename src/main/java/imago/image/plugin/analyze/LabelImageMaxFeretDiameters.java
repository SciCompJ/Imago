/**
 * 
 */
package imago.image.plugin.analyze;

import java.util.Collection;
import java.util.Map;

import imago.app.ImageHandle;
import imago.app.ObjectHandle;
import imago.app.shape.Shape;
import imago.gui.*;
import imago.image.ImageFrame;
import imago.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.image.Image;
import net.sci.image.analyze.region2d.MaxFeretDiameter;
import net.sci.geom.geom2d.PointPair2D;
import net.sci.table.Table;

/**
 * 
 * @author dlegland
 *
 */
public class LabelImageMaxFeretDiameters implements FramePlugin
{
    public LabelImageMaxFeretDiameters()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
        {
            return;
        }
        
        // retrieve image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (!image.isLabelImage())
        {
            System.out.println("Requires label image as input");
            return;
        }
        
        // check input data type
        Array<?> array = image.getData();
        if (!(array instanceof IntArray2D))
        {
            ImagoGui.showErrorDialog(frame, "Requires a planar array of labels");
            return;
        }

        ImagoGui gui = frame.getGui();
        
        GenericDialog dlg = new GenericDialog(frame, "Max Feret Diameters");
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = ImageHandle.getAllNames(gui.getAppli());
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        boolean showTable = dlg.getNextBoolean();
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        
        // Extract diameters
        MaxFeretDiameter algo = new MaxFeretDiameter();
        Map<Integer, PointPair2D> diams = algo.analyzeRegions(image);
        
        if (showTable)
        {
            // Convert ellipse to table, and display
            Table table = algo.createTable(diams);
            table.setName(ObjectHandle.appendSuffix(image.getName(), "maxFeret"));
            
            // add the new frame to the GUI
            TableFrame.create(table, frame);
        }
        
        if (overlay)
        {
            ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);
            
            // add to the document
            for (PointPair2D pair : diams.values())
            {
                LineSegment2D line = new LineSegment2D(pair.p1, pair.p2);
                handle.addShape(new Shape(line));
            }
            
            // update viewers
            handle.notifyImageHandleChange();
        }
    }
}
