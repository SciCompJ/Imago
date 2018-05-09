/**
 * 
 */
package imago.plugin.edit;

import imago.gui.ImageViewer;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTool;
import imago.gui.Plugin;

/**
 * Changes the current tool of the current ImagoFrame.
 * 
 * @author David Legland
 *
 */
public class ChangeCurrentTool implements Plugin
{
    ImagoTool tool;
    
    public ChangeCurrentTool(ImagoTool tool)
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
        ImageViewer viewer = ((ImagoDocViewer) frame).getImageView();
        viewer.setCurrentTool(tool);
    }
    
}
