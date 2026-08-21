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
import net.sci.geom.polygon2d.Box2D;

/**
 * Select a rectangular box on current viewer. Can be used for slice viewer of
 * 3D images as well.
 * 
 * @author David Legland
 */
public class SelectRectangleTool extends ImageTool
{
    // Starting point 
    double x1, y1;
    // ending point
    double x2, y2;
    
    // state: 0 for first point, 1 for second point, to for waiting state.
    int state = 0;
    
    /**
     * Default constructor.
     * 
     * @param viewer
     *            reference to the image viewer frame
     * @param name
     *            the name of this tool
     */
    public SelectRectangleTool(ImageFrame viewer, String name)
    {
        super(viewer, name);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.ImagoTool#select()
     */
    @Override
    public void select()
    {
        this.state = 0;
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
        
        System.out.println("[selectRectangle] Mouse pressed at (" + x + " ; " + y);
        
        if (state == 0)
        {
            this.x1 = x;
            this.y1 = y;
            this.state = 1;
        }
        else if (state == 1)
        {
            this.x2 = x;
            this.y2 = y;
            this.state = 2;
            
            Box2D box = new Box2D(x1, x2, y1, y2);

            display.setSelection(box);
            this.frame.getImageViewer().setSelection(box);
        }
        else
        {
            state = 0;
            display.setSelection(null);
        }
        
        this.frame.repaint();
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (this.state == 0 || this.state == 2)
        {
            return;
        }
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        double x = pos.x();
        double y = pos.y();
        
        Box2D box = new Box2D(x1, x, y1, y);
        display.setSelection(box);
        this.frame.repaint();
    }

}
