/**
 * 
 */
package imago.image.plugins.edit;

import java.util.Collection;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageHandle;
import imago.imagepair.ImagePairFrame;
import net.sci.image.Image;

/**
 * Chooses two images, and opens a new frame to display overlay.
 * 
 * @author David Legland
 *
 */
public class DisplayImagePair implements FramePlugin
{
	@Override
    public void run(ImagoFrame frame, String args)
    {
        ImagoApp app = frame.getGui().getAppli();
        Collection<String> imageNames = ImageHandle.getAllNames(app);

        // Case of no open document with image
        if (imageNames.size() == 0)
        { 
            return; 
        }

        String[] imageNameArray = imageNames.toArray(new String[] {});
        String firstImageName = imageNameArray[0];
        String secondImageName = imageNameArray[Math.min(1, imageNameArray.length - 1)];

        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Display Image Pair");
        gd.addChoice("Reference Image: ", imageNameArray, firstImageName);
        gd.addChoice("Other image: ", imageNameArray, secondImageName);
        gd.showDialog();

        if (gd.wasCanceled())
        { 
            return; 
        }

        // parse dialog results
        Image refImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();
        Image otherImage = ImageHandle.findFromName(app, gd.getNextChoice()).getImage();

        // check validity of input images
        if (refImage.getDimension() != 2 || otherImage.getDimension() != 2)
        {
            frame.showErrorDialog("Both images must be planar images", "Dimensionality Error");
            return;
        }

        // create new frame for display
        ImagePairFrame.create(refImage, otherImage, frame);
	}
}
