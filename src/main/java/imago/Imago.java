/**
 * 
 */
package imago;

import imago.app.ImagoApp;
import imago.gui.ImagoGui;

/**
 * The class that launches the Imago Application.
 * 
 * @author David Legland
 *
 */
public class Imago
{
	public static Imago instance = null;

	private Imago()
	{
	}

	public static Imago getInstance()
	{
		if (instance == null)
			instance = new Imago();
		return instance;
	}

	/**
	 * Creates a new ImagoApp object, the corresponding GUI, and shows the main
	 * Frame.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		ImagoApp app = new ImagoApp();
		ImagoGui gui = new ImagoGui(app);
		gui.showEmptyFrame(true);
	}

}
