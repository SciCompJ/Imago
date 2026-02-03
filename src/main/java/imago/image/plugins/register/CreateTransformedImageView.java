/**
 * 
 */
package imago.image.plugins.register;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.transform.TransformHandle;
import net.sci.algo.AlgoStub;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.ScalarArray3D;
import net.sci.array.numeric.UInt8Array;
import net.sci.array.numeric.impl.FunctionViewUInt8Array;
import net.sci.geom.Transform;
import net.sci.geom.geom2d.Transform2D;
import net.sci.geom.geom3d.Transform3D;
import net.sci.image.Calibration;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.image.TransformedImage3D;

/**
 * Creates a view to a transformation of the current image, by choosing a
 * transform stored within the workspace to the current image. Can specify the
 * size of output image, as well as calibration parameters.
 * 
 * @see ApplyExistingTransformToImage
 */
public class CreateTransformedImageView extends AlgoStub implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public CreateTransformedImageView()
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
//        gd.addEnumChoice("Output Type", ScalarOutputTypes.class, ScalarOutputTypes.SAME_AS_INPUT);

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
//        ScalarArray.Factory<?> factory = ((ScalarOutputTypes) gd.getNextEnumChoice()).factory();
//        if (factory == null)
//        {
//            factory = ((ScalarArray<?>) array).factory();
//        }
        
        this.addAlgoListener(imageFrame);
        UInt8Array result;
        if (nd == 2)
        {
            if (!(transform instanceof Transform2D))
            {
                throw new RuntimeException("Requires an instance of Transform2D, not " + transform.getClass().getName());
            }
            
            ScalarArray2D<?> movArray = ScalarArray2D.wrapScalar2d((ScalarArray<?>) array);
            Transform2D transfo2d = (Transform2D) transform;
            TransformedImage2D transformed = new TransformedImage2D(movArray, transfo2d);
            
            result = new FunctionViewUInt8Array(dims, pos -> transformed.evaluate(
                    pos[0] * spacing[0] + origin[0], 
                    pos[1] * spacing[1] + origin[1]));
        }
        else if (nd == 3)
        {
            if (!(transform instanceof Transform3D))
            {
                throw new RuntimeException("Requires an instance of Transform3D, not " + transform.getClass().getName());
            }
            
            ScalarArray3D<?> movArray = ScalarArray3D.wrapScalar3d((ScalarArray<?>) array);
            Transform3D transfo3d = (Transform3D) transform;
            TransformedImage3D transformed = new TransformedImage3D(movArray, transfo3d);
            
            result = new FunctionViewUInt8Array(dims, pos -> transformed.evaluate(
                    pos[0] * spacing[0] + origin[0],
                    pos[1] * spacing[1] + origin[1], 
                    pos[2] * spacing[2] + origin[2]));
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
