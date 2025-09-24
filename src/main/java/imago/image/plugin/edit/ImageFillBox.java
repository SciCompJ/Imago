/**
 * 
 */
package imago.image.plugin.edit;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.image.Image;

/**
 * Fills a 2D or 3D box within an image.
 * 
 * @author David Legland
 *
 */
public class ImageFillBox  implements FramePlugin
{
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
		Image image	= handle.getImage();
		Array<?> array = image.getData();

		if (!(array instanceof ScalarArray))
		{
		    throw new RuntimeException("Requires an image containing a ScalarArray");
		}
		
		// retrieve image dimensions
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        boolean is3d = array.dimensionality() == 3;
        int sizeZ = is3d ? array.size(2) : 1;
        
        // setup a dialog to choose box extent
		GenericDialog gd = new GenericDialog(frame, "Fill Box");
        gd.addNumericField("X Min", sizeX * 0.25, 0);
        gd.addNumericField("X Max", sizeX * 0.75, 0);
        gd.addNumericField("Y Min", sizeY * 0.25, 0);
        gd.addNumericField("Y Max", sizeY * 0.75, 0);
        if (is3d)
        {
            gd.addNumericField("Z Min", sizeZ * 0.25, 0);
            gd.addNumericField("Z Max", sizeZ * 0.75, 0);
        }
        gd.addNumericField("Fill Value ", 255, 2);
		
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// retrieve bounds from dialog
        int xMin = (int) gd.getNextNumber();
        int xMax = (int) gd.getNextNumber();
        int yMin = (int) gd.getNextNumber();
        int yMax = (int) gd.getNextNumber();
        int zMin = 0, zMax = 0;
        if (is3d)
        {
            zMin = (int) gd.getNextNumber();
            zMax = (int) gd.getNextNumber();
        }
        double value = gd.getNextNumber();
		
		// create disk
        if (!is3d)
        {
            fillBox2d(ScalarArray2D.wrap((ScalarArray<?>) array), xMin, xMax, yMin, yMax, value);
        }
        else
        {
            fillBox3d(ScalarArray3D.wrap((ScalarArray<?>) array), xMin, xMax, yMin, yMax, zMin, zMax, value);
        }
		
        // notify changes
        handle.notifyImageHandleChange(ImageHandle.Event.IMAGE_MASK | ImageHandle.Event.CHANGE_MASK);
	}
	
    private void fillBox2d(ScalarArray2D<?> array, int xmin, int xmax, int ymin, int ymax, double value)
    {
        for (int y = ymin; y < ymax; y++)
        {
            for (int x = xmin; x < xmax; x++)
            {
                array.setValue(x, y, value);
            }
        }
    }

    private void fillBox3d(ScalarArray3D<?> array, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, double value)
	{
        for (int z = zmin; z < zmax; z++)
        {
            for (int y = ymin; y < ymax; y++)
            {
                for (int x = xmin; x < xmax; x++)
                {
                    array.setValue(x, y, z, value);
                }
            }
        }
	}
}
