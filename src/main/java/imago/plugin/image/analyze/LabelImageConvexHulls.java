/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

import java.util.Map;

import net.sci.array.Array;
import net.sci.array.scalar.IntArray;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;
import net.sci.image.analyze.region2d.ConvexHull;
import net.sci.table.Table;

/**
 * Computes the convex hull of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageConvexHulls implements Plugin
{
    public LabelImageConvexHulls()
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
            throw new IllegalArgumentException("Requires label image as input");
        }
        
        Array<?> array = image.getData();
        int nd = array.dimensionality();
        
        if (nd == 2)
        {
            // check input data type
            if (!(array instanceof IntArray))
            {
                throw new IllegalArgumentException("Requires an array of int");
            }
    
            // Extract centroids as an array of points
//            IntArray2D<?> array2d = (IntArray2D<?>) array;
//            int[] labels = LabelImages.findAllLabels(array2d); 
//            Point2D[] centroids = RegionAnalysis2D.centroids(array2d, labels);
             
            ConvexHull algo = new ConvexHull();
            Map<Integer, Polygon2D> convexHulls = algo.analyzeRegions(image);
            // add to the document
            for (Polygon2D hull : convexHulls.values())
            {
                doc.addShape(new Shape(hull));
            }

            Table table = algo.createTable(convexHulls);
            // add the new frame to the GUI
            frame.getGui().createTableFrame(table, frame);
//            // add to the document
//            for (int i = 0; i < centroids.length; i++)
//            {
//                handle.addShape(new ImagoShape(centroids[i]));
//            }
            frame.repaint();
        }
        else
        {
            throw new IllegalArgumentException("Requires an input image with dimension 2.");
        }
    }
    
    public boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImageFrame))
        {
            return false;
        }
        
        // retrieve image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (!image.isLabelImage())
        {
            return false;
        }
        
        Array<?> array = image.getData();
        int nd = array.dimensionality();
        
        return nd == 2;
    }
}
