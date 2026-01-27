/**
 * 
 */
package imago.image.plugins.analyze;

import imago.app.ImagoApp;
import imago.app.ObjectHandle;
import imago.app.shape.Shape;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.table.TableFrame;
import imago.gui.FramePlugin;

import java.util.Collection;

import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.curve.MultiCurve2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.label.LabelImages;
import net.sci.table.Table;

/**
 * Computes the equivalent ellipse of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageEquivalentEllipses implements FramePlugin
{
    public LabelImageEquivalentEllipses()
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
        if (!(array instanceof IntArray2D))
        {
            ImagoGui.showErrorDialog(frame, "Requires a planar array of labels");
            return;
        }
        Calibration calib = image.getCalibration();

        ImagoGui gui = frame.getGui();
        
        // open dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "Equivalent Ellipses");
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = ImageHandle.getAllNames(gui.getAppli());
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.addCheckBox("Add to Shape Manager", true);
        dlg.showDialog();
        
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
       
        // Parse dialog options
        boolean showTable = dlg.getNextBoolean();
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        boolean addToShapeManager = dlg.getNextBoolean();
        
        // Extract ellipses
        IntArray2D<?> array2d = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(array2d); 
        Ellipse2D[] ellipses = RegionAnalysis2D.equivalentEllipses(array2d, labels, calib);
         
        // Display results within a table
        if (showTable)
        {
            // Convert ellipse to table, and display
            Table table = Table.create(ellipses.length, 5);
            table.setColumnNames(new String[]{"Center.X", "Center.Y", "MajorSemiAxisLength", "MinorSemiAxisLength", "Orientation"});
            for (int i = 0; i < ellipses.length; i++)
            {
                // update row name
                table.setRowName(i, Integer.toString(labels[i]));
                
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                table.setValue(i, 0, center.x());
                table.setValue(i, 1, center.y());
                table.setValue(i, 2, elli.semiMajorAxisLength());
                table.setValue(i, 3, elli.semiMinorAxisLength());
                table.setValue(i, 4, elli.orientation());
            }
            
            // setup meta-data
            table.setName(ObjectHandle.appendSuffix(image.getName(), "ellipses"));
            table.getRowAxis().setName("Label");
            
            // add the new frame to the GUI
            TableFrame.create(table, frame);
        }
        
        // Overlay results on an image
        if (overlay)
        {
            ImageHandle handle = ImageHandle.findFromName(gui.getAppli(), imageToOverlay);
            
            // add to the document
            for (int i = 0; i < ellipses.length; i++)
            {
                handle.addShape(new Shape(ellipses[i]));
            }
            handle.notifyImageHandleChange();
        }
        
        if (addToShapeManager)
        {
            MultiCurve2D curves = new MultiCurve2D(ellipses);
            ImagoApp app = gui.getAppli();
            GeometryHandle geomHandle = GeometryHandle.create(app, curves);
            
            // opens a dialog to choose name
            String name = geomHandle.getName();
            name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
    }
}
