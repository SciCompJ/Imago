/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import imago.gui.FramePlugin;

/**
 * Changes the current tool of the current ImagoFrame.
 * 
 * @author David Legland
 *
 */
public class ChangeCurrentTool implements FramePlugin
{
    ImageTool tool;
    
    public ChangeCurrentTool(ImageTool tool)
    {
        this.tool = tool;
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
        System.out.println("Select tool: " + tool.getName());
        
        // get current frame
        ImageViewer viewer = ((ImageFrame) frame).getImageViewer();
        viewer.setCurrentTool(tool);
    }
    
}
