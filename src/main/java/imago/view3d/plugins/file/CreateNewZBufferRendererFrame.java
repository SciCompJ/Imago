/**
 * 
 */
package imago.view3d.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.view3d.ZBufferRendererFrame;

/**
 * 
 */
public class CreateNewZBufferRendererFrame implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public CreateNewZBufferRendererFrame()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        System.out.println("run CreateNewZBufferRendererFrame plugin");
        
        ImagoGui gui = frame.getGui();
        ZBufferRendererFrame newFrame = new ZBufferRendererFrame(gui);
        
        gui.updateFrameLocation(newFrame, frame);
        newFrame.setVisible(true);
    }

}
