/**
 * 
 */
package imago.gui.action.analyze;

import imago.app.ImagoDoc;
import imago.app.shape.ImagoShape;
import imago.gui.GenericDialog;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.ImagoTableFrame;

import java.awt.event.ActionEvent;
import java.util.Collection;

import net.sci.array.Array;
import net.sci.array.data.scalar2d.IntArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.curve.Circle2D;
import net.sci.geom.geom2d.curve.Ellipse2D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis2D;
import net.sci.image.morphology.LabelImages;
import net.sci.table.Table;

/**
 * Computes the equivalent disk of each region in the current label image.
 * 
 * The center of the disk is the centroid if the region, and the radius is
 * computed such as the area of the disk corresponds to the area of the region.
 * 
 * @author dlegland
 *
 */
public class LabelImageEquivalentDisksAction extends ImagoAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LabelImageEquivalentDisksAction(ImagoFrame frame, String name)
    {
        super(frame, name);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        // Check type is image frame
        if (!(frame instanceof ImagoDocViewer))
        {
            return;
        }
        
        // retrieve image data
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
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

        GenericDialog dlg = new GenericDialog(this.frame, "Equivalent Disks");
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Overlay Results ", true);
        Collection<String> imageNames = gui.getAppli().getImageDocumentNames();
        String[] imageNameArray = imageNames.toArray(new String[]{});
        String firstImageName = doc.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.showDialog();
        
        boolean showTable = dlg.getNextBoolean();
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        
        // Extract ellipses
        IntArray2D<?> array2d = (IntArray2D<?>) array;
        int[] labels = LabelImages.findAllLabels(array2d);
        Ellipse2D[] ellipses = RegionAnalysis2D.inertiaEllipses(array2d, labels);
         
        if (showTable)
        {
            // Convert ellipse to table, and display
            Table tab = Table.create(ellipses.length, 3);
            tab.setColumnNames(new String[]{"Center.X", "Center.Y", "Radius"});
            for (int i = 0; i < ellipses.length; i++)
            {
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                tab.setValue(i, 0, center.getX());
                tab.setValue(i, 1, center.getY());
                double radius = Math.sqrt(elli.semiMajorAxisLength() * elli.semiMinorAxisLength());
                tab.setValue(i, 2, radius);
            }
            
            gui.addFrame(new ImagoTableFrame(this.frame, tab));
        }
        
        if (overlay)
        {
            ImagoDoc ovrDoc = gui.getAppli().getDocumentFromName(imageToOverlay);
            ImagoDocViewer viewer = gui.getDocumentViewer(ovrDoc);
            
            // add to the document
            for (int i = 0; i < ellipses.length; i++)
            {
                Ellipse2D elli = ellipses[i];
                Point2D center = elli.center();
                double radius = Math.sqrt(elli.semiMajorAxisLength() * elli.semiMinorAxisLength());
                ovrDoc.addShape(new ImagoShape(new Circle2D(center, radius)));
            }
            
            // TODO: maybe propagating events would be better
            viewer.repaint(); 
        }
    }
    
}
