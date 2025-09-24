/**
 * 
 */
package imago.image.plugin.process;

import java.util.Locale;
import java.util.stream.Stream;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.image.Image;
import net.sci.image.morphology.filtering.BlackTopHat;
import net.sci.image.morphology.filtering.Closing;
import net.sci.image.morphology.filtering.Dilation;
import net.sci.image.morphology.filtering.Erosion;
import net.sci.image.morphology.filtering.Gradient;
import net.sci.image.morphology.filtering.InnerGradient;
import net.sci.image.morphology.filtering.Laplacian;
import net.sci.image.morphology.filtering.Opening;
import net.sci.image.morphology.filtering.OuterGradient;
import net.sci.image.morphology.filtering.WhiteTopHat;
import net.sci.image.morphology.MorphologicalFilter;
import net.sci.image.morphology.Strel;
import net.sci.image.morphology.strel.Strel2D;
import net.sci.image.morphology.strel.Strel3D;

/**
 * Applies various types of morphological filters on a 2D or 3D image.
 * 
 * @author David Legland
 *
 */
public class ImageMorphologicalFilter implements FramePlugin
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
        /** Morphological erosion (local minima) */
        EROSION("Erosion", "Ero"),
        /** Morphological dilation (local maxima) */
        DILATION("Dilation", "Dil"),
        /** Morphological opening (erosion followed by dilation) */
        OPENING("Opening", "Op"),
        /** Morphological closing (dilation followed by erosion) */
        CLOSING("Closing", "Cl"),
        /** White Top-Hat */
        TOPHAT("White Top Hat", "WTH"),
        /** Black Top-Hat */
        BOTTOMHAT("Black Top Hat", "BTH"),
        /** Morphological gradient (difference of dilation with erosion) */
        GRADIENT("Gradient", "MGrad"),
        /**
         * Morphological Laplacian (difference of the outer gradient with the
         * inner gradient, equal to DIL+ERO-2*Img).
         */
        LAPLACIAN("Laplacian", "MLap"),
        /** Morphological internal gradient (difference of original image with erosion) */
        INNER_GRADIENT("Inner Gradient", "InnGrad"), 
        /** Morphological internal gradient (difference of dilation with original image) */
        OUTER_GRADIENT("Outer Gradient", "OutGrad");

        
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
        
        /**
         * Applies the current operator to the input array.
         * 
         * @param array
         *            the array to process
         * @param strel
         *            the structuring element to use
         * @return the result of morphological operation applied to array
         */
        public Array<?> process(Array<?> array, Strel strel) 
        {
            return createOperator(strel).process(array);
        }
        
        /**
         * Creates a new operator from this Operation enum.
         * 
         * @param strel
         *            the structuring element to use for the morphological
         *            operator.
         * @return a new instance of MorphologicalFilter
         */
        public MorphologicalFilter createOperator(Strel strel)
        {
            return switch (this)
            {
                case DILATION -> new Dilation(strel);
                case EROSION -> new Erosion(strel);
                case CLOSING -> new Closing(strel);
                case OPENING -> new Opening(strel);
                case TOPHAT -> new WhiteTopHat(strel);
                case BOTTOMHAT -> new BlackTopHat(strel);
                case GRADIENT -> new Gradient(strel);
                case LAPLACIAN -> new Laplacian(strel, 0.0);
                case INNER_GRADIENT -> new InnerGradient(strel);
                case OUTER_GRADIENT -> new OuterGradient(strel);
                default -> throw new IllegalArgumentException("Unexpected value: " + this);
            };
        }
        
        /**
         * Returns the suffix associated to this operation, that can be used to
         * build the name of the result image.
         * 
         * @return the suffix associated to this operation.
         */
        public String suffix()
        {
            return suffix;
        }
        
        /**
         * Returns the label of this operation, that can be used  to populate widgets.
         * 
         * @return the label of this operation.
         */
        public String toString() 
        {
            return this.label;
        }
        
        public static String[] getAllLabels()
        {
            return Stream.of(Operation.values())
                    .map(op -> op.label)
                    .toArray(String[]::new);
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
    
   /**
     * The operation to apply to the image.
     */
	Operation op = Operation.DILATION;

	/**
     * The shape of the structuring element for 2D images.
     */
    Strel2D.Shape shape2d = Strel2D.Shape.SQUARE;

    /**
     * The shape of the structuring element for 3D images.
     */
    Strel3D.Shape shape3d = Strel3D.Shape.CUBE;
    
    /**
     * The radius of the structuring element, in pixels.
     */
	int radius = 2;

	
    // =============================================================
    // Implements Plugin interface
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// get current image data
		ImageFrame imageFrame = (ImageFrame) frame;
		Image image	= imageFrame.getImageHandle().getImage();
		Array<?> array = image.getData();

		int nd = array.dimensionality();
		if (nd != 2 && nd != 3)
		{
			System.err.println("Requires image of dimensionality equal to 2 or 3");
			return;
		}
		
		// Create dialog for entering parameters
		GenericDialog gd = new GenericDialog(frame, "Morphological Filter");
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
		String shapeSuffix = "";
		Strel strel;
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
		MorphologicalFilter algo = op.createOperator(strel);
		
        // Execute core of the plugin on the array of original image
		Image resultImage = imageFrame.runOperator(algo, image);

		// setup name of result image
        String suffix = String.format(Locale.ENGLISH, "%s%s%02d", op.suffix(), shapeSuffix, radius);
		resultImage.setName(image.getName() + "-" + suffix);
		
		// add the image document to GUI
        ImageFrame.create(resultImage, frame);
	}
}
