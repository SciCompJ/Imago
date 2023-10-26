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
import net.sci.geom.geom2d.polygon.Polyline2D;

/**
 * Select a polyline region of interest on a planar viewer.
 * 
 * @author dlegland
 *
 */
public class SelectPolylineTool extends ImageTool
{
    ArrayList<Point2D> selectedPoints = new ArrayList<Point2D>();
    
    /** 
     * The point that was clicked, in gui pixel coordinates
     */
    Point lastClickedPoint = null;

    /**
     * The current state of the algorithm.
     *  
     * When true, points are added to the polyline. 
     * When false, a new point will start a new polyline.
     */
    boolean polylineStarted = false;
    
    public SelectPolylineTool(ImageFrame viewer, String name)
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
        System.out.println("selected the 'selectPolyline' tool");
        
        this.selectedPoints.clear();
        this.lastClickedPoint = null;
        this.polylineStarted = false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.ImagoTool#deselect()
     */
    @Override
    public void deselect()
    {
        System.out.println("deselected the 'selectPolyline' tool");
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
            this.polylineStarted = false;
        }
        else if (this.polylineStarted)
        {
            // if polygon was created, update it
            this.selectedPoints.add(pos);
        }
        else
        {
            // create a new polygon
            this.selectedPoints.clear();
            this.selectedPoints.add(pos);
            this.polylineStarted = true;
        }
        
        // creates a new polyline for selection
        Polyline2D poly = Polyline2D.create(selectedPoints, false);

        display.setSelection(poly);
        this.viewer.getImageView().setSelection(poly);
        
        this.viewer.repaint();
    }
    
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (!this.polylineStarted)
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
        display.setSelection(Polyline2D.create(selectedPoints, false));
        this.selectedPoints.remove(nv);
        
        this.viewer.repaint();
    }

}
