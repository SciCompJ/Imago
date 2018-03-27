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



/**
 * @author David Legland
 *
 */
public class ImageDuplicateAction extends ImagoAction
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public ImageDuplicateAction(ImagoFrame frame, String name)
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
        System.out.println("duplicate");
        
        // get current frame
        ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
        Image image = doc.getImage();
        
        Image result = image.duplicate();
        
        // add the image document to GUI
        this.gui.addNewDocument(result);
    }
}
