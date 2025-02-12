/**
 * 
 */
package imago.plugin.image.edit;

import java.util.Collection;

import imago.app.GeometryHandle;
import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.app.ObjectHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.image.ImageViewer;
import imago.gui.image.PlanarImageViewer;
import net.sci.array.Array;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;

/**
 * Copy the current selection as a new item in the workspace.
 * 
 * @author David Legland
 *
 */
public class ImportSelectionFromWorkspace implements FramePlugin
{
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

		ImagoApp app = frame.getGui().getAppli();
		Collection<GeometryHandle> handles = GeometryHandle.getAll(app);
        ObjectHandle[] handleArray = handles.toArray(new ObjectHandle[]{});
        
        // retrieve handle names
        Collection<String> names = ObjectHandle.getNames(handles);
        String[] nameArray = names.toArray(new String[]{});
        String firstName = nameArray[0];


        // Creates the dialog
        GenericDialog gd = new GenericDialog(frame, "Import Selection");
        gd.addChoice("Geometry Name: ", nameArray, firstName);
        gd.showDialog();
        if (gd.wasCanceled()) 
        {
            return;
        }
        
        // parse dialog results
        int geometryIndex = gd.getNextChoiceIndex();
        GeometryHandle handle = (GeometryHandle) handleArray[geometryIndex];
        
        Geometry geom = handle.getGeometry();
        if (!(geom instanceof Geometry2D))
        {
            throw new RuntimeException("An instance of Geometry2D was expected");
        }
        
        ((PlanarImageViewer) viewer).setSelection((Geometry2D) geom);
        viewer.repaint();
	}
}
