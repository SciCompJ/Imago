/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.Polygon2D;

/**
 * Select a polygon region of interest on a planar viewer.
 * 
 * @author David Legland
 *
 */
public class SelectPolygonTool extends ImageTool
{
    enum State
    {
        REST,
        POLYGON_STARTED;
    };
    
    ArrayList<Point2D> selectedPoints = new ArrayList<Point2D>();
    
    /** 
     * The point that was clicked, in gui pixel coordinates
     */
    Point lastClickedPoint = null;

    /**
     * The current state of the algorithm.
     */
    State state = State.REST;
    
    // creates a new polygon for selection
    Polygon2D currentPolygon = null;

    
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
        this.state = State.REST;
        this.currentPolygon = null;
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
        
        switch (this.state)
        {
            case REST:
            {
                // create a new polygon
                this.selectedPoints.clear();
                this.selectedPoints.add(pos);
                this.state = State.POLYGON_STARTED;
                break;
            }
            
            case POLYGON_STARTED:
            {
                if (!doubleClick)
                {
                    // update polygon
                    this.selectedPoints.add(pos);
                    this.currentPolygon = Polygon2D.create(selectedPoints);
                }
                else
                {
                    // if clicked twice on the same point, close the polygon
                    // using the point of first click
                    this.currentPolygon = Polygon2D.create(selectedPoints);
                    
                    // ensure positive signed area
                    if (this.currentPolygon.signedArea() < 0)
                    {
                        this.currentPolygon = this.currentPolygon.complement();
                    }
                    
                    // reset to restful state
                    this.state = State.REST;
                }

                display.setSelection(currentPolygon);
                this.frame.getImageViewer().setSelection(currentPolygon);
                this.frame.repaint();
                
                break;
            }
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (this.state == State.POLYGON_STARTED)
        {
            // Coordinate of mouse cursor
            ImageDisplay display = (ImageDisplay) evt.getSource();
            Point point = new Point(evt.getX(), evt.getY());

            // convert point to image pixel coordinates
            Point2D pos = display.displayToImage(point);

            // update vertices, add corresponding polygon, and remove last vertex
            int nv = this.selectedPoints.size();
            this.selectedPoints.add(pos);
            display.setSelection(Polygon2D.create(selectedPoints));
            this.selectedPoints.remove(nv);

            this.frame.repaint();
        }
    }

}
