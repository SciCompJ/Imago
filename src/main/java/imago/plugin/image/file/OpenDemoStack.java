/**
 * 
 */
package imago.plugin.image.file;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.scalar.BufferedUInt8Array3D;
import net.sci.array.scalar.UInt8Array3D;
import net.sci.image.Image;

/**
 * @author David Legland
 *
 */
public class OpenDemoStack implements Plugin
{
    String fileName;
    
    public OpenDemoStack()
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
        // System.out.println("Open demo stack");
        
        // Image dimension
        int width = 128;
        int height = 128;
        int depth = 128;
        
        // Create new image data
        byte[] data = new byte[width * height * depth];
        
        // Initialize image data with raster content
        int offset = 0;
        for (int z = 0; z < depth; z++)
        {
            double z2 = z - 64;
            for (int y = 0; y < height; y++)
            {
                double y2 = y - 64;
                for (int x = 0; x < width; x++)
                {
                    double x2 = x - 64;
                    double sum = Math.max(0, 64 * 64 - (x2 * x2 + y2 * y2 + z2 * z2));
                    // convert between 0 and 255
                    sum = Math.min(4 * Math.sqrt(sum), 255);
                    data[offset++] = (byte) Math.floor(sum);
                }
            }
        }
        
        UInt8Array3D img3d = new BufferedUInt8Array3D(width, height, depth, data);
        
        // create the image
        Image image = new Image(img3d);
        image.setName("Demo Stack");
        
        // add the image document to GUI
        frame.getGui().addNewDocument(image);
    }
    
}
