package imago.plugin.table.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
import imago.plugin.table.TablePlugin;
import net.sci.table.Table;
import net.sci.table.cluster.KMeans;

/**
 * Compute KMeans on a numeric data table.
 * 
 * @author dlegland
 *
 */
public class TableKMeans implements TablePlugin
{

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((ImagoTableFrame) frame).getTable();

        // TODO: check table is all numeric
        GenericDialog dlg = new GenericDialog("KMeans");
        dlg.addNumericField("Class Number: ", 3, 0);
        
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        int nClasses = (int) dlg.getNextNumber();
        
        KMeans km = new KMeans(nClasses);
        km.fit(table);

        Table classes = km.predict(table);
        
        frame.getGui().addFrame(new ImagoTableFrame(frame, classes)); 
    }

}
