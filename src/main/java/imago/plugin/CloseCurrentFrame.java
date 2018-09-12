/**
 * 
 */
package imago.plugin;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * Closes the current frame.
 * 
 * @author dlegland
 *
 */
public class CloseCurrentFrame implements Plugin
{
	/**
	 */
	public CloseCurrentFrame()
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("Close frame");
		frame.getGui().removeFrame(frame);
		frame.close();
	}
}
