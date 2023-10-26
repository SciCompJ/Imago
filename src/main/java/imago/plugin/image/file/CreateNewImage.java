/**
 * 
 */
package imago.plugin.image.file;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.color.RGB8;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.Int32Array;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.Image;

/**
 * Creates a new image, filled with the specified value.
 * 
 * @author David Legland
 *
 */
public class CreateNewImage implements FramePlugin
{
    public enum Type
    {
        BINARY("Binary", (dims,  value) -> {
            BinaryArray array = BinaryArray.create(dims); 
            if (value > 0)
            {
                array.fill(true);
            }
            return array;
        }),
        GRAY8("Gray8", (dims, value) -> {
            UInt8Array array = UInt8Array.create(dims);
            // convert fill value to int before calling fill method
            array.fillInt((int) value);
            return array;
        }),
        GRAY16("Gray16", (dims, value) -> {
            UInt16Array array = UInt16Array.create(dims);
            // convert fill value to int before calling fill method
            array.fillInt((int) value);
            return array;
        }),
        INT32("Int32", (dims, value) -> {
            Int32Array array = Int32Array.create(dims);
            // convert fill value to int before calling fill method
            array.fillInt((int) value);
            return array;
        }),
        FLOAT32("Float32", (dims, value) -> {
            Float32Array array = Float32Array.create(dims); 
            array.fillValue(value);
            return array;
        }),
        FLOAT64("Float64", (dims, value) -> {
            Float64Array array = Float64Array.create(dims); 
            array.fillValue(value);
            return array;
        }),
        RGB8("Color", (dims, value) -> {
            RGB8Array array = RGB8Array.create(dims);
            RGB8 rgb = new RGB8(value, value, value);
            array.fill(rgb);
            return array;
        });
        
        /**
         * Returns a set of labels for most of classical types.
         * 
         * @return a list of labels
         */
        public static String[] getAllLabels()
        {
            // array of all enumaration items
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
            else if (array instanceof Float32Array)
            {
                return FLOAT64;
            }
            else if (array instanceof RGB8Array)
            {
                return RGB8;
            }
            
            // return a default type
            return GRAY8;
        }

        private final String label;
        
        private final Factory factory;
        
        private Type(String label, Factory factory)
        {
            this.label = label;
            this.factory = factory;
        }
        
        public Array<?> createArray(int[] dims, double fillValue)
        {
            return factory.create(dims, fillValue);
        }
        
        /** 
         * @return the label associated to this shape.
         */
        public String toString()
        {
            return this.label;
        }

        /**
         * Inner interface used for defining the creation function used by the
         * different types.
         */
        private interface Factory
        {
            /**
             * Creates a new array.
             * 
             * @param dims
             *            the size of the new array
             * @param value
             *            the value to fill the array with.
             * @return a new array filled with the specified value.
             */
            public Array<?> create(int[] dims, double value);
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
        String baseName = frame.getGui().getAppli().createHandleName("NoName");
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
        gd.addNumericField("Size X: ", sizeX_init, 0);
		gd.addNumericField("Size Y: ", sizeY_init, 0);
		gd.addNumericField("Size Z: ", sizeZ_init, 0);
        gd.addChoice("Image Type: ", Type.getAllLabels(), type_init.toString());
		gd.addNumericField("Fill Value: ", 0, 0);
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
		double fillValue = gd.getNextNumber();
		
		// create dimension vector for the new array
		int[] dims = sizeZ <= 1 ? new int[]{sizeX, sizeY} : new int[]{sizeX, sizeY, sizeZ}; 
		
		// Create the array depending on the type
        Array<?> array = type.createArray(dims, fillValue);
        
		// Create image
		Image image = new Image(array);
		image.setName(imageName);
		
		// add the image document to GUI
		frame.createImageFrame(image);
	}
}
