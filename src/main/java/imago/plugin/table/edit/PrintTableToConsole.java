/**
 * 
 */
package imago.plugin.table.edit;

import imago.gui.ImagoFrame;
import imago.gui.TableFrame;
import imago.plugin.table.TablePlugin;

/**
 * @author dlegland
 *
 */
public class PrintTableToConsole implements TablePlugin
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
