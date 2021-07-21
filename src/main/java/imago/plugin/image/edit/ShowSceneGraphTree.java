/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.app.scene.Node;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.SceneGraphDisplayFrame;

/**
 * Opens a new frame to display scene graph of current Image Handle.
 * 
 * @author dlegland
 *
 */
public class ShowSceneGraphTree implements FramePlugin
{
	public ShowSceneGraphTree() 
	{
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("display image scene graph:");
		
		// get current frame
		ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		Node root = handle.getRootNode(); 
		
		if (root == null)
		{
		    frame.showErrorDialog("Imago Error", "No Scene graph stored within image");
			return;
		}
		
		String frameTitle = "Scene Graph of " + handle.getName();
		SceneGraphDisplayFrame sgFrame = new SceneGraphDisplayFrame(frame, frameTitle, root);
		sgFrame.setVisible(true);
    }
}
