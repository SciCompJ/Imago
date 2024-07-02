/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.numeric.UInt8Array3D;
import net.sci.array.numeric.impl.BufferedUInt8Array3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh.DefaultTriMesh3D;
import net.sci.image.Image;

/**
 * Create a 3D mesh representing an octahedron, generate a discrete 3D grid,
 * computes the distance from each grid vertex to the mesh, and display the
 * resulting distance map.
 * 
 * @author David Legland
 *
 */
public class CreateDistanceToOctahedronImage3D implements FramePlugin
{
    public CreateDistanceToOctahedronImage3D()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Create Octahedron
        DefaultTriMesh3D mesh = new DefaultTriMesh3D();
        // int v1 = mesh.addVertex(new Point3D(90, 50, 50));
        // int v2 = mesh.addVertex(new Point3D(50, 90, 50));
        // int v3 = mesh.addVertex(new Point3D(10, 50, 50));
        // int v4 = mesh.addVertex(new Point3D(50, 10, 50));
        // int v5 = mesh.addVertex(new Point3D(50, 50, 90));
        // int v6 = mesh.addVertex(new Point3D(50, 50, 10));
        mesh.addVertex(new Point3D(90, 50, 50));
        mesh.addVertex(new Point3D(50, 90, 50));
        mesh.addVertex(new Point3D(10, 50, 50));
        mesh.addVertex(new Point3D(50, 10, 50));
        mesh.addVertex(new Point3D(50, 50, 90));
        mesh.addVertex(new Point3D(50, 50, 10));
        int v1 = 0, v2 = 1, v3 = 2, v4 = 3, v5 = 4, v6 = 5;
        
        mesh.addFace(v1, v2, v5);
        mesh.addFace(v2, v3, v5);
        mesh.addFace(v3, v4, v5);
        mesh.addFace(v4, v1, v5);
        mesh.addFace(v1, v6, v2);
        mesh.addFace(v2, v6, v3);
        mesh.addFace(v3, v6, v4);
        mesh.addFace(v1, v4, v6);
        
        // Image dimension
        int width = 100;
        int height = 100;
        int depth = 100;
        
        // Create new image data
        byte[] data = new byte[width * height * depth];
        
        // Initialize image data with raster content
        long tic = System.nanoTime();
        int offset = 0;
        for (int z = 0; z < depth; z++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    double dist = mesh.distance(x, y, z);
                    data[offset++] = (byte) Math.min(Math.floor(dist * 5), 255);
                }
            }
        }
        long toc = System.nanoTime();
        double elapsed = (toc - tic) / 1000000.0;
        System.out.println("Elapsed time: " + elapsed + " ms, " + elapsed / 1000000. + " ms/voxel");
        
        UInt8Array3D img3d = new BufferedUInt8Array3D(width, height, depth, data);
        
        // create the image
        Image image = new Image(img3d);
        image.setName("OctahedronDistMap");
        
        // add the image document to GUI
        ImageFrame.create(image, frame);
    }
    
}
