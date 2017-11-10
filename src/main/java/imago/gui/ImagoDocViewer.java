/**
 * 
 */
package imago.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import imago.app.ImagoDoc;
import imago.gui.panel.StatusBar;
import imago.gui.tool.DisplayCurrentValueTool;
import imago.gui.viewer.PlanarImageViewer;
import imago.gui.viewer.StackSliceViewer;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.image.Image;


/**
 * Displays an image into a frame, with menu, and several sub-panels.
 * 
 * 
 * @author David Legland
 * 
 */
public class ImagoDocViewer extends ImagoFrame implements AlgoListener
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
		if (image != null)
		{
		    String filePath = image.getFilePath();
		    if (filePath != null && filePath.length() > 0)
		    {
		        this.setLastOpenPath(filePath);
		    }
		}

		// create menu
		GuiBuilder builder = new GuiBuilder();
		builder.createMenuBar(this);
		
        // Create a status bar
        this.statusBar = new StatusBar();

		// create default viewer for image
		createDefaultImageViewer();
        
		// layout the frame
		setupLayout();
		doLayout();
		updateTitle();
		
		// setup window listener
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                gui.removeFrame(ImagoDocViewer.this);
                ImagoDocViewer.this.dispose();
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
    }

    private void setupLayout() 
	{
		// put into global layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.GREEN);
		mainPanel.add(imageView, BorderLayout.CENTER);
		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
		
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
		// image name
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
        doLayout();
        
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
