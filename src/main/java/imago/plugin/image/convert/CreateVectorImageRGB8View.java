/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.color.RGB8Array;
import net.sci.array.color.VectorArrayRGB8View;
import net.sci.array.vector.VectorArray;
import net.sci.image.Image;


/**
 * 
 * @see VectorImageConvertToRGB
 * 
 * @author David Legland
 *
 */
public class CreateVectorImageRGB8View implements FramePlugin
{
	public CreateVectorImageRGB8View() 
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
        int nChannels = vectorArray.channelCount();
        
        // Create dialog for choosing channel indices
        GenericDialog dlg = new GenericDialog(frame, "Extract Channel");
        dlg.addNumericField("Red Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        dlg.addNumericField("Green Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        dlg.addNumericField("Blue Channel index", 0, 0, String.format("Between 0 and %d", nChannels));
        
        // Display dialog and wait for OK or Cancel
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // extract user choices
        int indR = (int) dlg.getNextNumber();
        if (indR < 0 || indR >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Red Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        int indG = (int) dlg.getNextNumber();
        if (indG < 0 || indG >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Green Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        int indB = (int) dlg.getNextNumber();
        if (indB < 0 || indB >= nChannels)
        {
            ImagoGui.showErrorDialog(frame, "Blue Channel index must be comprised between 0 and " + (nChannels-1), "Input Error");
            return;
        }
        
        RGB8Array rgbArray = new VectorArrayRGB8View(vectorArray, indR, indG, indB);
        
        // create the image corresponding to channels concatenation
        Image rgbImage = new Image(rgbArray, image);
        rgbImage.setName(image.getName() + "-RGB");

        // add the image document to GUI
        ImageFrame.create(rgbImage, frame);
	}

}
