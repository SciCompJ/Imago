/**
 * 
 */
package imago.image.plugins.convert;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;


/**
 * Convert a vector image to its norm.
 * 
 * @author David Legland
 *
 */
public class CreateVectorImageNorm implements FramePlugin
{
	public CreateVectorImageNorm() 
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
        if (!(array instanceof VectorArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
            return;
        }

        VectorArray<?,?> vectorArray = (VectorArray<?,?>) array;
        
        ScalarArray<?> norm = VectorArray.norm(vectorArray);
        
        // create the image corresponding to channels concatenation
        Image normImage = new Image(norm, image);
        normImage.setName(image.getName() + "-norm");

        // add the image document to GUI
        ImageFrame.create(normImage, frame);
	}

}
