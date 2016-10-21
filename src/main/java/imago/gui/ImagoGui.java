/**
 * 
 */
package imago.gui;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;

import java.util.ArrayList;

import javax.swing.UIManager;

import net.sci.image.Image;


/**
 * The GUI Manager, that create frames, stores the set of open frames, 
 * and keeps a reference to the application.
 * @author David Legland
 *
 */
public class ImagoGui {

	// ===================================================================
	// Static utility methods

//	/**
//	 * Creates a Java Buffered image that can be used to represent the given 
//	 * Image.
//	 * This functions automatically converts color palette images to true RGB
//	 * images. 
//	 */
//	public static java.awt.image.BufferedImage createAwtImage(Image image) {
//		// Extract data
// 		int[][] lut = image.getColorMap();
// 		Image imageData = image.getImage();
// 		
// 		// Check if needs to convert to true RGB
// 		if (imageData instanceof Gray8Image2DBuffer && lut != null) {
// 			return ((Gray8Image2DBuffer) imageData).getAsAwtImage(lut);
// 		} else {
// 			return imageData.getAsAwtImage();
// 		}
//	}
//	
//	/**
//	 * Creates a Java Buffered image that can be used to represent the given 
//	 * Image at the desired slice index.
//	 * This functions automatically converts color palette images to true RGB
//	 * images. 
//	 */
//	public static java.awt.image.BufferedImage buildAwtSliceImage(Image image, int sliceIndex) 
//	{
// 		return ImageUtils.createAwtImage(image, sliceIndex);
//	}
//	
	// ===================================================================
	// Class variables

	/**
	 * The parent application
	 */
	ImagoApp app;
	
	/**
	 * The list of frames associated to this GUI.
	 */
	ArrayList<ImagoFrame> frames = new ArrayList<ImagoFrame>(5);
	
	ImagoFrame emptyFrame = null;
	
	
	// ===================================================================
	// Constructor

	public ImagoGui(ImagoApp app) {
		this.app = app;
		setupLookAndFeel();
	}
	
	private void setupLookAndFeel() {
		// set up default font
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		UIManager.put("ComboBox.background", 
				UIManager.get("TextArea.background"));
	}

	
	// ===================================================================
	// General methods

	public void showEmptyFrame(boolean b) {
		if (this.emptyFrame == null) {
			this.emptyFrame = new ImagoEmptyFrame(this);
		}

		this.emptyFrame.setVisible(b);
	}
	
	/** 
	 * Creates a new document from an image, adds it to the application, 
	 * and returns a new frame associated to this document. 
	 */
	public ImagoDocViewer addNewDocument(Image image)
	{
		// create the document from image
		ImagoDoc doc = this.app.addNewDocument(image);

		// create the frame associated to the document
		return createDocumentFrame(doc);
	}

	/** 
	 * Creates a new document from an image, using settings given in parent 
	 * document, and adds the new document to the GUI 
	 */
	public ImagoDocViewer addNewDocument(Image image, ImagoDoc parentDoc) {
		// Create the new document, keeping the maximum of settings
		ImagoDoc doc = new ImagoDoc(image, parentDoc);
		
		// add to document manager
		this.app.addDocument(doc);
		
		// display in a new frame
		return createDocumentFrame(doc);
	}
	
	public ImagoApp getAppli() {
		return this.app;
	}
	
	public ImagoDocViewer createDocumentFrame(ImagoDoc doc) {
		ImagoDocViewer frame = new ImagoDocViewer(this, doc);
		
		this.frames.add(frame);
		
		frame.setVisible(true);
		return frame;
	}
	
 }
