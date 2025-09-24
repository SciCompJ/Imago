/**
 * 
 */
package imago.image.plugin.file;

import java.io.File;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import net.sci.image.Image;
import net.sci.image.io.TiffImageWriter;
import net.sci.image.io.tiff.BaselineTags;


/**
 * Save an image using the TIFF file format.
 * 
 * @author David Legland
 *
 */
public class SaveImageAsTiff implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public SaveImageAsTiff()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame)) return;
        ImageFrame iframe = (ImageFrame) frame;

        ImageViewer viewer = iframe.getImageViewer();
        Image image = viewer.getImage();

        // create file dialog using last save path
        File file = frame.getGui().chooseFileToSave(frame, 
                "Save TIFF Image",
                image.getName() + ".tif", 
                ImageFileFilters.TIFF);

        // Check the selected file is valid
        if (file == null)
        {
            return;
        }
        if (!file.getName().endsWith(".tif"))
        {
            file = new File(file.getParent(), file.getName() + ".tif");
        }

        // Create a writer with specified file
        TiffImageWriter writer = new TiffImageWriter(file);
        writer.addCustomTag(new BaselineTags.Software().setValue("Imago ")); // adds a space to make sure ImageJ read correctly
        
        long t0 = System.nanoTime();
        writer.addAlgoListener(iframe);
        try
        {
            writer.writeImage(image);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "Tiff Image Export Error");
            return;
        }
        long t1 = System.nanoTime();
        double dt = (t1 - t0) / 1_000_000.0;

        iframe.getStatusBar().setProgressBarPercent(0);
        iframe.showElapsedTime("Save As Tiff", dt, image);
	}
}
