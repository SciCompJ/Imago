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
import net.sci.array.Array2D;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * Draw current value on current position when user clicks.
 * 
 * Requires scalar image.
 * 
 * @author dlegland
 *
 */
public class DrawValueTool extends ImageTool
{
    /**
     * Default constructor.
     * 
     * @param viewer
     *            reference to the image viewer frame
     * @param name
     *            the name of this tool
     */
    public DrawValueTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
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
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        
        // convert to array coord
        int xi = (int) Math.round(pos.x());
        int yi = (int) Math.round(pos.y());
        
        // check position is within array bounds
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        if (xi < 0 || yi < 0) return;
        if (xi >= sizeX || yi >= sizeY) return;
        
        UserPreferences prefs = frame.getGui().getAppli().userPreferences;
        
        Array2D<?> slice = viewer.getCurrentSlice();
        if (slice.elementInstanceOf(Scalar.class))
        {
            // convert slice to a scalar array view
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray2D<?> array2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<? extends Scalar>) slice));
            // update array
            double value = prefs.brushValue;
            array2d.setValue(xi, yi, value);
        } 
        else if (slice.elementInstanceOf(RGB8.class))
        {
            // convert slice to an RGB8 array view
            RGB8Array2D array2d = RGB8Array2D.wrap(RGB8Array.wrap(slice));
            // update array
            RGB8 rgbValue = prefs.brushColor;
            array2d.set(xi, yi, rgbValue);
        }
        
        this.frame.getImageViewer().refreshDisplay();
        this.frame.repaint();
    }
}
