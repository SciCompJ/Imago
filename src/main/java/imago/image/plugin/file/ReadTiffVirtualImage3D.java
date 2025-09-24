/**
 * 
 */
package imago.image.plugin.file;

import java.io.File;
import java.io.IOException;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;


/**
 * @author David Legland
 *
 */
public class ReadTiffVirtualImage3D implements FramePlugin
{
	public ReadTiffVirtualImage3D() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open TIFF as Virtual Image", ImageFileFilters.TIFF);
        
        // Check the chosen file is valid
        if (file == null || !file.isFile())
        {
            return;
        }
        
		// Create a Tiff reader with the chosen file
		TiffImageReader reader;
		try 
		{
			reader = new TiffImageReader(file);
		}
		catch (Exception ex) 
		{
		    ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
			return;
		}
		
		Image image;
        try
        {
            image = reader.readVirtualImage3D();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
            return;
        }
		
		// add the image document to GUI
        ImageFrame.create(image, frame);
	}
}
