/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.ObjectHandle;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.geom2d.polygon.OrientedBox2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalyzer;
import net.sci.image.analyze.region2d.OrientedBoundingBox2D;
import net.sci.image.label.LabelImages;
import net.sci.table.Table;

/**
 * Computes the oriented bounding box of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageOrientedBoxes implements FramePlugin
{
    public LabelImageOrientedBoxes()
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

        // check input data type
        if (!(array instanceof IntArray2D))
        {
            System.out.println("Requires a planar array of ints");
            return;
        }

        ImagoGui gui = frame.getGui();

        GenericDialog dlg = new GenericDialog(frame, "Oriented Boxes");
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


        // Extract bounding boxes as an array of OrientedBox2D instances
        IntArray2D<?> array2d = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(array2d);

        OrientedBoundingBox2D analyzer = new OrientedBoundingBox2D();
        OrientedBox2D[] boxes = analyzer.analyzeRegions(array2d, labels, calib);

        if (showTable)
        {
            // Convert bounds to table, and display
            Table table = analyzer.createTable(RegionAnalyzer.createMap(labels, boxes));
            table.setName(ObjectHandle.appendSuffix(image.getName(), "obox"));

            // add the new frame to the GUI
            TableFrame.create(table, frame);
        }

        if (overlay)
        {
            ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);

            // add to the document
            int nBoxes = boxes.length;
            for (int i = 0; i < nBoxes; i++)
            {
                handle.addShape(new Shape(boxes[i]));
            }
            
            // update viewers
            handle.notifyImageHandleChange();
        }
    }
}
