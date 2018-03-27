/**
 * 
 */
package imago.gui.action.file;

import imago.gui.ImagoAction;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sci.image.Image;
import net.sci.image.io.MetaImageReader;

/**
 * @author David Legland
 *
 */
public class ImportMetaImageFileAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JFileChooser openWindow = null;

	public ImportMetaImageFileAction(ImagoFrame frame, String name)
	{
		super(frame, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// create file dialog if it doesn't exist
		if (openWindow == null)
		{
			openWindow = new JFileChooser(".");
			openWindow.setFileFilter(new FileNameExtensionFilter("MetaImage files (*.mhd, *.mha)", "mhd", "mha"));
		}

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(this.frame);
		if (ret != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		// Check the chosen file is value
		File file = openWindow.getSelectedFile();
		if (!file.isFile())
		{
			return;
		}

		// Create a MetaImage Format reader with the chosen file
		MetaImageReader reader;
		try
		{
			reader = new MetaImageReader(file);
		} catch (IOException ex)
		{
			System.err.println(ex);
			return;
		}

		// Try to read the image from the file
		Image image;
		try
		{
			image = reader.readImage();
		} catch (IOException ex)
		{
			System.err.println(ex);
			return;
		} catch (Exception ex)
		{
			System.err.println(ex);
			return;
		}

		// // If image is indexed, convert to true RGB
		// if (image.getColorMap() != null) {
		// int dim = image.getDimension();
		// switch (dim) {
		// case 2:
		// // Convert indexed 2D image to 2D RGB image
		// Gray8Image2D img2d = (Gray8Image2D) image.getImage();
		// RGB8Image2D rgb2d = RGB8Image2DByteBuffer.create(img2d,
		// image.getColorMap());
		// image = new MetaImage(rgb2d, image);
		// break;
		//
		// case 3:
		// // Convert indexed 3D image to 3D RGB image
		// Gray8Image3D img3d = (Gray8Image3D) image.getImage();
		// RGB8Image3D rgb3d = RGB8Image3DByteBuffer.create(img3d,
		// image.getColorMap());
		// image = new MetaImage(rgb3d, image);
		// break;
		//
		// default:
		// throw new RuntimeException("Unknown image dimension: "
		// + image.getDimension());
		// }
		// }

		image.setName(file.getName());

		// add the image document to GUI
		this.gui.addNewDocument(image);
	}

}
