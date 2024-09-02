/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import net.sci.image.Image;
import net.sci.image.io.ImageIOImageWriter;


/**
 * Save an image into a common image file format managed by the ImageIO package.
 * 
 * @see net.sci.image.io.ImageIOImageWriter;
 * @see javax.imageio.ImageIO; 
 *
 * @author David Legland
 */
public class SaveImageIO implements FramePlugin
{
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
        
        ImageViewer viewer = iframe.getImageViewer();
        Image image = viewer.getImage();
        
        // create file dialog using last save path
        File file = frame.getGui().chooseFileToSave(frame, "Save Image", image.getName() + ".png",
                ImageFileFilters.IMAGE_IO, ImageFileFilters.PNG, ImageFileFilters.JPEG, ImageFileFilters.GIF,
                ImageFileFilters.BMP);
		
		// Create a writer with specified file
		ImageIOImageWriter writer = new ImageIOImageWriter(file);
		long t0 = System.nanoTime();
		try
		{
			writer.writeImage(image);
		}
		catch(Exception ex)
		{
		    System.err.println(ex);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "ImageIO Save Error");
			return;
		}
		long t1 = System.nanoTime();
		double dt = (t1 - t0) / 1_000_000.0;

        iframe.getStatusBar().setProgressBarPercent(0);
        iframe.showElapsedTime("Save Image", dt, image);
	}
}
