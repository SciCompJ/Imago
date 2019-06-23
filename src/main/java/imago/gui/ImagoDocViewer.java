/**
 * 
 */
package imago.gui;

import imago.app.ImagoDoc;
import imago.gui.panel.ImageDisplayOptionsPanel;
import imago.gui.panel.StatusBar;
import imago.gui.tool.DisplayCurrentValueTool;
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
 * CAn also contains an ImageDisplayOptionsPanel.
 * 
 * @see ImageViewer
 * @see imago.gui.panel.StatusBar
 * @see imago.gui.panel.DisplayOptionsPanel
 * 
 * @author David Legland
 * 
 */
public class ImagoDocViewer extends ImagoFrame implements AlgoListener
{
	// ===================================================================
	// Static class variables

	// ===================================================================
	// Class variables

	ImagoDoc doc;
	Image image;

    ImageViewer imageView;
    ImageDisplayOptionsPanel imageDisplayOptionsPanel;
	StatusBar statusBar;
	
	
	// ===================================================================
	// Constructor

    public ImagoDocViewer(ImagoGui gui, ImagoDoc doc) 
	{
		super(gui, "Image Frame");
		this.doc = doc;
		this.image = doc.getImage();
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
		
        // Create a status bar
        this.statusBar = new StatusBar();

		// create default viewer for image
		createDefaultImageViewer();
        
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
                gui.removeFrame(ImagoDocViewer.this);
                ImagoDocViewer.this.jFrame.dispose();
            }           
        });
		
		// Initialize the current tool
		ImagoTool tool = new DisplayCurrentValueTool(this, "showValue");
		this.imageView.setCurrentTool(tool);
		
		putFrameMiddleScreen();
	}
    
    private void createDefaultImageViewer()
    {
        // create the image viewer
        if (image.getDimension() == 2) 
        {
            PlanarImageViewer viewer = new PlanarImageViewer(image);

            viewer.getImageDisplay().setShapes(doc.getShapes());

            this.imageView = viewer;
        }
        else 
        {
            StackSliceViewer sliceViewer = new StackSliceViewer(image);
            sliceViewer.setSliceIndex(this.doc.getCurrentSliceIndex());
            this.imageView = sliceViewer;
//          OrthoSlicesViewer sliceViewer = new OrthoSlicesViewer(image);
////            sliceViewer.setSliceIndex(this.doc.getCurrentSliceIndex());
//            this.imageView = sliceViewer;
        }
        
        this.imageDisplayOptionsPanel = new ImageDisplayOptionsPanel(this.imageView);
        
    }

    private void setupLayout() 
	{
        this.imageDisplayOptionsPanel.setPreferredSize(new Dimension(100, 100));
        this.imageDisplayOptionsPanel.setMinimumSize(new Dimension(50, 50));
        
		// put into global layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.GREEN);
		mainPanel.add((JPanel) imageView.getWidget(), BorderLayout.CENTER);
		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
		
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                imageDisplayOptionsPanel, mainPanel);
        splitPane.setResizeWeight(0.2);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        this.jFrame.setContentPane(splitPane);
	}
	
	private void putFrameMiddleScreen()
	{
		// set up frame size depending on screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = Math.min(800, screenSize.width - 100);
		int height = Math.min(700, screenSize.width - 100);
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
        String name = this.doc.getName();

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
	
	public ImagoDoc getDocument() 
	{
		return this.doc;
	}
	
	public ImageViewer getImageView()
	{
		return this.imageView;
	}
	
	/**
	 * Updates the view to the image stored in document.
	 * @param view the new view to the image.
	 */
    public void setImageView(ImageViewer view)
    {
        System.out.println("update image view");
        ImagoTool currentTool = null;
        
        if (this.imageView != null)
        {
            currentTool = this.imageView.getCurrentTool();
        }
        
        this.imageView = view;
        setupLayout();
        jFrame.doLayout();
        
        if (currentTool != null)
        {
            this.imageView.setCurrentTool(currentTool);
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
