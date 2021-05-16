/**
 * 
 */
package imago.gui.viewer;

import imago.app.shape.Shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;

import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Curve2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.polygon.PolygonalDomain2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.geom.graph.Graph2D;


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
	
	public void drawShape(Shape shape)
	{
		System.out.println("ImageDisplay.drawShape");
		
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) this.getGraphics();
        
        g2.setColor(shape.getColor());
        Stroke stroke = new BasicStroke((float) shape.getLineWidth());
        g2.setStroke(stroke);
        
        drawGeometry(g2, (Geometry2D) shape.getGeometry());
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
        Dimension dim = this.getDisplaySize();
        g.drawImage(this.image, offsetX, offsetY, dim.width, dim.height, null);
	}
	
    private void paintAnnotations(Graphics g)
    {
        // convert to Graphics2D to have more drawing possibilities
        Graphics2D g2 = (Graphics2D) g;
        
        for(Shape shape : this.shapes)
        {
            drawShape(g2, shape);
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
        g2.setColor(shape.getColor());
        Stroke stroke = new BasicStroke((float) shape.getLineWidth());
        g2.setColor(shape.getColor());
        g2.setStroke(stroke);
        Geometry geom = shape.getGeometry();
        if (geom instanceof Geometry2D)
        {
        	drawGeometry(g2, (Geometry2D) geom);
        }
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
        
        drawGeometry(g2, this.selection);
    }
    
    /**
	 * Draws a geometry on the specified graphics. Paint settings are assumed to be
	 * already defined.
	 * 
	 * @param g2 the instance of Graphics2D to paint on
	 * @param geom the geometry to draw
	 */
    private void drawGeometry(Graphics2D g2, Geometry2D geom)
    {
        // basic checkups
        if (geom == null)
        {
            throw new RuntimeException("Geometry should not be null");
        }

        // Process various geometry cases
        if (geom instanceof Point2D)
        {
            Point2D point = (Point2D) geom;
            drawPoint(g2, point);
        }
        else if (geom instanceof LineSegment2D)
        {
            LineSegment2D line = (LineSegment2D) geom;
            drawLineSegment(g2, line);
        }
        else if (geom instanceof Bounds2D)
        {
            Bounds2D box = (Bounds2D) geom;
            drawPolygon(g2, box.getRectangle());
        }
        else if (geom instanceof PolygonalDomain2D)
        {
        	PolygonalDomain2D poly = (PolygonalDomain2D) geom;
            drawPolygon(g2, poly);
        }
        else if (geom instanceof Ellipse2D)
        {
            Polyline2D poly = ((Ellipse2D) geom).asPolyline(120);
            drawPolyline(g2, poly);
        }
        else if (geom instanceof Polyline2D)
        {
            Polyline2D poly = (Polyline2D) geom;
            drawPolyline(g2, poly);
        }
        else if (geom instanceof Curve2D)
        {
            Curve2D curve = (Curve2D) geom;
            Polyline2D poly = curve.asPolyline(120);
            drawPolyline(g2, poly);
        }
        else if (geom instanceof Graph2D)
        {
            drawGraphEdges(g2, (Graph2D) geom);
            drawGraphVertices(g2, (Graph2D) geom);
        }
        else
        {
            // basic check to avoid errors
            System.out.println("[Image Display] can not handle geometry of class: " + geom.getClass());
        }
    }

    
    // ===================================================================
    // Specific geometry paint methods

    /**
     * Draws edges of a graph on the specified graphics. Paint settings are
     * assumed to be already defined.
     * 
     * @param g2
     *            the instance of Graphics2D to paint on
     * @param graph
     *            the graph whose edge need to be paint
     */
    private void drawGraphVertices(Graphics2D g2, Graph2D graph)
    {
        for (Graph2D.Vertex v : graph.vertices())
        {
            drawPoint(g2, v.position());
        }
    }
    
    /**
     * Draws edges of a graph on the specified graphics. Paint settings are
     * assumed to be already defined.
     * 
     * @param g2
     *            the instance of Graphics2D to paint on
     * @param graph
     *            the graph whose edge need to be paint
     */
    private void drawGraphEdges(Graphics2D g2, Graph2D graph)
    {
        for (Graph2D.Edge edge : graph.edges())
        {
            drawLineSegment(g2, edge.curve());
        }
    }

    /**
	 * Draws a point on the specified graphics. Paint settings are assumed to be
	 * already defined.
	 * 
	 * @param g2 the instance of Graphics2D to paint on
	 * @param point the point to draw
	 */
    private void drawPoint(Graphics2D g2, Point2D point)
    {
        point = imageToDisplay(point);
        int x = (int) point.getX();
        int y = (int) point.getY();
        g2.drawLine(x-2, y, x+2, y);
        g2.drawLine(x, y-2, x, y+2);
    	
    }
    
    /**
	 * Draws a line segment on the specified graphics. Paint settings are assumed to be
	 * already defined.
	 * 
	 * @param g2 the instance of Graphics2D to paint on
	 * @param line the line segment to draw
	 */
    private void drawLineSegment(Graphics2D g2, LineSegment2D line)
    {
    	Point2D p1 = imageToDisplay(line.getP1());
        int x1 = (int) p1.getX();
        int y1 = (int) p1.getY();
        Point2D p2 = imageToDisplay(line.getP2());
        int x2 = (int) p2.getX();
        int y2 = (int) p2.getY();
        g2.drawLine(x1, y1, x2, y2);
    }
    
    /**
     * Draws a polygon on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
     * @param g2 the instance of Graphics2D to paint on
     * @param poly the polygon to draw
     */
    private void drawPolyline(Graphics2D g2, Polyline2D poly)
    {
        // check size
        int nv = poly.vertexCount();
        if (nv < 2)
        {
            return;
        }
    
        // convert polygon into integer coords in display space
        int[] px = new int[nv];
        int[] py = new int[nv];

        Iterator<Point2D> iter = poly.vertexPositions().iterator();
        for (int i = 0; i < nv; i++)
        {
            Point2D point = iter.next();
            point = imageToDisplay(point);
            px[i] = (int) point.getX();
            py[i] = (int) point.getY();
        }

        // display the polygon
        if (poly.isClosed())
            g2.drawPolygon(px, py, nv);
        else
            g2.drawPolyline(px, py, nv);
    }    

    /**
	 * Draws a polygon on the specified graphics. Paint settings are assumed to be
	 * already defined.
	 * 
	 * @param g2 the instance of Graphics2D to paint on
	 * @param poly the polygon to draw
	 */
    private void drawPolygon(Graphics2D g2, PolygonalDomain2D poly)
    {
    	// check size
    	int nv = poly.vertexCount();
    	if (nv < 2)
    	{
    		return;
    	}
    
    	// convert polygon into integer coords in display space
    	int[] px = new int[nv];
    	int[] py = new int[nv];

    	// iterate over vertex positions
    	int i = 0;
    	for (Point2D point : poly.vertexPositions())
    	{
    		point = imageToDisplay(point);
    		px[i] = (int) point.getX();
    		py[i] = (int) point.getY();
    		i++;
    	}

    	// display the polygon
        g2.drawPolygon(px, py, nv);
    }
}
