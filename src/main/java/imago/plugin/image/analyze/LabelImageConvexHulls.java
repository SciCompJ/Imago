/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;

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
public class LabelImageConvexHulls implements FramePlugin
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
            
            // Extract convex hull of each region
            ConvexHull algo = new ConvexHull();
            Map<Integer, Polygon2D> convexHulls = algo.analyzeRegions(image);
            
            // overlay convex hulls on original image
            for (Polygon2D hull : convexHulls.values())
            {
                doc.addShape(new Shape(hull));
            }
            frame.repaint();
            
            // display results in a new Table
            Table table = algo.createTable(convexHulls);
            frame.createTableFrame(table);
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
