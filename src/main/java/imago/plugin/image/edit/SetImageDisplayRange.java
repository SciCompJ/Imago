/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImageViewer;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class SetImageDisplayRange implements Plugin
{
    public SetImageDisplayRange()
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
        System.out.println("set manual display range");
        
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getDocument();
		Image image = doc.getImage();

		Array<?> array = image.getData();
		if (array instanceof VectorArray) 
		{
			array = VectorArray.norm((VectorArray<?>) array);
		}
		
		if (!(array instanceof ScalarArray))
		{
			throw new IllegalArgumentException("Requires a scalar Image");
		}
		ScalarArray<?> scalarArray = (ScalarArray<?>) array;
		
		// Compute min and max values within the array 
		double[] extent = scalarArray.valueRange();
		double[] displayRange = image.getDisplaySettings().getDisplayRange();
		
		// Create new dialog populated with widgets
		GenericDialog gd = new GenericDialog(frame, "Set Display Range");
		String labelMin = String.format("Min value (%6.2f) ", extent[0]);
		gd.addNumericField(labelMin, displayRange[0], 3, "Minimal value to display as black");
		String labelMax = String.format("Max value (%6.2f) ", extent[1]);
		gd.addNumericField(labelMax, displayRange[1], 3, "Maximal value to display as white");
		
		// wait for user validation or cancellation
		gd.showDialog();
		if (gd.wasCanceled())
		{
			return;
		}
		
		// extract user inputs
		double minRange = gd.getNextNumber();
		double maxRange = gd.getNextNumber();
		extent = new double[]{minRange, maxRange};

		// update display settings
		image.getDisplaySettings().setDisplayRange(extent);
		
		// refresh display
		ImageViewer viewer = ((ImageFrame) frame).getImageView();
		viewer.refreshDisplay();
		viewer.repaint();
	}
}

