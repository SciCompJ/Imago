/**
 * 
 */
package imago.image.plugin.analyze;

import imago.app.ObjectHandle;
import imago.app.shape.Shape;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.table.TableFrame;
import imago.gui.FramePlugin;

import java.util.Collection;

import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Circle2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.label.LabelImages;
import net.sci.table.Table;

/**
 * Computes the equivalent disk of each region in the current label image.
 * 
 * The center of the disk is the centroid if the region, and the radius is
 * computed such as the area of the disk corresponds to the area of the region.
 * 
 * @author dlegland
 *
 */
public class LabelImageEquivalentDisks implements FramePlugin
{
    public LabelImageEquivalentDisks()
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
        if (!image.isLabelImage())
        {
            System.out.println("Requires label image as input");
            return;
        }
        
        // check input data type
        Array<?> array = image.getData();
        if (!(array instanceof IntArray2D))
        {
            ImagoGui.showErrorDialog(frame, "Requires a planar array of labels");
            return;
        }
        Calibration calib = image.getCalibration();

        ImagoGui gui = frame.getGui();
        
        GenericDialog dlg = new GenericDialog(frame, "Equivalent Disks");
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = ImageHandle.getAllNames(gui.getAppli());
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        boolean showTable = dlg.getNextBoolean();
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        
        // Extract ellipses
        IntArray2D<?> array2d = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(array2d);
        Ellipse2D[] ellipses = RegionAnalysis2D.equivalentEllipses(array2d, labels, calib);
         
        if (showTable)
        {
            // Convert ellipse to table, and display
            Table table = Table.create(ellipses.length, 3);
            table.setColumnNames(new String[]{"Center.X", "Center.Y", "Radius"});
            for (int i = 0; i < ellipses.length; i++)
            {
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                table.setValue(i, 0, center.x());
                table.setValue(i, 1, center.y());
                // equivalent radius obtained from ellipse area
                double radius = Math.sqrt(elli.semiMajorAxisLength() * elli.semiMinorAxisLength());
                table.setValue(i, 2, radius);
            }
            
            // add the new frame to the GUI
            table.setName(ObjectHandle.appendSuffix(image.getName(), "eqvdisks"));
            TableFrame.create(table, frame);
        }
        
        if (overlay)
        {
            ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);
            
            // add to the document
            for (int i = 0; i < ellipses.length; i++)
            {
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                double radius = Math.sqrt(elli.semiMajorAxisLength() * elli.semiMinorAxisLength());
                handle.addShape(new Shape(new Circle2D(center, radius)));
            }
            
            // update viewers
            handle.notifyImageHandleChange();
        }
    }
}
