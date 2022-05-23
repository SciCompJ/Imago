/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
        // create new file dialog
        JFileChooser openWindow = frame.getGui().createOpenFileDialog("Open TIFF as Virtual Image");
		openWindow.setFileFilter(new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff"));


		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(frame.getWidget());
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
