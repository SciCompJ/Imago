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
public enum ValuePairFunction
{
    PLUS("Plus", (a, b) -> a + b),
    MINUS("Minus", (a,b) -> a - b),
    MULTIPLY("Multiply", (a,b) -> a * b),
    DIVIDE("Divide", (a,b) -> a / b),
    DIFFERENCE("Difference", (a,b) -> Math.abs(a - b)),
    MIN("Minimum", Math::min),
    MAX("Maximum", Math::max),
    MODULO("Modulo", (a,b) -> a % b),
    POWER("Power", Math::pow),
    ATAN2("Atan2", Math::atan2),
    HYPOT("Hypot", Math::hypot);
    
    private String name;
    private BiFunction<Double,Double,Double> function;
    
    private ValuePairFunction(String name,BiFunction<Double,Double,Double> function)
    {
        this.name = name;
        this.function = function;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public BiFunction<Double,Double,Double> getFunction()
    {
        return this.function;
    }
    
    @Override
    public String toString()
    {
        return name;
    }

}
