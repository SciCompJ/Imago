/**
 * 
 */
package imago.plugin.table.edit;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Locale;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import imago.gui.table.TableFrame;
import net.sci.table.Column;
import net.sci.table.Table;

/**
 * Display summary of the table: table name, and short summary of the content of
 * each column.
 * 
 * @see imago.plugin.image.edit.PrintImageInfos;
 */
public class PrintTableSummary implements FramePlugin
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
            int nChars = 0;
            for (String name : table.getColumnNames())
            {
                nChars = Math.max(nChars, name.length());
            }
            nChars = Math.min(nChars, 15);
            
            // create format string
            String format = " [%" + nDigits + "d] %-" + nChars + "s: %s";
            
            // iterate over columns
            int c = 0;
            for (Column col : table.columns())
            {
                textLines.add(String.format(Locale.ENGLISH, format, c++, col.getName(), col.contentSummary()));
            }
        }
        else
        {
            String format = " [%" + nDigits + "d]: %s";
            // iterate over columns
            int c = 0;
            for (Column col : table.columns())
            {
                textLines.add(String.format(Locale.ENGLISH, format, c++, col.contentSummary()));
            }
        }
        
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "Table Summary", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
    }

}
