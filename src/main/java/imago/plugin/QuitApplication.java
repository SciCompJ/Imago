/**
 * 
 */
package imago.plugin;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;

/**
 * Tries to close all open frames, and quits the application.
 * 
 * @author dlegland
 *
 */
public class QuitApplication implements Plugin
{
	/**
	 */
	public QuitApplication()
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("Quit application");

		ImagoGui gui = frame.getGui();
		for (ImagoFrame frm : gui.getFrames())
		{
			System.out.println("  need to close: " + frm.getWidget().getName());
			frm.close();
		}
		
		gui.disposeEmptyFrame();
		
		System.exit(0);
	}

}
