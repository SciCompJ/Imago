/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

/**
 * Tries to close all open frames, and quits the application.
 * 
 * @author dlegland
 *
 */
public class QuitAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param frame the parent frame
	 * @param name the name of the action
	 */
	public QuitAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("Quit application");

		for (ImagoFrame frame : this.gui.getFrames())
		{
			System.out.println("  need to close: " + frame.getName());
			frame.dispose();
		}
		
		this.gui.disposeEmptyFrame();
		
		System.exit(0);
	}

}
