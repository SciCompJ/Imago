/**
 * 
 */
package imago.plugin.image.vectorize;

import java.awt.Color;
import java.util.Collection;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.array.scalar.Scalar;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.geom.geom3d.Plane3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.geom3d.polyline.Polyline3D;
import net.sci.geom.mesh.DefaultTriMesh3D;
import net.sci.geom.mesh.process.IntersectionMeshPlane;
import net.sci.image.Image;
import net.sci.image.vectorize.MorphologicalMarchingCubes;

/**
 * Computes isosurface of the current image, and saves the mesh into a text file
 * in OFF format.
 * 
 * @author dlegland
 *
 */
public class IsosurfaceSlices implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        Image image = iFrame.getImageHandle().getImage();
        ImagoGui gui = iFrame.getGui();
        
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            frame.showErrorDialog("Requires a scalar image input", "Data Type Error");
            return;
        }

        int nd = array.dimensionality();
        if (nd != 3)
        {
            frame.showErrorDialog("Requires a 3D image", "Dimensionality Error");
            return;
        }

        // wrap array into a 3D scalar array
        ScalarArray3D<? extends Scalar> scalar = ScalarArray3D.wrap((ScalarArray<?>) array);
        

        // Open a dialog to choose the iso-surface value
        GenericDialog dlg = new GenericDialog(frame, "Isosurface");
        double[] extent = image.getDisplaySettings().getDisplayRange(); 
        dlg.addSlider("Isosurface Value", extent[0], extent[1], (extent[0] + extent[1]) / 2);
        Collection<String> imageNames = gui.getAppli().getImageHandleNames();
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = iFrame.getImageHandle().getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        if (dlg.wasCanceled())
            return;
        // retrieve iso-surface value
        double value = dlg.getNextNumber();
        String imageToOverlayName = dlg.getNextChoice();

        
        // Call the morphological marching cube algorithm to compute a triangular mesh
        MorphologicalMarchingCubes mc = new MorphologicalMarchingCubes(value);
        
        // initialize algo monitoring
        mc.addAlgoListener(iFrame);
        iFrame.getStatusBar().setCurrentStepLabel("Compute Isosurface");
        
        // run isosurface computation
        long t0 = System.nanoTime();
        DefaultTriMesh3D mesh = DefaultTriMesh3D.convert(mc.process(scalar));
        
        // cleanup listener and status bar
        iFrame.getStatusBar().setProgressBarPercent(0);
        
        
        iFrame.getStatusBar().setCurrentStepLabel("Add isosurface to image shape tree");
        ImageHandle targetHandle = gui.getAppli().getImageHandleFromName(imageToOverlayName);
        ImageFrame viewer = gui.getImageFrame(targetHandle);
        
        // get serial sections node
        String nodeName = "isosurface";
        ImageSerialSectionsNode sectionsNode = getIsosurfaceNode(targetHandle, nodeName); 
        
        // compute slice for each z
        for (int z = 0; z < scalar.size(2); z++)
        {
            Plane3D plane = new Plane3D(new Point3D(0, 0, z+0.003), new Vector3D(0, 0, 1));
            Collection<Polyline3D> polylines = IntersectionMeshPlane.intersectionMeshPlane(mesh, plane);
            
            if (!polylines.isEmpty())
            {
                String name = String.format("slice-%03d", z);
                ImageSliceNode sliceNode = new ImageSliceNode(name, z);
                for (Polyline3D poly : polylines)
                {
                    sliceNode.addNode(createShapeNode(poly.projectXY(), "poly"));
                }
                sectionsNode.addSliceNode(sliceNode);
            }
        }
        
        viewer.repaint();
        
        // display elapsed time
        long t1 = System.nanoTime();
        iFrame.getStatusBar().setCurrentStepLabel("");
        iFrame.showElapsedTime("Compute Isosurface", (t1 - t0) / 1_000_000.0, image);
    }
    
    private ImageSerialSectionsNode getIsosurfaceNode(ImageHandle handle, String nodeName)
    {
        GroupNode rootNode = (GroupNode) handle.getRootNode();
        
        if (rootNode.hasChildWithName(nodeName))
        {
            return (ImageSerialSectionsNode) rootNode.getChild(nodeName);
        }
        ImageSerialSectionsNode sectionsNode = new ImageSerialSectionsNode(nodeName);
        rootNode.addNode(sectionsNode);
        return sectionsNode;
    }
    
    private ShapeNode createShapeNode(Polyline2D poly, String name)
    {
        // Create a new LinearRing shape from the boundary of the polygon
        ShapeNode shapeNode = new ShapeNode(name, poly);
        shapeNode.getStyle().setLineWidth(2.5);
        shapeNode.getStyle().setColor(Color.MAGENTA);
        return shapeNode;
    }

}