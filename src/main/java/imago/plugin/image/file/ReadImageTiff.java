/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.ProgressMonitor;

import imago.app.ImageHandle;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.util.imagej.ImagejRoi;
import imago.util.imagej.ImagejRoiDecoder;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;
import net.sci.image.io.tiff.ImagejMetadata;


/**
 * Opens a dialog to read a TIFF Image file.
 * 
 * @author David Legland
 *
 */
public class ReadImageTiff implements FramePlugin, AlgoListener
{
    /**
     * Updates the specified viewer to import and display specific meta data
     * stored within the file.
     * 
     * @param viewer
     *            the ImageViewer to update.
     */
    public static final void importImageMetaData(ImageViewer viewer)
    {
        // retrieve data
        ImageHandle handle = viewer.getImageHandle();
        Image image = handle.getImage();
        
        if (image.metadata.containsKey("imagej"))
        {
            ImagejMetadata metadata = (ImagejMetadata) image.metadata.get("imagej");
            
            if (metadata.overlayData != null)
            {
                // convert Image overlays as Shape instances within the ImageHandle
                int nOverlay = metadata.overlayData.length;
                for (int i = 0; i < nOverlay; i++)
                {
                    ImagejRoi roi = ImagejRoiDecoder.decode(metadata.overlayData[i]);
                    handle.addShape(roi.asShape());
                }
            }
            
            if (metadata.roiData != null)
            {
                // Convert the current ROI as a Selection for the viewer
                ImagejRoi roi = ImagejRoiDecoder.decode(metadata.roiData);
                Shape shape = roi.asShape();
                viewer.setSelection(shape.getGeometry());
                viewer.refreshDisplay();
            }
        }
    }
    
    /**
     * A dialog to show reading progress.
     */
    private ProgressMonitor progressMonitor;
    
	public ReadImageTiff() 
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
		ImageFrame newFrame = ImageFrame.create(image, frame);
        
		importImageMetaData(newFrame.getImageViewer());
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
