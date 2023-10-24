/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Set;
import java.util.TreeMap;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.IntArray;
import net.sci.array.scalar.IntArray2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.label.RegionAdjacencies;
import net.sci.image.label.RegionAdjacencies.LabelPair;
import net.sci.image.label.LabelImages;

/**
 * Computes the region adjacencies in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageAdjacencies implements FramePlugin
{
    public LabelImageAdjacencies()
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
            if (!(array instanceof IntArray && array.dimensionality() == 2))
            {
                System.out.println("Requires a planar array of int");
                return;
            }
            
            // wrap to 2D integer array
            IntArray2D<?> array2d = IntArray2D.wrap((IntArray<?>) array);

            // Extract centroids as an array of points
            int[] labels = LabelImages.findAllLabels(array2d);
            // Would be better to return the map directly
            Point2D[] centroids = RegionAnalysis2D.centroids(array2d, labels, calib);
            TreeMap<Integer, Point2D> centroidMap = new TreeMap<>();
            for (int i = 0; i < labels.length; i++)
            {
                centroidMap.put(labels[i], centroids[i]);
            }
            
            // compute adjacencies
            Set<LabelPair> adjList = RegionAdjacencies.computeAdjacencies(array2d);
             
            // add to the document
            for (int i = 0; i < centroids.length; i++)
            {
                doc.addShape(new Shape(centroids[i]));
            }
            for (LabelPair adj : adjList)
            {
                Point2D p1 = centroidMap.get(adj.label1);
                Point2D p2 = centroidMap.get(adj.label2);
                doc.addShape(new Shape(new LineSegment2D(p1, p2)));
            }
            frame.repaint();
        }
        else
        {
            throw new RuntimeException("Not implemented for dimensions > 2");
        }
    }
}
