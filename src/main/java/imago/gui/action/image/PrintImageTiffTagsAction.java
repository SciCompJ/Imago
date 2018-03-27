/**
 * 
 */
package imago.gui.action.image;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.image.Image;
import net.sci.image.io.TiffTag;

/**
 * @author dlegland
 *
 */
public class PrintImageTiffTagsAction extends ImagoAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PrintImageTiffTagsAction(ImagoFrame frame, String name) 
	{
		super(frame, name);
	}
	

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		System.out.println("print image tiff tags:");
		
		// get current frame
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image = doc.getImage();

		for (TiffTag tag : image.tiffTags)
		{
			String desc = tag.name == null ? "" : " (" + tag.name + ")";
			String info = String.format("Tag code: %5d %-30s", tag.code, desc);
			System.out.println(info + "\tType=" + tag.type + ", \tcount=" + tag.count + ", content=" + tag.content);
		}
	}

}
