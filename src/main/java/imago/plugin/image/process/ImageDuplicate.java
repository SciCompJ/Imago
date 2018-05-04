/**
 * 
 */
package imago.plugin.image.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import net.sci.image.Image;



/**
 * @author David Legland
 *
 */
public class ImageDuplicate implements Plugin
{
    public ImageDuplicate()
    {
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
        System.out.println("duplicate");
        
        // get current frame
        ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
        Image image = doc.getImage();
        
        Image result = image.duplicate();
        
        // add the image document to GUI
        frame.getGui().addNewDocument(result);
    }
}
