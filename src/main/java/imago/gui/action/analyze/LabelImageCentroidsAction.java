/**
 * 
 */
package imago.gui.action.analyze;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.array.data.scalar3d.IntArray3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.Point3D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.analyze.RegionAnalysis3D;
import net.sci.image.morphology.LabelImages;
import net.sci.table.Table;

/**
 * Computes the centroid of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageCentroidsAction extends ImagoAction
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LabelImageCentroidsAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
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
                System.out.println("Requires a planar array of int");
                return;
            }
    
            // Extract centroids as an array of points
            IntArray2D<?> array2d = (IntArray2D<?>) array;
            int[] labels = LabelImages.findAllLabels(array2d); 
            Point2D[] centroids = RegionAnalysis2D.centroids(array2d, labels);
             
            // add to the document
            for (int i = 0; i < centroids.length; i++)
            {
                doc.addShape(new ImagoShape(centroids[i]));
            }
            this.frame.repaint();
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
            
            gui.addFrame(new ImagoTableFrame(this.frame, tab));
        }
    }
}
