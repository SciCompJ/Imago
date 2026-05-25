/**
 * 
 */
package imago.image.plugins.file;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.numeric.Float32;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.Float64;
import net.sci.array.numeric.Float64Array;
import net.sci.array.numeric.Int32;
import net.sci.array.numeric.Int32Array;
import net.sci.array.numeric.UInt16;
import net.sci.array.numeric.UInt16Array;
import net.sci.array.numeric.UInt8;
import net.sci.array.numeric.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new image, filled with the specified value.
 * 
 * @author David Legland
 *
 */
public class CreateNewImage implements FramePlugin
{
    /**
     * Enumeration type to choose an image data factory.
     */
    public enum Type
    {
        BINARY("Binary",    ImageDataFactory.BINARY),
        GRAY8("Gray8",      ImageDataFactory.GRAY8),
        GRAY16("Gray16",    ImageDataFactory.GRAY16),
        INT32("Int32",      ImageDataFactory.INT32),
        FLOAT32("Float32",  ImageDataFactory.FLOAT32),
        FLOAT64("Float64",  ImageDataFactory.FLOAT64),
        COLOR("Color",      ImageDataFactory.COLOR);
        
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
            return switch (array)
            {
                case BinaryArray binary -> BINARY;
                case UInt8Array binary -> GRAY8;
                case UInt16Array binary -> GRAY16;
                case Int32Array binary -> INT32;
                case Float32Array binary -> FLOAT32;
                case Float64Array binary -> FLOAT64;
                case RGB8Array binary -> COLOR;
                default -> GRAY8;
            };
        }

        private final String label;
        
        private final ImageDataFactory factory;
        
        private Type(String label, ImageDataFactory factory)
        {
            this.label = label;
            this.factory = factory;
        }
        
        public Array<?> createArray(int[] dims, double fillValue)
        {
            return factory.create(dims, fillValue);
        }
        
        ImageDataFactory factory()
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("create new image");

        // determine default values for dialog
        String baseName = frame.getGui().getAppli().getWorkspace().createHandleName("NoName");
        int sizeX_init = 200;
        int sizeY_init = 200;
        int sizeZ_init = 1;
        Type type_init = Type.GRAY8;

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
        gd.addIntegerField("Size X: ", sizeX_init);
        gd.addIntegerField("Size Y: ", sizeY_init);
        gd.addIntegerField("Size Z: ", sizeZ_init);
        gd.addChoice("Image Type: ", Type.getAllLabels(), type_init.toString());
        gd.addNumericField("Fill Value: ", 0, 0);
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        String imageName = gd.getNextString();
        int sizeX = gd.getNextInteger();
        int sizeY = gd.getNextInteger();
        int sizeZ = gd.getNextInteger();
        Type type = Type.fromLabel(gd.getNextChoice());
        double fillValue = gd.getNextNumber();

        // create dimension vector for the new array
        int[] dims = sizeZ <= 1 ? new int[] { sizeX, sizeY } : new int[] { sizeX, sizeY, sizeZ };

        // Create the array depending on the type
        ImageDataFactory factory = type.factory();
        factory.addAlgoListener(frame);
        long t0 = System.nanoTime();
        Array<?> array = factory.create(dims, fillValue);
        long t1 = System.nanoTime();
        factory.removeAlgoListener(frame);
        frame.algoTerminated("Create New Image", (t1 - t0) / 1_000_000.0);
        
        // Create image
        Image image = new Image(array);
        image.setName(imageName);

        // add the image document to GUI
        ImageFrame.create(image, frame);
    }

    private static abstract class ImageDataFactory extends AlgoStub implements AlgoListener
    {
        public static final ImageDataFactory BINARY = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                BinaryArray.Factory factory = BinaryArray.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new Binary(initialValue > 0));
                factory.removeAlgoListener(this);
                return res;
            }
        };

        public static final ImageDataFactory GRAY8 = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                UInt8Array.Factory factory = UInt8Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new UInt8(UInt8.convert(initialValue)));
                factory.removeAlgoListener(this);
                return res;
            }
        };

        public static final ImageDataFactory GRAY16 = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                UInt16Array.Factory factory = UInt16Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new UInt16(UInt16.convert(initialValue)));
                factory.removeAlgoListener(this);
                return res;
            }
        };

        public static final ImageDataFactory INT32 = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                Int32Array.Factory factory = Int32Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new Int32(Int32.convert(initialValue)));
                factory.removeAlgoListener(this);
                return res;
            }
        };

        public static final ImageDataFactory FLOAT32 = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                Float32Array.Factory factory = Float32Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new Float32((float) initialValue));
                factory.removeAlgoListener(this);
                return res;
            }
        };

        public static final ImageDataFactory FLOAT64 = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                Float64Array.Factory factory = Float64Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new Float64(initialValue));
                factory.removeAlgoListener(this);
                return res;
            }
        };
        
        public static final ImageDataFactory COLOR = new ImageDataFactory()
        {
            @Override
            public Array<?> create(int[] dims, double initialValue)
            {
                RGB8Array.Factory factory = RGB8Array.defaultFactory;
                factory.addAlgoListener(this);
                Array<?> res = factory.create(dims, new RGB8(initialValue, initialValue, initialValue));
                factory.removeAlgoListener(this);
                return res;
            }
        };
        
        public abstract Array<?> create(int[] dims, double initialValue);
        
        @Override
        public void algoProgressChanged(AlgoEvent evt)
        {
            this.fireProgressChanged(evt);
        }

        @Override
        public void algoStatusChanged(AlgoEvent evt)
        {
            this.fireStatusChanged(evt);
        }
    }
}
