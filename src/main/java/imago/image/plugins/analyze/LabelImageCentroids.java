/**
 * 
 */
package imago.image.plugins.analyze;

import java.util.List;
import java.util.Map;

import imago.app.ImagoApp;
import imago.app.ObjectHandle;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.shape.ShapeManager;
import imago.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.Int;
import net.sci.array.numeric.IntArray3D;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.MultiPoint2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom3d.MultiPoint3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.image.Image;
import net.sci.image.analyze.RegionAnalysis3D;
import net.sci.image.analyze.region2d.Centroid2D;
import net.sci.image.label.LabelImages;
import net.sci.table.Table;

/**
 * Computes the centroid of each region within the current label image.
 * 
 * @author dlegland
 *
 */
public class LabelImageCentroids implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public LabelImageCentroids()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
        {
            return;
        }
        ImagoGui gui = frame.getGui();
        ImagoApp app = gui.getAppli();
        
        // check image type
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
        if (!image.isLabelImage())
        {
            throw new IllegalArgumentException("Requires label image as input");
        }
        
        // retrieve image data and  check input data type
        Array<?> array = image.getData();
        if (!Int.class.isAssignableFrom(array.elementClass()))
        {
            throw new IllegalArgumentException("Requires an array of Int values, not: " + array.elementClass().getName());
        }
        
        String[] imageNameArray = ImageHandle.getAllNames(app).stream().toArray(String[]::new);
        
        // open dialog to setup options
        GenericDialog dlg = new GenericDialog(frame, "Centroids");
        dlg.addCheckBox("Overlay Results ", true);
        String firstImageName = handle.getName();
        dlg.addChoice("Image to Overlay ", imageNameArray, firstImageName);
        dlg.addCheckBox("Display Table ", true);
        dlg.addCheckBox("Add to Shape Manager", true);
        dlg.showDialog();
        
        if (dlg.wasCanceled()) 
        {
            return;
        }
        
        // Parse dialog options
        boolean overlay = dlg.getNextBoolean();
        String imageToOverlay = dlg.getNextChoice();
        boolean showTable = dlg.getNextBoolean();
        boolean addToShapeManager = dlg.getNextBoolean();

        Geometry geometry;
        int nd = array.dimensionality();
        if (nd == 2)
        {
            // Extract centroid of each region
            Centroid2D algo = new Centroid2D();
            Map<Integer, Point2D> centroids = algo.analyzeRegions(image);
            
            // Overlay results on an image
            geometry = MultiPoint2D.create(centroids.values()); 
            if (overlay)
            {
                // add to the document
                ImageHandle handle2 = ImageHandle.findFromName(app, imageToOverlay);
                handle2.addShape(new Shape(geometry));
                handle2.notifyImageHandleChange();
            }
            
            // display results in a new Table
            if (showTable)
            {
                Table table = algo.createTable(centroids);
                table.setName(ObjectHandle.appendSuffix(image.getName(), "centroids"));
                TableFrame.create(table, frame);
            }
        }
        else if (nd == 3)
        {
            // check input data type
            if (!(array instanceof IntArray3D))
            {
                System.out.println("Requires a 3D array of int");
                return;
            }
    
            // Extract centroids as an array of points
            IntArray3D<?> array3d = (IntArray3D<?>) array;
            int[] labels = LabelImages.findAllLabels(array3d); 
            Point3D[] centroids = RegionAnalysis3D.centroids(array3d, labels);
            
            geometry = MultiPoint3D.create(List.of(centroids));
            
            // Convert centroid array to table, and display
            Table table = Table.create(centroids.length, 3);
            table.setColumnNames(new String[]{"Centroid.X", "Centroid.Y", "Centroid.Z"});
            for (int i = 0; i < centroids.length; i++)
            {
                Point3D centroid = centroids[i];
                table.setValue(i, 0, centroid.x());
                table.setValue(i, 1, centroid.y());
                table.setValue(i, 2, centroid.z());
            }
            table.setName(ObjectHandle.appendSuffix(image.getName(), "centroids"));

            // add the new frame to the GUI
            TableFrame.create(table, frame);
        }
        else
        {
            System.out.println("Can not manage image with data array with dimension: " + nd);
            return;
        }
        
        if (addToShapeManager)
        {
            GeometryHandle geomHandle = GeometryHandle.create(app, geometry);
            
            // opens a dialog to choose name
            String name = handle.getName() + "-centroids";
            name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
            geomHandle.setName(name);
            
            // ensure ShapeManager is visible
            ShapeManager manager = ShapeManager.getInstance(frame.getGui());
            manager.repaint();
            manager.setVisible(true);
        }
    }
}
