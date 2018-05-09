/**
 * 
 */
package imago.plugin.edit;

import java.util.Collection;

import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class PrintFrameList implements Plugin
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
		System.out.println(String.format("Current GUI contains %d frames: ", nFrames));
		for (ImagoFrame frm : frameList)
		{
		    if (frm instanceof ImagoDocViewer)
		    {
		        String docName = ((ImagoDocViewer) frm).getDocument().getName();
                System.out.println("  frame: " + frm.getWidget().getName() + ", doc: " + docName);
		    }
		    else
		    {
	            System.out.println("  frame: " + frame.getWidget().getName());
		    }
		}
	}

}
