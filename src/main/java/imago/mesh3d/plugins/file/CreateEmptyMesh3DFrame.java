/**
 * 
 */
package imago.mesh3d.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.mesh3d.Mesh3DViewer;

/**
 * 
 */
public class CreateEmptyMesh3DFrame implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public CreateEmptyMesh3DFrame()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        Mesh3DViewer newFrame = Mesh3DViewer.create(null, frame);
        newFrame.setVisible(true);
    }
}
