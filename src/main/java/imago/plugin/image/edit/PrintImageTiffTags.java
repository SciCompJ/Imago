/**
 * 
 */
package imago.plugin.image.edit;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;
import net.sci.image.io.TiffTag;

/**
 * @author dlegland
 *
 */
public class PrintImageTiffTags implements Plugin
{
	public PrintImageTiffTags() 
	{
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
		System.out.println("print image tiff tags:");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
		Image image = doc.getImage();

		for (TiffTag tag : image.tiffTags)
		{
			String desc = tag.name == null ? "" : " (" + tag.name + ")";
			String info = String.format("Tag code: %5d %-30s", tag.code, desc);
			System.out.println(info + "\tType=" + tag.type + ", \tcount=" + tag.count + ", content=" + tag.content);
		}
	}

}
