/**
 * 
 */
package imago.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import imago.app.ImagoApp;
import imago.app.ImagoDoc;
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
                frame.getWidget(), message, "Error", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    public static final void showErrorDialog(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(
                frame.getWidget(), message, title, 
                JOptionPane.ERROR_MESSAGE);
    }
    
    public static final void showExceptionDialog(ImagoFrame frame, Exception ex, String title)
    {
        // create error frame
        JFrame errorFrame = new JFrame(title);
        errorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // creates text area
        JTextArea textArea = new JTextArea(15, 80);
        textArea.setForeground(Color.RED);
        textArea.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        // populates text area with stack trace
        textArea.append(ex.toString());
        for (StackTraceElement item : ex.getStackTrace())
        {
            textArea.append("\n    at " + item.toString());
        }
        
        // add Textarea in to middle panel
        errorFrame.add(scroll);
        
        // display error frame
        errorFrame.setLocationRelativeTo(frame.getWidget());
        errorFrame.pack();
        errorFrame.setVisible(true);
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
	ArrayList<ImagoFrame> frames = new ArrayList<ImagoFrame>();
	
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
	    try
	    {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
	    
		// set up default font
		UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.put("ComboBox.background", UIManager.get("TextArea.background"));
	}

	
	// ===================================================================
	// General methods

	
	

	// ===================================================================
    // Creation of new viewers

    /** 
     * Creates a new document from an image, adds it to the application, 
     * and returns a new frame associated to this document. 
     */
    public ImagoDocViewer createImagoDocViewer(Image image, ImagoFrame parentFrame)
    {
        // create the document from image
        ImagoDocViewer viewer; 
        if (parentFrame instanceof ImagoDocViewer)
        {
            ImagoDoc parentDoc = ((ImagoDocViewer) parentFrame).getDocument();
            viewer = addNewDocument(image, parentDoc);
        }
        else
        {
            viewer = addNewDocument(image);
        }
        
        // create the frame associated to the document
        parentFrame.addChild(viewer);
        return viewer;
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
		return createImagoDocViewer(doc);
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
		return createImagoDocViewer(doc);
	}
		
	/**
     * Creates the viewer for the document, ensuring name is unique.
     * 
     * @param doc
     *            the document to view
     * @return a viewer for the document
     */
	public ImagoDocViewer createImagoDocViewer(ImagoDoc doc) 
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
	
	/**
	 * Finds the frame corresponding to he viewer of a given document.
	 * 
	 * @param doc the document
	 * @return an instance of ImagoDocViewer associated to this document
	 */
	public ImagoDocViewer getDocumentViewer(ImagoDoc doc)
	{
	    for (ImagoDocViewer viewer : getDocumentViewers())
	    {
	        if (doc == viewer.getDocument())
	        {
	            return viewer;
	        }
	    }
	    
	    throw new RuntimeException("Could not find a document viewer for document with name: " + doc.getName());
	}

	
    // ===================================================================
    // Frame management
    
    public Collection<ImagoFrame> getFrames()
    {
        ArrayList<ImagoFrame> res = new ArrayList<>(frames.size());
        res.addAll(this.frames);
        return res;
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
                System.err.println("Warning: frame " + frame.getWidget().getName() + " is not referenced by document " + doc.getName());
            }
            
            frameList.remove(frame);
            
            if (frameList.size() == 0)
            {
                app.removeDocument(viewer.getDocument());
            }
        }
        return this.frames.remove(frame);
    }

    public boolean containsFrame(ImagoFrame frame)
    {
        return this.frames.contains(frame);
    }
    
    public void showEmptyFrame(boolean b) 
    {
        if (this.emptyFrame == null) 
        {
            this.emptyFrame = new ImagoEmptyFrame(this);
        }
    
        this.emptyFrame.setVisible(b);
    }

    public void disposeEmptyFrame()
    {
        this.emptyFrame.getWidget().dispose();
    }
    

	// ===================================================================
    // Getters / setters

    public ImagoApp getAppli()
    {
    	return this.app;
    }
 }
