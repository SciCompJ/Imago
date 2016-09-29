/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.data.scalarnd.BufferedUInt8ArrayND;
import net.sci.array.data.scalarnd.UInt8ArrayND;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class CreateColorCubeStack extends ImagoAction 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String fileName;
	
	public CreateColorCubeStack(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		// Image dimension
		int width  = 128;
		int height = 128;
		int depth  = 128;
		
		// Create new image data
		byte[] data = new byte[width * height * depth * 3];
		
		// Initialize image data with raster content
		// Use XYZC indexing
		int offset = 0;
		// red channel
		for (int z = 0; z < depth; z++) 
		{
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					byte r = (byte) (x * 2);
					data[offset++] = r;
				}
			}
		}
		// green channel
		for (int z = 0; z < depth; z++) 
		{
			for (int y = 0; y < height; y++) 
			{
				byte g = (byte) (y * 2);
				for (int x = 0; x < width; x++) 
				{
					data[offset++] = g;
				}
			}
		}
		// blue channel
		for (int z = 0; z < depth; z++) 
		{
			byte b = (byte) (z * 2);
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					data[offset++] = b;
				}
			}
		}
		
//		// Initialize image data with raster content
//		// Use CXYZ indexing
//		int offset = 0;
//		for (int z = 0; z < depth; z++) 
//		{
//			byte b = (byte) (z * 2);
//			for (int y = 0; y < height; y++) 
//			{
//				byte g = (byte) (y * 2);
//				for (int x = 0; x < width; x++) 
//				{
//					byte r = (byte) (x * 2);
//					data[offset++] = r;
//					data[offset++] = g;
//					data[offset++] = b;
//				}
//			}
//		}

		UInt8ArrayND img3d = new BufferedUInt8ArrayND(new int[]{width, height, depth, 3}, data);
		
		// create the image
		Image image = new Image(img3d, Image.Type.COLOR);
		image.setName("Color Cube Image");
		
		// add the image document to GUI
		this.gui.addNewDocument(image); 
	}
}
