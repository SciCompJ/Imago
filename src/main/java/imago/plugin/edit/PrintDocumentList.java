/**
 * 
 */
package imago.plugin.edit;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;
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
		int nDocs = app.documentNumber();
		System.out.println(String.format("Current application contains %d documents: ", nDocs));
		for (ImagoDoc doc : app.getDocuments())
		{
			System.out.println("  " + doc.getName());
		}
	}

}
