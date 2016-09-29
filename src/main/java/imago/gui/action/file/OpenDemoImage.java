/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class OpenDemoImage extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String fileName;
	
	public OpenDemoImage(ImagoFrame frame, String name, String imageName) {
		super(frame, name);
		this.fileName = imageName;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
//		System.out.println("Open demo image");
		
		File file = new File(this.fileName);

		Image image;
		try 
		{
			image = Image.readImage(file);
		} 
		catch (FileNotFoundException ex) {
//			ex.printStackTrace(System.err);
			this.frame.showErrorDialog(
					"Could not find the file: " + file.getName()); 
			return;
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
			this.frame.showErrorDialog(ex.getMessage(), "File Input Error");
			return;
		}
		
		// add the image document to GUI
		this.gui.addNewDocument(image); 
	}

}
