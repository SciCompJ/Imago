/**
 * 
 */
package imago.image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Implements response to the action of choosing a tool for an
 * {@code ImageFrame}.
 */
public class ImageToolSelector implements ActionListener
{

    ImageFrame frame;
    ImageTool tool;

    public ImageToolSelector(ImageFrame frame, ImageTool tool)
    {
        this.frame = frame;
        this.tool = tool;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        System.out.println("Select tool: " + tool.getName());

        // get current frame
        ImageViewer viewer = this.frame.getImageViewer();
        viewer.setCurrentTool(tool);
    }

}
