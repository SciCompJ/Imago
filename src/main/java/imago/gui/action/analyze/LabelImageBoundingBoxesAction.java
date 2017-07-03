/**
 * 
 */
package imago.gui.action.analyze;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.geom.geom2d.Box2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.morphology.LabelImages;

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
        Image meta = doc.getImage();
        Array<?> array = meta.getData();
        
        // check input data type
        if (!(array instanceof IntArray2D))
        {
            System.out.println("Requires a planar array of ints");
            return;
        }

        // Extract centroids as an array of coordinates
        IntArray2D<?> image = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(image); 
        Box2D[] boxes = RegionAnalysis2D.boundingBoxes(image, labels);
         
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
    
}
