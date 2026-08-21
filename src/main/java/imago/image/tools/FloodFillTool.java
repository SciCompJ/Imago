/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.app.UserPreferences;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import imago.image.viewers.XYImageViewer;
import net.sci.array.Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.image.morphology.FloodFill;

/**
 * Flood-fills the current value within the image from the clicked point.
 * 
 * Requires scalar image.
 * 
 * @author dlegland
 *
 */
public class FloodFillTool extends ImageTool
{
    /**
     * Basic constructor.
     * 
     * @param viewer
     *            reference to the mage viewer
     * @param name
     *            the name of this tool
     */
    public FloodFillTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
        System.out.println("flood-fill");
        // check viewer class
        if (!(this.frame.getImageViewer() instanceof XYImageViewer)) return;
        XYImageViewer viewer = (XYImageViewer) this.frame.getImageViewer();
        
        // retrieve image data
        Image image = viewer.getImage();
        Array<?> array = image.getData();
        if (!array.isModifiable())
        {
            return;
        }
        
        // 
        if (!(array.elementInstanceOf(Scalar.class)))
        {
            return;
        }

        // Coordinates of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        double x = pos.x();
        double y = pos.y();
        
        // check position is within array bounds
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        if (xi < 0 || yi < 0) return;
        if (xi >= array.size(0) || yi >= array.size(1)) return;
        
        UserPreferences prefs = frame.getGui().getAppli().userPreferences;
        double value = prefs.brushValue;

        if (array.dimensionality() == 2)
        {
            ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
            FloodFill.floodFill(array2d, xi, yi, value, 4);
        }
        else if (array.dimensionality() == 3)
        {
            ScalarArray3D<?> array3d = ScalarArray3D.wrap((ScalarArray<?>) array);
            int zi = viewer.getSlicingPosition(2);
            FloodFill.floodFill(array3d, xi, yi, zi, value, 6);
        }
        
        viewer.refreshDisplay();
        this.frame.repaint();
    }
}
