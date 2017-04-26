/**
 * 
 */
package imago.gui.action.edit;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

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
			System.out.println("  " + frame.getName());
		}
	}

}
