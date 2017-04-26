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
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.line.LineSegment2D;

/**
 * Select a line segment on a planar viewer
 * 
 * @author David Legland
 *
 */
public class SelectLineSegmentTool extends ImagoTool
{
    // Starting point 
    double x1, y1;
    // ending point
    double x2, y2;
    
    // state: 0 for first point, 1 for second point.
    int state = 0;
    
    public SelectLineSegmentTool(ImagoDocViewer viewer, String name)
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
        double x = pos.getX();
        double y = pos.getY();
        
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
            
            if ((this.viewer.getImageView() instanceof PlanarImageViewer))
            {
                LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x2, y2));
                
                PlanarImageViewer piv = (PlanarImageViewer) this.viewer.getImageView();
                piv.setSelection(line);
                display.setSelection(line);
            }  
        }
        this.viewer.repaint();
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
        double x = pos.getX();
        double y = pos.getY();
     
        if ((this.viewer.getImageView() instanceof PlanarImageViewer))
        {
            LineSegment2D line = new LineSegment2D(new Point2D(x1, y1), new Point2D(x, y));
            
//            PlanarImageViewer piv = (PlanarImageViewer) this.viewer.getImageView();
            display.setSelection(line);
            this.viewer.repaint();
        }  
        

//        // check point is within image bounds
//        Image image = this.viewer.getImageView().getImage();
//        int sizeX = image.getSize(0);
//        int sizeY = image.getSize(1);
//        if (x < -.5 || x > sizeX - .5)
//        {
//            return;
//        }
//        if (y < -.5 || y > sizeY - .5)
//        {
//            return;
//        }
//        
//        Point2D p0 = display.imageToDisplay(new Point2D(this.x1, this.y1));
//        int x0 = (int) p0.getX(); 
//        int y0 = (int) p0.getY();
        
//        display.repaint();
//        display.getGraphics().setColor(Color.YELLOW);
//        display.getGraphics().drawLine(x0, y0, evt.getX(), evt.getY());
    }

}
