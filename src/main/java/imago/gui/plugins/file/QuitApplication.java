/**
 * 
 */
package imago.gui.plugins.file;

import java.util.ArrayList;
import java.util.Collection;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.FramePlugin;

/**
 * Tries to close all open frames, and quits the application by calling the
 * "System.exit(0)" command.
 * 
 * @author dlegland
 *
 */
public class QuitApplication implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public QuitApplication()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame parentFrame, String args)
    {
        ImagoGui gui = parentFrame.getGui();
        System.out.print("Closing the Imago application...");

        Collection<ImagoFrame> frames = gui.getFrames();

        Collection<ImagoFrame> framesToClose = new ArrayList<ImagoFrame>(frames.size());
        framesToClose.addAll(frames);

        for (ImagoFrame frame : framesToClose)
        {
            frame.close();
        }

        gui.disposeEmptyFrame();

        System.out.println(" bye bye!");
        System.exit(0);
    }

}
