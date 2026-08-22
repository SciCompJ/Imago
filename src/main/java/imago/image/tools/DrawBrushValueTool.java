/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import imago.image.viewers.ImageDisplay;
import imago.image.viewers.PlanarImageViewer;
import imago.image.viewers.StackSliceViewer;
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
import net.sci.geom.geom2d.curve.Circle2D;
import net.sci.image.Image;

/**
 * Draw current value as a large dot around current position when user clicks.
 * 
 * Requires scalar image.
 * 
 * @author dlegland
 *
 */
public class DrawBrushValueTool extends ImageTool
{
    // =============================================================
    // Class fields

     /** The x-coordinate of the previous mouse position (in array coordinates) */
    int xprev = 0;
    
    /** The y-coordinate of the previous mouse position (in array coordinates) */
    int yprev = 0;
    
 
    // =============================================================
    // Constructor

     /**
     * Default constructor.
     * 
     * @param viewer
     *            reference to the mage viewer
     * @param name
     *            the name of this tool
     */
    public DrawBrushValueTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }

 
    // =============================================================
    // Implementation of the ImagoTool methods

    /* (non-Javadoc)
     * @see imago.gui.ImagoTool#deselect()
     */
    @Override
    public void deselect()
    {
        this.frame.repaint();
    }


    // =============================================================
    // Implementation of the MouseListener and MouseMotionListener methods

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

        // Coordinates of mouse cursor
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

        // keep coordinates for next mouse move
        xprev = xi;
        yprev = yi;

        Preferences prefs = Preferences.userNodeForPackage(getClass());
        double radius = prefs.getDouble("BrushRadius", 5);
        double r2 = (radius + 0.5) * (radius + 0.5);
        int ri = (int) Math.floor(radius);

        Array2D<?> slice = viewer.getCurrentSlice();
        if (slice.elementInstanceOf(Scalar.class))
        {
            // convert slice to a scalar array view
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray2D<?> array2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<? extends Scalar>) slice));
            double value = prefs.getDouble("BrushValue", 255);

            // update the array
            for (int y2 = yi - ri; y2 <= yi + ri; y2++)
            {
                if (y2 < 0 || y2 > sizeY-1) continue;
                int deltaY = y2 - yi;
                int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                fillHorizontalInterval(array2d, Math.max(xi - deltaX, 0), Math.min(xi + deltaX, sizeX - 1), y2, value);
            }
        }
        else if (slice.elementInstanceOf(RGB8.class))
        {
            // convert slice to an RGB8 array view
            RGB8Array2D array2d = RGB8Array2D.wrap(RGB8Array.wrap(slice));
            RGB8 rgbValue = RGB8.fromIntCode(prefs.getInt("BrushColorIntCode", 0xFFFFFF));
            
            // update the array
            for (int y2 = yi - ri; y2 <= yi + ri; y2++)
            {
                if (y2 < 0 || y2 > sizeY-1) continue;
                int deltaY = y2 - yi;
                int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                fillHorizontalInterval(array2d, Math.max(xi - deltaX, 0), Math.min(xi + deltaX, sizeX - 1), y2, rgbValue);
            }
        }

        // refresh display
        viewer.refreshDisplay();
        this.frame.repaint();

        // propagate to other viewers
        viewer.getImageHandle().notifyImageHandleChange(ImageHandle.Event.CHANGE_MASK | ImageHandle.Event.IMAGE_MASK);
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        // retrieve image data
        Image image = this.frame.getImageViewer().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
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
        
        // update cursor display
        updateCursor(xi, yi);
        
        this.frame.repaint();
    }
    
    @Override
    public void mouseExited(MouseEvent evt)
    {
        ImageViewer viewer = this.frame.getImageViewer();
        if (this.frame.getImageViewer() instanceof PlanarImageViewer)
        {
            ((PlanarImageViewer) viewer).getImageDisplay().setCustomCursor(null);
        }
        else if (this.frame.getImageViewer() instanceof StackSliceViewer)
        {
            ((StackSliceViewer) viewer).getImageDisplay().setCustomCursor(null);
        }
        this.frame.repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent evt)
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
        
        // check case of movement within pixel 
        if (xi == xprev && yi == yprev)
        {
            return;
        }
        
        // check position is within array bounds
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        if (xi < 0 || yi < 0) return;
        if (xi >= sizeX || yi >= sizeY) return;
        
        // retrieve brush settings
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        double radius = prefs.getDouble("BrushRadius", 5);

        Array2D<?> slice = viewer.getCurrentSlice();
        if (slice.elementInstanceOf(Scalar.class))
        {
            // convert slice to a scalar array view
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ScalarArray2D<?> array2d = ScalarArray2D.wrap(ScalarArray.wrap((Array<? extends Scalar>) slice));
            
            double value = prefs.getDouble("BrushValue", 255);
            // use a faster processing for scalar arrays
            drawLineOnScalarArray(array2d, xprev, yprev, xi, yi, radius, value);
        }
        else if (slice.elementInstanceOf(RGB8.class))
        {
            // select the 2D array to update
            RGB8Array2D array2d = RGB8Array2D.wrap(RGB8Array.wrap(slice));
            RGB8 rgbValue = RGB8.fromIntCode(prefs.getInt("BrushColorIntCode", 0xFFFFFF));
            drawLineOnArray(array2d, xprev, yprev, xi, yi, radius, rgbValue);
        }
        
        // keep coordinates for next mouse move
        xprev = xi;
        yprev = yi;
        
        // refresh display
        updateCursor(xi, yi);
        
        viewer.getImageHandle().notifyImageHandleChange(ImageHandle.Event.CHANGE_MASK | ImageHandle.Event.IMAGE_MASK);
        viewer.refreshDisplay();
        this.frame.repaint();
    }
    
    private void updateCursor(int xi, int yi)
    {
        // create cursor shape
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        double radius = prefs.getDouble("BrushRadius", 5) + 0.5;
        Circle2D cursor = new Circle2D(new Point2D(xi+0.5, yi+0.5), radius);
        
        ImageViewer viewer = this.frame.getImageViewer();
        if (this.frame.getImageViewer() instanceof PlanarImageViewer)
        {
            ((PlanarImageViewer) viewer).getImageDisplay().setCustomCursor(cursor);
        }
        else if (this.frame.getImageViewer() instanceof StackSliceViewer)
        {
            ((StackSliceViewer) viewer).getImageDisplay().setCustomCursor(cursor);
        }        
    }
    
    private static void drawLineOnScalarArray(ScalarArray2D<?> array, int x1, int y1, int x2, int y2, double radius, double value)
    {
        // array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // compute integer radius
        int ri = (int) Math.floor(radius);
        
        // compute squared radius (consider diameter = 2*radius+1)
        double r2 = (radius + 0.5) * (radius + 0.5);
        
        // first determines if line is mostly horizontal or mostly vertical
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (Math.abs(dx) > Math.abs(dy))
        {
            // Process horizontal line
            double slope = dy / dx;
                    
            // ensure positive increment of y
            boolean swap = false;
            if (x1 > x2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
                swap = true;
            }
            
            // draw a thick (mostly horizontal) line between extremities
            for (int x = x1; x <= x2; x++)
            {
                int yc = (int) Math.round(y1 + slope * (x - x1));
                for (int y = yc - ri; y <= yc + ri; y++)
                {
                    if (y < 0 || y >= sizeY) continue;
                    array.setValue(x, y, value);
                }
            }
            
            // draw a half-disk after the end of the line
            if (swap)
            {
                // draw half disk for x < x1
                for (int x = x1 - 1; x >= x1 - ri; x--)
                {
                    if (x <= 0) break;
                    int deltaX = x1 - x;
                    int deltaY = (int) Math.floor(Math.sqrt(r2 - deltaX * deltaX));
                    fillVerticalInterval(array, x, Math.max(y1 - deltaY, 0), Math.min(y1 + deltaY, sizeY - 1), value);
                }
            }
            else
            {
                // draw half disk for x > x2
                for (int x = x2 + 1; x <= x2 + ri; x++)
                {
                    if (x >= sizeX) break;
                    int deltaX = x - x2;
                    int deltaY = (int) Math.floor(Math.sqrt(r2 - deltaX * deltaX));
                    fillVerticalInterval(array, x, Math.max(y2 - deltaY, 0), Math.min(y2 + deltaY, sizeY - 1), value);
                }
            }
        }
        else
        {
            // Process vertical line
            double slope = dx / dy;
                    
            // ensure positive increment of y
            boolean swap = false;
            if (y1 > y2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
                swap = true;
            }
            
            // draw a thick (mostly vertical) line between extremities
            for (int y = y1; y <= y2; y++)
            {
                int xc = (int) Math.round(x1 + slope * (y - y1));
                for (int x = xc - ri; x <= xc + ri; x++)
                {
                    if (x < 0 || x >= sizeX) continue;
                    array.setValue(x, y, value);
                }
            }
            
            // draw a half-disk after the end of the line
            if (swap)
            {
                // draw half disk for y < y1
                for (int y = y1 - 1; y >= y1 - ri; y--)
                {
                    if (y <= 0) break;
                    int deltaY = y1 - y;
                    int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                    fillHorizontalInterval(array, Math.max(x1 - deltaX, 0), Math.min(x1 + deltaX, sizeX - 1), y, value);
                }
            }
            else
            {
                // draw half disk for y > y2
                for (int y = y2 + 1; y <= y2 + ri; y++)
                {
                    if (y >= sizeY) break;
                    int deltaY = y - y2;
                    int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                    fillHorizontalInterval(array, Math.max(x2 - deltaX, 0), Math.min(x2 + deltaX, sizeX - 1), y, value);
                }
            }
        }
    }
    
    private static void fillVerticalInterval(ScalarArray2D<?> array, int x, int y1, int y2, double value)
    {
        for (int y = y1; y <= y2; y++)
        {
            array.setValue(x, y, value);
        }
    }
    
    private static void fillHorizontalInterval(ScalarArray2D<?> array, int x1, int x2, int y, double value)
    {
        for (int x = x1; x <= x2; x++)
        {
            array.setValue(x, y, value);
        }
    }
    
    private static <T> void drawLineOnArray(Array2D<T> array, int x1, int y1, int x2, int y2, double radius, T value)
    {
        // array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // compute integer radius
        int ri = (int) Math.floor(radius);
        
        // compute squared radius (consider diameter = 2*radius+1)
        double r2 = (radius + 0.5) * (radius + 0.5);
        
        // first determines if line is mostly horizontal or mostly vertical
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (Math.abs(dx) > Math.abs(dy))
        {
            // Process horizontal line
            double slope = dy / dx;
                    
            // ensure positive increment of y
            boolean swap = false;
            if (x1 > x2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
                swap = true;
            }
            
            // draw a thick (mostly horizontal) line between extremities
            for (int x = x1; x <= x2; x++)
            {
                int yc = (int) Math.round(y1 + slope * (x - x1));
                for (int y = yc - ri; y <= yc + ri; y++)
                {
                    if (y < 0 || y >= sizeY) continue;
                    array.set(x, y, value);
                }
            }
            
            // draw a half-disk after the end of the line
            if (swap)
            {
                // draw half disk for x < x1
                for (int x = x1 - 1; x >= x1 - ri; x--)
                {
                    if (x <= 0) break;
                    int deltaX = x1 - x;
                    int deltaY = (int) Math.floor(Math.sqrt(r2 - deltaX * deltaX));
                    fillVerticalInterval(array, x, Math.max(y1 - deltaY, 0), Math.min(y1 + deltaY, sizeY - 1), value);
                }
            }
            else
            {
                // draw half disk for x > x2
                for (int x = x2 + 1; x <= x2 + ri; x++)
                {
                    if (x >= sizeX) break;
                    int deltaX = x - x2;
                    int deltaY = (int) Math.floor(Math.sqrt(r2 - deltaX * deltaX));
                    fillVerticalInterval(array, x, Math.max(y2 - deltaY, 0), Math.min(y2 + deltaY, sizeY - 1), value);
                }
            }
        }
        else
        {
            // Process vertical line
            double slope = dx / dy;
                    
            // ensure positive increment of y
            boolean swap = false;
            if (y1 > y2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
                swap = true;
            }
            
            // draw a thick (mostly vertical) line between extremities
            for (int y = y1; y <= y2; y++)
            {
                int xc = (int) Math.round(x1 + slope * (y - y1));
                for (int x = xc - ri; x <= xc + ri; x++)
                {
                    if (x < 0 || x >= sizeX) continue;
                    array.set(x, y, value);
                }
            }
            
            // draw a half-disk after the end of the line
            if (swap)
            {
                // draw half disk for y < y1
                for (int y = y1 - 1; y >= y1 - ri; y--)
                {
                    if (y <= 0) break;
                    int deltaY = y1 - y;
                    int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                    fillHorizontalInterval(array, Math.max(x1 - deltaX, 0), Math.min(x1 + deltaX, sizeX - 1), y, value);
                }
            }
            else
            {
                // draw half disk for y > y2
                for (int y = y2 + 1; y <= y2 + ri; y++)
                {
                    if (y >= sizeY) break;
                    int deltaY = y - y2;
                    int deltaX = (int) Math.floor(Math.sqrt(r2 - deltaY * deltaY));
                    fillHorizontalInterval(array, Math.max(x2 - deltaX, 0), Math.min(x2 + deltaX, sizeX - 1), y, value);
                }
            }
        }
    }
    
    private static <T> void fillVerticalInterval(Array2D<T> array, int x, int y1, int y2, T value)
    {
        for (int y = y1; y <= y2; y++)
        {
            array.set(x, y, value);
        }
    }
    
    private static <T> void fillHorizontalInterval(Array2D<T> array, int x1, int x2, int y, T value)
    {
        for (int x = x1; x <= x2; x++)
        {
            array.set(x, y, value);
        }
    }
}
