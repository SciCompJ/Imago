/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;

import imago.gui.FramePlugin;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import net.sci.image.Image;
import net.sci.image.io.MetaImageWriter;


/**
 * Save an image to MetaImage file format.
 * 
 * @author David Legland
 *
 */
public class SaveImageMetaImage implements FramePlugin
{
	public SaveImageMetaImage() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        ImageViewer viewer = iframe.getImageView();
        Image image = viewer.getImage();
        
        // create file dialog using last save path
        File file = frame.getGui().chooseFileToSave(frame, "Save As MetaImage", image.getName() + ".mhd", CommonImageFileFilters.META_IMAGE);

		// Check the selected file is valid
		if (file == null)
		{
		    return;
		}
		if (!file.getName().endsWith(".mhd"))
		{
			file = new File(file.getParent(), file.getName() + ".mhd");
		}
		
		// Create a writer with specified file
		MetaImageWriter writer = new MetaImageWriter(file);
		writer.addAlgoListener(iframe);
		try
		{
			writer.writeImage(image);
		}
		catch(Exception ex)
		{
		    System.err.println(ex);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "MHD Image Export Error");
			return;
		}
		
		iframe.getStatusBar().setProgressBarPercent(0);
		System.out.println("save done");
	}
}
