/**
 * 
 */
package imago.image.tools;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import imago.image.viewers.ImageDisplay;
import imago.image.viewers.XYImageViewer;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Vector2D;
import net.sci.geom.polygon2d.DefaultPolygon2D;
import net.sci.geom.polygon2d.Polygon2D;

/**
 * Select a polygon region of interest on a planar viewer.
 * 
 * Provides different states:
 * <ul>
 * <li>First click initiate creation of a new polygon</li>
 * <li>Subsequent clicks add vertices to the polygon</li>
 * <li>Double-click terminates the polygon</li>
 * <li>When the polygon is terminated, clicking again will either select the
 * polygon (the vertices become visible), or remove the polygon</li>
 * <li>When the polygon is selected, dragging the mouse will move the
 * polygon</li>
 * </ul>
 * 
 * @author David Legland
 *
 */
public class SelectPolygonTool extends ImageTool
{
    private static final double SNAP_DISTANCE = 3.0;
    
    enum State
    {
        REST,
        POLYGON_STARTED,
        SELECT_POLYGON;
    }
    
    ArrayList<Point2D> selectedPoints = new ArrayList<Point2D>();
    
    /** 
     * The point that was clicked, in gui pixel coordinates
     */
    Point lastPoint = null;

    /**
     * The current state of the algorithm.
     */
    State state = State.REST;
    
    // creates a new polygon for selection
    DefaultPolygon2D currentPolygon = null;

    
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
        this.lastPoint = null;
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

        ImageViewer viewer =  this.frame.getImageViewer();
        if (viewer instanceof XYImageViewer xyViewer)
        {
            xyViewer.getImageDisplay().drawSelectionVertices(false);
        }
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
        boolean doubleClick = point.equals(this.lastPoint);
        this.lastPoint = point;
        
        // convert point to image pixel coordinates
        Point2D pos = display.displayToImage(point);
        
//        System.out.println("State: " + state);
//        System.out.println("Position: " + pos);
//        System.out.println("double-click: " + doubleClick);
        
        switch (this.state)
        {
            case REST:
            {
                // first check if click was 'close enough' from the polygon
                if (this.currentPolygon != null)
                {
                    double dist = this.currentPolygon.distance(pos);
                    if (dist < SNAP_DISTANCE)
                    {
                        this.state = State.SELECT_POLYGON;
                        display.drawSelectionVertices(true);
                        this.frame.repaint();
                    }
                }
                else
                {
                    // clicked on an "empty" space -> create a new polygon
                    this.selectedPoints.clear();
                    this.selectedPoints.add(pos);
                    updatePolygon();
                    this.state = State.POLYGON_STARTED;
                    display.drawSelectionVertices(false);
                }
                break;
            }
            
            case POLYGON_STARTED:
            {
                if (!doubleClick)
                {
                    // update polygon
                    this.selectedPoints.add(pos);
                    updatePolygon();
                    
                    updateSelection(display, currentPolygon);
                    this.frame.repaint();
                }
                else
                {
                    // if clicked twice on the same point, close the polygon
                    // using the point of first click
                    updatePolygon();
                    Polygon2D poly = this.currentPolygon;
                    
                    // ensure positive signed area
                    if (poly.signedArea() < 0)
                    {
                        poly = poly.complement();
                    }
                    
                    updateSelection(display, poly);
                    this.frame.repaint();
                    
                    // reset to restful state
                    this.state = State.REST;
                }

                break;
            }
            
            case SELECT_POLYGON:
            {
                if (doubleClick)
                {
                    // add a new point to the current list of points
                    double t = this.currentPolygon.boundary().projectedPosition(pos);
                    int iv0 = (int) Math.floor(t);
                    this.selectedPoints.add(iv0 + 1, pos);
                    updatePolygon();

                    // update display
                    updateSelection(display, currentPolygon);
                }
                else
                {
                    double dist = this.currentPolygon.distance(pos);
                    if (dist > SNAP_DISTANCE)
                    {
                        // reset to restful state
                        this.selectedPoints.clear();
                        this.currentPolygon = null;
                        this.state = State.REST;
                        display.drawSelectionVertices(false);
                        updateSelection(display, null);
                    }
                    else
                    {
                        System.out.println("selected polygon again");
                    }
                 
                }
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
    
    @Override
    public void mouseDragged(MouseEvent evt)
    {
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());

        switch (this.state)
        {
            case SELECT_POLYGON:
            {
                // convert point to image pixel coordinates
                Point2D pos = display.displayToImage(point);
                Point2D lastPos = display.displayToImage(this.lastPoint);
                
                Polygon2D poly = this.currentPolygon.transform(AffineTransform2D.createTranslation(Vector2D.of(lastPos, pos)));
                updateSelection(display, poly);
                this.frame.repaint();
            }
            
            default:
                break;
        }
    }
    @Override
    public void mouseReleased(MouseEvent evt)
    {
        // Coordinate of mouse cursor
        ImageDisplay display = (ImageDisplay) evt.getSource();
        Point point = new Point(evt.getX(), evt.getY());
        
        switch (this.state)
        {
            case SELECT_POLYGON:
            {
                Point2D pos = display.displayToImage(point);
                Point2D lastPos = display.displayToImage(this.lastPoint);
                AffineTransform2D transfo = AffineTransform2D.createTranslation(Vector2D.of(lastPos, pos));
                
                // apply translation to each selected point
                for (int i = 0; i < selectedPoints.size(); i++)
                {
                    this.selectedPoints.set(i, this.selectedPoints.get(i).transform(transfo));
                }
                
                updatePolygon();
                updateSelection(display, this.currentPolygon);
            }
            
            default:
                break;
        }
    }

    private void updatePolygon()
    {
        this.currentPolygon = new DefaultPolygon2D(selectedPoints);
    }
    
    private void updateSelection(ImageDisplay display, Polygon2D poly)
    {
        // update display
        display.setSelection(poly);
        this.frame.getImageViewer().setSelection(poly);
    }
}
