/**
 * 
 */
package imago.plugin.image.analyze;

import imago.app.ImagoDoc;
import imago.gui.GenericDialog;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.scalar.IntArray2D;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.image.Image;
import net.sci.image.analyze.GrayLevelCooccurenceMatrix;

/**
 * @author dlegland
 *
 */
public class GrayLevelImageCooccurenceMatrix implements Plugin
{
    
    /**
     * 
     */
    public GrayLevelImageCooccurenceMatrix()
    {
    }
    
    /* (non-Javadoc)
     * @see imago.gui.Plugin#run(imago.gui.ImagoFrame, java.lang.String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("Co occurence matrix");
        
        // get current image data
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        Array<?> array = image.getData();

        // check input data type
        if (!(array instanceof ScalarArray2D))
        {
            return;
        }
        
        // Create dialog for setting options
        GenericDialog gd = new GenericDialog(frame, "Gray Level Co-Occurence Matrix");
        gd.addNumericField("Shift X ", 1, 0);
        gd.addNumericField("Shift Y ", 0, 0);
        gd.showDialog();
        
        if (gd.wasCanceled()) 
        {
            return;
        }

        // parse dialog options
        int shiftX = (int) gd.getNextNumber();
        int shiftY = (int) gd.getNextNumber();
        
        // Create and configure operator
        GrayLevelCooccurenceMatrix algo = new GrayLevelCooccurenceMatrix(new int[] {shiftX, shiftY});
        
        // compute result
        IntArray2D<?> result = algo.process(array);
        
        // convert to Image
        Image resultImage = new Image(result);
        
        // add the image documents to GUI
        ImagoGui gui = frame.getGui();
        gui.addNewDocument(resultImage);
    }
    
}