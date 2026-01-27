/**
 * 
 */
package imago.image.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.ImageViewer;
import imago.image.viewers.PlanarImageViewer;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.util.StringUtils;
import net.sci.array.Array;
import net.sci.geom.Geometry;
import net.sci.image.Image;

/**
 * Copy the current selection as a new item in the workspace.
 * 
 * @author David Legland
 *
 */
public class ImageCopySelectionToWorkspace implements FramePlugin
{
    String lastName = null;
    
    /**
     * Default empty constructor.
     */
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
        if (!(frame instanceof ImageFrame)) return;
        ImageFrame iframe = (ImageFrame) frame;

        ImageViewer viewer = iframe.getImageViewer();
        if (!(viewer instanceof PlanarImageViewer))
        {
            System.out.println("requires an instance of planar image viewer");
            return;
        }

        // get current image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        Array<?> array = image.getData();

        if (array.dimensionality() != 2)
        {
            throw new RuntimeException("Requires an image containing 2D Array");
        }

        PlanarImageViewer piv = (PlanarImageViewer) viewer;
        Geometry selection = piv.getSelection();
        
        ImagoApp app = frame.getGui().getAppli();
        
        GenericDialog dlg = new GenericDialog(frame, "Add Selection To Workspace");
        String baseName = "roi-01";
        if (this.lastName != null)
        {
            baseName = StringUtils.addNumericIncrement(this.lastName);
        }
        dlg.addTextField("Name", baseName, 20);
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // retrieve name associated to handle
        String name = dlg.getNextString();
        this.lastName = name;
        
        // create handle, using tag based on geometry
        String baseTag = GeometryHandle.createTag(selection);
        app.createHandle(selection, name, baseTag);
        
        // optionally display shape manager
        if (ShapeManager.hasInstance(frame.getGui()))
        {
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
        }
	}
}
