/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;

import imago.image.ImageDisplay;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import imago.image.PlanarImageViewer;
import imago.image.StackSliceViewer;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;

/**
 * Select a line segment on a planar viewer
 * 
 * @author David Legland
 *
 */
public class SelectLineSegmentTool extends ImageTool
{
    // Starting point 
    double x1, y1;
    // ending point
    double x2, y2;
    
    // state: 0 for first point, 1 for second point.
    int state = 0;
    
    public SelectLineSegmentTool(ImageFrame viewer, String name)
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
        System.out.println("selected the 'selectLineSegment' tool");
        
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
        System.out.println("deselected the 'selectLineSegment' tool");
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
        
        System.out.println("[selectLineSegment] Mouse pressed at (" + x + " ; " + y);
        
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
            this.state = 0;
            ImageViewer imageView = this.frame.getImageViewer(); 
            if (imageView instanceof PlanarImageViewer)
            {
                LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x2, y2));
                
//                PlanarImageViewer piv = (PlanarImageViewer) imageViewer;
                imageView .setSelection(line);
                display.setSelection(line);
            }
            else if (imageView instanceof StackSliceViewer)
            {
                LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x2, y2));
                
//                PlanarImageViewer piv = (PlanarImageViewer) this.viewer.getImageView();
                imageView .setSelection(line);
                display.setSelection(line);
            }  
        }
        this.frame.repaint();
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (this.state == 0)
        {
            return;
        }
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        Point2D pos = display.displayToImage(point);
        double x = pos.x();
        double y = pos.y();
     
        ImageViewer imageView = this.frame.getImageViewer(); 
        if (imageView instanceof PlanarImageViewer)
        {
            LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x, y));
            display.setSelection(line);
            this.frame.repaint();
        }  
        else if (imageView instanceof StackSliceViewer)
        {
            LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x, y));
            display.setSelection(line);
            this.frame.repaint();
        }  
    }

}
