/**
 * 
 */
package imago.image.plugin.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.gui.FramePlugin;

/**
 * Clear all the shapes stored in this document.
 * 
 * @author David Legland
 *
 */
public class DocClearShapes implements FramePlugin
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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		doc.clearShapes();
		
		frame.repaint();
	}

}
