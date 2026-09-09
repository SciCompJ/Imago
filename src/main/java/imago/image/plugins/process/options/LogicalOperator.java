/**
 * 
 */
package imago.image.plugins.process.options;

import java.util.function.BiFunction;

/**
 * A collection of common math functions taking two floating point values as
 * argument and returning a floating point value as output.
 * 
 * @see SingleValueFunction
 */
public enum LogicalOperator
{
    EQUAL("Equal", "eq", (a, b) -> Double.compare(a, b) == 0),
    NOT_EQUAL("Not Equal", "ne", (a, b) -> Double.compare(a, b) != 0),
    GREATER_THAN("Greater Than", "gt", (a, b) -> a > b),
    GREATER_THAN_OR_EQUAL("Greater Than or Equal", "ge", (a, b) -> a >= b),
    LOWER_THAN("Greater Than", "lt", (a, b) -> a < b),
    LOWER_THAN_OR_EQUAL("Greater Than or Equal", "le", (a, b) -> a <= b);
    
    private String name;
    private String shortName;
    private BiFunction<Double,Double,Boolean> function;
    
    private LogicalOperator(String name, String shortName, BiFunction<Double,Double,Boolean> function)
    {
        this.name = name;
        this.shortName = shortName;
        this.function = function;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getShortName()
    {
        return this.shortName;
    }
    
    public BiFunction<Double,Double,Boolean> getFunction()
    {
        return this.function;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
