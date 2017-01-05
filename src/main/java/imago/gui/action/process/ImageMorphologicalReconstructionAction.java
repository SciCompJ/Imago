/**
 * 
 */
package imago.gui.action.process;

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
import net.sci.image.Image;
import net.sci.image.morphology.MorphologicalReconstruction;

/**
 * Applies morphological reconstruction of a marker image constrained by a mask image.
 * 
 * Works for both 2D or 3D pairs of images.
 * 
 * @author David Legland
 *
 */
public class ImageMorphologicalReconstructionAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageMorphologicalReconstructionAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		System.out.println("morphological reconstruction");

		// TODO: try to make the following piece of code more generic
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
				
		// Creates the dialog
		GenericDialog gd = new GenericDialog(this.frame, "Morpho. Rec.");
		gd.addChoice("Marker: ", imageNameArray, firstImageName);
		gd.addChoice("Mask: ", imageNameArray, firstImageName);
		gd.showDialog();
		
		if (gd.getOutput() == GenericDialog.Output.CANCEL) 
		{
			return;
		}
		
		// parse dialog results
		Image markerImage = nameToDocMap.get(gd.getNextChoice()).getImage();
		Image maskImage = nameToDocMap.get(gd.getNextChoice()).getImage();

		Array<?> marker = markerImage.getData();
		Array<?> mask = maskImage.getData();
		if (marker.dimensionality() != mask.dimensionality())
		{
			// TODO: display error dialog
			System.err.println("Both arrays must have same dimensionality");
			return;
		}
		
		Image resultImage = MorphologicalReconstruction.reconstructByDilation(markerImage, maskImage);
		resultImage.setName(markerImage.getName() + "-morphoRec");
		
		// add the image document to GUI
		this.gui.addNewDocument(resultImage);
	}
}
