/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImageHandle;
import imago.app.scene.Node;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class PrintImageSceneGraph implements Plugin
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
		System.out.println("print image scene graph:");
		
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
