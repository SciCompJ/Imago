/**
 * 
 */
package imago.image.plugins.process;

import java.util.function.Function;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.plugins.process.options.LogicalOperator;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.Scalar;
import net.sci.array.numeric.ScalarArray;
import net.sci.image.Image;

/**
 * Applies a relational operator ("equals", "larger than"...) to a an image and
 * a value, resulting in a binary image.
 * 
 * @author David Legland
 *
 * @see ImageApplyMathBinaryOperator
 */
public class ImageAndValueRelationalOperator implements FramePlugin
{
    /**
     * Default empty constructor.
     */
    public ImageAndValueRelationalOperator()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        String[] imageNames = ImageHandle.getAllNames(frame.getGui().getAppli()).toArray(new String[] {});
        int index = 0;
        if (frame instanceof ImageFrame)
        {
            String imageName = ((ImageFrame) frame).getImageHandle().getName();
            index = findStringIndex(imageName, imageNames);
        }

        GenericDialog gd = new GenericDialog(frame, "Math Binary Operator");
        gd.addChoice("Image", imageNames, imageNames[index]);
        gd.addEnumChoice("Operation", LogicalOperator.class, LogicalOperator.GREATER_THAN);
        gd.addNumericField("Value", 100.0, 2, "The scalar value to use as operand");
        gd.showDialog();

        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        { return; }

        // parse dialog results
        String imageName = gd.getNextChoice();
        LogicalOperator op = (LogicalOperator) gd.getNextEnumChoice();
        double value = gd.getNextNumber();
        System.out.println("value = " + value);

        // identify source image
        Image image = ImageHandle.findFromName(frame.getGui().getAppli(), imageName).getImage();

        // transform binary operator into unary operator by fixing constant
        Function<Double, Boolean> fun = (x) -> op.getFunction().apply(x, value);

        // extract arrays
        Array<?> array = image.getData();
        if (!array.elementInstanceOf(Scalar.class))
        {
            ImagoGui.showErrorDialog(frame, "Input image must contain scalar values");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ScalarArray scalarArray = ScalarArray.wrap((Array<Scalar>) array);

        BinaryArray res = BinaryArray.create(array.size());

        // compute result
        long t0 = System.nanoTime();
        res.fillBooleans(pos -> fun.apply(scalarArray.getValue(pos)));
        long t1 = System.nanoTime();

        // display elapsed time
        if (frame instanceof ImageFrame)
        {
            double dt = (t1 - t0) / 1_000_000.0;
            ((ImageFrame) frame).showElapsedTime(op.getName(), dt, image);
        }

        // create and display result image
        Image resultImage = new Image(res, image);
        resultImage.setName(String.format("(%s)_%s_%s", image.getName(), op.getShortName(), Double.toString(value)));

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

    private int findStringIndex(String string, String[] array)
    {
        if (string == null)
        { return 0; }

        for (int i = 0; i < array.length; i++)
        {
            if (string.equals(array[i]))
            { return i; }
        }

        return 0;
    }

    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame)) return false;

        return true;
    }
}
