/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.array.numeric.IntArray3D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom3d.Bounds3D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis3D;
import net.sci.image.analyze.RegionAnalyzer;
import net.sci.image.analyze.region2d.RegionBounds2D;
import net.sci.image.label.LabelImages;
import net.sci.table.Table;

/**
 * Computes the bounding box of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageBoundingBoxes implements FramePlugin
{
    public LabelImageBoundingBoxes()
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
        
        Array<?> array = image.getData();
        int nd = array.dimensionality();
        Calibration calib = image.getCalibration();

        if (nd == 2)
        {
            // check input data type
            if (!(array instanceof IntArray2D))
            {
                System.out.println("Requires a planar array of ints");
                return;
            }
            
            ImagoGui gui = frame.getGui();
            
            GenericDialog dlg = new GenericDialog(frame, "Bounding Boxes");
            dlg.addCheckBox("Display Table ", true);
            dlg.addCheckBox("Overlay Results ", true);
            Collection<String> imageNames = ImageHandle.getAllNames(gui.getAppli());
            String[] imageNameArray = imageNames.toArray(new String[]{});
            String firstImageName = doc.getName();
            dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
            dlg.showDialog();
            if (dlg.wasCanceled())
            {
                return;
            }
            
            boolean showTable = dlg.getNextBoolean();
            boolean overlay = dlg.getNextBoolean();
            String imageToOverlay = dlg.getNextChoice();
            
    
            // Extract bounding boxes as an array of Bound2D instances
            IntArray2D<?> array2d = (IntArray2D<?>) array;
            int[] labels = LabelImages.findAllLabels(array2d);
            
            RegionBounds2D analyzer = new RegionBounds2D();
            Bounds2D[] boxes = analyzer.analyzeRegions(array2d, labels, calib);
             
            if (showTable)
            {
                // Convert bounds to table, and display
                Table table = analyzer.createTable(RegionAnalyzer.createMap(labels, boxes));
                
                // add the new frame to the GUI
                TableFrame.create(table, frame);
            }

            if (overlay)
            {
                // retrieve handle of image to display result in
                ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);
                
                // add to the document
                int nBoxes = boxes.length;
                for (int i = 0; i < nBoxes; i++)
                {
                    Polygon2D poly = boxes[i].getRectangle();
                    handle.addShape(new Shape(poly));
                }
                
                // update viewers
                handle.notifyImageHandleChange();
            }
        }
        else if (nd == 3)
        {
            // check input data type
            if (!(array instanceof IntArray3D))
            {
                System.out.println("Requires a 3D array of ints");
                return;
            }
    
            // Extract centroids as an array of coordinates
            IntArray3D<?> array3d = (IntArray3D<?>) array;
            int[] labels = LabelImages.findAllLabels(array3d); 
            Bounds3D[] boxes = RegionAnalysis3D.boundingBoxes(array3d, labels);
            
            // Convert centroid array to table, and display
            Table table = Table.create(boxes.length, 6);
            table.setColumnNames(new String[]{"XMin", "XMax", "YMin", "YMax", "ZMin", "ZMax"});
            for (int i = 0; i < boxes.length; i++)
            {
                Bounds3D box = boxes[i];
                table.setValue(i, 0, box.getXMin());
                table.setValue(i, 1, box.getXMax());
                table.setValue(i, 2, box.getYMin());
                table.setValue(i, 3, box.getYMax());
                table.setValue(i, 4, box.getZMin());
                table.setValue(i, 5, box.getZMax());
            }
            table.setName(image.getName() + "-BBoxes");

            // add the new frame to the GUI
            TableFrame.create(table, frame);
        }
    }
}
