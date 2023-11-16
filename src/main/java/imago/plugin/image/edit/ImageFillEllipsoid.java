/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.geom3d.Rotation3D;
import net.sci.geom.geom3d.surface.Ellipsoid3D;
import net.sci.image.Image;

/**
 * Fills a 3D ellipsoid within the image.
 * 
 * @author David Legland
 *
 */
public class ImageFillEllipsoid implements FramePlugin
{
	public ImageFillEllipsoid()
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
		// get current image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
		
		Array<?> array = image.getData();
		if (!(array instanceof ScalarArray3D))
		{
		    throw new RuntimeException("Requires an image containing a ScalarArray3D");
		}
		
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        int sizeZ = array.size(2);
        int sizeMin = Math.min(Math.min(sizeX, sizeY), sizeZ);
        
        // create dialog to input ellipse parameters
		GenericDialog gd = new GenericDialog(frame, "Fill Ellipsoid");
        gd.addNumericField("X Center ", sizeX / 2, 2);
        gd.addNumericField("Y Center ", sizeY / 2, 2);
        gd.addNumericField("Z Center ", sizeZ / 2, 2);
        gd.addNumericField("Radius 1 ", sizeMin / 4, 2);
        gd.addNumericField("Radius 2 ", sizeMin / 4, 2);
        gd.addNumericField("Radius 3 ", sizeMin / 4, 2);
        gd.addNumericField("Euler Angle X (degrees) ", 0, 2);
        gd.addNumericField("Euler Angle Y (degrees) ", 0, 2);
        gd.addNumericField("Euler Angle Z (degrees) ", 0, 2);
        gd.addNumericField("Value ", 255, 2);
		
		gd.showDialog();
		
		if (gd.wasCanceled()) 
		{
			return;
		}
		
		// extract ellipsoid parameters
        double centerX = gd.getNextNumber();
        double centerY = gd.getNextNumber();
        double centerZ = gd.getNextNumber();
        double r1 = gd.getNextNumber();
        double r2 = gd.getNextNumber();
        double r3 = gd.getNextNumber();
        double eulerX = Math.toRadians(gd.getNextNumber());
        double eulerY = Math.toRadians(gd.getNextNumber());
        double eulerZ = Math.toRadians(gd.getNextNumber());
        double value = gd.getNextNumber();
		
		// create ellipsoid
        Point3D center = new Point3D(centerX, centerY, centerZ);
        Rotation3D orient = Rotation3D.fromEulerAngles(eulerX, eulerY, eulerZ);
        Ellipsoid3D elli = new Ellipsoid3D(center, r1, r2, r3, orient);
        fillEllipsoid((ScalarArray3D<?>) array, elli, value);
		
        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
	}
	
    public static final void fillEllipsoid(ScalarArray3D<?> array, Ellipsoid3D elli, double value)
    {
        // get image size
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        int sizeZ = array.size(2);
        
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (elli.isInside(x, y, z))
                    {
                        array.setValue(x, y, z, value);
                    }
                }
            }
        }
    }
}
