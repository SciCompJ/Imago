/**
 * 
 */
package imago.image.plugin.process.options;

import java.util.function.UnaryOperator;

/**
 * A collection of common math functions taking a single floating point values as
 * argument and returning a floating point value as output.
 * 
 * @see ValuePairFunction
 */
public enum SingleValueFunction
{
    ABS("Abs", Math::abs),
    SIGN("Sign", Math::signum),
    ROUND("Round", Math::rint),
    FLOOR("Floor", Math::floor),
    CEIL("Ceil", Math::ceil),
    SQRT("Sqrt", Math::sqrt),
    LOG("Log", Math::log),
    LOG2("Log2", (x) -> Math.log(x) / Math.log(2.0)),
    LOG10("Log10", Math::log10),
    LOG1P("Log1p", Math::log1p),
    EXP("Exp", Math::exp),
    COS("Cos", Math::cos),
    SIN("Sin", Math::sin),
    TAN("Tan", Math::tan),
    ACOS("ArcCos", Math::acos),
    ASIN("ArcSin", Math::asin),
    ATAN("ArcTan", Math::atan);
    
    private String name;
    private UnaryOperator<Double> function;
    
    private SingleValueFunction(String name, UnaryOperator<Double> function)
    {
        this.name = name;
        this.function = function;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public UnaryOperator<Double> getFunction()
    {
        return this.function;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
