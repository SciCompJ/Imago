/**
 * 
 */
package imago.image.plugin.binary;

import imago.gui.FramePlugin;
import imago.gui.GenericDialog;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.binary.BinaryArray;
import net.sci.array.numeric.IntArray;
import net.sci.image.Image;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform2DInt;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DFloat32;
import net.sci.image.binary.distmap.ChamferDistanceTransform3DInt;
import net.sci.image.binary.distmap.ChamferMask2D;
import net.sci.image.binary.distmap.ChamferMask3D;
import net.sci.image.binary.distmap.ChamferMasks2D;
import net.sci.image.binary.distmap.ChamferMasks3D;
import net.sci.image.binary.distmap.DistanceTransform;

/**
 * Distance map to nearest background pixel/voxel, using chamfer distances.
 * 
 * @author David Legland
 *
 */
public class BinaryImageChamferDistanceMap implements FramePlugin
{
    /*
     * (non-Javadoc)
     * 
     * @see imago.gui.Plugin#run(ImagoFrame, String)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        // retrieve current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        // check type of input
        if (!(array instanceof BinaryArray))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        // also check dimensionality
        int nd = array.dimensionality();
        if (nd != 2 && nd != 3)
        {
            frame.showErrorDialog("Can process only 2D and 3D images", "Dimensionality Error");
            return;
        }

        // default values for chamfer masks in 2D and 3D
        ChamferMasks2D mask2d = ChamferMasks2D.CHESSKNIGHT;
        ChamferMasks3D mask3d = ChamferMasks3D.SVENSSON_3_4_5_7;

        // build dialog for choosing options
        GenericDialog gd = new GenericDialog(frame, "Distance Map");
        if (nd == 2)
        {
            gd.addEnumChoice("Chamfer Mask: ", ChamferMasks2D.class, mask2d);
        }
        else if (nd == 3)
        {
            gd.addEnumChoice("Chamfer Mask: ", ChamferMasks3D.class, mask3d);
        }
        gd.addEnumChoice("Output Type: ", DistanceMapDataType.class, DistanceMapDataType.UINT16);
        gd.addCheckBox("Normalize", true);
        gd.showDialog();

        if (gd.wasCanceled())
        {
            return;
        }

        // parse dialog results
        DistanceTransform op = switch (nd)
        {
            case 2 -> {
                mask2d = (ChamferMasks2D) gd.getNextEnumChoice();
                DistanceMapDataType outputType = (DistanceMapDataType) gd.getNextEnumChoice();
                boolean normalize = gd.getNextBoolean();
                yield createAlgorithm(mask2d.getMask(), outputType, normalize);
            }
            case 3 -> {
                mask3d = (ChamferMasks3D) gd.getNextEnumChoice();
                DistanceMapDataType outputType = (DistanceMapDataType) gd.getNextEnumChoice();
                boolean normalize = gd.getNextBoolean();
                yield createAlgorithm(mask3d.getMask(), outputType, normalize);
            }
            default -> throw new RuntimeException("Dimension must be either 2 or 3.");
        };

        // Compute distance map
        Image resultImage = imageFrame.runOperator("Chamfer Distance Map", op, image);
        resultImage.setName(image.getName() + "-dist");

        // add the image document to GUI
        ImageFrame.create(resultImage, frame);
    }

    private static final DistanceTransform createAlgorithm(ChamferMask2D mask,
            DistanceMapDataType outputType, boolean normalize)
    {
        if (!outputType.isIntType())
        {
            return new ChamferDistanceTransform2DFloat32(mask, normalize);
        }
        ChamferDistanceTransform2DInt op = new ChamferDistanceTransform2DInt(mask, normalize);
        op.setFactory((IntArray.Factory<?>) outputType.factory());
        return op;
    }

    private static final DistanceTransform createAlgorithm(ChamferMask3D mask,
            DistanceMapDataType outputType, boolean normalize)
    {
        if (!outputType.isIntType())
        {
            return new ChamferDistanceTransform3DFloat32(mask, normalize);
        }
        ChamferDistanceTransform3DInt op = new ChamferDistanceTransform3DInt(mask, normalize);
        op.setFactory((IntArray.Factory<?>) outputType.factory());
        return op;
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
        if (!(frame instanceof ImageFrame))
            return false;
        
        // check image
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        if (image == null)
            return false;

        return image.isBinaryImage();
    }
}
