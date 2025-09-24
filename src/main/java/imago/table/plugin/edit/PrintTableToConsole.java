/**
 * 
 */
package imago.table.plugin.edit;

import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugin.TableFramePlugin;

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
