/**
 * 
 */
package imago.plugin;

import java.util.ArrayList;
import java.util.Collection;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;

/**
 * Tries to close all open frames, and quits the application.
 * 
 * @author dlegland
 *
 */
public class QuitApplication implements FramePlugin
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
	public void run(ImagoFrame parentFrame, String args)
	{
		System.out.println("Quit application");

		ImagoGui gui = parentFrame.getGui();
		
		// save user preferences for next use
		gui.getAppli().saveUserPreferences();
		
		Collection<ImagoFrame> frames = gui.getFrames();
        System.out.println("Need to close " + frames.size() + " frames");
        
		Collection<ImagoFrame> framesToClose = new ArrayList<ImagoFrame>(frames.size());
		framesToClose.addAll(frames);

		for (ImagoFrame frame : framesToClose)
		{
			System.out.println("  closing frame: " + frame.getWidget().getName());
			frame.close();
		}
		
		gui.disposeEmptyFrame();
		
		System.exit(0);
	}

}
