/**
 * 
 */
package imago.image.plugins.analyze;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.geom.polygon2d.OrientedBox2D;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.regionfeatures.RegionFeatures;
import net.sci.image.regionfeatures.morpho2d.Bounds;
import net.sci.image.regionfeatures.morpho2d.EquivalentEllipse;
import net.sci.image.regionfeatures.morpho2d.OrientedBoundingBox;
import net.sci.image.regionfeatures.morpho2d.core.ConvexHull;


/**
 * Compute a geometric feature from a label map image, and for each region
 * creates a new shape within the list of annotations of another image.
 */
public class RegionFeatureOverlay2D implements FramePlugin
{
    public static final String[] geometryNames = new String[] {"Ellipse", "Convex Hull", "Bounding Box", "Oriented Bounding Box"}; 
    
    /**
     * Default empty constructor.
     */
    public RegionFeatureOverlay2D()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
        {
            return;
        }
        
        // retrieve image data
        Image image = ((ImageFrame) frame).getImageHandle().getImage();
        if (!image.isLabelImage())
        {
            throw new IllegalArgumentException("Requires label image as input");
        }
        
        // retrieve list of image names
        ImagoApp app = frame.getGui().getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = ((ImageFrame) frame).getImageHandle().getName();
        
        GenericDialog dlg = new GenericDialog(frame, "Region Geometry");
        dlg.addChoice("Feature", geometryNames, geometryNames[0]);
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        // Display dialog and wait for user validation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // Parse dialog options
        int geometryIndex = dlg.getNextChoiceIndex();
        String imageToOverlay = dlg.getNextChoice();

        RegionFeatures analyzer = RegionFeatures.initialize(image);
        ImageHandle handle = ImageHandle.findFromName(app, imageToOverlay);
        
        AffineTransform2D transfo = createCalibrationTransform(image.getCalibration());
        
        switch(geometryIndex)
        {
            case 0 ->
            {
                // Process ellipses
                Ellipse2D[] ellipses = new EquivalentEllipse().compute(analyzer);
                // add to the Image handle
                for (int i = 0; i < ellipses.length; i++)
                {
                    Ellipse2D elli = ellipses[i].transform(transfo.inverse());
                    handle.addShape(new Shape(elli));
                }
            }
            case 1 ->
            {
                // Process convex hulls
                Polygon2D[] hulls = new ConvexHull().compute(analyzer);
                // add to the Image handle
                for (int i = 0; i < hulls.length; i++)
                {
                    handle.addShape(new Shape(hulls[i]));
                }
            }
            case 2 ->
            {
                // Process bounding boxes
                Bounds2D[] boxes = new Bounds().compute(analyzer);
                // add to the Image handle
                for (int i = 0; i < boxes.length; i++)
                {
                    Polygon2D poly = boxes[i].getRectangle().transform(transfo.inverse());
                    handle.addShape(new Shape(poly));
                }
            }
            case 3 ->
            {
                // Process oriented bounding boxes
                OrientedBox2D[] boxes = new OrientedBoundingBox().compute(analyzer);
                // add to the Image handle
                for (int i = 0; i < boxes.length; i++)
                {
                    Polygon2D poly = boxes[i].transform(transfo.inverse());
                    handle.addShape(new Shape(poly));
                }
            }
            default -> 
            {
                throw new RuntimeException("Unknown choice value");
            }
        }
        
        // notify change to update viewer(s)
        handle.notifyImageHandleChange();
    }
    
    private static final AffineTransform2D createCalibrationTransform(Calibration calib)
    {
        if (calib == null || !calib.isCalibrated()) return AffineTransform2D.IDENTITY;
        
        AffineTransform2D tra = AffineTransform2D.createTranslation(calib.getXAxis().getOrigin(), calib.getYAxis().getOrigin());
        AffineTransform2D sca = AffineTransform2D.createScaling(calib.getXAxis().getSpacing(), calib.getYAxis().getSpacing());
        return tra.compose(sca);
    }
}
