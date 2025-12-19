/**
 * 
 */
package imago.imagepair;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import imago.gui.panels.CollapsiblePanel;
import imago.image.ImageTool;
import imago.image.viewers.ImageDisplay;
import imago.imagepair.composite.AbsoluteDifferenceOfIntensitiesComposite;
import imago.imagepair.composite.CheckerBoardComposite;
import imago.imagepair.composite.DifferenceOfIntensitiesComposite;
import imago.imagepair.composite.ImagePairComposite;
import imago.imagepair.composite.MagentaGreenComposite;
import imago.imagepair.composite.MaximumIntensityComposite;
import imago.imagepair.composite.SumOfIntensitiesComposite;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;


/**
 * A Panel that displays a pair of planar images using a composite of the two
 * images.
 * 
 * @author dlegland
 *
 */
public class CompositeImagePairViewer extends ImagePairViewer implements ComponentListener 
{
    // ===================================================================
    // Enumerations
    
    public enum DisplayType
    {
        CHECKERBOARD("Checkerboard"), 
        MAGENTA_GREEN("Magenta-Green"),
        DIFFERENCE("Difference"),
        ABSOLUTE_DIFFERENCE("Abs. Difference"),
        MAX_INTENSITY("Max Of Intensities"),
        SUM("Sum Of Intensities");
        
        String label;
        
        DisplayType(String label)
        {
            this.label = label;
        }
        
        @Override
        public String toString()
        {
            return label;
        }
    }
    

	// ===================================================================
	// Class variables
	
    JPanel panel;
    
	JScrollPane scroll;
	ImageDisplay imageDisplay;
	
	BufferedImage awtImage;

    protected Image compositeImage;
    
    protected DisplayType displayType = DisplayType.MAGENTA_GREEN;
    
	ZoomMode zoomMode = ZoomMode.FILL;
	
	protected ImageTool currentTool = null;
	
	/**
	 * The shape of the current selection, usually a polyline or a rectangle, in pixel coordinates.
	 */
	protected Geometry2D selection = null;
	
	
	// ===================================================================
	// Constructor

	public CompositeImagePairViewer(Image refImage, Image image2)
	{
		super(refImage, image2);
		if (refImage.getDimension() != 2) 
		{
			throw new IllegalArgumentException("Requires a planar image as input");
		}
		
		updateCompositeImage();
		
		// use reference image for startup
		this.awtImage = compositeImage.getType().createAwtImage(compositeImage);
		
		setupLayout();
	}

	private void setupLayout()
	{
		// create the main display panel
		imageDisplay = new ImageDisplay(this.awtImage);
		
		// encapsulate into scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);
		
		this.panel = new JPanel();
		this.panel.setLayout(new BorderLayout());
		this.panel.add(scroll, BorderLayout.CENTER);

		this.panel.setBackground(Color.WHITE);
		
		// Add listeners
		// (mouse listeners are added from ImageFrame, when component is build)
		this.panel.addComponentListener(this);
	}
	
	
	// ===================================================================
	// General methods

	public void updateCompositeImage()
	{
	    ImagePairComposite compositer = switch(this.displayType)
        {
            case CHECKERBOARD -> new CheckerBoardComposite(50);
            case MAGENTA_GREEN -> new MagentaGreenComposite();
            case DIFFERENCE -> new DifferenceOfIntensitiesComposite();
            case ABSOLUTE_DIFFERENCE -> new AbsoluteDifferenceOfIntensitiesComposite();
            case MAX_INTENSITY -> new MaximumIntensityComposite();
            case SUM -> new SumOfIntensitiesComposite();
            default -> throw new RuntimeException("Unknown display type for image pair " + this.displayType);
        };
        this.compositeImage = compositer.process(refImage, otherImage);
	}
	
	
	public ImageDisplay getImageDisplay() 
	{
		return imageDisplay;
	}

	@Override
    public JPanel createOptionsPanel()
    {
        return new OptionsPanel();
    }

    /**
	 * @return the current selection, as a Geometry2D instance.
	 */
	public Geometry2D getSelection()
	{
		return this.selection;
	}

    /**
     * Returns the image to display, which can be a preview image, or the
     * reference image.
     * 
     * @return the preview image if is it not null, or the image otherwise.
     */
    public Image getImageToDisplay() 
    {
        return this.compositeImage;
    }

    public Image getCompositeImage() 
    {
        return this.compositeImage;
    }

	/**
     * @param selection
     *            the selection of the current viewer, as an instance of
     *            Geometry2D.
     */
	public void setSelection(Geometry selection)
	{
	    if (!(selection instanceof Geometry2D))
	    {
	        throw new RuntimeException("Selection must be an instance of Geometry2D");
	    }
	    
		this.selection = (Geometry2D) selection;
		this.imageDisplay.setSelection((Geometry2D) selection);
	}
	
	
	// ===================================================================
	// Zoom management

	public double getZoom() 
	{
		return imageDisplay.getZoom();
	}
	
	public void setZoom(double zoom) 
	{
		imageDisplay.setZoom(zoom);		
		imageDisplay.invalidate();
		this.panel.validate();
		imageDisplay.updateOffset();		
	}

	/**
	 * Computes the zoom factor that best fits the image within the limits of
	 * the panel.
	 */
	public void setBestZoom() 
	{
		Dimension dim0 = scroll.getSize();
		double ratioX = ((double) dim0.width - 5) / ((double) (refImage.getSize(0)));
		double ratioY = ((double) dim0.height - 5) / ((double) (refImage.getSize(1)));
		double zoom = Math.min(ratioX, ratioY);
		setZoom(zoom);
	}

	
	// ===================================================================
	// tool management

	@Override
	public ImageTool getCurrentTool() 
	{
		return currentTool;
	}

	@Override
	public void setCurrentTool(ImageTool tool) 
	{
		// remove previous tool
		if (currentTool != null) 
		{
			imageDisplay.removeMouseListener(currentTool);
			imageDisplay.removeMouseMotionListener(currentTool);
		}
		
		// setup current tool
		currentTool = tool;
		if (currentTool != null) 
		{
			imageDisplay.addMouseListener(currentTool);
			imageDisplay.addMouseMotionListener(currentTool);
		}
	}


	// ===================================================================
	// Display methods

	public void refreshDisplay()
	{
	    updateCompositeImage();
	    updateAwtImage();
	    
		this.imageDisplay.setBufferedImage(this.awtImage);
		imageDisplay.updateOffset();
		this.imageDisplay.repaint();
	}

	/**
	 * Updates the AWT Image displayed in the center of the ImageDisplay.
	 */
	public void updateAwtImage()
	{
        Image image = this.getImageToDisplay();
        this.awtImage = image.getType().createAwtImage(image);
	}
	
	public void repaint()
	{
	    this.panel.repaint();
	}


    // ===================================================================
	// Implementation of Component Listener

	@Override
	public void componentHidden(ComponentEvent evt)
	{
	}

	@Override
	public void componentMoved(ComponentEvent evt)
	{
	}

	@Override
	public void componentResized(ComponentEvent evt) 
	{
//		System.out.println("Planar Image View resized");
		if (zoomMode == ZoomMode.FILL) {
			setBestZoom();
			refreshDisplay();
		}
//		this.scroll.setSize(this.getSize());
	}

	@Override
	public void componentShown(ComponentEvent evt)
	{
	}

    @Override
    public Object getWidget()
    {
        return this.panel;
    }
    
    // ===================================================================
    // Inner class for setting options

    class OptionsPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        // no need to keep reference to parent Viewer, as it is referenced by nesting
        public OptionsPanel()
        {
            setupLayout();
        }
        
        private void setupLayout()
        {
            // layout panels onto main panel
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // create widget for choosing composite type
            DisplayType[] items = EnumSet.allOf(DisplayType.class).toArray(new DisplayType[] {});
            JComboBox<DisplayType> combo = new JComboBox<DisplayType>(items);
            combo.setSelectedItem(DisplayType.MAGENTA_GREEN);
            combo.addItemListener(evt ->
            {
                if (evt.getStateChange() != ItemEvent.SELECTED) return;
                CompositeImagePairViewer.this.displayType = (DisplayType) evt.getItem();
                updateCompositeImage();
                refreshDisplay();
            });
            
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, 2));
            panel.add(new JLabel("Display Type"));
            panel.add(combo);
            
            panel.setBorder(BorderFactory.createEtchedBorder());
            CollapsiblePanel cp = new CollapsiblePanel("Composite Display", panel);
            this.add(cp);
            
            this.add(Box.createVerticalGlue());

            this.invalidate();
        }

    }
}
