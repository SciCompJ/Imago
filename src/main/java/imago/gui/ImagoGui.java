/**
 * 
 */
package imago.gui;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import imago.app.ImagoApp;
import imago.app.ImageHandle;
import imago.app.TableHandle;
import net.sci.image.Image;
import net.sci.table.Table;


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

    public static final void showMessage(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(
                frame.getWidget(), message, title, 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
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
        
        // add Text area in the middle panel
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
	Map<String, ArrayList<ImageFrame>> imageFrames = new HashMap<>(); 
	
	/**
	 * An empty frame without document, displayed at startup.
	 */
	ImagoFrame emptyFrame = null;
	
	/**
	 * Some global settings.
	 */
	public Settings settings = new Settings();
	

	// ===================================================================
    // Private constants
	
	/**
	 * The amount of displacement in the x-direction to locate a new frame with
	 * respect to the parent one
	 */
	
	private static final int FRAME_OFFSET_X = 20;
	/**
	 * The amount of displacement in the y-direction to locate a new frame with
	 * respect to the parent one
	 */
	private static final int FRAME_OFFSET_Y = 30;	
	
	
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
    // Creation of new frames for specific objects
	
	public TableFrame createTableFrame(Table table, ImagoFrame parentFrame)
	{
//	    // try to get parent table handle
//	    TableHandle parentHandle = null;
//	    if (parentFrame instanceof TableFrame)
//	    {
//	        parentHandle = ((TableFrame) parentFrame).handle;
//	    }
//
	    // Create the new handle, keeping the maximum of settings
        TableHandle handle = this.app.createTableHandle(table);
        
        // create the frame
        TableFrame frame = new TableFrame(parentFrame, handle);

        if (parentFrame != null)
        {
			Point pos = parentFrame.getWidget().getLocation();
			frame.getWidget().setLocation(pos.x + FRAME_OFFSET_X, pos.y + FRAME_OFFSET_Y);
        }
        
        // add to frame manager
        this.addFrame(frame); 
        return frame;
	}
	

	// ===================================================================
    // Creation of new frames for images
	
    /** 
	 * Creates a new document from an image, adds it to the application, 
	 * and returns a new frame associated to this document. 
	 */
	public ImageFrame createImageFrame(Image image)
	{
		return createImageFrame(image, null);
	}

    /** 
     * Creates a new document from an image, adds it to the application, 
     * and returns a new frame associated to this document. 
     */
    public ImageFrame createImageFrame(Image image, ImagoFrame parentFrame)
    {
    	// First create a handle for the image
    	ImageHandle parentHandle = null;
    	if (parentFrame != null && parentFrame instanceof ImageFrame)
    	{
    		parentHandle = ((ImageFrame) parentFrame).getImageHandle();
    	}
    	ImageHandle handle = app.createImageHandle(image, parentHandle);

		// Create the frame
		ImageFrame frame = new ImageFrame(this, handle);
		
        if (parentFrame != null)
        {
			Point pos = parentFrame.getWidget().getLocation();
			frame.getWidget().setLocation(pos.x + FRAME_OFFSET_X, pos.y + FRAME_OFFSET_Y);
        }
			
        // link the frames
		this.frames.add(frame);
    	if (parentFrame != null)
    	{
    		parentFrame.addChild(frame);
    	}
		
		// add the frame to the list of frames associated to the document
		String docName = handle.getName();
		if (imageFrames.containsKey(docName))
		{
		    imageFrames.get(docName).add(frame);
		}
		else
		{
		    ArrayList<ImageFrame> frameList = new ArrayList<>(1);
		    frameList.add(frame);
		    imageFrames.put(docName, frameList);
		}
		
		frame.setVisible(true);
        return frame;
    }
	
	public Collection<ImageFrame> getImageFrames()
	{
		ArrayList<ImageFrame> viewers = new ArrayList<ImageFrame>(this.frames.size());
		for (ImagoFrame frame : this.frames)
		{
			if (frame instanceof ImageFrame)
			{
				viewers.add((ImageFrame) frame);
			}
		}
		
		return viewers;
	}
	
	/**
	 * Finds the frame corresponding to he viewer of a given document.
	 * 
	 * @param handle the document
	 * @return an instance of ImageFrame associated to this document
	 */
	public ImageFrame getImageFrame(ImageHandle handle)
	{
	    for (ImageFrame frame : getImageFrames())
	    {
	        if (handle == frame.getImageHandle())
	        {
	            return frame;
	        }
	    }
	    
	    throw new RuntimeException("Could not find any image frame for handle with name: " + handle.getName());
	}

	
    // ===================================================================
    // Frame management
    
    public Collection<ImagoFrame> getFrames()
    {
    	return Collections.unmodifiableCollection(this.frames);
    }

    public boolean addFrame(ImagoFrame frame)
    {
    	return this.frames.add(frame);
    }

    public boolean removeFrame(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame)
        {
            ImageHandle handle = ((ImageFrame) frame).getImageHandle();
            ArrayList<ImageFrame> frameList = imageFrames.get(handle.getName());
            if (!frameList.contains(frame))
            {
                System.err.println("Warning: frame " + frame.getWidget().getName() + " is not referenced by image handle " + handle.getName());
            }
            
            frameList.remove(frame);
            
            if (frameList.size() == 0)
            {
                app.removeHandle(handle);
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
