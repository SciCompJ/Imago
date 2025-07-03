/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Collection;
import java.util.Map;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.*;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.image.Image;
import net.sci.image.analyze.region2d.GeodesicDiameter;
import net.sci.image.binary.distmap.ChamferMask2D;
import net.sci.image.binary.distmap.ChamferMasks2D;
import net.sci.table.Table;

/**
 * 
 * @author dlegland
 *
 */
public class LabelImageGeodesicDiameters implements FramePlugin
{
    public LabelImageGeodesicDiameters()
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
        
        GenericDialog dlg = new GenericDialog(frame, "Geodesic Diameters");
        dlg.addChoice("Chamfer Weights: ", ChamferMasks2D.getAllLabels(), ChamferMasks2D.CHESSKNIGHT.toString());
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = ImageHandle.getAllNames(gui.getAppli());
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        ChamferMask2D mask = ChamferMasks2D.fromLabel(dlg.getNextChoice()).getMask();
        boolean showTable = dlg.getNextBoolean();
        boolean overlayPaths = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        
        // Extract diameters
        GeodesicDiameter algo = new GeodesicDiameter(mask);
        algo.setComputePaths(overlayPaths);
        Map<Integer, GeodesicDiameter.Result> diams = algo.analyzeRegions(image);
        
        if (showTable)
        {
            // create result frame and display
            Table table = algo.createTable(diams);
            table.setName(image.getName() + "-GeodDiam");
            TableFrame.create(table, frame);
        }
        
        if (overlayPaths)
        {
            ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);
            
            // add to the document
            for (GeodesicDiameter.Result res : diams.values())
            {
                Polyline2D poly = Polyline2D.create(res.path, false);
                handle.addShape(new Shape(poly));
            }
            
            // update viewers
            handle.notifyImageHandleChange();
        }
    }   
}
