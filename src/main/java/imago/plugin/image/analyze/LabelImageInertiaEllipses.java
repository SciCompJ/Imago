/**
 * 
 */
package imago.plugin.image.analyze;

import java.util.Collection;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.morphology.LabelImages;
import net.sci.table.Table;

/**
 * Computes the inertia ellipse of each region in the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageInertiaEllipses implements Plugin
{
    public LabelImageInertiaEllipses()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImagoDocViewer))
        {
            return;
        }
        
        // retrieve image data
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        
        // check image type
        if (!image.isLabelImage())
        {
            System.out.println("Requires label image as input");
            return;
        }

        // check input data type
        Array<?> array = image.getData();
        if (!(array instanceof IntArray2D))
        {
            ImagoGui.showErrorDialog(frame, "Requires a planar array of labels");
            return;
        }

        ImagoGui gui = frame.getGui();
        
        // open dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "Inertia Ellipses");
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = gui.getAppli().getImageDocumentNames();
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        // Parse dialog options
        boolean showTable = dlg.getNextBoolean();
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        
        // Extract ellipses
        IntArray2D<?> array2d = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(array2d); 
        Ellipse2D[] ellipses = RegionAnalysis2D.inertiaEllipses(array2d, labels);
         
        // Display results within a table
        if (showTable)
        {
            // Convert ellipse to table, and display
            Table tab = Table.create(ellipses.length, 5);
            tab.setColumnNames(new String[]{"Center.X", "Center.Y", "MajorSemiAxisLength", "MinorSemiAxisLength", "Orientation"});
            for (int i = 0; i < ellipses.length; i++)
            {
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                tab.setValue(i, 0, center.getX());
                tab.setValue(i, 1, center.getY());
                tab.setValue(i, 2, elli.semiMajorAxisLength());
                tab.setValue(i, 3, elli.semiMinorAxisLength());
                tab.setValue(i, 4, elli.orientation());
            }
            
            gui.addFrame(new ImagoTableFrame(frame, tab));
        }
        
        // Overlay results on an image
        if (overlay)
        {
            ImagoDoc ovrDoc = gui.getAppli().getDocumentFromName(imageToOverlay);
            ImagoDocViewer viewer = gui.getDocumentViewer(ovrDoc);
            
            // add to the document
            for (int i = 0; i < ellipses.length; i++)
            {
                ovrDoc.addShape(new ImagoShape(ellipses[i]));
            }
            // TODO: maybe propagating events would be better
            viewer.repaint(); 
        }
    }
    
}
