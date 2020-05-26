/**
 * 
 */
package imago.gui;

import org.junit.Test;

import imago.app.ImagoApp;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.image.Image;

/**
 * @author dlegland
 *
 */
public class ImagoGuiTest
{

	/**
	 * Test method for {@link imago.gui.ImagoGui#createImageFrame(net.sci.image.Image)}.
	 */
	@Test
	public void testCreateImageFrameImage()
	{
		ImagoApp app = new ImagoApp();
		ImagoGui gui = new ImagoGui(app);
		
		UInt8Array2D array = UInt8Array2D.create(400, 400);
		Image image = new Image(array);
		
		ImagoFrame frame = gui.createImageFrame(image);
		frame.close();
	}

}
