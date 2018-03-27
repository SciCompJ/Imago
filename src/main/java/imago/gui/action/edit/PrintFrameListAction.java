/**
 * 
 */
package imago.gui.action.edit;

import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.util.Collection;

/**
 * @author dlegland
 *
 */
public class PrintFrameListAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param frame
	 * @param name
	 */
	public PrintFrameListAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent arg0) 
	{
		Collection<ImagoFrame> frameList = this.gui.getFrames(); 
		int nFrames = frameList.size();
		System.out.println(String.format("Current GUI contains %d frames: ", nFrames));
		for (ImagoFrame frame : frameList)
		{
		    if (frame instanceof ImagoDocViewer)
		    {
		        String docName = ((ImagoDocViewer)frame).getDocument().getName();
                System.out.println("  frame: " + frame.getName() + ", doc: " + docName);
		    }
		    else
		    {
	            System.out.println("  frame: " + frame.getName());
		    }
		}
	}

}
