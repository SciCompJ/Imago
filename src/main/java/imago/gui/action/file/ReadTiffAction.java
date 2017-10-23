/**
 * 
 */
package imago.gui.action.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import net.sci.array.Array;
import net.sci.array.data.BinaryArray;
import net.sci.array.data.UInt8Array;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;


/**
 * @author David Legland
 *
 */
public class ReadTiffAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFileChooser openWindow = null;

	public ReadTiffAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		// create file dialog uqsing last open path
		String lastPath = getLastOpenPath();
		openWindow = new JFileChooser(lastPath);
		openWindow.setFileFilter(new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff"));


		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(this.frame);
		if (ret != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		// Check the chosen file is state
		File file = openWindow.getSelectedFile();
		if (!file.isFile()) 
		{
			return;
		}

		// eventually keep path for future opening
		String path = file.getPath();
		lastPath = this.frame.getLastOpenPath();
		if (lastPath == null || lastPath.isEmpty())
		{
			System.out.println("update frame path");
			this.frame.setLastOpenPath(path);
		}
		
		// Create a Tiff reader with the chosen file
		TiffImageReader reader;
		try 
		{
			reader = new TiffImageReader(file);
		}
		catch (IOException ex) 
		{
			System.err.println(ex);
			return;
		}
		
		// Try to read the image from the file
		Image image;
		try
		{
			image = reader.readImage();
			reader.close();
		} 
		catch (IOException ex)
		{
			System.err.println(ex);
			return;
		} 
		catch (Exception ex)
		{
			System.err.println(ex);
			return;
		}
		
        // If image data contains only two different values, convert to binary
        image = eventuallyConvertToBinary(image);
		 
//		// If image is indexed, convert to true RGB
//		if (image.getColorMap() != null)
//		{
//			int dim = image.getDimension();
//			int[][] map = image.getColorMap();
//			
//			switch (dim)
//			{
//			case 2:
//				// Convert indexed 2D image to 2D RGB image
//				UInt8Array3D img3d = (UInt8Array3D) image.getData();
//				
//				RGB8Image2D rgb2d = new ByteBufferedRGB8Image2D(img2d.getSize(0), img2d.getSize(1));
//				image = new Image(rgb2d, image);
//				image.setColorMap(map);
//				break;
//				
//			case 3:
//				// Convert indexed 3D image to 3D RGB image
//				RGB8Image3D rgb3d = new ByteBufferedRGB8Image3D(img3d.getSize(0), img3d.getSize(1), img3d.getSize(2));
//				image = new Image(rgb3d, image);
//				image.setColorMap(map);
//				break;
//
//			default:
//				throw new RuntimeException("Unknown image dimension: "
//						+ image.getDimension());
//			}
//		}
		
		image.setName(file.getName());
		
		// add the image document to GUI
		ImagoDocViewer frame = this.gui.addNewDocument(image);
		frame.setLastOpenPath(path);
	}

	private String getLastOpenPath()
	{
		String path = ".";
		path = this.frame.getLastOpenPath();
		if (path == null || path.isEmpty())
		{
			path = ".";
		}
		
		return path;
	}
	
	private Image eventuallyConvertToBinary(Image image)
	{
	    // if image is already binary, nothing to do
	    if (image.getType() == Image.Type.BINARY)
	    {
	        return image;
	    }
	    
        // if data array is not of UInt8 class, can not be binary
	    Array<?> data = image.getData();
	    if (!(data instanceof UInt8Array))
	    {
	        return image;
	    }
	    
        // check if data can be binary
        if (!canBeBinary((UInt8Array) data))
        {
            return image;
        }
        
        // convert UInt8 to binary
        BinaryArray binData = BinaryArray.convert((UInt8Array) data);
        
        // convert to binary image
        return new Image(binData, image);
	}
	
	private boolean canBeBinary(UInt8Array data)
	{
        boolean has1 = false;
        boolean has255 = false;
        UInt8Array.Iterator iter = data.iterator();
        while(iter.hasNext())
        {
            int value = iter.nextInt();
            if (value == 1)
            {
                has1 = true;
            }
            else if (value == 255)
            {
                has255 = true;
            }
            else if (value != 0)
            {
                return false;
            }
        }
        
        return !has1 || !has255;
	}
}
