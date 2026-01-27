/**
 * 
 */
package imago.shape.plugins.edit;

import java.awt.Color;
import java.util.Collection;

import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.app.shape.Style;
import imago.gui.Dialogs;
import imago.gui.ImagoFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.shape.plugins.ShapeManagerPlugin;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom3d.Geometry3D;
import net.sci.geom.geom3d.Plane3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.geom3d.polyline.Polyline3D;
import net.sci.geom.mesh3d.DefaultTriMesh3D;
import net.sci.geom.mesh3d.EdgeMesh3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.geom.mesh3d.Meshes3D;
import net.sci.geom.mesh3d.process.IntersectionMeshPlane;
import net.sci.image.Image;

/**
 * Opens a dialog to choose an image, and copies the selected shapes in the
 * scene graph tree of the selected image.
 */
public class CopyToImageShapeNode implements ShapeManagerPlugin
{
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ShapeManager sm = (ShapeManager) frame;
        
        ImageHandle imageHandle = Dialogs.chooseImage(frame, "Move To Image Shape Node", "Image to Update");
        if (imageHandle == null) 
        {
            return;
        }
        
        GroupNode rootNode = (GroupNode) imageHandle.getRootNode();
        for (GeometryHandle handle : sm.getSelectedHandles())
        {
            switch (handle.getGeometry())
            {
                case Geometry2D geom2d -> 
                {
                    rootNode.addNode(new ShapeNode(handle.getName(), geom2d));
                }
                case Geometry3D geom3d -> 
                {
                    Image image = imageHandle.getImage();
                    if (image.getDimension() != 3)
                    {
                        System.err.println("Requires a 3D image");
                        continue;
                    }
                    
                    int nSlices = image.getSize(2);
                    ImageSerialSectionsNode node = switch (geom3d)
                    {
                        case Mesh3D mesh -> computeSerialSectionNode(mesh, nSlices);
                        default -> null;
                    };
                    node.setName(handle.getName());
                    rootNode.addNode(node);
                }
                default -> 
                {
                    System.err.println("Unable to manage geometry with class: " + handle.getGeometry().getClass());
                }
            }
            ;
        }
        
        imageHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
    }
    
    private static final ImageSerialSectionsNode computeSerialSectionNode(Mesh3D mesh, int nSlices)
    {
        // requires an instance of "EdgeMesh" specialization for computing
        // intersections with planes
        EdgeMesh3D edgeMesh = mesh instanceof EdgeMesh3D ? (EdgeMesh3D) mesh
                : DefaultTriMesh3D.convert(Meshes3D.triangulate(mesh));
        
        // get serial sections node
        ImageSerialSectionsNode sectionsNode = new ImageSerialSectionsNode("isosurface");
        
        // create slice for display of mesh-plane intersection
        Style sliceStyle = new Style().setLineWidth(2.5).setLineColor(Color.MAGENTA);
        
        // for each slice, computes the intersection polygon, and if it is not
        // empty,
        // add it into a new "ImageSliceNode" within the sectionsNode instance.
        for (int z = 0; z < nSlices; z++)
        {
            // frame.algoProgressChanged(new AlgoEvent(this, "", z,
            // sliceCount));
            Plane3D plane = new Plane3D(new Point3D(0, 0, z + 0.003), new Vector3D(0, 0, 1));
            Collection<Polyline3D> polylines = IntersectionMeshPlane.intersectionMeshPlane(edgeMesh, plane);
            
            if (!polylines.isEmpty())
            {
                String name = String.format("slice-%03d", z);
                ImageSliceNode sliceNode = new ImageSliceNode(name, z);
                
                int i = 0;
                for (Polyline3D poly : polylines)
                {
                    sliceNode.addNode(new ShapeNode("poly" + (i++), poly.projectXY(), sliceStyle));
                }
                sectionsNode.addSliceNode(sliceNode);
            }
        }
        
        // frame.algoProgressChanged(new AlgoEvent(this, "", 1, 1));
        return sectionsNode;
    }
}
