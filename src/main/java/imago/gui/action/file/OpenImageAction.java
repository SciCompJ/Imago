/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class OpenImageAction extends ImagoAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFileChooser openWindow = null;

	public OpenImageAction(ImagoFrame frame, String name) {
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		// create file dialog if it doesn't exist
		if (openWindow == null)
		{
			openWindow = new JFileChooser(".");
			// openWindow.setFileFilter(fileFilter);
		}

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(this.frame);
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file is state
		File file = openWindow.getSelectedFile();
		if (!file.isFile())
		{
			return;
		}

		Image image;
		try
		{
			image = Image.readImage(file);
		} 
		catch (IOException ex)
		{
			ex.printStackTrace(System.err);
			// custom title, error icon
			JOptionPane.showMessageDialog(this.frame,
					"Could not read the image.", "Image I/O Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this.frame,
					"Could not read the image.", "Image I/O Error",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace(System.err);
			return;
		}

		image.setName(file.getName());
		
		// add the image document to GUI
		this.gui.addNewDocument(image); 
	}

}
