/**
 * 
 */
package imago.plugin;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * Closes the current frame as well as the children frame.
 * 
 * @author dlegland
 *
 */
public class CloseWithChildren implements Plugin
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
		System.out.println("Close frame with children");
//		frame.getGui().removeFrame(frame);
        frame.closeChildren();
		frame.close();
	}
}