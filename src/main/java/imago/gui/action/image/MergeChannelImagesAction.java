/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import net.sci.array.data.UInt8Array;
import net.sci.array.data.color.RGB8Array;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class MergeChannelImagesAction extends ImagoAction
{

	public MergeChannelImagesAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		// get pointer to app for finding images 
		ImagoApp app = this.gui.getAppli();
		
		// collect the names of documents containing UInt8 images
		ArrayList<String> imageNames = new ArrayList<>();
		app.getDocuments().stream()
			.filter(doc -> {
				Image img = doc.getImage();
				if (img == null) return false;
				return img.getData() instanceof UInt8Array;
			})
			.forEach(doc -> imageNames.add(doc.getName()));

		// do not continue if no UInt8 image is loaded
		if (imageNames.size() == 0)
		{
			return;
		}
		
		// Convert image name list to String array
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		// Create Dialog for choosing image names
		GenericDialog dialog = new GenericDialog("Merge Channels");
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
		this.gui.addNewDocument(rgbImage);
	}
}
