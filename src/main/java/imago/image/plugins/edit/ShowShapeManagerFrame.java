/**
 * 
 */
package imago.image.plugins.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.shapemanager.ShapeManager;

/**
 * Shows the current ShapeManager. 
 * 
 * Can be replaced by a single line:
 * {@snippet :
 *  addPlugin(menu, 
 *      (frame, options) -> {ShapeManager.getInstance(frame.getGui()).setVisible(true);},
 *      "Show Shape Manager");
 * }
 * 
 * @see ShapeManager
 *
 */
public class ShowShapeManagerFrame implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ShowShapeManagerFrame()
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
        ShapeManager manager = ShapeManager.getInstance(frame.getGui());
        manager.setVisible(true);
    }
}
