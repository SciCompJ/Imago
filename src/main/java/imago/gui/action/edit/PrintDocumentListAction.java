/**
 * 
 */
package imago.gui.action.edit;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

/**
 * @author dlegland
 *
 */
public class PrintDocumentListAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param frame
	 * @param name
	 */
	public PrintDocumentListAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent arg0) 
	{
		ImagoApp app = this.gui.getAppli();
		int nDocs = app.documentNumber();
		System.out.println(String.format("Current application contains %d documents: ", nDocs));
		for (ImagoDoc doc : app.getDocuments())
		{
			System.out.println("  " + doc.getName());
		}
	}

}
