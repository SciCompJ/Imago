/**
 * 
 */
package imago.gui.shape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import imago.app.shape.Shape;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Curve2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Circle2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.curve.MultiCurve2D;
import net.sci.geom.geom2d.polygon.LinearRing2D;
import net.sci.geom.geom2d.polygon.PolygonalDomain2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.geom.graph.Graph2D;

/**
 * An helper class that draws geometric shapes or geometries onto a Graphics2D.
 * 
 * Stores the conversion from user coordinates to display coordinates. Line
 * widths correspond to width in pixel unit.
 */
public class ShapeDrawer
{
    // ===================================================================
    // Class fields

    /** The scaling factor in the x direction. Default is 1.0. */
    double scaleX = 1.0;
    
    /** The scaling factor in the y direction. Default is 1.0. */
    double scaleY = 1.0;
    
    /** The shift in the x direction, applied after scaling. Default is 0.0. */
    double shiftX = 0.0;
    
    /** The shift in the y direction, applied after scaling. Default is 0.0. */
    double shiftY = 0.0;
    
    
    // ===================================================================
    // Constructors

    public ShapeDrawer()
    {
    }
        
    
    // ===================================================================
    // general methods
    
    public void setScaling(double s)
    {
        this.scaleX = s;
        this.scaleY = s;
    }
    
    public void setScaling(double sx, double sy)
    {
        this.scaleX = sx;
        this.scaleY = sy;
    }
    
    public void setShift(double vx, double vy)
    {
        this.shiftX = vx;
        this.shiftY = vy;
    }
    
    
    // ===================================================================
    // Drawing methods

    /**
     * Draws a shape on the specified graphics. In practice, sets uo the display
     * settings from shape style, and calls the <code>drawGeometry()</code>
     * method.
     * 
     * @param shape
     *            the shape to draw
     */
    public void drawShape(Graphics2D g2, Shape shape)
    {
        // setup draw style
        Stroke stroke = new BasicStroke((float) shape.getLineWidth());
        g2.setStroke(stroke);
        g2.setColor(shape.getColor());
        
        Geometry geom = shape.getGeometry();
        if (geom instanceof Geometry2D)
        {
            drawGeometry(g2, (Geometry2D) geom);
        }
        g2.setStroke(new BasicStroke());
    }
    
    /**
     * Draws a geometry on the specified graphics. Paint settings are assumed to
     * be already defined.
     * 
     * @param geom
     *            the geometry to draw
     */
    public void drawGeometry(Graphics2D g2, Geometry2D geom)
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
        else if (geom instanceof MultiCurve2D)
        {
            for (Curve2D curve : ((MultiCurve2D) geom).curves())
            {
                Polyline2D poly = curve.asPolyline(120);
                drawPolyline(g2, poly);
            }
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
     * @param point the point to draw
     */
    private void drawPoint(Graphics2D g2, Point2D point)
    {
        int x = (int) (point.x() * scaleX + shiftX);
        int y = (int) (point.y() * scaleY + shiftY);
        g2.drawLine(x-2, y, x+2, y);
        g2.drawLine(x, y-2, x, y+2);
        
    }
    
    /**
     * Draws a line segment on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
     * @param line the line segment to draw
     */
    private void drawLineSegment(Graphics2D g2, LineSegment2D line)
    {
        Point2D p1 = userToDisplay(line.getP1());
        int x1 = (int) p1.x();
        int y1 = (int) p1.y();
        Point2D p2 = userToDisplay(line.getP2());
        int x2 = (int) p2.x();
        int y2 = (int) p2.y();
        g2.drawLine(x1, y1, x2, y2);
    }
    
    /**
     * Draws a polygon on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
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
            Point2D point = userToDisplay(iter.next());
            px[i] = (int) point.x();
            py[i] = (int) point.y();
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
     * @param poly the polygon to draw
     */
    private void drawPolygon(Graphics2D g2, PolygonalDomain2D poly)
    {
        for (LinearRing2D ring : poly.rings())
        {
            drawLinearRing(g2, ring);
        }
    }
    
    /**
     * Draws a polygon on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
     * @param poly the polygon to draw
     */
    private void drawLinearRing(Graphics2D g2, LinearRing2D poly)
    {
        // check size
        int nv = poly.vertexCount();
        if (nv < 2)
        {
            return;
        }
    
        // initialize a path at first vertex of the polygon
        Path2D.Float path = new Path2D.Float();
        Point2D p = userToDisplay(poly.vertexPosition(0));
        path.moveTo(p.x(), p.y());

        // iterate over vertex positions
        for (int i = 1; i < nv; i++)
        {
            p = userToDisplay(poly.vertexPosition(i));
            path.lineTo(p.x(), p.y());
        }
        path.closePath();

        // display the polygon
        g2.draw(path);
    }
    
    
    private Point2D userToDisplay(Point2D point)
    {
        double x = point.x() * scaleX + shiftX;
        double y = point.y() * scaleY + shiftY;
        return new Point2D(x, y);
    }
    
    public static final void main(String... args)
    {
        JPanel drawPanel = new JPanel()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public void paintComponent(Graphics g) 
            {
                super.paintComponent(g);
                
                // create drawer
                ShapeDrawer drawer = new ShapeDrawer();
                drawer.setScaling(10, -10);
                drawer.setShift(200, 200);

                Graphics2D g2 = (Graphics2D) g;
                drawer.drawGeometry(g2, new LineSegment2D(new Point2D(-10, 0), new Point2D(+10, 0)));
                drawer.drawGeometry(g2, new LineSegment2D(new Point2D(0, -10), new Point2D(0, +10)));
                drawer.drawGeometry(g2, new Point2D(4, 3));
                g.setColor(Color.RED);
                drawer.drawGeometry(g2, new Circle2D(new Point2D(4, 3), 5));
            }
        };
        drawPanel.setBackground(Color.WHITE);
        
        JFrame frame = new JFrame("Drawer");
        frame.setLayout(new BorderLayout());
        frame.add(drawPanel, BorderLayout.CENTER);
        frame.add(new JLabel("Status bar"), BorderLayout.SOUTH);
        frame.setSize(new Dimension(500, 400));
        frame.validate();
        frame.setVisible(true);
    }
}