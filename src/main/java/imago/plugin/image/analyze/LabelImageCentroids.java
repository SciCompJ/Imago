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
import net.sci.array.scalar.IntArray3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.Point3D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis3D;
import net.sci.image.analyze.region2d.Centroid;
import net.sci.image.morphology.LabelImages;
import net.sci.table.Table;

/**
 * Computes the centroid of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageCentroids implements Plugin
{
    public LabelImageCentroids()
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
             
            Centroid algo = new Centroid();
            Map<Integer, Point2D> centroids = algo.analyzeRegions(image);
            // add to the document
            for (Point2D centroid : centroids.values())
            {
                doc.addShape(new Shape(centroid));
            }

            Table table = algo.createTable(centroids);
            // add the new frame to the GUI
            frame.getGui().createTableFrame(table, frame);
//            // add to the document
//            for (int i = 0; i < centroids.length; i++)
//            {
//                handle.addShape(new ImagoShape(centroids[i]));
//            }
            frame.repaint();
        }
        else if (nd == 3)
        {
            // check input data type
            if (!(array instanceof IntArray3D))
            {
                System.out.println("Requires a 3D array of int");
                return;
            }
    
            // Extract centroids as an array of points
            IntArray3D<?> array3d = (IntArray3D<?>) array;
            int[] labels = LabelImages.findAllLabels(array3d); 
            Point3D[] centroids = RegionAnalysis3D.centroids(array3d, labels);
            
            // Convert centroid array to table, and display
            Table table = Table.create(centroids.length, 3);
            table.setColumnNames(new String[]{"Centroid.X", "Centroid.Y", "Centroid.Z"});
            for (int i = 0; i < centroids.length; i++)
            {
                Point3D centroid = centroids[i];
                table.setValue(i, 0, centroid.getX());
                table.setValue(i, 1, centroid.getY());
                table.setValue(i, 2, centroid.getZ());
            }
            table.setName(image.getName() + "-Centroids");

            // add the new frame to the GUI
            frame.getGui().createTableFrame(table, frame);
        }
    }
}
