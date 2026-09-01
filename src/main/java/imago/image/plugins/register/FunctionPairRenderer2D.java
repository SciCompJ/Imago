/**
 * 
 */
package imago.image.plugins.register;

import net.sci.array.Array2D;
import net.sci.array.color.RGB8Array2D;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.interp.ScalarFunction2D;

/**
 * 
 */
public abstract class FunctionPairRenderer2D
{
    int sizeX;
    int sizeY;
    
    protected FunctionPairRenderer2D(int sizeX, int sizeY)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    
    public abstract Array2D<?> combine(ScalarFunction2D fun1, ScalarFunction2D fun2);
    
    
    public static class MagentaGreen extends FunctionPairRenderer2D
    {
        public MagentaGreen(int sizeX, int sizeY)
        {
            super(sizeX, sizeY);
        }

        @Override
        public Array2D<?> combine(ScalarFunction2D fun1, ScalarFunction2D fun2)
        {
            RGB8Array2D res = RGB8Array2D.create(sizeX, sizeY);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double v1 = fun1.evaluate(x, y);
                    double v2 = fun2.evaluate(x, y);
                    res.setIntCode(new int[] {x, y}, combine(v1, v2));
                }
            }
            
            return res;
        }
        
        int combine(double v1, double v2)
        {
            int v1i = Double.isFinite(v1) ? (int) (v1 + 0.5) : 0;
            int v2i = Double.isFinite(v2) ? (int) (v2 + 0.5) : 0;
            return intCode(v1i, v2i);
        }
        
        private static final int intCode(int v1, int v2)
        {
            return (v1 & 0x00FF) << 16 | (v2 & 0x00FF) << 8 | (v1 & 0x00FF); 
        }
    }
    
    private static abstract class ScalarFunctionPairRenderer2D extends FunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        protected ScalarFunctionPairRenderer2D(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY);
            this.factory = factory;
        }

        @Override
        public Array2D<?> combine(ScalarFunction2D fun1, ScalarFunction2D fun2)
        {
            ScalarArray2D<?> res = ScalarArray2D.wrapScalar2d(factory.create(sizeX, sizeY));
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double v1 = fun1.evaluate(x, y);
                    double v2 = fun2.evaluate(x, y);
                    double vres = combine(v1, v2);
                    
                    if (Double.isFinite(vres))
                        res.setValue(x, y, vres);
                    else
                        res.setValue(x, y, 0);
                }
            }
            
            return res;
        }
        
        abstract double combine(double v1, double v2);
    }
    
    public static class CheckerBoard extends FunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        int tileSize = 50;
        
        public CheckerBoard(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public CheckerBoard(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY);
            this.factory = factory;
        }
        
        public CheckerBoard setTileSize(int size)
        {
            this.tileSize = size;
            return this;
        }

        @Override
        public Array2D<?> combine(ScalarFunction2D fun1, ScalarFunction2D fun2)
        {
            ScalarArray2D<?> res = ScalarArray2D.wrapScalar2d(factory.create(sizeX, sizeY));
            
            for (int y = 0; y < sizeY; y++)
            {
                boolean tileY = y / tileSize % 2 == 1;
                
                for (int x = 0; x < sizeX; x++)
                {
                    boolean image1 = tileY ^ (x / tileSize % 2 == 1);
                    if (image1)
                    {
                        res.setValue(x, y, fun1.evaluate(x, y));
                    }
                    else
                    {
                        res.setValue(x, y, fun2.evaluate(x, y));
                    }
                }
            }
            
            return res;
        }
    }
    
    public static class MaxIntensity extends ScalarFunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        public MaxIntensity(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public MaxIntensity(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY, factory);
        }

        @Override
        double combine(double v1, double v2)
        {
            if (Double.isFinite(v1))
            {
                return Double.isFinite(v2) ? Math.max(v1, v2) : v1;
            }
            else
            {
                return Double.isFinite(v2) ? v2 : Double.NaN; 
            }
        }
    }
    
    public static class IntensitySum extends ScalarFunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        public IntensitySum(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public IntensitySum(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY, factory);
        }

        @Override
        double combine(double v1, double v2)
        {
            if (Double.isFinite(v1))
            {
                return Double.isFinite(v2) ? (v1 + v2) : v1;
            }
            else
            {
                return Double.isFinite(v2) ? v2 : Double.NaN; 
            }
        }
    }
    
    public static class AverageIntensity extends ScalarFunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        public AverageIntensity(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public AverageIntensity(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY, factory);
        }

        @Override
        double combine(double v1, double v2)
        {
            if (Double.isFinite(v1))
            {
                return Double.isFinite(v2) ? (v1 + v2) * 0.5 : v1;
            }
            else
            {
                return Double.isFinite(v2) ? v2 : Double.NaN; 
            }
        }
    }
    
    public static class Difference extends ScalarFunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        public Difference(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public Difference(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY, factory);
        }

        @Override
        double combine(double v1, double v2)
        {
            if (Double.isFinite(v1))
            {
                return Double.isFinite(v2) ? v1 - v2 : v1;
            }
            else
            {
                return Double.isFinite(v2) ? v2 : Double.NaN; 
            }
        }
    }
    
    public static class AbsoluteDifference extends ScalarFunctionPairRenderer2D
    {
        ScalarArray.Factory<?> factory;
        
        public AbsoluteDifference(int sizeX, int sizeY)
        {
            this(sizeX, sizeY, UInt8Array.defaultFactory);
        }

        public AbsoluteDifference(int sizeX, int sizeY, ScalarArray.Factory<?> factory)
        {
            super(sizeX, sizeY, factory);
        }

        @Override
        double combine(double v1, double v2)
        {
            if (Double.isFinite(v1))
            {
                return Double.isFinite(v2) ? Math.abs(v1 - v2) : Math.abs(v1);
            }
            else
            {
                return Double.isFinite(v2) ? Math.abs(v2) : Double.NaN; 
            }
        }
    }
}
