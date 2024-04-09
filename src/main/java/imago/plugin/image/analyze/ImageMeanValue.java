/**
 * 
 */
package imago.plugin.image.analyze;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.DefaultPolygon2D;
import net.sci.image.Image;
import net.sci.table.DefaultNumericTable;

/**
 * Compute mean value within the whole image or within the current region selection.
 * 
 * @author David Legland
 *
 */
public class ImageMeanValue implements FramePlugin
{
    public ImageMeanValue()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        Image meta = iframe.getImageHandle().getImage();
        Array<?> array = meta.getData();
        
        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }
        
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof DefaultPolygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        DefaultPolygon2D poly = (DefaultPolygon2D) selection;
        
        // manage clockwise and counter-clockwise polygons
        boolean clockWise = poly.signedArea() < 0;
        
        DefaultNumericTable table = null;
        if (array instanceof ScalarArray)
        {
        	ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
        	
            int sizeX = array2d.size(0);
            int sizeY = array2d.size(1);
            
            double sum = 0;
            int count = 0;
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (poly.contains(new Point2D(x, y)) ^ clockWise)
                    {
                        sum += array2d.getValue(x, y);
                        count++;
                    }
                }
            }

            double meanValue = count == 0 ? Double.NaN : sum / count;
            table = new DefaultNumericTable(1, 3);
            table.setColumnNames(new String[] { "Mean", "Sum", "Count" });
            table.setValue(0, 0, meanValue);
            table.setValue(0, 1, sum);
            table.setValue(0, 2, count);
            
            table.show();
        }
//        else if (array instanceof RGB8Array2D)
//        {
//            table = colorProfile((RGB8Array2D) array, p1, p2, n);
//            plotRGB8LineProfile(table);
//        }
//        else if (array instanceof VectorArray2D)
//        {
//            ScalarArray2D<?> normImage = ((VectorArray2D<?>) array).norm();
//            table = intensityProfile(normImage, p1, p2, n);
//            plotIntensityProfile(table);
//        }
        else
            throw new RuntimeException(
                    "Can not process image from class: " + array.getClass().getName());
        
    }
}
