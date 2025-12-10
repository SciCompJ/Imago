/**
 * 
 */
package imago.image.plugins.binary;

import java.util.Locale;
import java.util.function.Function;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.morphology.BinaryMorphologicalFilter;
import net.sci.image.morphology.Strel;
import net.sci.image.morphology.filtering.BinaryBlackTopHat;
import net.sci.image.morphology.filtering.BinaryClosing;
import net.sci.image.morphology.filtering.BinaryDilation;
import net.sci.image.morphology.filtering.BinaryErosion;
import net.sci.image.morphology.filtering.BinaryGradient;
import net.sci.image.morphology.filtering.BinaryInnerGradient;
import net.sci.image.morphology.filtering.BinaryOpening;
import net.sci.image.morphology.filtering.BinaryOuterGradient;
import net.sci.image.morphology.filtering.BinaryWhiteTopHat;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.image.morphology.strel.Strel3D;


/**
 * Computes morphological filtering on the current binary image.
 *
 * @see imago.image.plugins.process.ImageMorphologicalFilter
 * 
 * @author David Legland
 */
public class BinaryImageMorphologicalFilter implements FramePlugin
{
    // =======================================================================
    // Inner enumeration
    
    /**
     * A pre-defined set of basis morphological operations, that can be easily 
     * used with a GenericDialog.
     *  
     * Example:
     * {@snippet lang="java" :
     * // Use a generic dialog to define an operator 
     * GenericDialog gd = new GenericDialog();
     * gd.addChoice("Operation", Operation.getAllLabels();
     * gd.showDialog();
     * Operation op = Operation.fromLabel(gd.getNextChoice());
     * // Apply the operation on the current array
     * Array<?> array = image.getData();
     * op.apply(array, SquareStrel.fromRadius(2));
     * }
     */
    public enum Operation
    {
        /** Morphological erosion */
        EROSION("Erosion", "Ero", BinaryErosion::new),
        /** Morphological dilation */
        DILATION("Dilation", "Dil", BinaryDilation::new),
        /** Morphological opening (erosion followed by dilation) */
        OPENING("Opening", "Op", BinaryOpening::new),
        /** Morphological closing (dilation followed by erosion) */
        CLOSING("Closing", "Cl", BinaryClosing::new),
        /** Morphological gradient (difference of dilation with erosion) */
        GRADIENT("Gradient", "Grad", BinaryGradient::new),
        /** Morphological internal gradient (difference of original image with erosion) */
        INNER_GRADIENT("Inner Gradient", "InnGrad", BinaryInnerGradient::new), 
        /** Morphological internal gradient (difference of dilation with original image) */
        OUTER_GRADIENT("Outer Gradient", "OutGrad", BinaryOuterGradient::new),
        /** White Top-Hat */
        WHITETOPHAT("White Top Hat", "WTH", BinaryWhiteTopHat::new),
        /** Black Top-Hat */
        BLACKTOPHAT("Black Top Hat", "BTH", BinaryBlackTopHat::new);
        
        /**
         * A label that can be used for display in graphical widgets.
         */
        private final String label;
        
        /**
         * A suffix intended to be used for creating result image names. 
         */
        private String suffix;
        
        private Function<Strel, BinaryMorphologicalFilter> factory;
        
        private Operation(String label, String suffix, Function<Strel, BinaryMorphologicalFilter> factory) 
        {
            this.label = label;
            this.suffix = suffix;
            this.factory = factory;
        }
        
        /**
         * Creates a new operator based on the specified structuring element.
         * 
         * @param strel
         *            the structuring element to use for the operation
         * @return a new instance of BinaryMorphologicalFilter that can be used
         *         to process binary arrays
         */
        public BinaryMorphologicalFilter createOperator(Strel strel)
        {
            return this.factory.apply(strel);        
        }
        
        /**
         * Returns the suffix used to create new image name.
         * 
         * @return the suffix used to create new image name.
         */
        public String suffix()
        {
            return suffix;
        }
        
        /**
         * Creates a String representation of this operator.
         * 
         * @return a String representation of this operator.
         */
        public String toString() 
        {
            return this.label;
        }
        
        /**
         * Returns all the operator labels within this enumeration.
         * 
         * @return all the operator labels within this enumeration.
         */
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
     * The shape of the structuring element for 2D images.
     */
    Strel2D.Shape shape2d = Strel2D.Shape.DISK;

    /**
     * The shape of the structuring element for 3D images.
     */
    Strel3D.Shape shape3d = Strel3D.Shape.BALL;

    /**
     * The radius of the structuring element, in pixels.
     */
    int radius = 2;
    
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
        
        // Create dialog for entering parameters
        GenericDialog gd = new GenericDialog(frame, "Binary Morphological Filter");
        gd.addEnumChoice("Operation", Operation.class, this.op);
        if (nd == 2)
        {
            gd.addEnumChoice("Element", Strel2D.Shape.class, this.shape2d);
            gd.addNumericField("Radius (in pixels)", this.radius, 0);
        }
        else
        {
            gd.addEnumChoice("Element", Strel3D.Shape.class, this.shape3d);
            gd.addNumericField("Radius (in voxels)", this.radius, 0);
        }
        gd.showDialog();
        
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        // extract chosen parameters
        this.op = (Operation) gd.getNextEnumChoice();
        Strel strel;
        String shapeSuffix;
        if (nd == 2)
        {
            this.shape2d = (Strel2D.Shape) gd.getNextEnumChoice();
            shapeSuffix = this.shape2d.suffix();
            this.radius  = (int) gd.getNextNumber();
            strel = shape2d.fromRadius(radius);
        }
        else
        {
            this.shape3d = (Strel3D.Shape) gd.getNextEnumChoice();
            shapeSuffix = this.shape3d.suffix();
            this.radius  = (int) gd.getNextNumber();
            strel = shape3d.fromRadius(radius);
        }
        
        // Create the morphological filter
        BinaryMorphologicalFilter algo = op.createOperator(strel);
        
        // Execute core of the plugin on the array of original image
        Image resultImage = imageFrame.runOperator(algo, image);
        
        // setup name of result image
        String suffix = String.format(Locale.ENGLISH, "%s%s%02d", op.suffix(), shapeSuffix, radius);
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
