/**
 * 
 */
package imago.gui.action.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableReader;


/**
 * @author David Legland
 *
 */
public class ShowDemoTable extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String fileName;
	
	public ShowDemoTable(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {

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
	    
	    ImagoTableFrame frame = new ImagoTableFrame(this.frame, table);
	    
	    // add the new frame to the GUI
		this.gui.addFrame(frame); 
	}

}
