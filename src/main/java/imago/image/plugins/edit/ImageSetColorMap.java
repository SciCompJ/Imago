/**
 * 
 */
package imago.image.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageDataRenderer;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import imago.image.render.BinaryImageRenderer;
import imago.image.render.IndexedColorMapImageRenderer;
import net.sci.array.color.ColorMap;

/**
 * Choose the colormap of the current scalar image, by selecting one of the
 * colormaps managed by the application.
 * 
 * @see ImageSetColorMapFactory
 */
public class ImageSetColorMap implements FramePlugin
{
    String colorMapName;
    
	public ImageSetColorMap(String colorMapName)
	{
		this.colorMapName = colorMapName;
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
	    if (frame instanceof ImageFrame iframe)
	    {
	        ImageHandle handle = iframe.getImageHandle();

	        ImagoApp app = frame.getGui().getAppli();
	        ColorMap colorMap = app.getColorMap(colorMapName);
	        if (colorMap == null)
	        {
	            ImagoGui.showErrorDialog(frame, "No Color map found with name: " + colorMapName, "ColorMap Error");
	            return;
	        }
	        
	        // update display settings of current frame
	        iframe.getImageViewer().getDisplaySettings().setColorMap(colorMap);
	        
	        // also updates the renderer
	        ImageDataRenderer renderer = iframe.getImageViewer().getRenderer();
	        switch (renderer)
	        {
	            case BinaryImageRenderer r -> r.setColorMap(colorMap);
	            case IndexedColorMapImageRenderer r -> r.setColorMap(colorMap);
	            default -> throw new IllegalArgumentException("Unexpected value: " + renderer);
	        }

	        // notify changes
	        handle.notifyImageHandleChange(ImageHandle.Event.LUT_MASK | ImageHandle.Event.CHANGE_MASK);
	    }
	}
}
