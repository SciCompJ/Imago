/**
 * 
 */
package imago.plugin.image.vectorize;

import java.util.Collection;

import imago.app.GeometryHandle;
import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.shape.ShapeManager;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.geom.geom2d.MultiPoint2D;
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
        // get current image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
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
        
        // Choose export options
        GenericDialog dlg = new GenericDialog(frame, "Find Non-Zero Pixels");
        dlg.addCheckBox("Add to Image shapes", true);
        dlg.addCheckBox("Add to Shape Manager", false);
        dlg.showDialog();

        if (dlg.wasCanceled()) return;
        boolean addToImage = dlg.getNextBoolean();
        boolean addToShapeManager = dlg.getNextBoolean();

        // compute collection of points
        ScalarArray2D<?> array2d = (ScalarArray2D<?>) array;
        Collection<Point2D> points = Find.findPixels(array2d);
        
        if (addToImage)
        {
            for (Point2D point : points)
            {
                Shape shape = new Shape(point);
                handle.addShape(shape);
            }

            // notify changes
            handle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
        }
        
        if (addToShapeManager)
        {
            ImagoApp app = frame.getGui().getAppli();
            MultiPoint2D multiPoint = MultiPoint2D.create(points);
            GeometryHandle geomHandle = GeometryHandle.create(app, multiPoint);
            
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
