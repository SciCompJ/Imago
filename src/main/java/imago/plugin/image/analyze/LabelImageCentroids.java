/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Map;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;
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
        if (!(frame instanceof ImagoDocViewer))
        {
            return;
        }
        
        // retrieve image data
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
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
                doc.addShape(new ImagoShape(centroid));
            }

            Table table = algo.createTable(centroids);
            frame.getGui().addFrame(new ImagoTableFrame(frame, table));
//            // add to the document
//            for (int i = 0; i < centroids.length; i++)
//            {
//                doc.addShape(new ImagoShape(centroids[i]));
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
            Table tab = Table.create(centroids.length, 3);
            tab.setColumnNames(new String[]{"Centroid.X", "Centroid.Y", "Centroid.Z"});
            for (int i = 0; i < centroids.length; i++)
            {
                Point3D centroid = centroids[i];
                tab.setValue(i, 0, centroid.getX());
                tab.setValue(i, 1, centroid.getY());
                tab.setValue(i, 2, centroid.getZ());
            }
            tab.setName(image.getName() + "-Centroids");

            frame.getGui().addFrame(new ImagoTableFrame(frame, tab));
        }
    }
}
