/**
 * 
 */
package imago.gui.plugin.file;

import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;

/**
 * Closes the current frame as well as the children frame.
 * 
 * @author dlegland
 *
 */
public class CloseWithChildren implements FramePlugin
{
	/**
	 */
	public CloseWithChildren()
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
//		frame.getGui().removeFrame(frame);
        frame.closeChildren();
		frame.close();
	}
}
