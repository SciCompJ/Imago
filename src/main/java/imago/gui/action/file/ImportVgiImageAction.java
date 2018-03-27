/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sci.image.Image;
import net.sci.image.io.VgiImageReader;

/**
 * @author David Legland
 *
 */
public class ImportVgiImageAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFileChooser openWindow = null;

	public ImportVgiImageAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// create file dialog if it doesn't exist
		if (openWindow == null)
		{
			openWindow = new JFileChooser(".");
			openWindow.setFileFilter(new FileNameExtensionFilter("MetaImage files (*.vgi)", "vgi"));
		}

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(this.frame);
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file is value
		File file = openWindow.getSelectedFile();
		if (!file.isFile())
		{
			return;
		}

		// keep path for future opening
		String path = file.getPath();
		this.frame.setLastOpenPath(path);

		// Create a MetaImage Format reader with the chosen file
		VgiImageReader reader;
		try
		{
			reader = new VgiImageReader(file);
		} 
		catch (IOException ex)
		{
			System.err.println(ex);
			return;
		}

		// Try to read the image from the file
		Image image;
		try
		{
			image = reader.readImage();
		} catch (IOException ex)
		{
			System.err.println(ex);
			return;
		} catch (Exception ex)
		{
			System.err.println(ex);
			return;
		}
		image.setName(file.getName());
		
		// add the image document to GUI
        ImagoDocViewer frame = this.gui.addNewDocument(image);
        frame.setLastOpenPath(path);
	}

}
