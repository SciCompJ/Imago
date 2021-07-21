/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.GeometryHandle;
import imago.app.ImageHandle;
import imago.app.ObjectHandle;
import imago.app.Workspace;
import imago.gui.GenericDialog;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import imago.gui.viewer.PlanarImageViewer;

import java.util.Collection;

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
	public ImportSelectionFromWorkspace()
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
		System.out.println("import selection from workspace");

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
		ImageHandle doc = ((ImageFrame) frame).getImageHandle();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (array.dimensionality() != 2)
		{
		    throw new RuntimeException("Requires an image containing 2D Array");
		}

		
        Workspace ws = frame.getGui().getAppli().getWorkspace();
        Collection<ObjectHandle> handles = ws.getHandles(Geometry.class);
        Collection<String> names = ObjectHandle.getNames(handles);
        ObjectHandle[] handleArray = handles.toArray(new ObjectHandle[]{});
        
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
