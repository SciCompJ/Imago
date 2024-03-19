/**
 * 
 */
package imago.plugin.image.process.options;

import java.util.function.BiFunction;

/**
 * A collection of common math functions taking two floating point values as
 * argument and returning a floating point value as output.
 */
public enum ValuePairFunction
{
    PLUS("Plus", (a, b) -> a + b),
    MINUS("Minus", (a,b) -> a - b),
    MULTIPLY("Multiply", (a,b) -> a * b),
    DIVIDE("Divide", (a,b) -> a / b),
    DIFFERENCE("Difference", (a,b) -> Math.abs(a - b)),
    MIN("Minimum", java.lang.Math::min),
    MAX("Maximum", java.lang.Math::max),
    MODULO("Modulo", (a,b) -> a % b),
    POWER("Power", java.lang.Math::pow);
    
    private String name;
    private BiFunction<Double,Double,Double> function;
    
    private ValuePairFunction(String name,BiFunction<Double,Double,Double> function)
    {
        this.name = name;
        this.function = function;
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
