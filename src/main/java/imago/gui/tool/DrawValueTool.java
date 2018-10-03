/**
 * 
 */
package imago.gui.tool;

import imago.gui.ImagoDocViewer;
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
 * Draw selected value on current position when user clicks.
 * 
 * Requires scalar image.
 * 
 * @author dlegland
 *
 */
public class DrawValueTool extends ImagoTool
{
    /** The value to draw */
    double value;
    
    /**
     * Basic constructor, using white color as default.
     * 
     * @param viewer
     *            reference to the mage viewer
     * @param name
     *            the name of this tool
     */
    public DrawValueTool(ImagoDocViewer viewer, String name)
    {
        this(viewer, name, Double.MAX_VALUE);
    }

    /**
     * @param viewer
     *            reference to the mage viewer
     * @param name
     *            the name of this tool
     * @param value
     *            the value to draw on current point
     */
    public DrawValueTool(ImagoDocViewer viewer, String name, double value)
    {
        super(viewer, name);
        this.value = value;
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
        
        // convert to array coord
        // TODO: manage spatial calibration
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        if (xi < 0 || yi < 0) return;
        if (xi < 0 || yi < 0) return;
        if (xi >= array.getSize(0) || yi > array.getSize(1)) return;

        if (array.dimensionality() == 2)
        {
            ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
            array2d.setValue(xi, yi, this.value);
        }
        else if (array.dimensionality() == 3)
        {
            ScalarArray3D<?> array3d = ScalarArray3D.wrap((ScalarArray<?>) array);
            // TODO: check class or use abstraction
            int zi = ((StackSliceViewer) this.viewer.getImageView()).getSliceIndex();
            array3d.setValue(xi, yi, zi, this.value);
        }
        
        this.viewer.getImageView().refreshDisplay();
        this.viewer.repaint();
    }

}
