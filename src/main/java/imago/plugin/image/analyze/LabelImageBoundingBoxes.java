/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.IntArray2D;
import net.sci.array.scalar.IntArray3D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom3d.Bounds3D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.analyze.RegionAnalysis3D;
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

        if (nd == 2)
        {
            // check input data type
            if (!(array instanceof IntArray2D))
            {
                System.out.println("Requires a planar array of ints");
                return;
            }
    
            // Extract centroids as an array of coordinates
            IntArray2D<?> array2d = (IntArray2D<?>) array;
            int[] labels = LabelImages.findAllLabels(array2d); 
            Bounds2D[] boxes = RegionAnalysis2D.boundingBoxes(array2d, labels);
             
            // number of boxes
            int nPoints = boxes.length;
    
            // add to the document
            for (int i = 0; i < nPoints; i++)
            {
            	Polygon2D poly = boxes[i].getRectangle();
                doc.addShape(new Shape(poly));
            }
            frame.repaint();
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
            Table tab = Table.create(boxes.length, 6);
            tab.setColumnNames(new String[]{"XMin", "XMax", "YMin", "YMax", "ZMin", "ZMax"});
            for (int i = 0; i < boxes.length; i++)
            {
                Bounds3D box = boxes[i];
                tab.setValue(i, 0, box.getXMin());
                tab.setValue(i, 1, box.getXMax());
                tab.setValue(i, 2, box.getYMin());
                tab.setValue(i, 3, box.getYMax());
                tab.setValue(i, 4, box.getZMin());
                tab.setValue(i, 5, box.getZMax());
            }
            tab.setName(image.getName() + "-BBoxes");

            // add the new frame to the GUI
            frame.getGui().createTableFrame(tab, frame);
        }
    }
}
