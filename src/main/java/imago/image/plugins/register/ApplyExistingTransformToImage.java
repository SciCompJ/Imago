/**
 * 
 */
package imago.image.plugins.register;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.plugins.process.ScalarOutputTypes;
import imago.transform.TransformHandle;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.geom.Transform;
import net.sci.geom.geom2d.Transform2D;
import net.sci.geom.geom3d.Transform3D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.image.TransformedImage3D;

/**
 * Applies a transform stored within the workspace to the current image. Can
 * specify the size of output image, as well as calibration parameters.
 */
public class ApplyExistingTransformToImage extends AlgoStub implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ApplyExistingTransformToImage()
    {
    }

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        
        Array<?> array = image.getData();
        if (!(array instanceof ScalarArray))
        {
            frame.showErrorDialog("Can only process images containing scalar data", 
                    "Data Type error");
            return;
        }
        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            frame.showErrorDialog("Can only process images of dimension 2 and 3", 
                    "Dimensionality error");
            return;
        }
        
        // retrieve list of transforms
        ImagoApp app = frame.getGui().getAppli();
        String[] names = TransformHandle.getAllNames(app).toArray(String[]::new);
        if (names.length == 0)
        {
            frame.showErrorDialog("No Transform found,\n please create or load a transform", 
                    "No available transform");
            return;
        }
        
        // create a dialog for the user to choose options
        GenericDialog gd = new GenericDialog(frame, "Apply Transform");
        gd.addChoice("Transform", names, names[0]);
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Result Size dim. " + (d+1), array.size(d), 0);
        }
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Spacing dim. " + (d+1), 1.0, 2);
        }
        for (int d = 0; d < nd; d++)
        {
            gd.addNumericField("Origin dim. " + (d+1), 0.0, 2);
        }
        gd.addEnumChoice("Output Type", ScalarOutputTypes.class, ScalarOutputTypes.SAME_AS_INPUT);

        // wait the user to choose
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL) 
        {
            return;
        }
        
        // parse dialog results
        String transformName = gd.getNextChoice();
        Transform transform = TransformHandle.findFromName(app, transformName).getTransform();
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
        ScalarArray.Factory<?> factory = ((ScalarOutputTypes) gd.getNextEnumChoice()).factory();
        if (factory == null)
        {
            factory = ((ScalarArray<?>) array).factory();
        }
        
        this.addAlgoListener(imageFrame);
        ScalarArray<?> result = factory.create(dims);
        if (nd == 2)
        {
            if (!(transform instanceof Transform2D))
            {
                throw new RuntimeException("Requires an instance of Transform2D, not " + transform.getClass().getName());
            }
            
            ScalarArray2D<?> movArray = ScalarArray2D.wrapScalar2d((ScalarArray<?>) array);
            ScalarArray2D<?> res2d = ScalarArray2D.wrapScalar2d(result);
            Transform2D transfo2d = (Transform2D) transform;
            TransformedImage2D transformed = new TransformedImage2D(movArray, transfo2d);
            
            int sizeX = dims[0];
            int sizeY = dims[1];
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double x2 = x * spacing[0] + origin[0];
                    double y2 = y * spacing[1] + origin[1];
                    res2d.setValue(x, y, transformed.evaluate(x2, y2));
                }
            }
        }
        else if (nd == 3)
        {
            if (!(transform instanceof Transform3D))
            {
                throw new RuntimeException("Requires an instance of Transform3D, not " + transform.getClass().getName());
            }
            
            ScalarArray3D<?> movArray = ScalarArray3D.wrapScalar3d((ScalarArray<?>) array);
            ScalarArray3D<?> res3d = ScalarArray3D.wrapScalar3d(result);
            Transform3D transfo3d = (Transform3D) transform;
            TransformedImage3D transformed = new TransformedImage3D(movArray, transfo3d);
            
            int sizeX = dims[0];
            int sizeY = dims[1];
            int sizeZ = dims[2];
            for (int z = 0; z < sizeZ; z++)
            {
                this.fireProgressChanged(this, z, sizeZ);
                for (int y = 0; y < sizeY; y++)
                {
                    for (int x = 0; x < sizeX; x++)
                    {
                        double x2 = x * spacing[0] + origin[0];
                        double y2 = y * spacing[1] + origin[1];
                        double z2 = z * spacing[2] + origin[2];
                        res3d.setValue(x, y, z, transformed.evaluate(x2, y2, z2));
                    }
                }
            }
        }
        else
        {
            throw new RuntimeException("Can not process images with dimension " + nd);
        }
        
        // create and setup new image
        Image resultImage = new Image(result, image.getType());
        resultImage.setName(image.getName() + "-transfo");
        Calibration calib = resultImage.getCalibration();
        calib.setSpatialCalibration(spacing, origin, "");
        
        // display into new frame
        ImageFrame.create(resultImage, frame);
    }
}
