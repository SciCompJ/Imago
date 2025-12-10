package imago.table.plugins.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.table.TableFrame;
import imago.table.plugins.TableFramePlugin;
import net.sci.table.Table;
import net.sci.table.cluster.KMeans;

/**
 * Compute KMeans on a numeric data table.
 * 
 * @author dlegland
 *
 */
public class TableKMeans implements TableFramePlugin
{

    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get table references by the frame
        Table table = ((TableFrame) frame).getTable();

        // TODO: check table is all numeric
        
        // create dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "KMeans");
        dlg.addNumericField("Class Number: ", 3, 0);
        dlg.addCheckBox("Create Centroid Table: ", true);
        
        // wait for user validation
        dlg.showDialog();
        if (dlg.wasCanceled())
        {
            return;
        }
        
        // parse dialog inputs
        int nClasses = (int) dlg.getNextNumber();
        boolean createCentroids = dlg.getNextBoolean();
        
        // create KMeans class to compute classes
        KMeans km = new KMeans(nClasses);
        km.fit(table);
        
        // associate class to each element of input table
        Table classes = km.predict(table);
        
        // add the new frame to the GUI
        TableFrame.create(classes, frame);
        
        if (createCentroids)
        {
            // associate class to each element of input table
            Table centroids = km.centroids();

            // add the new frame to the GUI
            TableFrame.create(centroids, frame);
        }
    }

}
