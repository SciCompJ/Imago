/**
 * 
 */
package imago.image.plugins.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.io.MetaImageReader;

/**
 * @author David Legland
 *
 */
public class ImportImageMetaImage implements FramePlugin
{
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
        // choose the file to open
	    File file = frame.getGui().chooseFileToOpen(frame, "Open MetaImage File", ImageFileFilters.META_IMAGE);
	    
		// Check the chosen file is valid
		if (file == null || !file.isFile())
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
            ex.printStackTrace(System.err);
            throw new RuntimeException("Unable to read image from file: " + file.getName(), ex);
        }

		// add the image document to GUI
        ImageFrame.create(image, frame);
	}

}
