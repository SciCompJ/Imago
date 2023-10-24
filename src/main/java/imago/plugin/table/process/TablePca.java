package imago.plugin.table.process;

import net.sci.table.Table;
import net.sci.table.transform.PCA;
import imago.gui.ImagoFrame;
import imago.gui.table.TableFrame;
import imago.plugin.table.TablePlugin;

/**
 * Compute PCA on a numeric data table.
 * 
 * @author dlegland
 *
 */
public class TablePca implements TablePlugin
{

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();

        PCA pca = new PCA().fit(table);
        
        // add new frames to the GUI
        TableFrame.create(pca.eigenValues(), frame);
        TableFrame.create(pca.loadings(), frame);
        TableFrame.create(pca.scores(), frame);
    }
}
