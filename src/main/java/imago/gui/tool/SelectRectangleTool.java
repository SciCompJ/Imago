/**
 * 
 */
package imago.gui.tool;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.gui.ImagoDocViewer;
import imago.gui.ImagoTool;
import imago.gui.viewer.ImageDisplay;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.geom.geom2d.Box2D;
import net.sci.geom.geom2d.Point2D;

/**
 * Select a rectangular box on a planar viewer
 * 
 * @author David Legland
 *
 */
public class SelectRectangleTool extends ImagoTool
{
    // Starting point 
    double x1, y1;
    // ending point
    double x2, y2;
    
    // state: 0 for first point, 1 for second point, to for waiting state.
    int state = 0;
    
    public SelectRectangleTool(ImagoDocViewer viewer, String name)
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
        System.out.println("selected the 'selectRectangle' tool");
        
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
        System.out.println("deselected the 'selectRectangle' tool");
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
        double x = pos.getX();
        double y = pos.getY();
        
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
            
            if ((this.viewer.getImageView() instanceof PlanarImageViewer))
            {
                Box2D box = new Box2D(x1, x2, y1, y2);
                
//                PlanarImageViewer piv = (PlanarImageViewer) this.viewer.getImageView();
//                piv.setSelection(box);
                display.setSelection(box);
            }  
        }
        else
        {
            state = 0;
            display.setSelection(null);
        }
        
        this.viewer.repaint();
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
        double x = pos.getX();
        double y = pos.getY();
     
        if ((this.viewer.getImageView() instanceof PlanarImageViewer))
        {
            Box2D line = new Box2D(x1, x, y1, y);
            display.setSelection(line);
            this.viewer.repaint();
        }  
    }

}