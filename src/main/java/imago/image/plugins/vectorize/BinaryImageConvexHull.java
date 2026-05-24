/**
 * 
 */
package imago.image.plugins.vectorize;

import java.util.Collection;
import java.util.List;

import imago.app.ImagoApp;
import imago.app.scene.GroupNode;
import imago.app.scene.ShapeNode;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.geom.polygon2d.Polygons2D;
import net.sci.image.Image;
import net.sci.image.vectorize.BinaryImageBoundaryFacetMidPoints;

/**
 * 
 */
public class BinaryImageConvexHull extends AlgoStub implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public BinaryImageConvexHull()
    {
    }

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // get current image data
        ImageFrame iFrame = (ImageFrame) frame;
        ImageHandle handle = iFrame.getImageHandle();
        Image image = handle.getImage();
        ImagoApp app = frame.getGui().getAppli();
        
        // check input data type and dimensions
        Array<?> array = image.getData();
        if (array.elementClass() != Binary.class)
        {
            frame.showErrorDialog("Requires a binary image as input", "Data Type Error");
            return;
        }
        
        // Open a dialog to choose the iso-surface value
        GenericDialog dlg = new GenericDialog(frame, "Convex Hull");
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
        boolean addToImage = dlg.getNextBoolean();
        String imageToOverlayName = dlg.getNextChoice();
        boolean addToShapeManager = dlg.getNextBoolean();

        int nd = array.dimensionality();
        if (nd == 2)
        {
            BinaryArray2D array3d = BinaryArray2D.wrap(BinaryArray.wrap(array));
            long t0 = System.nanoTime();
            List<Point2D> points = new BinaryImageBoundaryFacetMidPoints().processBinary2d(array3d); 
            Polygon2D hull = Polygons2D.convexHull(points);
           
            // cleanup listener and status bar
            iFrame.getStatusBar().setProgressBarPercent(0);

            if (addToImage)
            {
                ImageHandle targetHandle = ImageHandle.findFromName(app, imageToOverlayName);

                
                ShapeNode hullNode = new ShapeNode("convexHull", hull);
                frame.algoProgressChanged(new AlgoEvent(this, "", 1, 1));
                
                // add new node to image handle
                ((GroupNode) handle.getRootNode()).addNode(hullNode);
                targetHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
            }
            
            // display elapsed time
            long t1 = System.nanoTime();
            iFrame.getStatusBar().setCurrentStepLabel("");
            iFrame.showElapsedTime("Compute Convex Hull", (t1 - t0) / 1_000_000.0, image);
            
            if (addToShapeManager)
            {
                GeometryHandle geomHandle = GeometryHandle.create(app, hull);
                
                // opens a dialog to choose name
                String name = "convexHull";
                name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
                geomHandle.setName(name);
                
                // ensure ShapeManager is visible
                ShapeManager manager = ShapeManager.getInstance(frame.getGui());
                manager.repaint();
                manager.setVisible(true);
            }
        }
        else
        {
            frame.showErrorDialog("Requires a 2D image", "Dimensionality Error");
            return;
        }
    }

}
