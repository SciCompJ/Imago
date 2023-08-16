/**
 * 
 */
package imago.plugin.image.binary;

import net.sci.array.scalar.Float32Array;
import net.sci.array.scalar.Int32Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.UInt16Array;
import net.sci.array.scalar.UInt8Array;

/**
 * An enumeration of data types that can be used to represent distance maps.
 * 
 * @author dlegland
 *
 */
public enum DistanceMapDataType
{
    UINT8("UInt8", true),
    UINT16("UInt16", true),
    INT32("Int32", true),
    FLOAT32("Float32", false);
    
    private DistanceMapDataType(String label, boolean intType)
    {
        this.label = label;
        this.intType = intType;
    }
    
    public boolean isIntType()
    {
        return intType;
    }
    
    public ScalarArray.Factory<?> factory()
    {
        return switch (this)
        {
            case UINT8 -> UInt8Array.defaultFactory;
            case UINT16 -> UInt16Array.defaultFactory;
            case INT32 -> Int32Array.defaultFactory;
            case FLOAT32 -> Float32Array.defaultFactory;
            default -> throw new RuntimeException("Unknown Array factory");
        };
    }
    
    String label;
    boolean intType;
    
    @Override
    public String toString()
    {
        return this.label;
    }

}
