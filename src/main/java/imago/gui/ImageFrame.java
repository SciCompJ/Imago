/**
 * 
 */
package imago.gui;

import imago.app.ImageHandle;
import imago.gui.panel.ImageDisplayOptionsPanel;
import imago.gui.panel.StatusBar;
import imago.gui.tool.DisplayCurrentValueTool;
import imago.gui.viewer.Image5DXYSliceViewer;
import imago.gui.viewer.PlanarImageViewer;
import imago.gui.viewer.StackSliceViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.image.Image;


/**
 * Displays an image into a frame, with menu, and several sub-panels.
 * 
 * Contains at least an instance of ImageViewer, and a status bar.
 * Can also contains an optional ImageDisplayOptionsPanel.
 * 
 * @see ImageViewer
 * @see imago.gui.panel.StatusBar
 * @see imago.gui.panel.DisplayOptionsPanel
 * 
 * @author David Legland
 * 
 */
public class ImageFrame extends ImagoFrame implements AlgoListener
{
	// ===================================================================
	// Static class variables

	// ===================================================================
	// Class variables
    
    /** The handle to the image displayed in this frame.*/
    ImageHandle imageHandle;

    /** The image to display.*/
    Image image;
    
    /** The image viewer panel. */
    ImageViewer imageViewer;
    
    /** The panel containing display options: Z,T slice index...*/ 
    ImageDisplayOptionsPanel imageDisplayOptionsPanel;
    
    /** Used to display information about image, cursor, current process... */
	StatusBar statusBar;
	
	
	JSplitPane splitPane;
	
	
	// ===================================================================
	// Constructor

    public ImageFrame(ImagoGui gui, ImageHandle handle) 
	{
		super(gui, "Image Frame");
		this.imageHandle = handle;
		this.image = handle.getImage();
		if (image != null)
		{
		    String filePath = image.getFilePath();
		    if (filePath != null && filePath.length() > 0)
		    {
		        this.setLastOpenPath(filePath);
		    }
		}

		// create menu
		GuiBuilder builder = new GuiBuilder(this);
		builder.createMenuBar();
		
		// Create the different panels
		createImageViewer();
        this.imageDisplayOptionsPanel = new ImageDisplayOptionsPanel(this.imageViewer);
        this.statusBar = new StatusBar();

		// layout the frame
		setupLayout();
		jFrame.doLayout();
		
		updateTitle();
		
		// setup window listener
		this.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(ImageFrame.this);
                ImageFrame.this.jFrame.dispose();
            }           
        });
		
		// Initialize the current tool
		ImagoTool tool = new DisplayCurrentValueTool(this, "showValue");
		this.imageViewer.setCurrentTool(tool);
		
		putFrameMiddleScreen();
	}
    
    private void createImageViewer()
    {
        // create the image viewer
        if (image.getDimension() == 2)
        {
            PlanarImageViewer viewer = new PlanarImageViewer(imageHandle);

            viewer.getImageDisplay().setShapes(imageHandle.getShapes());

            this.imageViewer = viewer;
        }
        else if (image.getDimension() == 3) 
        {
            StackSliceViewer sliceViewer = new StackSliceViewer(imageHandle);
            sliceViewer.setSliceIndex(this.imageHandle.getCurrentSliceIndex());
            this.imageViewer = sliceViewer;
        }
        else 
        {
            Image5DXYSliceViewer sliceViewer = new Image5DXYSliceViewer(imageHandle);
            sliceViewer.setSliceIndex(this.imageHandle.getCurrentSliceIndex());
            this.imageViewer = sliceViewer;
        }
    }

    private void setupLayout() 
	{
        this.imageDisplayOptionsPanel.setPreferredSize(new Dimension(0, 0));
        this.imageDisplayOptionsPanel.setMinimumSize(new Dimension(0, 0));
        
		// put into global layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.GREEN);
		mainPanel.add((JPanel) imageViewer.getWidget(), BorderLayout.CENTER);
		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
		
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                imageDisplayOptionsPanel, mainPanel);
        if (image.getDimension() < 4)
        {
            splitPane.setResizeWeight(0.0);
        }
        else
        {
            splitPane.setResizeWeight(0.25);
        }
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        this.jFrame.setContentPane(splitPane);
	}
	
	private void putFrameMiddleScreen()
	{
		// set up frame size depending on screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = Math.min(800, screenSize.width - 100);
		int height = Math.min(600, screenSize.width - 100);
		Dimension frameSize = new Dimension(width, height);
		this.jFrame.setSize(frameSize);

		// set up frame position depending on frame size
		int posX = (screenSize.width - width) / 4;
		int posY = (screenSize.height - height) / 4;
		this.jFrame.setLocation(posX, posY);
	}
	
	
	// ===================================================================
	// General methods
	
	/**
     * Updates the title of the frame with a sting containing document name,
     * image dimensions and type.
     */
	public void updateTitle()
	{
		// use document name for base title
        String name = this.imageHandle.getName();

        // string containing image dimensions
		String dimString = "(unknown size)";
		int dims[] = this.image.getSize();
		if (dims.length > 0)
		{
		    dimString = "" + dims[0];
		    for (int d = 1; d < dims.length; d++)
		    {
		        dimString += "x" + dims[d];
		    }
		}
		
		// image type
		String typeString = this.image.getType().toString();
		
		// setup title
		String titleString = name + " - " + dimString + " - " + typeString;
		this.setTitle(titleString);
	}
	
	public ImageHandle getImageHandle() 
	{
		return this.imageHandle;
	}
	
	public Image getImage() 
	{
		return this.image;
	}
	
	public ImageViewer getImageView()
	{
		return this.imageViewer;
	}
	
	/**
	 * Updates the view to the image stored in document.
	 * @param view the new view to the image.
	 */
    public void setImageView(ImageViewer view)
    {
        System.out.println("update image view");
        ImagoTool currentTool = null;
        
        if (this.imageViewer != null)
        {
            currentTool = this.imageViewer.getCurrentTool();
        }
        
        this.imageViewer = view;
        setupLayout();
        jFrame.doLayout();
        
        if (currentTool != null)
        {
            this.imageViewer.setCurrentTool(currentTool);
        }
    }
    
	public StatusBar getStatusBar () 
	{
		return statusBar;
	}

	@Override
	public void algoProgressChanged(AlgoEvent evt)
	{
//		System.out.println("progress: " + evt.getCurrentProgress() + "/" + evt.getTotalProgress());
		int progress = (int) (evt.getProgressRatio() * 100);
		this.getStatusBar().setProgressBarPercent(progress);
	}

	@Override
	public void algoStatusChanged(AlgoEvent evt)
	{
		System.out.println("status: " + evt.getStatus());
		this.getStatusBar().setCurrentStepLabel(evt.getStatus());
	}
}
