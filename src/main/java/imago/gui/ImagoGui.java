/**
 * 
 */
package imago.gui;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
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
    // Static methods

    public static final void showErrorDialog(ImagoFrame frame, String message)
    {
        JOptionPane.showMessageDialog(
                frame, message, "Error", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    public static final void showErrorDialog(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(
                frame, message, title, 
                JOptionPane.ERROR_MESSAGE);
    }
    
    
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
	
	/**
     * The list of frames associated to each document.
     * 
     * Used to remove the document from the app instance when the last frame
     * referring to a document is closed.
     */
	Map<String, ArrayList<ImagoFrame>> docFrames = new HashMap<>(); 
	
	/**
	 * An empty frame without document, displayed at startup.
	 */
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
		
		// add the frame to the list of frames associated to the document
		String docName = doc.getName();
		if (docFrames.containsKey(docName))
		{
		    docFrames.get(docName).add(frame);
		}
		else
		{
		    ArrayList<ImagoFrame> frameList = new ArrayList<>(1);
		    frameList.add(frame);
		    docFrames.put(docName, frameList);
		}
		
		frame.setVisible(true);
		return frame;
	}
	
	public boolean addFrame(ImagoFrame frame)
	{
		return this.frames.add(frame);
	}

	public boolean removeFrame(ImagoFrame frame)
	{
		if (frame instanceof ImagoDocViewer)
		{
			ImagoDocViewer viewer = (ImagoDocViewer) frame;
			ImagoDoc doc = ((ImagoDocViewer) frame).getDocument();
			ArrayList<ImagoFrame> frameList = docFrames.get(doc.getName());
			if (!frameList.contains(frame))
			{
			    System.err.println("Warning: frame " + frame.getName() + " is not referenced by document " + doc.getName());
			}
			
			frameList.remove(frame);
			
			if (frameList.size() == 0)
			{
			    app.removeDocument(viewer.getDocument());
			}
		}
		return this.frames.remove(frame);
	}
	
	public Collection<ImagoFrame> getFrames()
	{
		return Collections.unmodifiableList(this.frames);
	}

	public void disposeEmptyFrame()
	{
		this.emptyFrame.dispose();
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
