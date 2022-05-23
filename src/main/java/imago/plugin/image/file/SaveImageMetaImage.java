/**
 * 
 */
package imago.plugin.image.file;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.frames.ImageFrame;
import imago.gui.FramePlugin;
import net.sci.image.Image;
import net.sci.image.io.MetaImageWriter;


/**
 * Save an image to MetaImag file format.
 * 
 * @author David Legland
 *
 */
public class SaveImageMetaImage implements FramePlugin
{
	public SaveImageMetaImage() 
	{
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame iframe = (ImageFrame) frame;
        
        ImageViewer viewer = iframe.getImageView();
        Image image = viewer.getImage();
        
        // create file dialog using last open path
        // create new file dialog
        JFileChooser openWindow = frame.getGui().createOpenFileDialog("Save As MetaImage");
		openWindow.setFileFilter(new FileNameExtensionFilter("MetaImage files (*.mhd, *.mha)", "mhd", "mha"));


		// Open dialog to choose the file
		int ret = openWindow.showSaveDialog(frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		// Check the chosen file is state
		File file = openWindow.getSelectedFile();
		
		if (!file.getName().endsWith(".mhd"))
		{
			file = new File(file.getParent(), file.getName() + ".mhd");
		}
		
		// Create a writer with specified file
		MetaImageWriter writer = new MetaImageWriter(file);
		writer.addAlgoListener(iframe);
		try
		{
			writer.writeImage(image);
		}
		catch(Exception ex)
		{
		    System.err.println(ex);
            ImagoGui.showErrorDialog(frame, ex.getLocalizedMessage(), "MHD Image Export Error");
			return;
		}
		
		System.out.println("save done");
	}

//	private String getLastOpenPath(ImagoFrame frame)
//	{
//		String path = ".";
//		frame.get
//		path = frame.getLastOpenPath();
//		if (path == null || path.isEmpty())
//		{
//			path = ".";
//		}
//		
//		return path;
//	}
	
}
