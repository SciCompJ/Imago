/**
 * 
 */
package imago.plugin.image.vectorize;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray2D;
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
public class ImageFindNonZeroPixels implements FramePlugin
{
    public ImageFindNonZeroPixels()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("image find non zero pixels");

        // get current image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
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
            Shape shape = new Shape(point);
            doc.addShape(shape);
        }
        
        frame.repaint();
    }
    
}
