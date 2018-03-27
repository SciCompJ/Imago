/**
 * 
 */
package imago.gui.action.process;

import imago.app.ImagoDoc;
import imago.gui.ImagoAction;
import imago.gui.ImagoDocViewer;
import imago.gui.ImagoFrame;

import java.awt.event.ActionEvent;

import net.sci.array.Array;
import net.sci.array.data.color.RGB8Array;
import net.sci.array.data.scalar2d.Int32Array2D;
import net.sci.image.Image;

/**
 * Computes the three bivariate histograms of a RGB8 image.
 * 
 * @author David Legland
 *
 */
public class ColorImageBivariateHistogramsAction extends ImagoAction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ColorImageBivariateHistogramsAction(ImagoFrame frame, String name)
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
		System.out.println("RGB8 to bivariate histograms");

		// get current image data
		ImagoDoc doc = ((ImagoDocViewer) this.frame).getDocument();
		Image image	= doc.getImage();
		Array<?> array = image.getData();

		if (!(array instanceof RGB8Array))
		{
			return;
		}
		
//		Map<String, UInt8Array> channels = RGB8Array.mapChannels((RGB8Array) array);
//		UInt8Array red = channels.get("red");
//		UInt8Array green = channels.get("green");
//		UInt8Array blue = channels.get("blue");
		
		Int32Array2D RGHist = Int32Array2D.create(256, 256);
		Int32Array2D RBHist = Int32Array2D.create(256, 256);
		Int32Array2D GBHist = Int32Array2D.create(256, 256);
		
		RGB8Array.Iterator iterRGB = ((RGB8Array) array).iterator();
//		Int32Array.Iterator iterRG = RGHist.iterator();
//		Int32Array.Iterator iterRB = RGHist.iterator();
//		Int32Array.Iterator iterGB = RGHist.iterator();
		
		while (iterRGB.hasNext())
		{
			int[] rgb = iterRGB.next().getSamples();
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			RGHist.setInt(r, g, RGHist.getInt(r, g) + 1);		
			RBHist.setInt(r, b, RBHist.getInt(r, b) + 1);		
			GBHist.setInt(g, b, GBHist.getInt(g, b) + 1);		
		}
		
		// apply operator on current image
		String name = image.getName();
		Image rgImage = new Image(RGHist, image);
		rgImage.setName(name + "-RG");
		Image rbImage = new Image(RBHist, image);
		rbImage.setName(name + "-RB");
		Image gbImage = new Image(GBHist, image);
		gbImage.setName(name + "-GB");
		
		// add the image documents to GUI
		this.gui.addNewDocument(rgImage);
		this.gui.addNewDocument(rbImage);
		this.gui.addNewDocument(gbImage);
	}

}
