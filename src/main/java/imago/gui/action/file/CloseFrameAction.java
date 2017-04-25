/**
 * 
 */
package imago.gui.action.file;

import java.awt.event.ActionEvent;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

/**
 * Closes the current frame.
 * 
 * @author dlegland
 *
 */
public class CloseFrameAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param frame the parent frame
	 * @param name the name of the action
	 */
	public CloseFrameAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("Close frame");
		this.gui.removeFrame(this.frame);
		this.frame.dispose();
	}

}
