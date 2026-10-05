/**
 * 
 */
package imago.gui.plugins.file;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImagoEmptyFrame;

import java.util.ArrayList;
import java.util.Collection;

import imago.gui.FramePlugin;

/**
 * Closes all frames of the Imago Application except the empty frame.
 * 
 * @author dlegland
 *
 */
public class CloseAllFrames implements FramePlugin
{
	/**
	 * Default empty constructor.
	 */
	public CloseAllFrames()
	{
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame baseFrame, String args)
	{
	    ImagoGui gui = baseFrame.getGui();
	    
        // create a new collection with all frames, to avoid concurrent
        // modification of frame list
	    Collection<ImagoFrame> framesToClose = new ArrayList<ImagoFrame>();
	    framesToClose.addAll(gui.getFrames());
	    
	    // remove frames
		for (ImagoFrame frame : framesToClose)
		{
		    if (!(frame instanceof ImagoEmptyFrame))
		    {
	            frame.close();
		    }
		}
		
		ImagoFrame emptyFrame = (ImagoEmptyFrame) gui.getEmptyFrame(); 
		emptyFrame.setVisible(true);
	}
}
