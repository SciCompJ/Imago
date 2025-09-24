/**
 * 
 */
package imago.image.plugin.file;

import java.io.File;
import java.util.Collection;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.image.io.TiffImageReader;
import net.sci.image.io.tiff.ImageFileDirectory;
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
        // opens a dialog to choose the file
        File file = frame.getGui().chooseFileToOpen(frame, "Open Tiff Image", ImageFileFilters.TIFF);
        
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
		    ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
			return;
		}
		
		Collection<ImageFileDirectory> fileInfoList = reader.getImageFileDirectories();
	    System.out.println("Tiff Image File with " + fileInfoList.size() + " Image File Directories.");
        
	    ImageFileDirectory info = fileInfoList.iterator().next();
	    // display tags on console
        for (TiffTag tag : info.entries())
        {
            String id = tag.name == null ? "" : " (" + tag.name + ")";
            String desc = String.format("Tag code: %5d %-30s", tag.code, id);
            System.out.println(desc + "\tType=" + tag.type + ", \tcount=" + tag.count + ", content=" + tag.content);
        }
	}
}
