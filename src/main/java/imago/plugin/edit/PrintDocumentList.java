/**
 * 
 */
package imago.plugin.edit;

import imago.app.ImagoApp;

import java.awt.Dimension;
import java.util.ArrayList;

import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import imago.gui.FramePlugin;

/**
 * @author dlegland
 *
 */
public class PrintDocumentList implements FramePlugin
{
	/**
	 */
	public PrintDocumentList()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
		ImagoApp app = frame.getGui().getAppli();
		
		ArrayList<String> textLines = new ArrayList<String>();

		int nDocs = app.imageHandleNumber();
		textLines.add(String.format("Current application contains %d documents: ", nDocs));
		for (ImageHandle doc : app.getImageHandles())
		{
		    textLines.add("  " + doc.getName());
		}
		
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Frame List", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(400, 300));
        newFrame.setVisible(true);
	}
}
