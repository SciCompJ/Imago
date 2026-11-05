/**
 * 
 */
package imago.view3d;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import net.sci.geom.geom3d.AffineTransform3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * A simple 3D rendered based on a z-buffer.
 */
public class ZBufferRenderer
{
    // =============================================================
    // Public enumeration

    public enum Shading
    {
        NONE,
        FLAT;
    }
    
    
    // =============================================================
    // Class fields

    /** the width of the rendering image */
    int width;
    
    /** the height of the rendering image */
    int height;
    
    /**
     * The image used to render the 3D scene.
     */
    BufferedImage img = null;
    
    /**
     * The z-buffer, with as many elements as the number of pixels of image.
     */
    double[] zBuffer = null;
    
    // world to image transform parameters (in degrees)
    Point3D lookat = new Point3D();
    double heading = 0;
    double pitch = 0;
    double zoom = 1.0;
    
    // centered world-to-image transform.
    // set to null when modifying transform parameters.
    // the rendering methods need to check if transform need to be recomputed.
    AffineTransform3D transform = null;

    /**
     * The shading parameter for rendering of mesh faces.
     */
    Shading shading = Shading.FLAT;


    // =============================================================
    // private constants

    private static final double EDGE_RENDERING_TOLERANCE = 1;
    

    // =============================================================
    // Constructors

    /**
     * Creates a new z-buffer with the specified size.
     * 
     * @param width
     *            the width of the image to render
     * @param height
     *            the height of the image to render
     */
    public ZBufferRenderer(int width, int height)
    {
        resize(width, height);
    }
    
    
    // =============================================================
    // Modifiers

    public ZBufferRenderer setShading(Shading shading)
    {
        this.shading = shading;
        return this;
    }
    
    /**
     * Sets the point located in the middle of the 3D viewer.
     * 
     * @param point
     *            the point to look at.
     * @return a reference to this renderer.
     */
    public ZBufferRenderer setLookAt(Point3D point)
    {
        this.lookat = point;
        updateTransform();
        return this;
    }
    
    public ZBufferRenderer setZoom(double zoom)
    {
        this.zoom = zoom;
        updateTransform();
        return this;
    }
    
    public ZBufferRenderer setHeading(double heading)
    {
        this.heading = heading;
        updateTransform();
        return this;
    }
    
    public ZBufferRenderer setPitch(double pitch)
    {
        this.pitch = pitch;
        updateTransform();
        return this;
    }
    
    
    // =============================================================
    // General methods

    public void resize(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.zBuffer = new double[width * height];
        updateTransform();
    }

    /**
     * Retrieves the image used to render the scene.
     * 
     * @return the image used to render the scene.
     */
    public BufferedImage getImage()
    {
        return img;
    }

    
    // =============================================================
    // Rendering management

    public void reset()
    {
        // fill background using a gradient
        for (int y = 0; y < height; y++)
        {
            int val = 255 - (int) (y * 255) / height;
            int rgb = new Color(val, val, val).getRGB();
            for (int x = 0; x < width; x++)
            {
                img.setRGB(x, y, rgb);
            }
        }

        // reset z-buffer
        for (int q = 0; q < zBuffer.length; q++)
        {
            zBuffer[q] = Double.NEGATIVE_INFINITY;
        }
        
        updateTransform();
        
        // drawBasis
//        renderBasisCylinders(10, 1);
        renderBasisEdges(10.0);
    }
    
    public AffineTransform3D updateTransform()
    {
        double heading2 = Math.toRadians(this.heading);
        double pitch2 = Math.toRadians(this.pitch);
        
        // apply a rotation to have z pointing upward
        AffineTransform3D centering = AffineTransform3D.createTranslation(this.lookat).inverse();
        AffineTransform3D rotInit = AffineTransform3D.createRotationOx(Math.PI/2);
        AffineTransform3D scaling = AffineTransform3D.createScaling(zoom, zoom, zoom);
        
        AffineTransform3D rotHeading = AffineTransform3D.createRotationOy(heading2);
        AffineTransform3D rotPitch = AffineTransform3D.createRotationOx(pitch2);
        AffineTransform3D translate = AffineTransform3D.createTranslation(width / 2, height / 2, 0);
        this.transform = translate.compose(rotHeading).compose(rotPitch).compose(scaling).compose(rotInit).compose(centering);
        return transform;
    }

    /**
     * Draw three mutually orthogonal edges starting at the origin and parallel
     * to one of the main axes. All edges have the same length.
     * 
     * @param length
     *            the length of the edges
     */
    public void renderBasisEdges(double length)
    {
        // draw three orthogonal edges from the ori
        Point3D p0 = transform.transform(Point3D.of(0, 0, 0));
        Point3D p1 = transform.transform(Point3D.of(length, 0, 0));
        Point3D p2 = transform.transform(Point3D.of(0, length, 0));
        Point3D p3 = transform.transform(Point3D.of(0, 0, length));
        renderTransformedEdge(p0, p1, Color.RED);
        renderTransformedEdge(p0, p2, Color.GREEN);
        renderTransformedEdge(p0, p3, Color.BLUE);
    }
    
    /**
     * Renders three cylinders with same length and same radius that start at
     * origin, and that span the three principal directions.
     * 
     * @param length
     *            the length of the cylinders
     * @param radius
     *            the radius of the cylinders
     */
    public void renderBasisCylinders(double length, double radius)
    {
        Point3D p0 = new Point3D(0, 0, 0);
        Point3D p1 = new Point3D(length, 0, 0);
        Point3D p2 = new Point3D(0, length, 0);
        Point3D p3 = new Point3D(0, 0, length);
        renderCylinder(p0, p1, radius, Color.RED);
        renderCylinder(p0, p2, radius, Color.GREEN);
        renderCylinder(p0, p3, radius, Color.BLUE);
    }
    
    public void renderCylinder(Point3D p1, Point3D p2, double r, Color color)
    {
        Cylinder3D cyl = new Cylinder3D(p1, p2, r);
        TriMesh3D mesh = Meshes3D.triangulate(cyl.createMesh(36));
        renderMesh(mesh, color);
    }

    /**
     * Renders a triangle mesh, using the specified color.
     * 
     * @param mesh
     *            the mesh to render
     * @param faceColor
     *            the color used to render the mesh
     */
    public void renderMesh(TriMesh3D mesh, Color faceColor)
    {
        renderMeshFaces(mesh, faceColor);
    }
    
    private void renderMeshFaces(TriMesh3D mesh, Color faceColor)
    {
        for (Mesh3D.Face face : mesh.faces())
        {
            // transform vertex coords
            Iterator<? extends Mesh3D.Vertex> iter = face.vertices().iterator();
            Point3D v1 = transform.transform(iter.next().position());
            Point3D v2 = transform.transform(iter.next().position());
            Point3D v3 = transform.transform(iter.next().position());
            
            switch(shading)
            {
                case NONE -> renderTriangle_shadingNone(v1, v2, v3, faceColor);
                case FLAT -> renderTriangle_shadingFlat(v1, v2, v3, faceColor);
            }
        }        
    }
    
    private void renderTriangle_shadingNone(Point3D v1, Point3D v2, Point3D v3, Color faceColor)
    {
        // Calculate the range to be processed
        int minX = (int) Math.max(0, Math.ceil(min(v1.x(), v2.x(), v3.x())));
        int maxX = (int) Math.min(width - 1, Math.floor(max(v1.x(), v2.x(), v3.x())));
        int minY = (int) Math.max(0, Math.ceil(min(v1.y(), v2.y(), v3.y())));
        int maxY = (int) Math.min(height - 1, Math.floor(max(v1.y(), v2.y(), v3.y())));

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                // check face orientation
                Point3D p = Point3D.of(x, y, 0);
                // Judge once for each vertex
                boolean V1 = isSameSide(v1, v2, v3, p);
                boolean V2 = isSameSide(v2, v3, v1, p);
                boolean V3 = isSameSide(v3, v1, v2, p);
                if (V3 && V2 && V1)
                {
                    double depth = v1.z() + v2.z() + v3.z();
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth)
                    {
                        img.setRGB(x, y, faceColor.getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }
    
    private void renderTriangle_shadingFlat(Point3D v1, Point3D v2, Point3D v3, Color faceColor)
    {
        // compute face normal 
        Vector3D v12 = Vector3D.of(v1, v2);
        Vector3D v13 = Vector3D.of(v1, v3);
        Vector3D norm = Vector3D.crossProduct(v12, v13).normalize();
        
        double angleCos = Math.abs(norm.z());
        
        // Calculate the range to be processed
        int minX = (int) Math.max(0, Math.ceil(min(v1.x(), v2.x(), v3.x())));
        int maxX = (int) Math.min(width - 1, Math.floor(max(v1.x(), v2.x(), v3.x())));
        int minY = (int) Math.max(0, Math.ceil(min(v1.y(), v2.y(), v3.y())));
        int maxY = (int) Math.min(height - 1, Math.floor(max(v1.y(), v2.y(), v3.y())));

        Vector3D v23 = Vector3D.of(v2, v3);
        double triangleArea = v13.y() * v23.x() - v23.y() * v13.x();

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                double b1 = ((y - v3.y()) * (v2.x() - v3.x()) + (v2.y() - v3.y()) * (v3.x() - x)) / triangleArea;
                double b2 = ((y - v1.y()) * (v3.x() - v1.x()) + (v3.y() - v1.y()) * (v1.x() - x)) / triangleArea;
                double b3 = ((y - v2.y()) * (v1.x() - v2.x()) + (v1.y() - v2.y()) * (v2.x() - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1)
                {
                    double depth = b1 * v1.z() + b2 * v2.z() + b3 * v3.z();
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth)
                    {
                        img.setRGB(x, y, getShade(faceColor, angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }
    
    /**
     * Renders the edges of a triangle mesh, using the specified color.
     * 
     * @param mesh
     *            the mesh to render
     * @param edgeColor
     *            the color used to render the mesh edges
     */
    public void renderWireframeMesh(TriMesh3D mesh, Color edgeColor)
    {
        for (Mesh3D.Face face : mesh.faces())
        {
            // transform vertex coords
            Iterator<? extends Mesh3D.Vertex> iter = face.vertices().iterator();
            Point3D v1 = transform.transform(iter.next().position());
            Point3D v2 = transform.transform(iter.next().position());
            Point3D v3 = transform.transform(iter.next().position());
            renderTransformedEdge(v1, v2, edgeColor);
            renderTransformedEdge(v2, v3, edgeColor);
            renderTransformedEdge(v3, v1, edgeColor);
        }        
    }

    private void renderTransformedEdge(Point3D p1, Point3D p2, Color color)
    {
        int rgb = color.getRGB();
        
        Vector3D v12 = Vector3D.of(p1, p2);
        double[] deltas = new double[] {Math.abs(v12.x()), Math.abs(v12.y()), Math.abs(v12.z())};
        int dim = 0;
        if (deltas[1] > deltas[dim]) dim = 1;
        
        // make sure delta in the largest direction is positive
        if (dim == 0 && v12.x() < 0 || dim == 1 && v12.y() < 0)
        {
            Point3D tmp = p1;
            p1 = p2;
            p2 = tmp;
            v12 = v12.opposite();
        }
        
        if (dim == 0)
        {
            double dy = v12.y() / deltas[0];
            double dz = v12.z() / deltas[0];
            // compute the range to process
            int minX = (int) Math.max(0, Math.ceil(p1.x()));
            int maxX = (int) Math.min(width - 1, Math.floor(p2.x()));
            
            double t0 = minX - 1 - p1.x();
            double yf = p1.y() + t0 * dy;
            double zf = p1.z() + t0 * dz;
            for (int x = minX; x <= maxX; x++)
            {
                yf += dy;
                zf += dz;
                int y = (int) yf;
                if (y >= 0 && y < height)
                {
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < zf + EDGE_RENDERING_TOLERANCE)
                    {
                        img.setRGB(x, y, rgb);
                        zBuffer[zIndex] = zf;
                    }
                }
            }
        }
        else
        {
            double dx = v12.x() / deltas[1];
            double dz = v12.z() / deltas[1];
            // compute the range to process
            int minY = (int) Math.max(0, Math.ceil(p1.y()));
            int maxY = (int) Math.min(height - 1, Math.floor(p2.y()));
            
            double t0 = minY - 1 - p1.y();
            double xf = p1.x() + t0 * dx;
            double zf = p1.z() + t0 * dz;
            for (int y = minY; y <= maxY; y++)
            {
//              double xf = p1.x() + (y - p1.y()) * dx;
//              double zf = p1.z() + (y - p1.y()) * dz;
                xf += dx;
                zf += dz;
                int x = (int) xf;
                if (x >= 0 && x < width)
                {
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < zf + EDGE_RENDERING_TOLERANCE)
                    {
                        img.setRGB(x, y, rgb);
                        zBuffer[zIndex] = zf;
                    }
                }
            }
        }
    }
    
    
    // =============================================================
    // Computation helper methods

    /**
     * Returns the minimum of three values
     * @param v1 the first value
     * @param v2 the second value
     * @param v3 the third value
     * @return the minimum of the three values
     */
    private static final double min(double v1, double v2, double v3)
    {
        return Math.min(v1, Math.min(v2, v3));
    }
    
    /**
     * Returns the maximum of three values
     * @param v1 the first value
     * @param v2 the second value
     * @param v3 the third value
     * @return the maximum of the three values
     */
    private static final double max(double v1, double v2, double v3)
    {
        return Math.max(v1, Math.max(v2, v3));
    }

    private static final boolean isSameSide(Point3D A, Point3D B, Point3D C, Point3D p)
    {
        Vector3D v12 = Vector3D.of(A, B);
        Vector3D v13 = Vector3D.of(A, C);
        Vector3D v1p = Vector3D.of(A, p);

        // If the cross product of vector V1V2 and vector V1V3 is the same as
        // the one of vector V1V2 and vector V1p, they are on the same side.
        // We only need to judge the direction of z
        double v12xv13 = v12.x() * v13.y() - v13.x() * v12.y();
        double v12xv1p = v12.x() * v1p.y() - v1p.x() * v12.y();

        return v12xv13 * v12xv1p >= 0;
    }
    
    private static final Color getShade(Color color, double shade)
    {
        int red = gammaShade(color.getRed(), shade);
        int green = gammaShade(color.getGreen(), shade);
        int blue = gammaShade(color.getBlue(), shade);

        return new Color(red, green, blue);
    }
    
    private static final int gammaShade(int v, double shade)
    {
        int value = (int) Math.pow(Math.pow(v, 2.4) * shade, 1.0 / 2.4);
        return Math.min(255, Math.max(0, value));
    }
}
