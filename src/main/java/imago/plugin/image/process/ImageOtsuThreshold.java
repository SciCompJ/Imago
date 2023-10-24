/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;


/**
 * @author David Legland
 *
 */
public class ImageOtsuThreshold implements FramePlugin 
{
	public ImageOtsuThreshold() 
	{
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

		OtsuThreshold op = new OtsuThreshold();
		Image resultImage = ((ImageFrame) frame).runOperator("Otsu Threshold", op, image);
		resultImage.setName(image.getName() + "-segOtsu");
		
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}

}
