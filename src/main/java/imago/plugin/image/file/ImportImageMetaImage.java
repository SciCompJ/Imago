/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.io.MetaImageReader;

/**
 * @author David Legland
 *
 */
public class ImportImageMetaImage implements FramePlugin
{
	private JFileChooser openWindow = null;

	public ImportImageMetaImage()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// create file dialog if it doesn't exist
		if (openWindow == null)
		{
			openWindow = new JFileChooser(".");
			openWindow.setFileFilter(new FileNameExtensionFilter("MetaImage files (*.mhd, *.mha)", "mhd", "mha"));
		}

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(frame.getWidget());
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

		// Create a MetaImage Format reader with the chosen file
		MetaImageReader reader;
		try
		{
			reader = new MetaImageReader(file);
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("Unable to find input file: " + file.getName(), ex);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Unable to open file: " + file.getName(), ex);
		}

		// Try to read the image from the file
		Image image;
		try
		{
			image = reader.readImage();
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("Unable to find image file from MHD file: " + file.getName(), ex);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Unable to read image from file: " + file.getName(), ex);
        }

		// add the image document to GUI
		frame.createImageFrame(image);
	}

}
