/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.process.shape.Flip;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class FlipXImageAction extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FlipXImageAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		System.out.println("flip X");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		Flip filter = new Flip(0);
		Image result = image.apply(filter);
				
		// add the image document to GUI
		this.gui.addNewDocument(result); 
	}

}
