/**
 * 
 */
package imago.gui.plugins.file;

import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;

/**
 * Closes the current frame.
 * 
 * @author dlegland
 *
 */
public class CloseCurrentFrame implements FramePlugin
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
//		frame.getGui().removeFrame(frame);
		frame.close();
	}
}
