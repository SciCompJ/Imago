/**
 * 
 */
package imago.gui.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.app.UserPreferences;
import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImagoTool;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.ScalarArray3D;
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
public class DrawValueTool extends ImagoTool
{
    /**
     * Basic constructor.
     * 
     * @param viewer
     *            reference to the mage viewer
     * @param name
     *            the name of this tool
     */
    public DrawValueTool(ImageFrame viewer, String name)
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
        double x = pos.x();
        double y = pos.y();
        
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
        
        UserPreferences prefs = viewer.getGui().getAppli().userPreferences;
        double value = prefs.brushValue;

        if (array.dimensionality() == 2)
        {
            ScalarArray2D<?> array2d = ScalarArray2D.wrap((ScalarArray<?>) array);
            array2d.setValue(xi, yi, value);
        }
        else if (array.dimensionality() == 3)
        {
            ScalarArray3D<?> array3d = ScalarArray3D.wrap((ScalarArray<?>) array);
            int zi = this.viewer.getImageView().getSlicingPosition(2);
            array3d.setValue(xi, yi, zi, value);
        }
        
        this.viewer.getImageView().refreshDisplay();
        this.viewer.repaint();
    }

}