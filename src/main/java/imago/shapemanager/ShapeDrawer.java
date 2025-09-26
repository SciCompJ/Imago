/**
 * 
 */
package imago.shapemanager;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import imago.app.shape.Shape;
import imago.app.shape.Style;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Curve2D;
import net.sci.geom.geom2d.Domain2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Circle2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.geom2d.curve.MultiCurve2D;
import net.sci.geom.graph.Graph2D;
import net.sci.geom.polygon2d.LinearRing2D;
import net.sci.geom.polygon2d.PolygonalDomain2D;
import net.sci.geom.polygon2d.Polyline2D;

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
     * Draws a shape on the specified graphics. In practice, sets up the display
     * settings from shape style, and calls the <code>drawGeometry()</code>
     * method.
     * 
     * @param shape
     *            the shape to draw
     */
    public void drawShape(Graphics2D g, Shape shape)
    {
        // creates a new Graphics on top of original graphics instance
        Graphics2D g2 = (Graphics2D) g.create();
        
        // small setup
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Geometry geom = shape.getGeometry();
        
        // call the fill method only for domain geometries 
        if (geom instanceof Domain2D)
        {
            Color fillColor = setOpacity(shape.getStyle().getFillColor(), shape.getStyle().getFillOpacity());
            g2.setPaint(fillColor);
            fillGeometry(g2, (Geometry2D) geom);
        }
        
        if (geom instanceof Point2D)
        {
            drawPoint(g2, (Point2D) geom, shape.getStyle());
        }
        else if (geom instanceof Geometry2D)
        {
            // setup line draw style
            Stroke stroke = new BasicStroke((float) shape.getLineWidth());
            g2.setStroke(stroke);
            g2.setColor(shape.getColor());
            
            drawGeometry(g2, (Geometry2D) geom);
        }
    }
    
    private Color setOpacity(Color baseColor, double opacity)
    {
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (255 * opacity));
    }
    
    public void drawShapes(Graphics2D g2, Collection<Shape> shapes)
    {
        for (Shape shape : shapes)
        {
            drawShape(g2, shape);
        }
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
            System.out.println("ShapeDrawer can not draw geometry with class: " + geom.getClass());
        }
    }

    /**
     * Draws a geometry on the specified graphics. Paint settings are assumed to
     * be already defined.
     * 
     * @param geom
     *            the geometry to draw
     */
    public void fillGeometry(Graphics2D g2, Geometry2D geom)
    {
        // basic checkups
        if (geom == null)
        {
            throw new RuntimeException("Geometry should not be null");
        }

        if (geom instanceof PolygonalDomain2D)
        {
            PolygonalDomain2D poly = (PolygonalDomain2D) geom;
            fillPolygon(g2, poly);
        }
        else if (geom instanceof Ellipse2D)
        {
            LinearRing2D poly = ((Ellipse2D) geom).asPolyline(120);
            fillLinearRing(g2, poly);
        }
        else
        {
            // basic check to avoid errors
            System.out.println("ShapeDrawer can not fill geometry with class: " + geom.getClass());
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
        point = userToDisplay(point);
        int x = (int) point.x();
        int y = (int) point.y();
        g2.drawLine(x-2, y, x+2, y);
        g2.drawLine(x, y-2, x, y+2);
    }
    
    /**
     * Draws a point on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
     * @param point the point to draw
     */
    private void drawPoint(Graphics2D g2, Point2D point, Style style)
    {
        point = userToDisplay(point);
        float xc = (float) point.x();
        float yc = (float) point.y();
        float r = style.getMarkerSize() * 0.5f;
        
        // setup line draw style
        Stroke stroke = new BasicStroke((float) style.getLineWidth());
        g2.setStroke(stroke);
        g2.setColor(style.getLineColor());
        
        switch(style.getMarkerType())
        {
            case CIRCLE -> {
                g2.draw(new java.awt.geom.Ellipse2D.Float(xc - r, yc - r, 2 *r, 2*r));
            }
            case PLUS -> {
                g2.draw(new Line2D.Float((int) (xc-r), (int) yc, (int) (xc+r), (int) yc));
                g2.draw(new Line2D.Float((int) xc, (int) (yc-r), (int) xc, (int) (yc+r)));
            }
            case CROSS -> {
                g2.draw(new Line2D.Float((int) (xc-r), (int) (yc-r), (int) (xc+r), (int) (yc+r)));
                g2.draw(new Line2D.Float((int) (xc-r), (int) (yc+r), (int) (xc+r), (int) (yc-r)));
            } 
            case ASTERISK -> {
                g2.draw(new Line2D.Float((int) (xc-r), (int) yc, (int) (xc+r), (int) yc));
                g2.draw(new Line2D.Float((int) xc, (int) (yc-r), (int) xc, (int) (yc+r)));
                float r2 = (float) (r * Math.sqrt(2) * 0.5);
                g2.draw(new Line2D.Float((int) (xc-r2), (int) (yc-r2), (int) (xc+r2), (int) (yc+r2)));
                g2.draw(new Line2D.Float((int) (xc-r2), (int) (yc+r2), (int) (xc+r2), (int) (yc-r2)));
            } 
            case SQUARE -> {
                g2.draw(new java.awt.geom.Rectangle2D.Float(xc - r, yc - r, 2 *r, 2*r));
            }
            case DIAMOND -> {
                Path2D.Float path = new Path2D.Float();
                path.moveTo(xc, yc + r);
                path.lineTo(xc - r, yc);
                path.lineTo(xc, yc - r);
                path.lineTo(xc + r, yc);
                path.lineTo(xc, yc + r);
                g2.draw(path);
            } 
            case TRIANGLE_DOWN -> {
                float dy = (float) (Math.sqrt(3) * r / 3.0);
                Path2D.Float path = new Path2D.Float();
                path.moveTo(xc, yc + dy * 2.0);
                path.lineTo(xc - r, yc - dy);
                path.lineTo(xc + r, yc - dy);
                path.lineTo(xc, yc + dy * 2.0);
                g2.draw(path);
            } 
            case TRIANGLE_UP-> {
                float dy = (float) (Math.sqrt(3) * r / 3.0);
                Path2D.Float path = new Path2D.Float();
                path.moveTo(xc, yc - dy * 2.0);
                path.lineTo(xc + r, yc + dy);
                path.lineTo(xc - r, yc + dy);
                path.lineTo(xc, yc - dy * 2.0);
                g2.draw(path);
            } 
            case TRIANGLE_LEFT-> {
                float dx = (float) (Math.sqrt(3) * r / 3.0);
                Path2D.Float path = new Path2D.Float();
                path.moveTo(xc - dx * 2.0, yc);
                path.lineTo(xc + dx, yc - r);
                path.lineTo(xc + dx, yc + r);
                path.lineTo(xc - dx * 2.0, yc);
                g2.draw(path);
            } 
            case TRIANGLE_RIGHT-> {
                float dx = (float) (Math.sqrt(3) * r / 3.0);
                Path2D.Float path = new Path2D.Float();
                path.moveTo(xc + dx * 2.0, yc);
                path.lineTo(xc - dx, yc + r);
                path.lineTo(xc - dx, yc - r);
                path.lineTo(xc + dx * 2.0, yc);
                g2.draw(path);
            } 
            default -> throw new RuntimeException("Could not manage marker type: " + style.getMarkerType());
        };
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
    private void fillPolygon(Graphics2D g2, PolygonalDomain2D poly)
    {
        for (LinearRing2D ring : poly.rings())
        {
            fillLinearRing(g2, ring);
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
        Path2D path = createPath(poly);
        if (path != null)
        {
            g2.draw(path);
        }
    }
    
    /**
     * Draws a polygon on the specified graphics. Paint settings are assumed to be
     * already defined.
     * 
     * @param poly the polygon to draw
     */
    private void fillLinearRing(Graphics2D g2, LinearRing2D poly)
    {
        Path2D path = createPath(poly);
        if (path != null)
        {
            g2.fill(path);
        }
    }

    private Path2D createPath(LinearRing2D poly)
    {
        // check size
        int nv = poly.vertexCount();
        if (nv < 2)
        {
            return null;
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
        
        return path;
    }
    
    private Point2D userToDisplay(Point2D point)
    {
        double x = (point.x() + 0.5) * scaleX + shiftX;
        double y = (point.y() + 0.5) * scaleY + shiftY;
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
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // create drawer
                ShapeDrawer drawer = new ShapeDrawer();
                drawer.setScaling(10, -10);
                drawer.setShift(200, 200);

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
