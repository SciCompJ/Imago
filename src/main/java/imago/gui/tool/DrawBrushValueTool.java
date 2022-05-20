/**
 * 
 */
package imago.gui.tool;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.gui.ImagoTool;
import imago.gui.frames.ImageFrame;
import imago.gui.viewer.ImageDisplay;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;

/**
 * Draw current value as a large dot around current position when user clicks.
 * 
 * Requires scalar image.
 * 
 * @author dlegland
 *
 */
public class DrawBrushValueTool extends ImagoTool
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
     * Basic constructor.
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
     * @see imago.gui.ImagoTool#select()
     */
    @Override
    public void select()
    {
    }

    /* (non-Javadoc)
     * @see imago.gui.ImagoTool#deselect()
     */
    @Override
    public void deselect()
    {
    }


    // =============================================================
    // Implementation of the MouseListener and MouseMotionListener methods

    @Override
    public void mousePressed(MouseEvent evt)
    {
        // retrieve image data
        Image image = this.viewer.getImageView().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            return;
        }
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        
//        System.out.println("[DrawValue] Mouse pressed at (" + x + " ; " + y);
        
        // convert to array coord
        int xi = (int) Math.round(pos.getX());
        int yi = (int) Math.round(pos.getY());
        
        // check position is within array bounds
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        if (xi < 0 || yi < 0) return;
        if (xi >= sizeX || yi >= sizeY) return;
        
        // keep coordinates for next mouse move
        xprev = xi;
        yprev = yi;
        
        double value = this.viewer.getGui().userPreferences.brushValue;
        double radius = this.viewer.getGui().userPreferences.brushRadius;
        double r2 = (radius + 0.5) * (radius + 0.5);
        int ri = (int) Math.ceil(radius);
        
        // select the 2D array to update
        ScalarArray2D<?> array2d = wrapArray(array);
        
        // update the array
        for (int y2 = yi - ri; y2 <= yi + ri; y2++)
        {
            if (y2 < 0 || y2 > sizeY-1) continue;
            double dy2 = (y2 - yi) * (y2 - yi);
            
            for (int x2 = xi - ri; x2 <= xi + ri; x2++)
            {
                if (x2 < 0 || x2 > sizeX-1) continue;
                
                double d2 = (x2 - xi) * (x2 - xi) + dy2;
                if (d2 < r2)
                {
                    array2d.setValue(x2, y2, value);
                }
            }
        }
        
        // refresh display
        this.viewer.getImageView().refreshDisplay();
        this.viewer.repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent evt)
    {
        // retrieve image data
        Image image = this.viewer.getImageView().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            return;
        }
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        
        // wrap to scalar 2D
        ScalarArray2D<?> array2d = wrapArray(array);
        
        // convert to array coord
        int xi = (int) Math.round(pos.getX());
        int yi = (int) Math.round(pos.getY());
        
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
        double value = this.viewer.getGui().userPreferences.brushValue;
        double radius = this.viewer.getGui().userPreferences.brushRadius;
        
        drawLineOnArray(array2d, xprev, yprev, xi, yi, radius, value);
        
        // keep coordinates for next mouse move
        xprev = xi;
        yprev = yi;
        
        // refresh display
        this.viewer.getImageView().refreshDisplay();
        this.viewer.repaint();
    }
    
    private void drawLineOnArray(ScalarArray2D<?> array, int x1, int y1, int x2, int y2, double radius, double value)
    {
        // array size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        
        // compute integer radius
        int ri = (int) Math.round(radius);

        // first determines if line is mostly horizontal or mostly vertical
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (Math.abs(dx) > Math.abs(dy))
        {
            // Process horizontal line
            double slope = dy / dx;
                    
            // ensure positive increment of y
            if (x1 > x2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
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
        }
        else
        {
            // Process vertical line
            double slope = dx / dy;
                    
            // ensure positive increment of y
            if (y1 > y2)
            {
                // swap coords
                int tmp = y1; y1 = y2; y2 = tmp;
                tmp = x1; x1 = x2; x2 = tmp;
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
        }
    }
    
    private ScalarArray2D<?> wrapArray(Array<?> array)
    {
        // select the 2D array to update
        if (array.dimensionality() == 2)
        {
            return ScalarArray2D.wrap((ScalarArray<?>) array);
        }
        else if (array.dimensionality() == 3)
        {
            int zi = this.viewer.getImageView().getSlicingPosition(2);
            return ScalarArray3D.wrap((ScalarArray<?>) array).slice(zi);
        }
        
        throw new RuntimeException("Requires either a 2D or a 3D array");
    }
}
