/**
 * 
 */
package imago.plugin.image.process;

import java.util.EnumSet;

import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Float64Array;
import net.sci.array.scalar.Int16Array;
import net.sci.array.scalar.Int32Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;

/**
 * @author dlegland
 *
 */
public enum ScalarOutputTypes
{
    SAME_AS_INPUT("Same as Input", null), 
    UINT8("UInt8", UInt8Array.defaultFactory), 
    UINT16("UInt16", UInt16Array.defaultFactory), 
    INT16("Int16", Int16Array.defaultFactory),
    INT32("Int32", Int32Array.defaultFactory),
    FLOAT32("Float32", Float32Array.defaultFactory),
    FLOAT64("Float64", Float64Array.defaultFactory);

    private String label;
    private ScalarArray.Factory<?> factory;

    ScalarOutputTypes(String label, ScalarArray.Factory<?> factory)
    {
        this.label = label;
        this.factory = factory;
    }

    public ScalarArray.Factory<?> getFactory()
    {
        return factory;
    }

    public String toString() 
    {
        return this.label;
    }

    public static String[] getAllLabels()
    {
        int n = ScalarOutputTypes.values().length;
        String[] result = new String[n];

        int i = 0;
        for (ScalarOutputTypes op : ScalarOutputTypes.values())
            result[i++] = op.label;

        return result;
    }

    /**
     * Determines the output type type from its label.
     * 
     * @param typeLabel
     *            the label of the output type
     * @return the parsed output type
     * @throws IllegalArgumentException
     *             if label is not recognized.
     */
    public static ScalarOutputTypes fromLabel(String typeLabel)
    {
        if (typeLabel != null)
            typeLabel = typeLabel.toLowerCase();
        for (ScalarOutputTypes op : ScalarOutputTypes.values()) 
        {
            String cmp = op.label.toLowerCase();
            if (cmp.equals(typeLabel))
                return op;
        }
        throw new IllegalArgumentException("Unable to parse OutputType from label: " + typeLabel);
    }

    public static EnumSet<ScalarOutputTypes> all()
    {
        return EnumSet.allOf(ScalarOutputTypes.class);
    }
}


