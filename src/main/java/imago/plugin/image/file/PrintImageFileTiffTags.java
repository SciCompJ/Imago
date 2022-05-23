/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.image.io.TiffImageReader;
import net.sci.image.io.tiff.TiffFileInfo;
import net.sci.image.io.tiff.TiffTag;


/**
 * Opens a dialog to select a file in TIFF format, and displays the Tiff Tags
 * contained in it.
 * 
 * @author David Legland
 *
 */
public class PrintImageFileTiffTags implements FramePlugin
{
	public PrintImageFileTiffTags() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // create new file dialog
        JFileChooser openWindow = frame.getGui().createOpenFileDialog("Open Tiff Image");
		openWindow.setFileFilter(new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff"));

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		// Check the chosen file is valid
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
		
		Collection<TiffFileInfo> fileInfoList = reader.getImageFileDirectories();
	    System.out.println("Tiff Image File with " + fileInfoList.size() + " Image File Directories.");
        
	    TiffFileInfo info = fileInfoList.iterator().next();
	      // display tags on console
        for (TiffTag tag : info.tags.values())
        {
            String id = tag.name == null ? "" : " (" + tag.name + ")";
            String desc = String.format("Tag code: %5d %-30s", tag.code, id);
            System.out.println(desc + "\tType=" + tag.type + ", \tcount=" + tag.count + ", content=" + tag.content);
        }
	}
}
