/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray3D;
import net.sci.array.vector.VectorArray;
import net.sci.array.vector.VectorArray2D;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertImage3DToVectorImage implements FramePlugin
{
	public ConvertImage3DToVectorImage() 
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();
		
		if (image == null)
		{
			return;
		}
		Array<?> array = image.getData();
		if (array == null)
		{
			return;
		}
		if (array.dimensionality() != 3 || !(array instanceof ScalarArray))
		{
            ImagoGui.showErrorDialog(frame, "Requires a 3D scalar stack", "Data Type Error");
			return;
		}

		VectorArray<?,?> vectArray = VectorArray2D.fromStack((ScalarArray3D<?>) array);
		Image result = new Image(vectArray, image);
				
		// add the image document to GUI
        ImageFrame.create(result, frame);
	}

}
