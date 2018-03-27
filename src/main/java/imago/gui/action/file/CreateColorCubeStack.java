/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.data.color.RGB8Array3D;
import net.sci.array.type.RGB8;
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
		this.gui.addNewDocument(image); 
	}
}
