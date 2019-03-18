/**
 * 
 */
package imago.plugin.table.edit;

import java.util.ArrayList;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class MergeTablesByColumns implements Plugin
{
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // collect the names of frames containing tables
        ArrayList<String> tableNames = findTableNameList(frame.getGui());
        
        // do not continue if no UInt8 image is loaded
        if (tableNames.size() == 0)
        {
            return;
        }
        
        // Convert image name list to String array
        String[] tableNameArray = tableNames.toArray(new String[]{});
        String firstImageName = tableNameArray[0];
                
        // Create Dialog for choosing image names
        GenericDialog dialog = new GenericDialog(frame, "Merge tables");
        dialog.addChoice("Red table:", tableNameArray, firstImageName);
        dialog.addChoice("Green table:", tableNameArray, firstImageName);
        dialog.addChoice("Blue table:", tableNameArray, firstImageName);

        // Display dialog and wait for OK or Cancel
        dialog.showDialog();
        if (dialog.wasCanceled())
        {
            return;
        }
        
    }
    
    private ArrayList<String> findTableNameList(ImagoGui gui)
    {
        ArrayList<String> tableNames = new ArrayList<>();
        gui.getFrames().stream()
            .filter(frame -> frame instanceof ImagoTableFrame)
            .forEach(frame -> tableNames.add(((ImagoTableFrame) frame).getTable().getName()));
        return tableNames;
    }
}
