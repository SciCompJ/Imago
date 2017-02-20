/**
 * 
 */
package imago.gui.viewer;

import imago.app.shape.ImagoShape;
import imago.gui.ImagoDocViewer;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;

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
	
    public Point2D displayToImage(Point point) 
    {
        double x = (point.x - this.offsetX) / zoom;
        double y = (point.y - this.offsetY) / zoom;
        return new Point2D(x, y);
    }

    public Point2D imageToDisplay(Point2D point) 
    {
        double x = point.getX() * zoom + this.offsetX;
        double y = point.getY() * zoom + this.offsetY;
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
	}

	private void paintImage(Graphics g)
	{
        Dimension dim = this.getDisplaySize();
        g.drawImage(this.image, offsetX, offsetY, dim.width, dim.height, null);
	}
	
    private void paintAnnotations(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(offsetX, offsetY);
        //      //g2.scale(zoom, zoom);
        //      g2.setColor(new Color(255, 0, 0));
        //      Shape rect = new Rectangle2D.Double(20 * zoom, 30 * zoom, 101 * zoom, 121 * zoom); 
        //      g2.draw(rect);
        
        for(ImagoShape shape : this.shapes)
        {
            g2.setColor(shape.getColor());
            Geometry geom = shape.getGeometry();
            if (geom instanceof Point2D)
            {
                Point2D point = (Point2D) geom;
                int x = (int) (point.getX() * zoom);
                int y = (int) (point.getY() * zoom);
                g2.fillOval(x-2, y-2, 5, 5);
            }
            else if (geom instanceof LineSegment2D)
            {
                LineSegment2D line = (LineSegment2D) geom;
                Point2D p1 = line.getP1();
                int x1 = (int) (p1.getX() * zoom);
                int y1 = (int) (p1.getY() * zoom);
                Point2D p2 = line.getP2();
                int x2 = (int) (p2.getX() * zoom);
                int y2 = (int) (p2.getY() * zoom);
                g2.drawLine(x1, y1, x2, y2);
            }
            else
            {
                System.out.println("can not handle geometry of class: " + geom.getClass());
            }
        }
    }
}
