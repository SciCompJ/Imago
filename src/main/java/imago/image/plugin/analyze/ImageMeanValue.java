/**
 * 
 */
package imago.image.plugin.analyze;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.PlanarImageViewer;
import imago.table.TableFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.VectorArray;
import net.sci.array.numeric.VectorArray2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.DefaultPolygon2D;
import net.sci.image.Image;
import net.sci.table.NumericTable;

/**
 * Compute mean value within the whole image or within the current region
 * selection.
 * 
 * @author David Legland
 *
 */
public class ImageMeanValue implements FramePlugin
{
    /**
     * Default empty constructor.
     */
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
        Image image = iframe.getImageHandle().getImage();
        Array<?> array = image.getData();
        
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
        
        // ensure counter-clockwise orientation of polygon
        if (poly.signedArea() < 0)
        {
            poly = poly.complement();
        }
        
        NumericTable table = null;
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
                    if (poly.contains(new Point2D(x, y)))
                    {
                        sum += array2d.getValue(x, y);
                        count++;
                    }
                }
            }

            double meanValue = count == 0 ? Double.NaN : sum / count;
            table = NumericTable.create(1, 3);
            table.setValue(0, 0, meanValue);
            table.setValue(0, 1, sum);
            table.setValue(0, 2, count);
            
            table.setColumnNames(new String[] { "Mean", "Sum", "Count" });
            table.setRowAxis(image.getCalibration().getChannelAxis());
            table.setName(image.getName() + "-roi");
            TableFrame.create(table, frame);
        }
        else if (array instanceof VectorArray2D)
        {
            VectorArray2D<?,?> array2d = VectorArray2D.wrap((VectorArray<?,?>) array);
            
            int sizeX = array2d.size(0);
            int sizeY = array2d.size(1);
            int nChannels = array2d.channelCount();
            
            double[] sums = new double[nChannels];
            int[] counts = new int[nChannels];
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (poly.contains(new Point2D(x, y)))
                    {
                        for (int c = 0; c < nChannels; c++)
                        {
                            sums[c] += array2d.getValue(x, y, c);
                            counts[c] ++;
                        }
                    }
                }
            }

            table = NumericTable.create(nChannels, 3);
            
            for (int c = 0; c < nChannels; c++)
            {
                double meanValue = counts[c] == 0 ? Double.NaN : sums[c] / counts[c];
                table.setValue(c, 0, meanValue);
                table.setValue(c, 1, sums[c]);
                table.setValue(c, 2, counts[c]);
            }
            
            table.setColumnNames(new String[] { "Mean", "Sum", "Count" });
            table.setRowAxis(image.getCalibration().getChannelAxis());
            table.setName(image.getName() + "-roi");
            TableFrame.create(table, frame);
        }
        else
            throw new RuntimeException(
                    "Can not process image with array class: " + array.getClass().getName());
        
    }
}
