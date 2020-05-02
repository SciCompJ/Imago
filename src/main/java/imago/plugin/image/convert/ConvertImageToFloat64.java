/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class ConvertImageToFloat64 implements Plugin 
{
	public ConvertImageToFloat64() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		System.out.println("convert to float64 image");
		
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getDocument();
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
        if (!(array instanceof ScalarArray))
        {
            return;
        }
		
		Float64Array result = Float64Array.convert((ScalarArray<?>) array);
		Image resultImage = new Image(result, image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(resultImage); 
	}

}
