/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;


/**
 * @author David Legland
 *
 */
public class ImageOtsuThreshold implements Plugin 
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
		System.out.println("Otsu Threshold");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		OtsuThreshold op = new OtsuThreshold();
		Image result = op.process(image);
				
		// add the image document to GUI
		frame.getGui().addNewDocument(result); 
	}

}
