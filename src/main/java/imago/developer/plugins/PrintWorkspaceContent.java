/**
 * 
 */
package imago.developer.plugins;

import imago.app.ObjectHandle;
import imago.app.Workspace;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;

/**
 * Displays the content of the workspace on the console.
 * 
 * @author dlegland
 */
public class PrintWorkspaceContent implements FramePlugin
{
	/**
	 */
	public PrintWorkspaceContent()
	{
	}

	@Override
    public void run(ImagoFrame frame, String args)
	{
	    Workspace ws = frame.getGui().getAppli().getWorkspace();
	    String pattern = "  %-8s %-10s %s\n";
	    
        System.out.println("Workspace content:");
        System.out.printf(pattern, "Tag", "Type", "Name");
        
        for (ObjectHandle handle : ws.getHandles())
	    {
	        String className = handle.getItemClassName();
	        String name = "\"" + handle.getName() + "\"";
            System.out.printf(pattern, handle.getTag(), className, name);
        }
	}
}
