/**
 * 
 */
package imago.plugin.image.convert;

import imago.app.ImageHandle;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.array.Array;
import net.sci.array.color.Color;
import net.sci.array.color.RGB8Array;
import net.sci.array.scalar.UInt8Array;
import net.sci.image.ColorMap;
import net.sci.image.Image;

/**
 * Convert a 8-bits image to 24 bits RGB using current LUT.
 * 
 * @author dlegland
 *
 */
public class ConvertUInt8ImageToRGB implements Plugin
{

    /**
     * 
     */
    public ConvertUInt8ImageToRGB()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void run(ImagoFrame frame, String args)
    {
        System.out.println("convert uint8 image to rgb8 image");
        
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        
        if (image == null)
        {
            return;
        }
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(array instanceof UInt8Array))
        {
            ImagoGui.showErrorDialog(frame, "Requires a UInt8 image", "Data Type Error");
            return;
        }
        UInt8Array uint8Array = (UInt8Array) array;
        
        ColorMap colorMap = image.getDisplaySettings().getColorMap();

        RGB8Array rgb = RGB8Array.create(array.size());
        for (int[] pos : rgb.positions())
        {
            int index = uint8Array.getInt(pos);
            Color color = colorMap.getColor(index);
            int r = (int) Math.round(255 * color.red());
            int g = (int) Math.round(255 * color.green());
            int b = (int) Math.round(255 * color.blue());
            rgb.setSamples(pos, new int[]{r, g, b});
        }

        Image rgbImage = new Image(rgb, image);
                
        // add the image document to GUI
        frame.getGui().createImageFrame(rgbImage); 
    }
}