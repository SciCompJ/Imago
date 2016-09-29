/**
 * 
 */
package imago.gui;

import imago.app.ImagoDoc;
import imago.gui.panel.StatusBar;
import imago.gui.viewer.ImageDisplay;
import imago.gui.viewer.PlanarImageViewer;
import imago.gui.viewer.StackSliceViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JPanel;

import net.sci.image.Image;


/**
 * Displays an image into a frame, with menu, and several sub-panels.
 * 
 * 
 * @author David Legland
 * 
 */
public class ImagoDocViewer extends ImagoFrame
{

	// ===================================================================
	// Static class variables

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ===================================================================
	// Class variables

	ImagoDoc doc;
	Image image;

	ImageViewer imageView;
	StatusBar statusBar;
	
	
	// ===================================================================
	// Constructor

	public ImagoDocViewer(ImagoGui gui, ImagoDoc doc) 
	{
		super(gui, "Image Frame");
		this.doc = doc;
		this.image = doc.getImage();

		GuiBuilder builder = new GuiBuilder(this);
		builder.createMenuBar();
		
		setupLayout();
		doLayout();
		updateTitle();
		
		// Initialize the current tool
//		ImagoTool tool = new SelectionTool(this, "selection");
//		ImagoTool tool = new SelectLineTool(this, "selectLine");
//		this.imageView.setCurrentTool(tool);
		
		putFrameMiddleScreen();
	}

	private void setupLayout() 
	{
		// create the image viewer
		if (image.getDimension() == 2) 
		{
			this.imageView = new PlanarImageViewer(image);
		}
		else 
		{
			StackSliceViewer sliceViewer = new StackSliceViewer(image);
			sliceViewer.setSliceIndex(this.doc.getCurrentSliceIndex());
			this.imageView = sliceViewer;
		}
		
		// Create a status bar
		this.statusBar = new StatusBar();

		// put into global layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.GREEN);
		mainPanel.add(imageView, BorderLayout.CENTER);
		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
		
		// Add listeners
//		ImagoTool tool = new DisplayCurrentValueTool(this, "showValue");
		ImageDisplay display = null;
		if (image.getDimension() == 2) 
		{
			display = ((PlanarImageViewer) imageView).getImageDisplay(); 
		}
		else
		{
			display = ((StackSliceViewer) imageView).getImageDisplay(); 
		}
		if (display != null)
		{
//			display.addMouseListener(tool); 
//			display.addMouseMotionListener(tool); 
		}

		this.setContentPane(mainPanel);
	}
	
	private void putFrameMiddleScreen()
	{
		// set up frame size depending on screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = Math.min(800, screenSize.width - 100);
		int height = Math.min(700, screenSize.width - 100);
		Dimension frameSize = new Dimension(width, height);
		this.setSize(frameSize);

		// set up frame position depending on frame size
		int posX = (screenSize.width - width) / 4;
		int posY = (screenSize.height - height) / 4;
		this.setLocation(posX, posY);
	}
	
	// ===================================================================
	// General methods
	
	public void updateTitle()
	{
		String name = this.image.getName();
		if (name == null || name.isEmpty()) 
		{
			name = "No Name";
		}
		
		String dimString = "(unknown size)";
		int dim[] = this.image.getSize();
		if (dim.length == 2) 
		{
			dimString = dim[0] + "x" + dim[1];
		} 
		else if (dim.length == 3) 
		{
			dimString = dim[0] + "x" + dim[1] + "x" + dim[2];
		} 
		else if (dim.length == 4) 
		{
			dimString = dim[0] + "x" + dim[1] + "x" + dim[2] + "x" + dim[3];
		}
		
		String typeString = this.image.getType().toString();
		
		String titleString = name + " - " + dimString + " - " + typeString;
		this.setTitle(titleString);
	}
	
	public ImagoDoc getDocument() 
	{
		return this.doc;
	}
	
	public ImageViewer getImageView()
	{
		return this.imageView;
	}
	
	public StatusBar getStatusBar () 
	{
		return statusBar;
	}

	public void repaint() {
//		System.out.println("repaint Doc Viewer");
		super.repaint();
	}
}
