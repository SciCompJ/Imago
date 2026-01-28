/**
 * 
 */
package imago.developer.plugins;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;

/**
 * @author dlegland
 *
 */
public class PrintFrameList implements FramePlugin
{
	/**
	 */
	public PrintFrameList()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		Collection<ImagoFrame> frameList = frame.getGui().getFrames(); 
		int nFrames = frameList.size();

		ArrayList<String> textLines = new ArrayList<String>();

		textLines.add(String.format("Current GUI contains %d frames: ", nFrames));
		for (ImagoFrame frm : frameList)
		{
		    if (frm instanceof ImageFrame)
		    {
		        String docName = ((ImageFrame) frm).getImageHandle().getName();
		        textLines.add("  frame: " + frm.getWidget().getName() + ", image handle: " + docName);
		    }
		    else
		    {
		        textLines.add("  frame: " + frame.getWidget().getName());
		    }
		}
		
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Frame List", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(400, 300));
        newFrame.setVisible(true);
	}

}
