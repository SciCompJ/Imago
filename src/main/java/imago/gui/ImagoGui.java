/**
 * 
 */
package imago.gui;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.UIManager;

import net.sci.image.Image;


/**
 * The GUI Manager, that create frames, stores the set of open frames, 
 * and keeps a reference to the current application.
 * 
 * @author David Legland
 *
 */
public class ImagoGui 
{
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

	public ImagoGui(ImagoApp app) 
	{
		this.app = app;
		setupLookAndFeel();
	}
	
	private void setupLookAndFeel()
	{
		// set up default font
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		UIManager.put("ComboBox.background", 
				UIManager.get("TextArea.background"));
	}

	
	// ===================================================================
	// General methods

	public void showEmptyFrame(boolean b) 
	{
		if (this.emptyFrame == null) 
		{
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
	public ImagoDocViewer addNewDocument(Image image, ImagoDoc parentDoc)
	{
		// Create the new document, keeping the maximum of settings
		ImagoDoc doc = new ImagoDoc(image, parentDoc);
		
		// add to document manager
		this.app.addDocument(doc);
		
		// display in a new frame
		return createDocumentFrame(doc);
	}
	
	public ImagoApp getAppli()
	{
		return this.app;
	}
	
	public ImagoDocViewer createDocumentFrame(ImagoDoc doc) 
	{
		ImagoDocViewer frame = new ImagoDocViewer(this, doc);
		
		this.frames.add(frame);
		
		frame.setVisible(true);
		return frame;
	}
	
	public Collection<ImagoDocViewer> getDocumentViewers()
	{
		ArrayList<ImagoDocViewer> viewers = new ArrayList<ImagoDocViewer>(this.frames.size());
		for (ImagoFrame frame : this.frames)
		{
			if (frame instanceof ImagoDocViewer)
			{
				viewers.add((ImagoDocViewer) frame);
			}
		}
		
		return viewers;
	}
 }
