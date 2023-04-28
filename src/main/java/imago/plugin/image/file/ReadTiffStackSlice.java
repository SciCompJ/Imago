/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;


/**
 * Reads a chosen slice from a TIFF stack.
 * 
 * @author David Legland
 *
 */
public class ReadTiffStackSlice implements FramePlugin
{
	public ReadTiffStackSlice() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open Tiff Slice", ImageFileFilters.TIFF);
        
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

		// Opens a dialog to choose the slice
		int nSlices = reader.getImageFileDirectories().size();
		GenericDialog dlg = new GenericDialog(frame, "Slice Index");
		String label = String.format("Slice index (0-%d)", nSlices-1);
		dlg.addNumericField(label, nSlices/2, 0, "Index of the slice, 0-based");
		dlg.showDialog();
		
		// index of selected slice
		int sliceIndex = (int) dlg.getNextNumber();
		
		// Try to read the image from the file
		Image image;
		try
		{
			image = reader.readImage(sliceIndex);
		} 
		catch (IOException ex)
		{
			ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
			return;
		} 
		catch (Exception ex)
		{
            ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
            return;
		}
		
		// add the image document to GUI
		frame.createImageFrame(image);
	}
}
