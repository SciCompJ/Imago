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
		long t0 = System.nanoTime();
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
		long t1 = System.nanoTime();
		double dt = (t1 - t0) / 1_000_000.0;

        iframe.getStatusBar().setProgressBarPercent(0);
        iframe.showElapsedTime("Save To MHD", dt, image);
	}
}
