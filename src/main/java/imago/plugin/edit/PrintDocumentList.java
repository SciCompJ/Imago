/**
 * 
 */
package imago.plugin.edit;

import imago.app.ImagoApp;
import imago.app.ImageHandle;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class PrintDocumentList implements Plugin
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
		int nDocs = app.imageHandleNumber();
		System.out.println(String.format("Current application contains %d documents: ", nDocs));
		for (ImageHandle doc : app.getImageHandles())
		{
			System.out.println("  " + doc.getName());
		}
	}

}
