/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Map;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.scalar.IntArray3D;
import net.sci.geom.geom3d.surface.Ellipsoid3D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.region3d.EquivalentEllipsoid3D;
import net.sci.table.Table;

/**
 * Computes the equivalent ellipsoid of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageEquivalentEllipsoids implements FramePlugin
{
    public LabelImageEquivalentEllipsoids()
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
        
        // check image type
        if (!image.isLabelImage())
        {
            System.out.println("Requires label image as input");
            return;
        }

        // check input data type
        Array<?> array = image.getData();
        if (!(array instanceof IntArray3D))
        {
            ImagoGui.showErrorDialog(frame, "Requires a 3D label maps as input");
            return;
        }

        // retrieve necessary information
        IntArray3D<?> array2d = (IntArray3D<?>) array;
        Calibration calib = image.getCalibration();
        
        // Extract ellipsoids
        EquivalentEllipsoid3D algo = new EquivalentEllipsoid3D();
        Map<Integer, Ellipsoid3D> ellipsoids = algo.analyzeRegions(array2d, calib);
         
        // Convert ellipsoids to table
        Table table = algo.createTable(ellipsoids);
        table.setName(image.getName() + "-ellipsoids");

        // display table as a new frame
        TableFrame.create(table, frame);
    }
}
