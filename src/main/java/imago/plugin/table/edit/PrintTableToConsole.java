/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TableFramePlugin;

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
