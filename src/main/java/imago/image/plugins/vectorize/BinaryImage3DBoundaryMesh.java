/**
 * 
 */
package imago.image.plugins.vectorize;

import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.mesh3d.Mesh3DViewer;
import imago.gui.FramePlugin;
import net.sci.array.Array;
import net.sci.array.binary.Binary;
import net.sci.array.binary.BinaryArray;
import net.sci.array.binary.BinaryArray3D;
import net.sci.geom.geom3d.Point3D;
import net.sci.geom.mesh3d.Mesh3D;
import net.sci.image.Image;

/**
 * Compute boundary quad mesh of a 3D binary image.
 * 
 * @author David Legland
 *
 */
public class BinaryImage3DBoundaryMesh implements FramePlugin
{
    public BinaryImage3DBoundaryMesh()
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
        // get current image data
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
        Image image = handle.getImage();
        Array<?> array = image.getData();
        if (!array.elementInstanceOf(Binary.class))
        {
            frame.showErrorDialog("Requires a binary image input", "Data Type Error");
            return;
        }

        int nd = array.dimensionality();
        if (nd != 3)
        {
            frame.showErrorDialog("Can process only 3D", "Dimensionality Error");
            return;
        }

        // wrap into a binary 3D
        BinaryArray3D binary = BinaryArray3D.wrap(BinaryArray.wrap(array));

        // compute the boundary mesh
        Mesh3D mesh = new net.sci.image.vectorize.BinaryImage3DBoundaryMesh().process(binary);

        //
        ImagoGui gui = frame.getGui();
        Mesh3DViewer newFrame = new Mesh3DViewer(gui, mesh);
        Point3D imageCenter = Point3D.of(array.size(0) * 0.5, array.size(1) * 0.5, array.size(2) * 0.5);
        newFrame.getRenderer().setLookAt(imageCenter);
        gui.updateFrameLocation(newFrame, frame);
        newFrame.setVisible(true);
    }

    @Override
    public boolean isEnabled(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame imageFrame)
        {
            Image image = imageFrame.getImageHandle().getImage();
            if (image == null) return false;

            return image.isBinaryImage() && image.getDimension() == 3;
        }
        
        return false;
    }
}
