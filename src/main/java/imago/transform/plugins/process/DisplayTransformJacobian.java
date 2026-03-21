/**
 * 
 */
package imago.transform.plugins.process;

import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.plugins.register.CreateTransformedImageView;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.plugins.TransformManagerPlugin;
import net.sci.algo.AlgoStub;
import net.sci.array.numeric.Float32Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.geom.Transform;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Transform2D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.image.ImageType;

/**
 * Display the determinant of the Jacobian of the selected transform as a new
 * image.
 * 
 * @see CreateTransformedImageView
 */
public class DisplayTransformJacobian extends AlgoStub implements TransformManagerPlugin
{
    /**
     * Default empty constructor.
     */
    public DisplayTransformJacobian()
    {
    }

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
        
        TransformHandle handle = tm.getSelectedHandle();
        if (handle == null) return;
        
        Transform transfo = handle.getTransform();
        int nd = transfo.dimensionality();
        if (nd != 2 && nd != 3)
        {
            frame.showErrorDialog("Can only process transforms of dimension 2 and 3", 
                    "Dimensionality error");
            return;
        }
        
        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Apply Transform");
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Result Size dim. " + (d+1), 100, 0);
        }
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Spacing dim. " + (d+1), 1.0, 2);
        }
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Origin dim. " + (d+1), 0.0, 2);
        }
        gd.addCheckBox("Compute log", true);
        
        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        int[] dims = new int[nd];
        for (int d = 0; d < nd; d++)
        {
            dims[d] = (int) gd.getNextNumber();
        }
        double[] spacing = new double[nd];
        for (int d = 0; d < nd; d++)
        {
            spacing[d] = gd.getNextNumber();
        }
        double[] origin = new double[nd];
        for (int d = 0; d < nd; d++)
        {
            origin[d] = gd.getNextNumber();
        }
        boolean computeLog = gd.getNextBoolean();
        
        
        double minVal = Double.POSITIVE_INFINITY;
        double maxVal = Double.NEGATIVE_INFINITY;
        
        ScalarArray<?> result = Float32Array.defaultFactory.create(dims);
        if (nd == 2)
        {
            if (!(transfo instanceof Transform2D))
            {
                throw new RuntimeException("Requires an instance of Transform2D, not " + transfo.getClass().getName());
            }
            
            ScalarArray2D<?> res2d = ScalarArray2D.wrapScalar2d(result);
            Transform2D transfo2d = (Transform2D) transfo;
            
            System.out.println("compute");
            int sizeX = dims[0];
            int sizeY = dims[1];
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double x2 = x * spacing[0] + origin[0];
                    double y2 = y * spacing[1] + origin[1];
                    double v = det2(transfo2d.jacobian(new Point2D(x2, y2)));
                    res2d.setValue(x, y, v);
                    
                    minVal = Math.min(minVal, v);
                    maxVal = Math.max(maxVal, v);
                }
            }
        }
        else
        {
            throw new RuntimeException("Can not process transforms with dimension " + nd);
        }
        
        if (computeLog)
        {
            System.out.println("convert to log");
            
            // reset computation of min/max values
            minVal = Double.POSITIVE_INFINITY;
            maxVal = Double.NEGATIVE_INFINITY;
            
            for (int[] pos : result.positions())
            {
                double v = result.getValue(pos);
                if (v > 0)
                {
                    v = Math.log(v);
                    result.setValue(pos, v);
                    
                    minVal = Math.min(minVal, v);
                    maxVal = Math.max(maxVal, v);
                }
                else
                {
                    result.setValue(pos, Double.NaN);
                }
            }
        }
        
        // create and setup new image
        Image resultImage = new Image(result, computeLog ? ImageType.DIVERGING : ImageType.INTENSITY);
        resultImage.setName(handle.getName() + "-jacobian");
        Calibration calib = new Calibration(nd);
        calib.setSpatialCalibration(spacing, origin, "");
        
        double[] displayRange;
        if (computeLog)
        {
            double maxAbs = Math.max(Math.abs(minVal), Math.abs(maxVal));
            displayRange = new double[]{-maxAbs, maxAbs};
        }
        else
        {
            displayRange = new double[]{minVal, maxVal};
        }
        resultImage.getDisplaySettings().setDisplayRange(displayRange);
        
        
        // display into new frame
        ImageFrame.create(resultImage, frame);
    }
    
    private static final double det2(double[][] mat)
    {
        return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
    }
}
