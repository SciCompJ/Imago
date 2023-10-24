/**
 * 
 */
package imago.gui.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.GuiBuilder;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.image.tools.DisplayCurrentValueTool;
import imago.gui.panels.StatusBar;
import net.sci.algo.AlgoEvent;
import net.sci.algo.AlgoListener;
import net.sci.array.ArrayOperator;
import net.sci.image.Image;
import net.sci.image.ImageArrayOperator;


/**
 * Displays an image into a frame, with menu, and several sub-panels.
 * 
 * Contains at least an instance of ImageViewer, and a status bar.
 * Can also contains an optional ImageDisplayOptionsPanel.
 * 
 * @see ImageViewer
 * @see imago.gui.panels.StatusBar
 * @see imago.gui.panel.DisplayOptionsPanel
 * 
 * @author David Legland
 * 
 */
public class ImageFrame extends ImagoFrame implements AlgoListener
{
    // ===================================================================
    // Static methods
    
    /**
     * Creates a new frame for displaying an image, located with respect to the
     * specified frame.
     * 
     * Creates a new document from an image, adds it to the application, and
     * returns a new frame associated to this document.
     * 
     * @param image
     *            the image to display
     * @param parentFrame
     *            (optional) an existing frame used to locate the new frame. If
     *            null, a new ImagoGui is created.
     * @return a new frame representing the input image
     */
    public static final ImageFrame create(Image image, ImagoFrame parentFrame)
    {
        // retrieve gui, or create one if necessary
        ImagoGui gui = parentFrame != null ? parentFrame.getGui() : new ImagoGui(new ImagoApp());
        ImagoApp app = gui.getAppli();
        
        // create a handle for the image
        ImageHandle parentHandle = null;
        if (parentFrame != null && parentFrame instanceof ImageFrame)
        {
            parentHandle = ((ImageFrame) parentFrame).getImageHandle();
        }
        ImageHandle handle = app.createImageHandle(image, parentHandle);

        // Create the frame
        ImageFrame frame = new ImageFrame(gui, handle);
        gui.updateFrameLocation(frame, parentFrame);
            
        // link the frames
        gui.addFrame(frame);
        if (parentFrame != null)
        {
            parentFrame.addChild(frame);
        }
        
        frame.setVisible(true);
        return frame;
    }
    
    public static final Collection<ImageFrame> getImageFrames(ImagoGui gui)
    {
        ArrayList<ImageFrame> res = new ArrayList<ImageFrame>();
        for (ImagoFrame frame : gui.getFrames())
        {
            if (frame instanceof ImageFrame)
            {
                res.add((ImageFrame) frame);
            }
        }
        
        return res;
    }
    
    /**
     * Returns the ImageFrame in the specified GUI instance that contains the
     * image with the specified name.
     * 
     * @param gui
     *            the GUI to explore.
     * @param name
     *            the name of the image within the frame
     * @return the frame containing the image, or null if no such Image exists.
     */
    public static final ImageFrame getImageFrame(ImagoGui gui, String name)
    {
        for (ImageFrame frame : getImageFrames(gui))
        {
            if (name.equals(frame.getImage().getName()))
            {
                return frame;
            }
        }
        
        return null;
    }
    

    
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
		
		putFrameMiddleScreen();
	}
    
    private void createImageViewer()
    {
        // create the image viewer
        if (image.getDimension() == 2)
        {
            PlanarImageViewer viewer = new PlanarImageViewer(imageHandle);

            viewer.getImageDisplay().setShapes(imageHandle.getShapes());
            
            ImagoTool cursorDisplay = new DisplayCurrentValueTool(this, "showValue");
            viewer.getImageDisplay().addMouseListener(cursorDisplay);
            viewer.getImageDisplay().addMouseMotionListener(cursorDisplay);

            this.imageViewer = viewer;
        }
        else if (image.getDimension() == 3) 
        {
            StackSliceViewer sliceViewer = new StackSliceViewer(imageHandle);
            sliceViewer.setSlicingPosition(2, this.imageHandle.getCurrentSliceIndex());
            
            ImagoTool cursorDisplay = new DisplayCurrentValueTool(this, "showValue");
            sliceViewer.getImageDisplay().addMouseListener(cursorDisplay);
            sliceViewer.getImageDisplay().addMouseMotionListener(cursorDisplay);

            this.imageViewer = sliceViewer;
        }
        else 
        {
            Image5DXYSliceViewer sliceViewer = new Image5DXYSliceViewer(imageHandle);
            sliceViewer.setSlicingPosition(2, this.imageHandle.getCurrentSliceIndex());
            
            ImagoTool cursorDisplay = new DisplayCurrentValueTool(this, "showValue");
            sliceViewer.getImageDisplay().addMouseListener(cursorDisplay);
            sliceViewer.getImageDisplay().addMouseMotionListener(cursorDisplay);

            this.imageViewer = sliceViewer;
        }
    }

    private void setupLayout() 
	{
        this.imageDisplayOptionsPanel.setPreferredSize(new Dimension(0, 0));
        this.imageDisplayOptionsPanel.setMinimumSize(new Dimension(0, 0));
        
		// put into global layout
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add((JPanel) imageViewer.getWidget(), BorderLayout.CENTER);
		mainPanel.add(this.statusBar, BorderLayout.SOUTH);
		
		// setup the layout for the option panel:
		// uses JSplitPanel, initial visibility depends on image dimensionality
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageDisplayOptionsPanel, mainPanel);
        splitPane.setResizeWeight(image.getDimension() < 4 ? 0.0 : 0.25);
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
     * Utility methods that run the algorithm on the data array from an image
     * and return the result image, by managing the algorithm events and
     * displaying elapsed time at the end.
     * 
     * Performs the following operations:
     * <ol>
     * <li>Add the frame as algorithm listener to the operator, in order to
     * monitor the process</li>
     * <li>Run the operator on the input image, generating a new image</li>
     * <li>Reset progress monitoring</li>
     * <li>Display elapsed time in status bar</li>
     * </ol>
     * 
     * @see net.sci.algo.AlgoListener
     * @see #showElapsedTime(String, double, Image)
     * 
     * @param opName
     *            the name of the operator to run. Used to populate the status
     *            bar at the beginning of the algorithm.
     * @param op
     *            the array operator to run. Can be an instance of
     *            ImageArrayOperator; in that case, the operator will run the
     *            "process(Image)" method instead of the "process(Array)"
     *            method.
     * @param image
     *            the image containing the data array to process
     * @return a new Image instance encapsulating the result of array processing
     */
    public Image runOperator(String opName, ArrayOperator op, Image image)
    {
        // reset status bar
        this.getStatusBar().setCurrentStepLabel("Run: " + opName);
        this.getStatusBar().setProgressBarPercent(0);
        
        // initialize listener and timer
        op.addAlgoListener(this);
        long t0 = System.nanoTime();
        
        // run process, switching to the best appropriate method depending on
        // the class of the operator
        Image result = (op instanceof ImageArrayOperator) 
                ? ((ImageArrayOperator) op).process(image) 
                : new Image(op.process(image.getData()), image);
        long t1 = System.nanoTime();
        
        // cleanup listener and status bar
        op.removeAlgoListener(this);
        this.getStatusBar().setProgressBarPercent(0);
        
        // display elapsed time
        showElapsedTime(opName, (t1 - t0) / 1_000_000.0, image);
        
        return result;
    }
    
    /**
     * Utility methods that run the algorithm on the data array from an image
     * and return the result image, by managing the algorithm events and
     * displaying elapsed time at the end.
     * 
     * Performs the following operations:
     * <ol>
     * <li>Add the frame as algorithm listener to the operator, in order to
     * monitor the process</li>
     * <li>Run the operator on the input image, generating a new image</li>
     * <li>Reset progress monitoring</li>
     * <li>Display elapsed time in status bar</li>
     * </ol>
     * 
     * @see net.sci.algo.AlgoListener
     * @see #showElapsedTime(String, double, Image)
     * 
     * @param op
     *            the array operator to run. Can be an instance of
     *            ImageArrayOperator; in that case, the operator will run the
     *            "process(Image)" method instead of the "process(Array)"
     *            method.
     * @param image
     *            the image containing the data array to process
     * @return a new Image instance encapsulating the result of array processing
     */
    public Image runOperator(ArrayOperator op, Image image)
    {
        String opName = op.getClass().getSimpleName();
        return runOperator(opName, op, image);
    }
    
    /**
     * Display elapsed time, converted into seconds, and computes the number of
     * processed elements per second. Also returns the created message.
     * 
     * <p>
     * Example of use:
     * 
     * <pre>
     * {@code
     * // initialize processing 
     * UInt8Array3D array = ...;
     * Strel3D strel = CubeStrel.fromDiameter(5);
     * 
     * // initialize timing 
     * long t0 = System.currentTimeMillis();
     * 
     * // start processing 
     * Array<?> res = MorphologicalFilters.dilation(image, strel);
     * Image resImage = new Image(res);
     * resImage.show();
     * 
     * // Display elapsed time
     * long t1 = System.currentTimeMillis();
     * currentFrame.showElapsedTime("dilation", t1 - t0, resPlus);
     * }
     * </pre>
     *
     * @param opName
     *            the name of the operation (algorithm, plugin...)
     * @param timeInMillis
     *            the elapsed time, in milliseconds
     * @param refImage
     *            the image on which process was applied
     * @return the String corresponding to the message displayed in status bar
     */
    public String showElapsedTime(String opName, double timeInMillis, Image refImage) 
    {
        // compute number of elements within image (using double to avoid int overflow)
        double nItems = elementCount(refImage);
        
        // adapt output display to image dimensionality
        String elementName = refImage.getDimension() == 3 ? "voxels" : "pixels";
        
        // compute number of processed elements per unit time
        double timeInSecs = timeInMillis / 1000.0;
        int elementsPerSecond = (int) (nItems / timeInSecs);
        
        // format display
        String pattern = "%s: %.3f seconds, %d %s/second";
        String status = String.format(Locale.ENGLISH, pattern, opName, timeInSecs, elementsPerSecond, elementName);
        
        // display message
        System.out.println(status);
        this.statusBar.setCurrentStepLabel(status);
        // also reset porogress bar
        this.statusBar.setProgressBarPercent(0);
        return status;
    }
    
    /**
     * Computes the number of elements within an image, using double to avoid
     * int overflow.
     * 
     * @param image
     *            the image to count elements.
     * @return the number of elements within image.
     */
    private static final double elementCount(Image image)
    {
        int[] dims = image.getSize();
        double nItems = 1;
        for (int d = 0; d < dims.length; d++)
        {
            nItems *= dims[d];
        }
        return nItems;
    }
        
	/**
     * Updates the title of the frame with a sting containing document name,
     * image dimensions and type.
     */
	public void updateTitle()
	{
		// use document name for base title
        String name = this.imageHandle.getName();
        if (!this.image.getExtension().isEmpty())
        {
        	name = name + "." + this.image.getExtension(); 
        }

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
    
	public StatusBar getStatusBar() 
	{
		return statusBar;
	}

	/**
     * Completes the default implementation to display the progression in the
     * status bar.
     * 
     * @param evt
     *            the algorithm event
     */
	@Override
	public void algoProgressChanged(AlgoEvent evt)
	{
	    super.algoProgressChanged(evt);
		int progress = (int) (evt.getProgressRatio() * 100);
		this.getStatusBar().setProgressBarPercent(progress);
	}

    /**
     * Completes the default implementation to display the algorithm status in
     * the status bar.
     * 
     * @param evt
     *            the algorithm event
     */
	@Override
	public void algoStatusChanged(AlgoEvent evt)
	{
        super.algoProgressChanged(evt);
		System.out.println("status: " + evt.getStatus());
		this.getStatusBar().setCurrentStepLabel(evt.getStatus());
	}
}
