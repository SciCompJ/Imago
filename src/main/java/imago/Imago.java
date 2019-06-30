/**
 * 
 */
package imago;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;

import java.io.File;
import java.io.IOException;

import net.sci.image.Image;

/**
 * The class that launches the Imago Application.
 * 
 * @author David Legland
 *
 */
public class Imago
{
    // ===================================================================
    // Static methods

    /**
     * Reads an image from the specified file. If an exception occurs, displays
     * it into an error frame centered on the specified frame, and returns null.
     * 
     * @param file
     *            the file containing the image
     * @param frame
     *            the frame used to display error message
     * @return the image, or null if an error occurred
     */
    public static Image readImage(File file, ImagoFrame frame)
    {
        Image image;
        try
        {
            image = Image.readImage(file);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
            if (frame != null)
            {
                ImagoGui.showErrorDialog(frame,
                        "Could not read image " + file.getName(),
                        "Image I/O Error");
            }
            return null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            if (frame != null)
            {
                ImagoGui.showErrorDialog(frame,
                        "Could not read image " + file.getName(),
                        "Image I/O Error");
            }
            return null;
        }
        
        return  image;
    }
    
    public static Imago getInstance()
    {
        if (instance == null)
            instance = new Imago();
        return instance;
    }

    
    // ===================================================================
    // Static class members

	private static Imago instance = null;

	
    // ===================================================================
    // Constructors

	/**
	 * Private constructor to prevent instantiation from outside classes
	 */
	private Imago()
	{
	}


	// ===================================================================
    // Constructors

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
