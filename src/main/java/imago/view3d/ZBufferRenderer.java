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
import net.sci.geom.mesh3d.TriMesh3D;

/**
 * A simple 3D rendered based on a z-buffer.
 */
public class ZBufferRenderer
{
    public enum Shading
    {
        NONE,
        FLAT;
    }
    
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
    double heading = 0;
    double pitch = 0;
    double zoom = 1.0;
    
    // centered world-to-image transform
    AffineTransform3D transform = AffineTransform3D.IDENTITY;

    /**
     * The shading parameter for rendering of mesh faces.
     */
    Shading shading = Shading.FLAT;

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
    
    public ZBufferRenderer setShading(Shading shading)
    {
        this.shading = shading;
        return this;
    }
    
    public ZBufferRenderer setZoom(double zoom)
    {
        this.zoom = zoom;
        return this;
    }
    
    public ZBufferRenderer setHeading(double heading)
    {
        this.heading = heading;
        return this;
    }
    
    public ZBufferRenderer setPitch(double pitch)
    {
        this.pitch = pitch;
        return this;
    }
    
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
    }
    
    public void resize(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.zBuffer = new double[width * height];
    }
    
    public AffineTransform3D updateTransform()
    {
        double heading2 = Math.toRadians(this.heading);
        double pitch2 = Math.toRadians(this.pitch);
        
        // apply a rotation to have z pointing upward
        AffineTransform3D rotInit = AffineTransform3D.createRotationOx(Math.PI/2);
        AffineTransform3D scaling = AffineTransform3D.createScaling(zoom, zoom, zoom);
        
        AffineTransform3D rotHeading = AffineTransform3D.createRotationOy(heading2);
        AffineTransform3D rotPitch = AffineTransform3D.createRotationOx(pitch2);
        AffineTransform3D translate = AffineTransform3D.createTranslation(width / 2, height / 2, 0);
        this.transform = translate.compose(rotHeading).compose(rotPitch).compose(scaling).compose(rotInit);
        return transform;
    }
    
    public void renderMesh(TriMesh3D mesh, Color faceColor)
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
     * Retrieves the image used to render the scene.
     * 
     * @return the image used to render the scene.
     */
    public BufferedImage getImage()
    {
        return img;
    }
    
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
