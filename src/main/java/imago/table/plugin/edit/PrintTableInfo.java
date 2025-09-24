/**
 * 
 */
package imago.table.plugin.edit;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Locale;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import imago.table.TableFrame;
import net.sci.table.Column;
import net.sci.table.Table;

/**
 * Display some info on the table: table name, and short summary of the content of
 * each column.
 * 
 * @see imago.plugin.image.edit.PrintImageInfos;
 */
public class PrintTableInfo implements FramePlugin
{

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        Table table = ((TableFrame) frame).getTable();
        ArrayList<String> textLines = new ArrayList<String>();

        textLines.add("Table name: " + table.getName());
        
        // number of digits for display of column index
        int nDigits = table.columnCount() > 9 ? 2 : 1;
        
        // Use different processing depending on column names exist or not
        String[] colNames = table.getColumnNames();
        if (colNames != null)
        {
            // create format string
            int nChars = Math.min(maxLength(colNames), 15);
            String format = " [%" + nDigits + "d] %-" + nChars + "s: %s";
            
            // iterate over columns
            for (int c = 0; c < table.columnCount(); c++)
            {
                Column col = table.column(c);
                textLines.add(String.format(Locale.ENGLISH, format, c, col.getName(), col.contentSummary()));
            }
        }
        else
        {
            String format = " [%" + nDigits + "d]: %s";
            // iterate over columns
            for (int c = 0; c < table.columnCount(); c++)
            {
                textLines.add(String.format(Locale.ENGLISH, format, c, table.column(c).contentSummary()));
            }
        }
        
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Table Summary", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
    }
    
    private static final int maxLength(String[] strings)
    {
        int nChars = 0;
        for (String str : strings)
        {
            nChars = Math.max(nChars, str.length());
        }
        return nChars;
    }
}
