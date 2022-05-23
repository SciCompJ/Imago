/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
        // create new file dialog
        JFileChooser openWindow = frame.getGui().createOpenFileDialog("Open Tiff Slice");
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
