/**
 * 
 */
package imago.gui.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import imago.app.shape.Shape;
import imago.gui.shape.ShapeDrawer;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;


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

	/**
	 * The representation of the image data to display, as a BufferedImage instance.
	 */
	BufferedImage image;
	
	Collection<Shape> shapes = new ArrayList<Shape>();

	/**
	 * A list of 2D shapes corresponding either to scene items, or to slice
	 * views of the scene items.
	 */
	Collection<Shape> sceneGraphItems = new ArrayList<Shape>();

    /**
     * The shape of the current selection, usually a polyline or a rectangle
     */
    protected Geometry2D selection = null;
    
    /**
     * The zoom level used to represent the image.
     */
	double zoom = 1;
	
	/**
     * The X- and Y- offset of the image with respect to the upper left corner
     * of the panel. Values depends on panel and image size, and of current zoom
     * level.
     */
	int offsetX;
	int offsetY;
	
	
	// ===================================================================
	// Constructor
	
	public ImageDisplay(BufferedImage image) 
	{
		this.image = image;
		this.setBackground(Color.LIGHT_GRAY);
	}

	// ===================================================================
	// General methods

	public BufferedImage getImage() 
	{
		return this.image;
	}
	

	// ===================================================================
	// Shape management

    public Collection<Shape> getShapes()
    {
        return shapes;
    }
    
    public void setShapes(Collection<Shape> newShapes)
    {
        this.shapes = newShapes;
    }
    
	public void addShape(Shape shape)
	{
	    this.shapes.add(shape);
	}
	
	
	// ===================================================================
	// Scene graph items management

	public void addSceneGraphItem(Shape shape)
	{
		this.sceneGraphItems.add(shape);
	}

	public void addSceneGraphItem(Geometry geom)
	{
		this.sceneGraphItems.add(new Shape(geom));
	}

	public void clearSceneGraphItems()
	{
		this.sceneGraphItems.clear();
	}
	
	
	// ===================================================================
	// Selection management

    public Geometry2D getSelection()
    {
        return this.selection;
    }
    
    public void setSelection(Geometry2D shape)
    {
        this.selection = shape;
    }
    
//	public ImageFrame getViewer() 
//	{
//		Container container = this.getParent();
//		while (!(container instanceof ImageFrame))
//		{
//			container = container.getParent();
//		}
//		return (ImageFrame) container;
//	}
	
	
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
        double x = (point.x() + .5) * zoom + this.offsetX;
        double y = (point.y() + .5) * zoom + this.offsetY;
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
	public void updateOffset() 
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
	
	public void drawShape(Shape shape)
	{
		System.out.println("ImageDisplay.drawShape");
		
        // create drawer
        ShapeDrawer drawer = createDrawer();
        drawer.drawShape((Graphics2D) this.getGraphics(), shape);
	}
	
	
    // ===================================================================
    // paint methods

	public void paintComponent(Graphics g) 
	{
        super.paintComponent(g);

        paintImage(g);
	    paintAnnotations(g);
	    paintSceneGraphItems(g);
	    
	    drawSelection(g);
	}

	private void paintImage(Graphics g)
	{
//        System.out.println("paint image");
        Dimension dim = this.getDisplaySize();
        g.drawImage(this.image, offsetX, offsetY, dim.width, dim.height, null);
	}
	
    private void paintAnnotations(Graphics g)
    {
//        System.out.println("paint annotations");
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        ShapeDrawer drawer = createDrawer();
        
        for(Shape shape : this.shapes)
        {
            drawer.drawShape(g2, shape);
        }
    }
    
    private void paintSceneGraphItems(Graphics g)
    {
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        
        for(Shape shape : this.sceneGraphItems)
        {
            drawShape(g2, shape);
        }
    }
    
    private void drawShape(Graphics2D g2, Shape shape)
    {
        ShapeDrawer drawer = createDrawer();
        drawer.drawShape(g2, shape);
        g2.setStroke(new BasicStroke());
    }
    
    private void drawSelection(Graphics g)
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
        ShapeDrawer drawer = createDrawer();
        drawer.drawGeometry(g2, this.selection);
    }

    private ShapeDrawer createDrawer()
    {
        ShapeDrawer drawer = new ShapeDrawer();
        drawer.setScaling(zoom);
        drawer.setShift(offsetX, offsetY);
        return drawer;
    }
}
