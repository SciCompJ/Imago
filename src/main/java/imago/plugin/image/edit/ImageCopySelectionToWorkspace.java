/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.app.Workspace;
import imago.gui.GenericDialog;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.polygon.Polygon2D;
import net.sci.image.Image;

/**
 * Copy the current selection as a new item in the workspace.
 * 
 * @author David Legland
 *
 */
public class ImageCopySelectionToWorkspace implements Plugin
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
		System.out.println("selection to workspace");

		// Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        
        ImageViewer viewer = iframe.getImageView();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

		// get current image data
		ImageHandle doc = ((ImageFrame) frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 2)
		{
		    throw new RuntimeException("Requires an image containing 2D Array");
		}

		
        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry2D selection = piv.getSelection();
        if (!(selection instanceof Polygon2D))
        {
            System.out.println("requires selection to be a simple polygon");
            return;
        }
        
        Workspace ws = frame.getGui().getAppli().getWorkspace();
        
        GenericDialog dlg = new GenericDialog(frame, "Add Selection To Workspace");
        dlg.addTextField("Name", "polygon", 20);
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        String name = dlg.getNextString();
        ws.createHandle(selection, name, "poly");
	}
}
