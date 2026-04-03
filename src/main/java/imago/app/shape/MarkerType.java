/**
 * 
 */
package imago.app.shape;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * Different types of marker.
 */
public interface MarkerType
{
    public static final MarkerType CIRCLE = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(new java.awt.geom.Ellipse2D.Float(x - s, y - s, 2 * s, 2 * s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(new java.awt.geom.Ellipse2D.Float(x - s, y - s, 2 * s, 2 * s));
        }
    };
    
    public static final MarkerType CROSS = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(new Line2D.Float((int) (x-s), (int) (y-s), (int) (x+s), (int) (y+s)));
            g2.draw(new Line2D.Float((int) (x-s), (int) (y+s), (int) (x+s), (int) (y-s)));
        }
    };
    
    public static final MarkerType PLUS = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(new Line2D.Float((int) (x-s), (int) y, (int) (x+s), (int) y));
            g2.draw(new Line2D.Float((int) x, (int) (y-s), (int) x, (int) (y+s)));
        }
    };
    
    public static final MarkerType ASTERISK = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(new Line2D.Float((int) (x-s), (int) y, (int) (x+s), (int) y));
            g2.draw(new Line2D.Float((int) x, (int) (y-s), (int) x, (int) (y+s)));
            float r2 = (float) (s * Math.sqrt(2) * 0.5);
            g2.draw(new Line2D.Float((int) (x-r2), (int) (y-r2), (int) (x+r2), (int) (y+r2)));
            g2.draw(new Line2D.Float((int) (x-r2), (int) (y+r2), (int) (x+r2), (int) (y-r2)));
        }
    };
    
    public static final MarkerType SQUARE = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(new java.awt.geom.Rectangle2D.Float(x - s, y - s, 2 *s, 2*s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(new java.awt.geom.Rectangle2D.Float(x - s, y - s, 2 *s, 2*s));
        }
    };
    
    public static final MarkerType DIAMOND = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(createPath(x, y, s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(createPath(x, y, s));
        }
        
        private Path2D createPath(float x, float y, float s)
        {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x, y + s);
            path.lineTo(x - s, y);
            path.lineTo(x, y - s);
            path.lineTo(x + s, y);
            path.lineTo(x, y + s);
            return path;
        }
    };
    
    public static final MarkerType TRIANGLE_UP = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(createPath(x, y, s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(createPath(x, y, s));
        }
        
        private Path2D createPath(float x, float y, float s)
        {
            float dy = (float) (Math.sqrt(3) * s / 3.0);
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x, y - dy * 2.0);
            path.lineTo(x + s, y + dy);
            path.lineTo(x - s, y + dy);
            path.lineTo(x, y - dy * 2.0);
            return path;
        }
    };
    
    public static final MarkerType TRIANGLE_DOWN = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(createPath(x, y, s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(createPath(x, y, s));
        }
        
        private Path2D createPath(float x, float y, float s)
        {
            float dy = (float) (Math.sqrt(3) * s / 3.0);
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x, y + dy * 2.0);
            path.lineTo(x - s, y - dy);
            path.lineTo(x + s, y - dy);
            path.lineTo(x, y + dy * 2.0);
            return path;
        }
    };
    
    public static final MarkerType TRIANGLE_LEFT = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(createPath(x, y, s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(createPath(x, y, s));
        }
        
        private Path2D createPath(float x, float y, float s)
        {
            float dx = (float) (Math.sqrt(3) * s / 3.0);
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x - dx * 2.0, y);
            path.lineTo(x + dx, y - s);
            path.lineTo(x + dx, y + s);
            path.lineTo(x - dx * 2.0, y);
            return path;            
        }
    };
    
    public static final MarkerType TRIANGLE_RIGHT = new MarkerType()
    {
        @Override
        public void draw(Graphics2D g2, float x, float y, float s)
        {
            g2.draw(createPath(x, y, s));
        }
        
        @Override
        public void fill(Graphics2D g2, float x, float y, float s)
        {
            g2.fill(createPath(x, y, s));
        }
        
        private Path2D createPath(float x, float y, float s)
        {
            float dx = (float) (Math.sqrt(3) * s / 3.0);
            Path2D.Float path = new Path2D.Float();
            path.moveTo(x + dx * 2.0, y);
            path.lineTo(x - dx, y + s);
            path.lineTo(x - dx, y - s);
            path.lineTo(x + dx * 2.0, y);
            return path;
        }
    };
    
    /**
     * Draws a marker of this type at the specified position, and with the
     * specified size.
     * 
     * @param g2
     *            the graphics to draw on
     * @param x
     *            the x-position of the marker
     * @param y
     *            the y-position of the marker
     * @param s
     *            the size of the marker to draw
     */
    public void draw(Graphics2D g2, float x, float y, float s);
    
    /**
     * Fills the content of a marker of this type at the specified position, and
     * with the specified size (optional operation). The default behavior is to
     * do nothing, but it can be overridden for markers with interior.
     * 
     * @param g2
     *            the graphics to draw on
     * @param x
     *            the x-position of the marker
     * @param y
     *            the y-position of the marker
     * @param s
     *            the size of the marker to fill
     */
    public default void fill(Graphics2D g2, float x, float y, float s)
    {
    }
}
