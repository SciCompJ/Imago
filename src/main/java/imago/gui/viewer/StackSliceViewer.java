/**
 * 
 */
package imago.gui.viewer;

import imago.gui.ImageViewer;
import imago.gui.ImagoTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.BufferedImageUtils;
import net.sci.image.Image;


/**
 * A Panel that displays a single slice of the current 3D image.
 * 
 * @author David Legland
 *
 */
public class StackSliceViewer extends ImageViewer implements ChangeListener, ActionListener, ComponentListener 
{	
	// ===================================================================
	// Class variables

	JPanel panel;
	
	BufferedImage awtImage;

	ZoomMode zoomMode = ZoomMode.FILL;
		
	int sliceIndex = 0;

	// GUI items
	JScrollPane scroll;
	ImageDisplay imageDisplay;
	JSlider sliceSlider;
	JTextField sliceEdit;
	
	/**
     * The shape of the current selection, usually a polyline or a rectangle, in pixels coordinates.
     */
    protected Geometry2D selection = null;

	protected ImagoTool currentTool = null;

	
	// ===================================================================
	// Constructors

	public StackSliceViewer(Image image) 
	{
		super(image);
		int depth = image.getSize(2);
		sliceIndex = (int) Math.floor(depth / 2);
		
		awtImage = BufferedImageUtils.createAwtImage(image, sliceIndex);
		setupLayout();
	}
	
	public StackSliceViewer(Image image, int sliceIndex) 
	{
		super(image);
		this.sliceIndex = sliceIndex;
		
		awtImage = BufferedImageUtils.createAwtImage(image, sliceIndex);
		setupLayout();
	}
	
	private void setupLayout() 
	{
	    this.panel = new JPanel();
	    this.panel.setBackground(Color.WHITE);
	    
		// create the main display panel
		this.imageDisplay = new ImageDisplay(awtImage);
		
		// encapsulate the display into a scroll panel
		scroll = new JScrollPane(this.imageDisplay);
		scroll.setBackground(Color.WHITE);

		// create a slider for changing slice state
		int nz = image.getSize(2);
		int z0 = (int) Math.round(nz / 2.0);
		sliceSlider = new JSlider(JSlider.VERTICAL, 0, nz-1, z0);
		sliceSlider.setInverted(true);
		sliceSlider.setMinorTickSpacing(10);
		sliceSlider.setMajorTickSpacing(nz-1);
		sliceSlider.setPaintLabels(true);
		sliceSlider.addChangeListener(this);

		// create a text field for changing slice state
		sliceEdit = new JTextField(Integer.toString(z0), 4);
		sliceEdit.setHorizontalAlignment(JTextField.CENTER);
		sliceEdit.addActionListener(this);
		
		// Setup the general layout
		this.panel.setLayout(new BorderLayout());
		this.panel.add(scroll, BorderLayout.CENTER);
		JPanel sliderPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Slice");
		label.setHorizontalAlignment(JTextField.CENTER);
		sliderPanel.add(label, BorderLayout.NORTH);
		sliderPanel.add(sliceSlider, BorderLayout.CENTER);
		sliderPanel.add(sliceEdit, BorderLayout.SOUTH);
		this.panel.add(sliderPanel, BorderLayout.WEST);
		
		// Add listeners
		this.panel.addComponentListener(this);
	}
	
	// ===================================================================
	// General methods

	public int getSliceIndex() 
	{
		return sliceIndex;
	}

	public void setSliceIndex(int index)
	{
		this.sliceIndex = index;
		
		// update widgets
		String txt = Integer.toString(index);
		this.sliceSlider.setValue(index);
		this.sliceEdit.setText(txt);
		
//		// also update document containing the image
//		ImagoDocViewer viewer = this.getViewer();
//		if (viewer != null)
//		{
//			ImagoDoc doc = viewer.getDocument();
//			doc.setCurrentSliceIndex(index);
//		}
	}

	public ImageDisplay getImageDisplay() 
	{
		return imageDisplay;
	}
	
	
    // ===================================================================
    // Selection management

	public Geometry2D getSelection()
    {
        return this.selection;
    }

    public void setSelection(Geometry2D shape)
    {
        this.selection = shape;
    }
	    
	// ===================================================================
	// Display methods

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
		double ratioX = ((double) dim0.width - 5) / ((double) (image.getSize(0)));
		double ratioY = ((double) dim0.height - 5) / ((double) (image.getSize(1)));
		double zoom = Math.min(ratioX, ratioY);
		setZoom(zoom);
	}

	/**
	 * Recompute the AWT slice image from current index.
	 */
	public void updateSliceImage() 
	{
//	    System.out.println("update slice image");
		awtImage = BufferedImageUtils.createAwtImage(image, sliceIndex);
		imageDisplay.setBufferedImage(awtImage);
	}

	// ===================================================================
	// tool management

	@Override
	public ImagoTool getCurrentTool() 
	{
		return currentTool;
	}

	@Override
	public void setCurrentTool(ImagoTool tool) 
	{
		if (currentTool != null)
		{
			imageDisplay.removeMouseListener(currentTool);
			imageDisplay.removeMouseMotionListener(currentTool);
		}
		currentTool = tool;
		
		if (currentTool != null)
		{
			imageDisplay.addMouseListener(currentTool);
			imageDisplay.addMouseMotionListener(currentTool);
		}
	}

    public void refreshDisplay()
    {
        imageDisplay.updateOffset();
        Image image = this.getImageToDisplay();
//        System.out.println("refresh StackSLiceViewer display");
        
        this.awtImage = BufferedImageUtils.createAwtImage(image, this.sliceIndex);
        this.imageDisplay.setBufferedImage(this.awtImage);
        this.imageDisplay.repaint();
    }

    // ===================================================================
	// Implementation of StateListener interface

	@Override
	public void stateChanged(ChangeEvent evt)
	{
		int index = sliceSlider.getValue();
		this.setSliceIndex(index);
		updateSliceImage();
		this.panel.repaint();
	}

	// ===================================================================
	// Implementation of ActionListener interface

	public void actionPerformed(ActionEvent evt) 
	{
	    String text = sliceEdit.getText();
	    int index = Integer.parseInt(text);
	    if (index < 0 || index >= this.image.getSize(2)) 
	    {
			this.setSliceIndex(sliceIndex);
	    	return;
	    }
	    
		this.setSliceIndex(index);
		updateSliceImage();
		this.panel.repaint();
	}
	
	// ===================================================================
	// Implementation of Component Listener

	@Override
	public void componentHidden(ComponentEvent evt)	{}

	@Override
	public void componentMoved(ComponentEvent evt) {}

	@Override
	public void componentResized(ComponentEvent evt) 
	{
		if (zoomMode == ZoomMode.FILL) 
		{
			setBestZoom();
			refreshDisplay();
		}
	}

	@Override
	public void componentShown(ComponentEvent evt) {}

    @Override
    public Object getWidget()
    {
        return this.panel;
    }
}
