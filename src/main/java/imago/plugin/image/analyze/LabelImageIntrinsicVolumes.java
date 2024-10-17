/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImageHandle;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.Array;
import net.sci.array.numeric.IntArray2D;
import net.sci.image.Image;
import net.sci.image.analyze.region2d.IntrinsicVolumes2D;
import net.sci.table.Table;

/**
 * Compute intrinsic volumes within a binary or label image. In 2D, intrinsic
 * volumes correspond to area, perimeter (boundary length) and (2D) Euler
 * number. In 3D, they correspond to Volume, surface area, mean breadth and (3D)
 * Euler number.
 * 
 * @author dlegland
 *
 */
public class LabelImageIntrinsicVolumes implements FramePlugin
{
    public LabelImageIntrinsicVolumes()
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
        
        // retrieve image data
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
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

        // Display dialog to choose parameters
        GenericDialog dlg = new GenericDialog(frame, "Intrinsic Volumes");
        dlg.addChoice("Direction number: ", new String[] {"2", "4"}, "4");
        dlg.addChoice("Connectivity: ", new String[] {"C4", "C8"}, "C8");
        dlg.showDialog();
        
        // parse user choices
        int[] dirNumbers = new int[] {2, 4};
        int directionNumber = dirNumbers[dlg.getNextChoiceIndex()];
        int[] conns = new int[] {4, 8}; 
        int conn =  conns[dlg.getNextChoiceIndex()];
        
        // create analyzer
        IntrinsicVolumes2D algo = new IntrinsicVolumes2D();
        algo.setDirectionNumber(directionNumber);
        algo.setConnectivity(conn);
        
        
        // compute feature for all regions within image
        Table table = algo.computeTable(image);
        // Equivalent to:
        //  Map<Integer, IntrinsicVolumesAnalyzer2D.Result> results = algo.analyzeRegions(image);
        //  Table table = algo.createTable(results);
        
        // add the new frame to the GUI
        TableFrame.create(table, frame);
    }
    
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        if (!(frame instanceof ImageFrame)) return false;
        
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        if (doc == null) return false;

        Image image = doc.getImage();
        if (image == null) return false;

        if (image.getDimension() != 2) return false;
        if (!image.isLabelImage()) return false;

        return true;
    }
}
