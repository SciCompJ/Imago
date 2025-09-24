/**
 * 
 */
package imago.image.plugin.edit;

import imago.app.ImageHandle;
import imago.app.scene.Node;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.gui.FramePlugin;

/**
 * Display the content of the scene graph on console.
 * 
 * @author dlegland
 *
 */
public class PrintImageSceneGraph implements FramePlugin
{
	public PrintImageSceneGraph() 
	{
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current frame
		ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		Node root = handle.getRootNode(); 
		
		if (root == null)
		{
			System.out.println("No Scene graph stored within image");
			return;
		}
		
		System.out.println("Image Scene graph:");
		root.printTree(System.out, 0);
    }
}
