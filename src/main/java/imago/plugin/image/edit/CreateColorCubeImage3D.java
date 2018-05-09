/**
 * 
 */
package imago.plugin.image.edit;

import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.array.data.color.RGB8Array3D;
import net.sci.array.type.RGB8;
import net.sci.image.Image;


/**
 * @author David Legland
 *
 */
public class CreateColorCubeImage3D implements Plugin
{
	public CreateColorCubeImage3D() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// Image dimension
		int sizeX = 128;
		int sizeY = 128;
		int sizeZ = 128;
	
		// Initialize image data from voxel coordinates
		RGB8Array3D rgb3d = RGB8Array3D.create(sizeX, sizeY, sizeZ);
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					rgb3d.set(x, y, z, new RGB8(x * 2, y * 2, z * 2));
				}
			}
		}

		// create the image
		Image image = new Image(rgb3d, Image.Type.COLOR);
		image.setName("Color Cube Image");
		
		// add the image document to GUI
		frame.getGui().addNewDocument(image); 
	}
}
