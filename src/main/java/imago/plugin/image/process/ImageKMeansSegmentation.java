/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.Image.Type;
import net.sci.image.process.segment.KMeansSegmentation;


/**
 * Opens a dialog to choose a number of classes, and creates a kmeans classifier.
 * The resulting image is a label image.
 *  
 * @author David Legland
 *
 */
public class ImageKMeansSegmentation implements FramePlugin
{
	public ImageKMeansSegmentation() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args) 
	{
		System.out.println("KMeans");
		
		// get current frame
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image = doc.getImage();

		// Creates generic dialog
        GenericDialog gd = new GenericDialog(frame, "KMeans");
        
        gd.addNumericField("Class number", 3, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        int nc = (int) gd.getNextNumber();
        
        // compute k-means
        KMeansSegmentation algo = new KMeansSegmentation(nc);
        Array<?> labelMap = algo.process(image.getData());
        
        // create result image
		Image resultImage = new Image(labelMap, Type.LABEL);
		resultImage.setCalibration(image.getCalibration());
				
		// add the image document to GUI
		frame.getGui().createImageFrame(resultImage); 
	}

}