/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.geom.Geometry;
import net.sci.geom.polygon2d.Polygon2D;
import net.sci.image.Image;

/**
 * Copy the current selection as a new item in the workspace.
 * 
 * @author David Legland
 *
 */
public class ImageCopySelectionToWorkspace implements FramePlugin
{
	public ImageCopySelectionToWorkspace()
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
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        
        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 2)
		{
		    throw new RuntimeException("Requires an image containing 2D Array");
		}

		
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry selection = piv.getSelection();
        if (!(selection instanceof Polygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        ImagoApp app = frame.getGui().getAppli();
        
        GenericDialog dlg = new GenericDialog(frame, "Add Selection To Workspace");
        dlg.addTextField("Name", "polygon", 20);
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        String name = dlg.getNextString();
        app.createHandle(selection, name, "poly");
	}
}
