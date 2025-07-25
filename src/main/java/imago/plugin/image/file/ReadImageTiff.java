/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.ProgressMonitor;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;


/**
 * Opens a dialog to read a TIFF Image file.
 * 
 * @author David Legland
 *
 */
public class ReadImageTiff implements FramePlugin, AlgoListener
{
    /**
     * A dialog to show reading progress.
     */
    private ProgressMonitor progressMonitor;
    
    /**
     * Default empty constructor.
     */
    public ReadImageTiff()
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

        // Configure a progress monitor to display reading progress
        progressMonitor = new ProgressMonitor(frame.getWidget(), "Reading a TIFF Image", "", 0, 100);
        progressMonitor.setMillisToDecideToPopup(10);
        progressMonitor.setMillisToPopup(100);
        progressMonitor.setProgress(0);

        reader.addAlgoListener(this);

        // Try to read the image from the file
        Image image;
        try
        {
            image = reader.readImage();
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
        ImageFrame.create(image, frame);
    }

    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        int progress = (int) Math.round(evt.getProgressRatio() * 100);
        progressMonitor.setProgress(progress);
    }

    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        progressMonitor.setNote(evt.getStatus());
    }
}
