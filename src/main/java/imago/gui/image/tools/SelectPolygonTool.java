/**
 * 
 */
package imago.gui.image.tools;

import imago.gui.image.ImageDisplay;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageTool;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.geom.geom2d.polygon.DefaultPolygon2D;

/**
 * Select a polygon region of interest on a planar viewer.
 * 
 * @author David Legland
 *
 */
public class SelectPolygonTool extends ImageTool
{
    ArrayList<Point2D> selectedPoints = new ArrayList<Point2D>();
    
    /** 
     * The point that was clicked, in gui pixel coordinates
     */
    Point lastClickedPoint = null;

    /**
     * The current state of the algorithm.
     *  
     * When true, points are added to the polygon. 
     * When false, a new point will start a new polygon.
     */
    boolean polygonStarted = false;
    
    public SelectPolygonTool(ImageFrame viewer, String name)
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
        System.out.println("selected the 'selectPolygon' tool");
        
        this.selectedPoints.clear();
        this.lastClickedPoint = null;
        this.polygonStarted = false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.ImagoTool#deselect()
     */
    @Override
    public void deselect()
    {
        System.out.println("deselected the 'selectPolygon' tool");
        this.selectedPoints.clear();
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

        // check termination condition
        boolean doubleClick = point.equals(this.lastClickedPoint);
        this.lastClickedPoint = point;
        
        // convert point to image pixel coordinates
        Point2D pos = display.displayToImage(point);
        
        if (doubleClick)
        {
            // if clicked twice on the same point, close the polygon and add it to selection
            this.polygonStarted = false;
        }
        else if (this.polygonStarted)
        {
            // if polygon was created, update it
            this.selectedPoints.add(pos);
        }
        else
        {
            // create a new polygon
            this.selectedPoints.clear();
            this.selectedPoints.add(pos);
            this.polygonStarted = true;
        }
        
        // creates a new polygon for selection
        Polygon2D poly = Polygon2D.create(selectedPoints);
        
        // if new polygon is created, ensures signed area > 0
        if (doubleClick)
        {
           if (poly.signedArea() < 0) poly = poly.complement();
        }

        display.setSelection(poly);
        this.frame.getImageViewer().setSelection(poly);
        
        this.frame.repaint();
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (!this.polygonStarted)
        {
            return;
        }
        
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        
        // convert point to image pixel coordinates
        Point2D pos = display.displayToImage(point);
        
        // update vertices, add corresponding polygon, and remove last vertex
        int nv = this.selectedPoints.size();
        this.selectedPoints.add(pos);
        display.setSelection(new DefaultPolygon2D(selectedPoints));
        this.selectedPoints.remove(nv);
        
        this.frame.repaint();
    }

}
