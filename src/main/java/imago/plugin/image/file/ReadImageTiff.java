/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;


/**
 * @author David Legland
 *
 */
public class ReadImageTiff implements Plugin
{
	private JFileChooser openWindow = null;

	public ReadImageTiff() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		// create file dialog uqsing last open path
		String lastPath = getLastOpenPath(frame);
		openWindow = new JFileChooser(lastPath);
		openWindow.setFileFilter(new FileNameExtensionFilter("TIFF files (*.tif, *.tiff)", "tif", "tiff"));


		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(frame.getWidget());
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
		lastPath = frame.getLastOpenPath();
		if (lastPath == null || lastPath.isEmpty())
		{
			System.out.println("update frame path");
			frame.setLastOpenPath(path);
		}
		
		// Create a Tiff reader with the chosen file
		TiffImageReader reader;
		try 
		{
			reader = new TiffImageReader(file);
		}
		catch (Exception ex) 
		{
		    System.err.println(ex);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
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
			ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
			return;
		} 
		catch (Exception ex)
		{
            ex.printStackTrace();
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "TIFF Image Reading Error");
            return;
		}
		
//        // If image data contains only two different values, convert to binary
//        image = eventuallyConvertToBinary(image);
		 
        // populates some meta-data
		image.setName(file.getName());
		
		// add the image document to GUI
		ImageFrame newFrame = frame.getGui().addNewDocument(image);
		newFrame.setLastOpenPath(path);
	}

	private String getLastOpenPath(ImagoFrame frame)
	{
		String path = ".";
		path = frame.getLastOpenPath();
		if (path == null || path.isEmpty())
		{
			path = ".";
		}
		
		return path;
	}
	
//	private Image eventuallyConvertToBinary(Image image)
//	{
//	    // if image is already binary, nothing to do
//	    if (image.getType() == Image.Type.BINARY)
//	    {
//	        return image;
//	    }
//	    
//        // if data array is not of UInt8 class, can not be binary
//	    Array<?> data = image.getData();
//	    if (!(data instanceof UInt8Array))
//	    {
//	        return image;
//	    }
//	    
//        // check if data can be binary
//        if (!canBeBinary((UInt8Array) data))
//        {
//            return image;
//        }
//        
//        // convert UInt8 to binary
//        BinaryArray binData = BinaryArray.convert((UInt8Array) data);
//        
//        // convert to binary image
//        return new Image(binData, image);
//	}
//	
//	private boolean canBeBinary(UInt8Array data)
//	{
//        boolean has1 = false;
//        boolean has255 = false;
//        UInt8Array.Iterator iter = data.iterator();
//        while(iter.hasNext())
//        {
//            int value = iter.nextInt();
//            if (value == 1)
//            {
//                has1 = true;
//            }
//            else if (value == 255)
//            {
//                has255 = true;
//            }
//            else if (value != 0)
//            {
//                return false;
//            }
//        }
//        
//        return !has1 || !has255;
//	}
}
