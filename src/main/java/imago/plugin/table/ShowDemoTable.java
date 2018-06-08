/**
 * 
 */
package imago.plugin.table;

import java.io.File;
import java.io.IOException;

import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;


/**
 * @author David Legland
 *
 */
public class ShowDemoTable implements Plugin
{
	public ShowDemoTable() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
    public void run(ImagoFrame frame, String args)
    {
	    File file = new File("files/tables/fisherIris.txt");
	    Table table;
        try
        {
            table = new DelimitedTableReader().readTable(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
	    
        table.setName("fisherIris");
	    ImagoTableFrame tableFrame = new ImagoTableFrame(frame, table);
	    
	    // add the new frame to the GUI
		frame.getGui().addFrame(tableFrame); 
	}

}
