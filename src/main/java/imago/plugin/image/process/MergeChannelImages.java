/**
 * 
 */
package imago.plugin.image.process;

import java.util.ArrayList;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class MergeChannelImages implements Plugin
{
	public MergeChannelImages()
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
		// get pointer to app for finding images 
		ImagoApp app = frame.getGui().getAppli();
		
		// collect the names of documents containing UInt8 images
		ArrayList<String> imageNames = findUInt8ArrayNameList(app);
		
		// do not continue if no UInt8 image is loaded
		if (imageNames.size() == 0)
		{
			return;
		}
		
		// Convert image name list to String array
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		// Create Dialog for choosing image names
		GenericDialog dialog = new GenericDialog(frame, "Merge Channels");
		dialog.addChoice("Red channel:", imageNameArray, firstImageName);
		dialog.addChoice("Green channel:", imageNameArray, firstImageName);
		dialog.addChoice("Blue channel:", imageNameArray, firstImageName);

		// Display dialog and wait for OK or Cancel
		dialog.showDialog();
		if (dialog.wasCanceled())
		{
			return;
		}
		
		// Retrieve images from document names
		Image redChannelImage 	= app.getDocumentFromName(dialog.getNextChoice()).getImage();
		Image greenChannelImage = app.getDocumentFromName(dialog.getNextChoice()).getImage();
		Image blueChannelImage 	= app.getDocumentFromName(dialog.getNextChoice()).getImage();
		
		// extract arrays containing image data
		UInt8Array redChannel 	= (UInt8Array) redChannelImage.getData();
		UInt8Array greenChannel = (UInt8Array) greenChannelImage.getData();
		UInt8Array blueChannel 	= (UInt8Array) blueChannelImage.getData();
		
		// concatenate the three channels to create an RGB8 array
		RGB8Array rgbArray = RGB8Array.mergeChannels(redChannel, greenChannel, blueChannel);
		
		// create the image corresponding to channels concatenation
		Image rgbImage = new Image(rgbArray, redChannelImage);
		rgbImage.setName(redChannelImage.getName() + "-mergeChannels");

		// add the image document to GUI
		frame.getGui().addNewDocument(rgbImage);
	}
	
	private ArrayList<String> findUInt8ArrayNameList(ImagoApp app)
	{
	    ArrayList<String> imageNames = new ArrayList<>();
        app.getDocuments().stream()
            .filter(doc -> {
                Image img = doc.getImage();
                if (img == null) return false;
                return img.getData() instanceof UInt8Array;
            })
            .forEach(doc -> imageNames.add(doc.getName()));
        return imageNames;
	}
}
