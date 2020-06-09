/**
 * 
 */
package imago.plugin.plugin.crop;

import javax.swing.JOptionPane;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;

/**
 * @author dlegland
 *
 */
public class Crop3D_Initialize implements Plugin
{

	@Override
	public void run(ImagoFrame frame, String args)
	{
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;

        ImageViewer viewer = ((ImageFrame) frame).getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }
        
        // get handle to the document
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		
        if (Crop3D.hasCrop3dNodes(handle))
        {
			int dialogResult = JOptionPane.showConfirmDialog(frame.getWidget(),
					"This frame contains Crop3D objects. Initialize anyway?",
					"Crop3D Warning", JOptionPane.YES_NO_OPTION);
			if (dialogResult != JOptionPane.YES_OPTION)
			{
				return;
			}
        }
        
        Crop3D.initializeCrop3dNodes(handle);

        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        viewer.repaint();
	}
	
}
