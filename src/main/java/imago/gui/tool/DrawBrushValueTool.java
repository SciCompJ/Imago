/**
 * 
 */
package imago.gui.tool;

import imago.gui.ImageFrame;
import imago.gui.ImagoTool;
import imago.gui.viewer.ImageDisplay;
import imago.gui.viewer.StackSliceViewer;

import java.awt.Point;
import java.awt.event.MouseEvent;

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

    @Override
    public void mousePressed(MouseEvent evt)
    {
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        double x = pos.getX();
        double y = pos.getY();
        
        Image image = this.viewer.getImageView().getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            return;
        }
        
//        System.out.println("[DrawValue] Mouse pressed at (" + x + " ; " + y);
        
        int sizeX = array.size(0);
        int sizeY = array.size(1);

        // convert to array coord
        // TODO: manage spatial calibration
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        if (xi < 0 || yi < 0) return;
        if (xi >= sizeX || yi >= sizeY) return;
        
        double value = this.viewer.getGui().settings.brushValue;
        double radius = this.viewer.getGui().settings.brushRadius;
        double r2 = (radius + 0.5) * (radius + 0.5);
        int ri = (int) Math.ceil(radius);
        
        if (array.dimensionality() == 2)
        {
            ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
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
        }
        else if (array.dimensionality() == 3)
        {
            ScalarArray3D<?> array3d = ScalarArray3D.wrap((ScalarArray<?>) array);
            // TODO: check class or use abstraction
            int zi = ((StackSliceViewer) this.viewer.getImageView()).getSliceIndex();
            array3d.setValue(xi, yi, zi, value);
        }
        
        this.viewer.getImageView().refreshDisplay();
        this.viewer.repaint();
    }

}
