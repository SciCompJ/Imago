/**
 * 
 */
package imago.image.plugin.binary;

import java.awt.Color;
import java.util.ArrayList;

import imago.app.ImagoApp;
import imago.app.shape.Shape;
import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.shapemanager.GeometryHandle;
import imago.shapemanager.ShapeManager;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray2D;
import net.sci.geom.geom2d.curve.MultiCurve2D;
import net.sci.geom.polygon2d.Polyline2D;
import net.sci.image.Image;
import net.sci.image.ImageType;
import net.sci.image.vectorize.BinaryImage2DChangComponentsLabeling;

/**
 * 
 */
public class BinaryImageComponentsLabelingAndContouring implements FramePlugin
{
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        ImageFrame imageFrame = (ImageFrame) frame;

        // retrieve image data
        ImageHandle handle = imageFrame.getImageHandle(); 
        Image image = handle.getImage();
        Array<?> array = image.getData();
        if (!(array instanceof BinaryArray))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        // check image dimensionality
        int nd = array.dimensionality();
        if (nd != 2)
        {
            frame.showErrorDialog("Can process only 2D images", "Dimensionality Error");
            return;
        }

        GenericDialog gd = new GenericDialog(frame, "Chang Components Labeling");
        gd.addCheckBox("Create Label Map", true);
        String[] contourOptions = new String[] { "Do not Display", "Overlay to Image", "Add to Shape Manager" };
        gd.addChoice("Contours: ", contourOptions, contourOptions[1]);
        gd.showDialog();
        if (gd.getOutput() == GenericDialog.Output.CANCEL)
        {
            return;
        }

        // parse dialog results
        boolean showLabelMap = gd.getNextBoolean();
        int contourDisplayIndex = gd.getNextChoiceIndex();
//        int bitDepthIndex = gd.getNextChoiceIndex();
//        IntArray.Factory<?> factory = switch (bitDepthIndex)
//        {
//            case 0 -> UInt8Array.defaultFactory;
//            case 1 -> UInt16Array.defaultFactory;
//            case 2 -> Int32Array.defaultFactory;
//            case 3 -> new RunLengthInt32ArrayFactory();
//            default -> throw new IllegalArgumentException("Bit depth index out of range");
//        };

        BinaryImage2DChangComponentsLabeling algo = new BinaryImage2DChangComponentsLabeling();
        BinaryArray2D binaryArray = BinaryArray2D.wrap(BinaryArray.wrap(array));
        
        algo.addAlgoListener(imageFrame);
        long t0 = System.nanoTime();
        BinaryImage2DChangComponentsLabeling.Result res = algo.getResult(binaryArray);
        long t1 = System.nanoTime();
        imageFrame.showElapsedTime("Components Labeling & Countouring", (t1 - t0) / 1_000_000.0, image);
        
        if (showLabelMap)
        {
            Image labelMapImage = new Image(res.labelMap, ImageType.LABEL, image);
            labelMapImage.setName(image.getName() + "-lbl");
            
            // add the image document to GUI
            ImageFrame.create(labelMapImage, frame);
        }

        switch(contourDisplayIndex)
        {
            case 1 -> 
            {
                for (Polyline2D poly : res.outerContours)
                {
                    Shape shape = new Shape(poly);
                    shape.setColor(Color.BLUE);
                    handle.addShape(shape);
                }
                for (Polyline2D poly : res.innerContours)
                {
                    Shape shape = new Shape(poly);
                    shape.setColor(Color.GREEN);
                    handle.addShape(shape);
                }
                handle.notifyImageHandleChange();
            }
            case 2 ->
            {
                ArrayList<Polyline2D> allContours = new ArrayList<Polyline2D>(); 
                allContours.addAll(res.outerContours);
                allContours.addAll(res.innerContours);
                MultiCurve2D contourSet = new MultiCurve2D(allContours);
                
                ImagoApp app = frame.getGui().getAppli();
                GeometryHandle geomHandle = GeometryHandle.create(app, contourSet);
                
                // opens a dialog to choose name
                String name = image.getName() + "-contours";
                name = ImagoGui.showInputDialog(frame, "Name of new geometry:", "Choose Geometry Name", name);
                geomHandle.setName(name);
                
                // ensure ShapeManager is visible
                ShapeManager manager = ShapeManager.getInstance(frame.getGui());
                manager.repaint();
                manager.setVisible(true);
            }
        }
    }
    
    /**
     * Returns true if the current frame contains a binary image.
     * 
     * @param frame
     *            the frame from which the plugin will be called
     * @return true if the frame contains a binary image
     */
    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        // check frame class
        if (!(frame instanceof ImageFrame)) return false;

        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null) return false;

        return image.isBinaryImage();
    }
}
