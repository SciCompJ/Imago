/**
 * 
 */
package imago.gui.action.edit;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

/**
 * Clear all the shapes stored in this document.
 * 
 * @author David Legland
 *
 */
public class DocClearShapesAction extends ImagoAction 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DocClearShapesAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		System.out.println("clear shapes");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		doc.clearShapes();
		
		this.frame.repaint();
	}

}
