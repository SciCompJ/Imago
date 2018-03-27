/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.util.Collection;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.image.process.Find;

/**
 * Find non-zero pixels within a planar image and add them as ImagoShape to the
 * current document.
 * 
 * @author dlegland
 *
 */
public class ImageFindNonZeroPixelsAction extends ImagoAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
   
    public ImageFindNonZeroPixelsAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        System.out.println("image find non zero pixels");

        // get current image data
        ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
        Image image = doc.getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2)
        {
            System.out.println("limited to 2D arrays");
            return;
        }
        if (!(array instanceof ScalarArray2D))
        {
            System.out.println("requires an instance of ScalarArray");
            return;
        }
        
        ScalarArray2D<?> array2d = (ScalarArray2D<?>) array;
        Collection<Point2D> points = Find.findPixels(array2d);
        
        for (Point2D point : points)
        {
            ImagoShape shape = new ImagoShape(point);
            doc.addShape(shape);
        }
        
        this.frame.repaint();
    }
    
}
