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
import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.morphology.LabelImages;

/**
 * Computes the inertia ellipse of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageInertiaEllipsesAction extends ImagoAction
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LabelImageInertiaEllipsesAction(ImagoFrame frame, String name)
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

        // Extract centroids as an array of points
        IntArray2D<?> image = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(image); 
        Ellipse2D[] ellipses = RegionAnalysis2D.inertiaEllipses(image, labels);
         
        // add to the document
        for (int i = 0; i < ellipses.length; i++)
        {
            doc.addShape(new ImagoShape(ellipses[i]));
        }
        this.frame.repaint();
    }
    
}
