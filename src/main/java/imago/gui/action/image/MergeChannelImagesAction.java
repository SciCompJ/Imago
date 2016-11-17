/**
 * 
 */
package imago.gui.action.image;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
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
	public void actionPerformed(ActionEvent arg0)
	{
		Collection<ImagoDocViewer> viewers = this.gui.getDocumentViewers();
		HashMap<String, ImagoDoc> nameToDocMap = new HashMap<String, ImagoDoc>();
		
		ArrayList<String> imageNames = new ArrayList<String>(viewers.size());
		for (ImagoDocViewer viewer : viewers)
		{
			ImagoDoc doc = viewer.getDocument(); 
			if (doc == null) continue;
			Image image = doc.getImage();
			if (image == null) continue;
			
			// restrict the image selection to instance of UInt8 arrays
			Array<?> data = image.getData();
			if (data instanceof UInt8Array)
			{
				imageNames.add(doc.getName());
				nameToDocMap.put(doc.getName(), doc);
			}
		}
		
		if (imageNames.size() == 0)
		{
			return;
		}
		
		String[] imageNameArray = imageNames.toArray(new String[]{});
		String firstImageName = imageNameArray[0];
				
		GenericDialog dialog = new GenericDialog("Merge Channels");
		dialog.addChoice("Red channel:", imageNameArray, firstImageName);
		dialog.addChoice("Green channel:", imageNameArray, firstImageName);
		dialog.addChoice("Blue channel:", imageNameArray, firstImageName);

		dialog.showDialog();
		if (dialog.wasCanceled())
		{
			return;
		}
		
		Image redChannelImage 	= nameToDocMap.get(dialog.getNextChoice()).getImage();
		Image greenChannelImage = nameToDocMap.get(dialog.getNextChoice()).getImage();
		Image blueChannelImage 	= nameToDocMap.get(dialog.getNextChoice()).getImage();
		
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
