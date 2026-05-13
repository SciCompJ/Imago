/**
 * 
 */
package imago.image.plugins.vectorize;

import java.util.Collection;
import java.util.List;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.geom.geom2d.MultiPoint2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.image.vectorize.BinaryImageBoundaryFacetMidPoints;

/**
 * 
 */
public class BinaryImageBoundaryPoints implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public BinaryImageBoundaryPoints()
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
        GenericDialog dlg = new GenericDialog(frame, "Boundary Points");
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

        
        // create the computation class
        BinaryImageBoundaryFacetMidPoints algo = new BinaryImageBoundaryFacetMidPoints();
        
        // switch depending on image dimensionality
        int nd = array.dimensionality();
        if (nd == 2) 
        {
            BinaryArray2D array2d = BinaryArray2D.wrap(BinaryArray.wrap(array));
            List<Point2D> pointList = BinaryImageBoundaryFacetMidPoints.reduce(algo.processBinary2d(array2d));
            MultiPoint2D multiPoint = MultiPoint2D.create(pointList);
            
            if (addToImage)
            {
                iFrame.getStatusBar().setCurrentStepLabel("Add boundary points to image shape tree");
                ImageHandle targetHandle = ImageHandle.findFromName(app, imageToOverlayName);
                
                Shape shape = new Shape(multiPoint);
                
                // add new node to image handle
                targetHandle.addShape(shape);
                targetHandle.notifyImageHandleChange(ImageHandle.Event.SHAPES_MASK | ImageHandle.Event.CHANGE_MASK);
            }
            
            if (addToShapeManager)
            {
                GeometryHandle geomHandle = GeometryHandle.create(app, multiPoint);
                
                // opens a dialog to choose name
                String name = image.getName() + "-boundaryPoints";
                name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
                geomHandle.setName(name);
                
                // ensure ShapeManager is visible
                ShapeManager manager = ShapeManager.getInstance(frame.getGui());
                manager.repaint();
                manager.setVisible(true);
            }
        }
    }
    
}
