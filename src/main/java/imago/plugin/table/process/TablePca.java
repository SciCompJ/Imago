package imago.plugin.table.process;

import net.sci.table.Table;
import net.sci.table.transform.PCA;
import imago.gui.ImagoFrame;
import imago.gui.ImagoTableFrame;
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
        Table table = ((ImagoTableFrame) frame).getTable();

        PCA pca = new PCA().fit(table);
        
        frame.getGui().addFrame(new ImagoTableFrame(frame, pca.eigenValues())); 
        
        frame.getGui().addFrame(new ImagoTableFrame(frame, pca.loadings())); 

        frame.getGui().addFrame(new ImagoTableFrame(frame, pca.scores())); 
    }

}
