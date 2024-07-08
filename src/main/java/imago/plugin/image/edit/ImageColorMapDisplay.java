/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.image.ImageFrame;
import imago.gui.table.TableFrame;
import net.sci.array.color.Color;
import net.sci.array.color.ColorMap;
import net.sci.image.Image;
import net.sci.table.Table;


/**
 * @author David Legland
 *
 */
public class ImageColorMapDisplay implements FramePlugin
{
    public ImageColorMapDisplay()
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
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        Image image = iframe.getImageHandle().getImage();
        
        // retrieve image color map
        ColorMap colorMap = image.getDisplaySettings().getColorMap();
        if (colorMap == null)
        {
            frame.showErrorDialog("The current image is not associated to a colormap", "Image Error");
            return;
        }
        
        // convert colormap to table
        int nColors = colorMap.size();
        Table table = Table.create(nColors, 3);
        table.setColumnNames(new String[] {"Red", "Green", "Blue"});
        for (int i = 0; i < nColors; i++)
        {
            Color color = colorMap.getColor(i);
            table.setValue(i, 0, (int) (color.red() * 255));
            table.setValue(i, 1, (int) (color.green() * 255));
            table.setValue(i, 2, (int) (color.blue() * 255));
        }
        
        table.setName(image.getName() + "-colormap");
        
        // add the new frame to the GUI
        TableFrame.create(table, frame);
	}
	
	/**
     * Returns true if this frame is an instance of ImageFrame, and the image it
     * contains has a color map.
     * 
     * @return true if this frame is an instance of ImageFrame, and the image it
     *         contains has a color map.
     */
	@Override
    public boolean isEnabled(ImagoFrame frame)
    {
	    if (!(frame instanceof ImageFrame)) return false;
	    Image image = ((ImageFrame) frame).getImageHandle().getImage();
        return image.getDisplaySettings().getColorMap() != null;
    }
}
