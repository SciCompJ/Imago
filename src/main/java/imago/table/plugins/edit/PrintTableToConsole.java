/**
 * 
 */
package imago.table.plugins.edit;

import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugins.TableFramePlugin;

/**
 * @author dlegland
 *
 */
public class PrintTableToConsole implements TableFramePlugin
{

    /**
     * 
     */
    public PrintTableToConsole()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        ((TableFrame) frame).getTable().print();
    }

}
