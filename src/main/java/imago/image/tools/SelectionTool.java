/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import net.sci.geom.geom2d.Point2D;

/**
 * The default tool for choosing pixel on an image.
 * 
 * @author David Legland
 *
 */
public class SelectionTool extends ImageTool
{

    double x1, y1;
    double x2, y2;
    int state = 0;

    /**
     * Default constructor.
     * 
     * @param viewer
     *            reference to the image viewer frame
     * @param name
     *            the name of this tool
     */
    public SelectionTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }

    /**
     * When the button is pressed, the current mouse position is registered, and
     * state of the tool is changed.
     */
    @Override
    public void mousePressed(MouseEvent evt)
    {
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        double x = pos.x();
        double y = pos.y();

        System.out.println("Mouse pressed at (" + x + " ; " + y);
    }
}
