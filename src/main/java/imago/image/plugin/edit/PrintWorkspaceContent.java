/**
 * 
 */
package imago.image.plugin.edit;

import imago.app.ObjectHandle;
import imago.app.Workspace;
import imago.gui.ImagoFrame;
import imago.gui.FramePlugin;

import java.util.Locale;

/**
 * @author dlegland
 *
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
        System.out.println("Workspace content:");

        for (ObjectHandle handle : ws.getHandles())
	    {
	        String className = handle.getObject().getClass().getSimpleName();
            System.out.println(String.format(Locale.ENGLISH,
                    "  %s (%s): \"%s\"", handle.getTag(), className, handle.getName()));
        }
	}
}
