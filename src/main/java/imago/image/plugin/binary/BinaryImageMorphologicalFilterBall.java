/**
 * 
 */
package imago.image.plugin.binary;

import java.util.Locale;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.ArrayOperator;
import net.sci.array.binary.BinaryArray;
import net.sci.image.Image;
import net.sci.image.morphology.filtering.BallBinaryDilation;
import net.sci.image.morphology.filtering.BallBinaryErosion;


/**
 * Computes morphological filtering on the current binary image, using faster
 * algorithm. This implementation is mostly a quick fix to allow processing of
 * binary images with ball structuring elements using efficient algorithm. In a
 * future release, it could managed in the same way as other operators.
 *
 * @see net.sci.image.morphology.filtering.BallBinaryDilation
 * @see net.sci.image.morphology.filtering.BallBinaryErosion
 * 
 * @author David Legland
 */
public class BinaryImageMorphologicalFilterBall implements FramePlugin
{
    // =======================================================================
    // Inner enumeration
    
    /**
     * A pre-defined set of basis morphological operations, that can be easily 
     * used with a GenericDialog.</p>
     *  
     * Example:
     * <pre>
     * {@code
     * // Use a generic dialog to define an operator 
     * GenericDialog gd = new GenericDialog();
     * gd.addChoice("Operation", Operation.getAllLabels();
     * gd.showDialog();
     * Operation op = Operation.fromLabel(gd.getNextChoice());
     * // Apply the operation on the current array
     * Array<?> array = image.getData();
     * op.apply(array, SquareStrel.fromRadius(2));
     * }</pre>
     */
    public enum Operation
    {
        /** Morphological erosion */
        EROSION("Erosion", "Ero"),
        /** Morphological dilation */
        DILATION("Dilation", "Dil"),
//        /** Morphological opening (erosion followed by dilation) */
//        OPENING("Opening", "Op"),
//        /** Morphological closing (dilation followed by erosion) */
//        CLOSING("Closing", "Cl"),
//        /** Morphological gradient (difference of dilation with erosion) */
//        GRADIENT("Gradient", "Grad"),
//        /** Morphological internal gradient (difference of original image with erosion) */
//        INNER_GRADIENT("Inner Gradient", "InnGrad"), 
//        /** Morphological internal gradient (difference of dilation with original image) */
//        OUTER_GRADIENT("Outer Gradient", "OutGrad"),
//        /** White Top-Hat */
//        WHITETOPHAT("White Top Hat", "WTH"),
//        /** Black Top-Hat */
//        BLACKTOPHAT("Black Top Hat", "BTH");
        ;
        
        /**
         * A label that can be used for display in graphical widgets.
         */
        private final String label;
        
        /**
         * A suffix intended to be used for creating result image names. 
         */
        private String suffix;
        
        private Operation(String label, String suffix) 
        {
            this.label = label;
            this.suffix = suffix;
        }
        
        public String suffix()
        {
            return suffix;
        }
        
        public String toString() 
        {
            return this.label;
        }
        
        public static String[] getAllLabels()
        {
            int n = Operation.values().length;
            String[] result = new String[n];
            
            int i = 0;
            for (Operation op : Operation.values())
                result[i++] = op.label;
            
            return result;
        }
        
        /**
         * Determines the operation type from its label.
         * 
         * @param opLabel
         *            the label of the operation
         * @return the parsed Operation
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        public static Operation fromLabel(String opLabel)
        {
            if (opLabel != null)
                opLabel = opLabel.toLowerCase();
            for (Operation op : Operation.values()) 
            {
                String cmp = op.label.toLowerCase();
                if (cmp.equals(opLabel))
                    return op;
            }
            throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
        }
    }
    

    // =============================================================
    // Class variables
    
    Operation op = Operation.DILATION;

    /**
     * The radius of the structuring element, in pixels.
     */
    double radius = 2.0;
    
    
    // =============================================================
    // Implementation of the FramePlugin interface
    
	@Override
	public void run(ImagoFrame frame, String args) 
	{
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            System.err.println("Requires image of dimensionality equal to 2 or 3");
            return;
        }
        if (!(array instanceof BinaryArray))
        {
            System.err.println("Requires an instance of BinaryArray as input");
            return;
        }
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Ball Binary Morphological Filter");
        gd.addEnumChoice("Operation", Operation.class, this.op);
        gd.addNumericField("Radius (in pixels)", this.radius, 0);
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        // extract chosen parameters
        this.op = (Operation) gd.getNextEnumChoice();
        this.radius = gd.getNextNumber();
        
        ArrayOperator algo = switch (op)
        {
            case DILATION -> new BallBinaryDilation(radius);
            case EROSION -> new BallBinaryErosion(radius);
            default -> throw new RuntimeException("Operation is not managed: " + op);
        };
        
        // run the operator on the image
        Image resultImage = imageFrame.runOperator(algo, image);
        
        // setup name of result image
        String strelName = nd == 2 ? "Disk" : "Ball";
        String suffix = String.format(Locale.ENGLISH, "%s%s%02.0f", op.suffix(), strelName, radius);
        resultImage.setName(image.getName() + "-" + suffix);
        
        ImageFrame.create(resultImage, frame);
	}

    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isBinaryImage();
    }
}
