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
import net.sci.array.numeric.Vector;
import net.sci.array.numeric.VectorArray;
import net.sci.image.Image;
import net.sci.image.ImageType;


/**
 * Computes the norm of each element within a vector image, and returns the
 * result as an intensity image.
 * 
 * @author David Legland
 *
 */
public class CreateVectorImageNorm implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public CreateVectorImageNorm()
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
        if (!(Vector.class.isAssignableFrom(array.elementClass())))
        {
            ImagoGui.showErrorDialog(frame, "Requires a Vector image", "Data Type Error");
            return;
        }

        // wrap into a VectorArray
        @SuppressWarnings({ "unchecked", "rawtypes" })
        VectorArray<?,?> vectorArray = (VectorArray<?,?>) VectorArray.wrap((Array<Vector>) array);
        
        ScalarArray<?> norm = VectorArray.norm(vectorArray);
        
        // create the image corresponding to channels concatenation
        Image normImage = new Image(norm, ImageType.INTENSITY, image);
        normImage.setName(image.getName() + "-norm");

        // add the image document to GUI
        ImageFrame.create(normImage, frame);
	}

}
