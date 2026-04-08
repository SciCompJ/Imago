/**
 * 
 */
package imago.image.plugins.edit;

import java.util.Random;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.Float64Array;
import net.sci.array.numeric.Int32Array;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new image containing random values, with distribution of values
 * centered around the specified mean.
 */
public class CreateGaussianNoiseImage implements FramePlugin
{
    /**
     * Enumeration type to choose an image data factory.
     */
    public enum Type
    {
        BINARY("Binary", BinaryArray.defaultFactory),
        GRAY8("Gray8", UInt8Array.defaultFactory),
        GRAY16("Gray16", UInt16Array.defaultFactory),
        INT32("Int32", Int32Array.defaultFactory),
        FLOAT32("Float32", Float32Array.defaultFactory),
        FLOAT64("Float64", Float64Array.defaultFactory);
        
        /**
         * Returns a set of labels for most of classical types.
         * 
         * @return a list of labels
         */
        public static String[] getAllLabels()
        {
            // array of all enumeration items
            Type[] values = Type.values();
            int n = values.length;
            
            // keep all values but the last one ("Custom")
            String[] result = new String[n];
            for (int i = 0; i < n; i++)
            {
                result[i] = values[i].label;
            }
            return result;
        }

        /**
         * Determines the type from its label.
         * 
         * @param label
         *            the type name
         * @return a new Type instance that can be used to create image
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        
        public static Type fromLabel(String label)
        {
            if (label != null)
                label = label.toLowerCase();
            for (Type type : Type.values()) 
            {
                if (type.label.toLowerCase().equals(label))
                    return type;
            }
            throw new IllegalArgumentException("Unable to parse Type with label: " + label);
        }
        
        /**
         * Returns the type corresponding to the class of the input array, or a
         * default type if no type corresponds to the class.
         * 
         * @param array
         *            the array to determine the type of
         * @return a type for creating new arrays
         */
        public static Type fromArray(Array<?> array)
        {
            if (array instanceof BinaryArray)
            {
                return BINARY;
            }
            else if (array instanceof UInt8Array)
            {
                return GRAY8;
            }
            else if (array instanceof UInt16Array)
            {
                return GRAY16;
            }
            else if (array instanceof Int32Array)
            {
                return INT32;
            }
            else if (array instanceof Float32Array)
            {
                return FLOAT32;
            }
            else if (array instanceof Float64Array)
            {
                return FLOAT64;
            }
            
            // return a default type
            return FLOAT32;
        }

        private final String label;
        
        private final ScalarArray.Factory<?> factory;
        
        private Type(String label, ScalarArray.Factory<?> factory)
        {
            this.label = label;
            this.factory = factory;
        }
        
        public Array<?> createArray(int[] dims)
        {
            return factory.create(dims);
        }
        
        ScalarArray.Factory<?> factory()
        {
            return factory;
        }
        
        /** 
         * @return the label associated to this shape.
         */
        public String toString()
        {
            return this.label;
        }
    };

    /**
     * Default empty constructor.
     */
    public CreateGaussianNoiseImage()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // determine default values for dialog
        String baseName = frame.getGui().getAppli().getWorkspace().createHandleName("GaussianNoise");
        int sizeX_init = 200;
        int sizeY_init = 200;
        int sizeZ_init = 1;
        Type type_init = Type.FLOAT32;

        // if the plugin is run from an image frame, use default values
        // corresponding to the image
        if (frame instanceof ImageFrame)
        {
            Image image = ((ImageFrame) frame).getImageHandle().getImage();
            sizeX_init = image.getSize(0);
            sizeY_init = image.getSize(1);
            if (image.getDimension() > 2)
            {
                sizeZ_init = image.getSize(2);
            }
            type_init = Type.fromArray(image.getData());
        }

        // create dialog to enter options
        GenericDialog gd = new GenericDialog(frame, "Create Image");
        gd.addTextField("Name: ", baseName);
        gd.addNumericField("Size X: ", sizeX_init, 0);
        gd.addNumericField("Size Y: ", sizeY_init, 0);
        gd.addNumericField("Size Z: ", sizeZ_init, 0);
        gd.addChoice("Image Type: ", Type.getAllLabels(), type_init.toString());
        gd.addNumericField("Mean: ", 0, 3);
        gd.addNumericField("Std. Dev.: ", 1.0, 3);
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        String imageName = gd.getNextString();
        int sizeX = (int) gd.getNextNumber();
        int sizeY = (int) gd.getNextNumber();
        int sizeZ = (int) gd.getNextNumber();
        Type type = Type.fromLabel(gd.getNextChoice());
        double mean = gd.getNextNumber();
        double std = gd.getNextNumber();

        // create dimension vector for the new array
        int[] dims = sizeZ <= 1 ? new int[] { sizeX, sizeY } : new int[] { sizeX, sizeY, sizeZ };

        // Create the array depending on the type
        ScalarArray.Factory<?> factory = type.factory();
        factory.addAlgoListener(frame);
        ScalarArray<?> array = factory.create(dims);
        factory.removeAlgoListener(frame);
        
        // iterate over values to set random value
        Random rng = new Random();
        ScalarArray.Iterator<? extends Scalar<?>> iter = array.iterator();
        while (iter.hasNext())
        {
            iter.setNextValue(rng.nextGaussian(mean, std));
        }

        // Create image
        Image image = new Image(array);
        image.setName(imageName);

        // add the image document to GUI
        ImageFrame.create(image, frame);
    }
}
