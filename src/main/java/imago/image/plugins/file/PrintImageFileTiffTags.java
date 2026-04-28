/**
 * 
 */
package imago.image.plugins.file;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import net.sci.image.io.TiffImageReader;
import net.sci.image.io.tiff.Entry;
import net.sci.image.io.tiff.ImageFileDirectory;
import net.sci.image.io.tiff.TiffTag;


/**
 * Opens a dialog to select a file in TIFF format, and displays the Tiff Tags
 * contained in it.
 * 
 * @see imago.image.plugins.edit.PrintImageTiffTags
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
		
		// retrieve first image file directory
		Collection<ImageFileDirectory> fileInfoList = reader.getImageFileDirectories();
	    System.out.println("Tiff Image File with " + fileInfoList.size() + " Image File Directories.");
        ImageFileDirectory info = fileInfoList.iterator().next();
        
	    Map<Integer, TiffTag> knownTags = TiffTag.getAllTags();
	    
	    // display tags on console
        for (Entry entry : info.entries())
        {
            TiffTag tag = knownTags.get(entry.code);
            String id = tag == null ? "" : " (" + tag.name + ")";
            String desc = String.format("Tag code: %5d %-30s", entry.code, id);
            System.out.print(desc + "\tType=" + entry.type + ", \tcount=" + entry.count);
            System.out.println(", content=" + entry.contentSummary());
//            System.out.println(desc + "\tType=" + entry.type + ", \tcount=" + entry.count + ", content=" + entry.contentSummary());
        }
	}
}
