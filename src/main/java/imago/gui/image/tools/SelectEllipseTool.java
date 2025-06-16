/**
 * 
 */
package imago.gui.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageTool;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;

/**
 * Select an ellipse on current viewer. Can be used for slice viewer of 3D
 * images as well.
 * 
 * @author David Legland
 */
public class SelectEllipseTool extends ImageTool
{
    // Starting point
    Point2D p0;
    
    // current state of the tool: 
    // 0 -> wait for first point
    // 1 -> wait for second point
    int state = 0;
    
    public SelectEllipseTool(ImageFrame viewer, String name)
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
        System.out.println("selected the 'selectEllipse' tool");
        this.state = 0;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.ImagoTool#deselect()
     */
    @Override
    public void deselect()
    {
        System.out.println("deselected the 'selectEllipse' tool");
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
        
//        double x = pos.x();
//        double y = pos.y();
//        System.out.println("[selectEllipse] Mouse pressed at (" + x + " ; " + y);
        
        if (state == 0)
        {
            this.p0 = pos;
            this.state = 1;
        }
        else if (state == 1)
        {
            // create ellipse
            Ellipse2D elli = Ellipse2D.fromCorners(p0, pos);
            
            display.setSelection(elli);
            this.frame.getImageViewer().setSelection(elli);
            
            this.state = 0;
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
        
        Ellipse2D elli = Ellipse2D.fromCorners(p0, pos);
        display.setSelection(elli);
        
        this.frame.repaint();
    }
}
