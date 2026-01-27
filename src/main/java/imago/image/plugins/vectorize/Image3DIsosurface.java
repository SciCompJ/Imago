/**
 * 
 */
package imago.image.plugins.vectorize;

import java.awt.Color;
import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.ShapeNode;
import imago.app.shape.Style;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import net.sci.algo.AlgoEvent;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.geom.geom3d.Plane3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Vector3D;
import net.sci.geom.geom3d.polyline.Polyline3D;
import net.sci.geom.mesh3d.DefaultTriMesh3D;
import net.sci.geom.mesh3d.process.IntersectionMeshPlane;
import net.sci.image.Image;
import net.sci.image.vectorize.MorphologicalMarchingCubes;

/**
 * Computes isosurface of the current 3D scalar image. Result can be dispatched
 * on image XY slices, or added into the ShapeManager.
 * 
 * @author dlegland
 *
 */
public class Image3DIsosurface implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();
        Image image = handle.getImage();
        ImagoApp app = frame.getGui().getAppli();
        
        // check input data type and dimensions
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
        ScalarArray3D<?> scalar = ScalarArray3D.wrap((ScalarArray<?>) array);
        

        // Open a dialog to choose the iso-surface value
        GenericDialog dlg = new GenericDialog(frame, "Isosurface");
        double[] extent = image.getDisplaySettings().getDisplayRange(); 
        dlg.addSlider("Isosurface Value", extent[0], extent[1], (extent[0] + extent[1]) / 2);
        dlg.addCheckBox("Add to Image shapes", true);
        Collection<String> imageNames = ImageHandle.getAllNames(app);
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = handle.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.addCheckBox("Add to Shape Manager", false);
        dlg.showDialog();
        
        if (dlg.wasCanceled())
            return;
        
        // retrieve user choices
        double value = dlg.getNextNumber();
        boolean addToImage = dlg.getNextBoolean();
        String imageToOverlayName = dlg.getNextChoice();
        boolean addToShapeManager = dlg.getNextBoolean();

        
        // Call the morphological marching cube algorithm to compute a triangular mesh
        MorphologicalMarchingCubes mc = new MorphologicalMarchingCubes(value);
        
        // initialize algo monitoring
        mc.addAlgoListener(frame);
        iFrame.getStatusBar().setCurrentStepLabel("Compute Isosurface");
        
        // run isosurface computation
        long t0 = System.nanoTime();
        DefaultTriMesh3D mesh = DefaultTriMesh3D.convert(mc.process(scalar));
        
        // cleanup listener and status bar
        iFrame.getStatusBar().setProgressBarPercent(0);

        if (addToImage)
        {
            iFrame.getStatusBar().setCurrentStepLabel("Add isosurface to image shape tree");
            ImageHandle targetHandle = ImageHandle.findFromName(app, imageToOverlayName);

            // get serial sections node
            ImageSerialSectionsNode sectionsNode = new ImageSerialSectionsNode("isosurface");
            
            // create slice for display of mesh-plane intersection
            Style sliceStyle = new Style().setLineWidth(2.5).setLineColor(Color.MAGENTA);

            // for each slice, computes the intersection polygon, and if it is not empty, 
            // add it into a new "ImageSliceNode" within the sectionsNode instance.
            int sliceCount = scalar.size(2);
            for (int z = 0; z < sliceCount; z++)
            {
                frame.algoProgressChanged(new AlgoEvent(this, "", z, sliceCount));
                Plane3D plane = new Plane3D(new Point3D(0, 0, z+0.003), new Vector3D(0, 0, 1));
                Collection<Polyline3D> polylines = IntersectionMeshPlane.intersectionMeshPlane(mesh, plane);

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
            
            frame.algoProgressChanged(new AlgoEvent(this, "", 1, 1));
            
            // add new node to image handle
            ((GroupNode) handle.getRootNode()).addNode(sectionsNode);
            targetHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
        }
        
        // display elapsed time
        long t1 = System.nanoTime();
        iFrame.getStatusBar().setCurrentStepLabel("");
        iFrame.showElapsedTime("Compute Isosurface", (t1 - t0) / 1_000_000.0, image);
        
        if (addToShapeManager)
        {
            GeometryHandle geomHandle = GeometryHandle.create(app, mesh);
            
            // opens a dialog to choose name
            String name = geomHandle.getName();
            name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
    }
}
