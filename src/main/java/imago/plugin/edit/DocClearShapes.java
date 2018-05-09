/**
 * 
 */
package imago.plugin.edit;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * Clear all the shapes stored in this document.
 * 
 * @author David Legland
 *
 */
public class DocClearShapes implements Plugin
{
	public DocClearShapes()
    {
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("clear shapes");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		doc.clearShapes();
		
		frame.repaint();
	}

}
