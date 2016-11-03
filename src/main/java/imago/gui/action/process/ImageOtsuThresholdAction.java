/**
 * 
 */
package imago.gui.action.process;

import java.awt.event.ActionEvent;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.image.Image;
import net.sci.image.process.segment.OtsuThreshold;


/**
 * @author David Legland
 *
 */
public class ImageOtsuThresholdAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ImageOtsuThresholdAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("Otsu Threshold");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		OtsuThreshold op = new OtsuThreshold();
		Image result = op.process(image);
				
		// add the image document to GUI
		this.gui.addNewDocument(result); 
	}

}
