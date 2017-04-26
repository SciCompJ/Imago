/**
 * 
 */
package imago.gui.viewer;

import imago.app.shape.ImagoShape;
import imago.gui.ImagoDocViewer;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.line.LineSegment2D;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;


/**
 * A specialization of JPanel that displays a buffered AWT image.
 *  
 * Responsibilities for this class:
 * <ul>
 * <li>Containing an instance of BufferedImage</li>
 * <li>Managing the zoom</li>
 * <li>Computing the optimal display size and the component size</li>
 * <li>Computing the offset for image display (when zoom is small)</li>
 * <li>Managing coordinate changes between panel coords and image coords</li>
 * </ul>
 * 
 * @author David Legland
 *
 */
public class ImageDisplay extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// ===================================================================
	// Class variables

	BufferedImage image;
	
	Collection<ImagoShape> shapes = new ArrayList<ImagoShape>();
	
    /**
     * The shape of the current selection, usually a polyline or a rectangle
     */
    protected Geometry2D selection = null;
    
	double zoom = 1;
	
	int offsetX;
	int offsetY;

	
	// ===================================================================
	// Constructor
	
	public ImageDisplay(BufferedImage image) 
	{
		this.image = image;
		this.setBackground(Color.WHITE);
	}

	// ===================================================================
	// General methods

	public BufferedImage getImage() 
	{
		return this.image;
	}
	
    public Collection<ImagoShape> getShapes()
    {
        return shapes;
    }
    
    public void setShapes(Collection<ImagoShape> newShapes)
    {
        this.shapes = newShapes;
    }
    
	public void addShape(ImagoShape shape)
	{
	    this.shapes.add(shape);
	}
	
    public Geometry2D getSelection()
    {
        return this.selection;
    }
    
    public void setSelection(Geometry2D shape)
    {
        this.selection = shape;
    }
    
	public ImagoDocViewer getViewer() 
	{
		Container container = this.getParent();
		while (!(container instanceof ImagoDocViewer))
		{
			container = container.getParent();
		}
		return (ImagoDocViewer) container;
	}
	
	
	// ===================================================================
	// Zoom management

	public double getZoom()
	{
		return this.zoom;
	}
	
	public void setZoom(double zoom) 
	{
		this.zoom = zoom;
	}

	public BufferedImage getBufferedImage() 
	{
		return image;
	}

	public void setBufferedImage(BufferedImage image) 
	{
		this.image = image;
	}

	// ===================================================================
	// Display methods

	public void refreshDisplay()
	{
//		System.out.println("ImageDisplay: refreshDisplay");
		updateOffset();
	}
	
//	public void repaint() {
//		super.repaint();
//		System.out.println("ImageDisplay: repaint");
//	}
	
	/**
	 * Converts a position from display coordinate (the Graphics) to Image coordinates.
	 * 
	 * Display coordinates are positive. Image coordinates are between -.5 and size(d)-.5.
	 *   
	 * @param point position in display coordinates
	 * @return position in image coordinates
	 */
    public Point2D displayToImage(Point point) 
    {
        double x = (point.x - this.offsetX) / zoom - .5;
        double y = (point.y - this.offsetY) / zoom - .5;
        return new Point2D(x, y);
    }

    /**
     * Converts a position from Image coordinates to display coordinate (the Graphics).
     * 
     * Display coordinates are positive. Image coordinates are between -.5 and size(d)-.5.
     *   
     * @param point position in image coordinates
     * @return position in display coordinates
     */
    public Point2D imageToDisplay(Point2D point) 
    {
        double x = (point.getX() + .5) * zoom + this.offsetX;
        double y = (point.getY() + .5) * zoom + this.offsetY;
        return new Point2D(x, y);
    }

	/**
	 * @return the current offset at which image is displayed. Always greater
	 *         than 0 for each coordinate.
	 */
	public Point getOffset() 
	{
		return new Point(this.offsetX, this.offsetY);
	}

	/**
	 * Compute new offset such that image is either in center position, or at
	 * left-most or top most position.
	 */
	protected void updateOffset() 
	{
		Dimension dim0 = this.getSize();
		Dimension dim = this.getDisplaySize();

		this.offsetX = (int) Math.max(0, Math.floor((dim0.width - dim.width) * .5));
		this.offsetY = (int) Math.max(0, Math.floor((dim0.height - dim.height) * .5));
	}

	public Dimension getPreferredSize()
	{
		return getDisplaySize();
	}
	
	public Dimension getDisplaySize() 
	{
		int width = (int) Math.ceil(this.image.getWidth() * zoom);
		int height = (int) Math.ceil(this.image.getHeight() * zoom);
		return new Dimension(width, height);
	}
	
	
    // ===================================================================
    // paint methods

	public void paintComponent(Graphics g) 
	{
	    paintImage(g);
	    paintAnnotations(g);
	    
	    paintSelection(g);
	}

	private void paintImage(Graphics g)
	{
        Dimension dim = this.getDisplaySize();
        g.drawImage(this.image, offsetX, offsetY, dim.width, dim.height, null);
	}
	
    private void paintAnnotations(Graphics g)
    {
        // basic check to avoid errors
        if (this.shapes == null)
        {
            return;            
        }
        
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        
        for(ImagoShape shape : this.shapes)
        {
            g2.setColor(shape.getColor());
            Geometry geom = shape.getGeometry();
            if (geom instanceof Point2D)
            {
                Point2D point = (Point2D) geom;
                point = imageToDisplay(point);
                int x = (int) point.getX();
                int y = (int) point.getY();
//                g2.fillOval(x-2, y-2, 5, 5);
                g2.drawLine(x-2, y, x+2, y);
                g2.drawLine(x, y-2, x, y+2);
            }
            else if (geom instanceof LineSegment2D)
            {
                LineSegment2D line = (LineSegment2D) geom;
                Point2D p1 = imageToDisplay(line.getP1());
                int x1 = (int) p1.getX();
                int y1 = (int) p1.getY();
                Point2D p2 = imageToDisplay(line.getP2());
                int x2 = (int) p2.getX();
                int y2 = (int) p2.getY();
                g2.drawLine(x1, y1, x2, y2);
            }
            else
            {
                System.out.println("can not handle geometry of class: " + geom.getClass());
            }
        }
    }
    
    private void paintSelection(Graphics g)
    {
        // basic check to avoid errors
        if (this.selection == null)
        {
            return;            
        }
     
//        System.out.println("paint selection");
        
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.YELLOW);
        
        if (this.selection instanceof Point2D)
        {
            Point2D point = (Point2D) this.selection;
            point = imageToDisplay(point);
            int x = (int) point.getX();
            int y = (int) point.getY();
//                g2.fillOval(x-2, y-2, 5, 5);
            g2.drawLine(x-2, y, x+2, y);
            g2.drawLine(x, y-2, x, y+2);
        }
        else if (this.selection instanceof LineSegment2D)
        {
            LineSegment2D line = (LineSegment2D) this.selection;
            Point2D p1 = imageToDisplay(line.getP1());
            int x1 = (int) p1.getX();
            int y1 = (int) p1.getY();
            Point2D p2 = imageToDisplay(line.getP2());
            int x2 = (int) p2.getX();
            int y2 = (int) p2.getY();
            g2.drawLine(x1, y1, x2, y2);
        }
        else
        {
            System.out.println("can not handle geometry of class: " + selection.getClass());
        }
    }
    
}
