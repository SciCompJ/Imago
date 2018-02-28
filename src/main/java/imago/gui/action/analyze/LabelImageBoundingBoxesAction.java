/**
 * 
 */
package imago.gui.action.analyze;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.array.data.scalar3d.IntArray3D;
import net.sci.geom.geom2d.Box2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom3d.Box3D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.analyze.RegionAnalysis3D;
import net.sci.image.morphology.LabelImages;
import net.sci.table.Table;

/**
 * Computes the bounding box of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageBoundingBoxesAction extends ImagoAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LabelImageBoundingBoxesAction(ImagoFrame frame, String name)
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
                System.out.println("Requires a planar array of ints");
                return;
            }
    
            // Extract centroids as an array of coordinates
            IntArray2D<?> array2d = (IntArray2D<?>) array;
            int[] labels = LabelImages.findAllLabels(array2d); 
            Box2D[] boxes = RegionAnalysis2D.boundingBoxes(array2d, labels);
             
            // number of boxes
            int nPoints = boxes.length;
    
            // add to the document
            for (int i = 0; i < nPoints; i++)
            {
            	Polygon2D poly = boxes[i].getRectangle();
                doc.addShape(new ImagoShape(poly));
            }
            this.frame.repaint();
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
            Box3D[] boxes = RegionAnalysis3D.boundingBoxes(array3d, labels);
            
            // Convert centroid array to table, and display
            Table tab = Table.create(boxes.length, 6);
            tab.setColumnNames(new String[]{"XMin", "XMax", "YMin", "YMax", "ZMin", "ZMax"});
            for (int i = 0; i < boxes.length; i++)
            {
                Box3D box = boxes[i];
                tab.setValue(i, 0, box.getXMin());
                tab.setValue(i, 1, box.getXMax());
                tab.setValue(i, 2, box.getYMin());
                tab.setValue(i, 3, box.getYMax());
                tab.setValue(i, 4, box.getZMin());
                tab.setValue(i, 5, box.getZMax());
            }
            tab.setName(image.getName() + "-BBoxes");
            gui.addFrame(new ImagoTableFrame(this.frame, tab));
        }
    }
    
}
