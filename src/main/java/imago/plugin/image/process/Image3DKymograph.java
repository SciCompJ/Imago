/**
 * 
 */
package imago.plugin.image.process;

import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.plugin.image.ImagePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.axis.Axis;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Curve2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.LineSegment2D;
import net.sci.geom.geom2d.polygon.LineString2D;
import net.sci.geom.geom2d.polygon.Polyline2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.ImageAxis;
import net.sci.image.process.shape.Kymograph3D;

/**
 * Simple demo for line profile that computes profile along image diagonal.
 * 
 * @author David Legland
 *
 */
public class Image3DKymograph implements ImagePlugin
{
    public Image3DKymograph()
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
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        ImageViewer viewer = iframe.getImageView();
//        if (!(viewer instanceof PlanarImageViewer))
//        {
//            System.out.println("requires an instance of planar image viewer");
//            return;
//        }
        Image image = viewer.getImage();
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray3D))
        {
            System.out.println("requires a 3D scalar image as input");
            return;
        }

        Geometry selection = viewer.getSelection();
        if (!(selection instanceof Geometry2D))
        {
            return;
        }
        Geometry2D selection2d = (Geometry2D) selection;
        
        if (!(selection2d instanceof Curve2D))
        {
            System.out.println("requires selection to be a line string");
            return;
        }
        
        Curve2D curve = (Curve2D) selection;
        Polyline2D poly;
        if (curve instanceof Polyline2D)
            poly = (Polyline2D) curve;
        else if (curve instanceof LineSegment2D)
        {
            LineSegment2D line = (LineSegment2D) curve;   
            poly = LineString2D.create(line.getP1(), line.getP2());
        }
        else
        {
            poly = curve.asPolyline(100);
        }
        Kymograph3D algo = new Kymograph3D(poly);
        Array<?> res = algo.process(image.getData());
        
        Image resImage = new Image(res);
        Calibration calib = image.getCalibration();
        Calibration resCalib = resImage.getCalibration();
        resCalib.setAxis(0, new ImageAxis("Curvilinear Abscissa", Axis.Type.SPACE, 1.0, 0.0, ""));
        resCalib.setAxis(1, calib.getAxis(2));
        
        // add the image document to GUI
        frame.getGui().createImageFrame(resImage, frame);
    }
    
}
