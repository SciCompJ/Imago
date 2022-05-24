/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
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
        File file = frame.getGui().chooseFileToOpen(frame, "Open TIFF as Virtual Image", CommonImageFileFilters.TIFF);
        
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
		    System.err.println(ex);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
			return;
		}
		
//		TiffVirtualUInt8Array3D array3d = new TiffVirtualUInt8Array3D(path, reader.getImageFileDirectories());
		        
		
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
		frame.createImageFrame(image);
	}
}
