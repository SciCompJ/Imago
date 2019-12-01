/**
 * 
 */
package imago.gui.viewer;

import imago.gui.ImageViewer;
import imago.gui.ImagoTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sci.image.BufferedImageUtils;
import net.sci.image.Image;
import net.sci.image.process.shape.ImageSlicer;


/**
 * A Panel that displays three orthogonal slices of the current 3D image.
 * 
 * @author David Legland
 *
 */
public class OrthoSlicesViewer extends ImageViewer implements ChangeListener, ActionListener, ComponentListener 
{
	// ===================================================================
	// Class variables

    JPanel panel;
    
    BufferedImage awtImageXY; // upper left display
    BufferedImage awtImageZY; // upper right display
    BufferedImage awtImageXZ; // lower left display

	ZoomMode zoomMode = ZoomMode.FILL;
		
	int[] slicesCenter = new int[3];

	// GUI items
	JScrollPane scrollXY;
	JScrollPane scrollZY;
	JScrollPane scrollXZ;
    ImageDisplay imageDisplayXY;
    ImageDisplay imageDisplayZY;
    ImageDisplay imageDisplayXZ;
    
	protected ImagoTool currentTool = null;

	
	// ===================================================================
	// Constructors

	public OrthoSlicesViewer(Image image) 
	{
		super(image);
		int[] dims = image.getSize();
		for (int i = 0; i < 3; i++)
		{
		    this.slicesCenter[i] = (int) Math.floor(dims[i] / 2);
		}

		// create AWT images corresponding to each slice
		updateSliceImage();
		
		setupLayout();
	}
	
	private void setupLayout() 
	{
	    this.panel = new JPanel();
	    this.panel.setBackground(Color.MAGENTA); //TODO: temporary
	    
		// create the main display panel
        this.imageDisplayXY = new ImageDisplay(awtImageXY);
        this.imageDisplayXZ = new ImageDisplay(awtImageXZ);
        this.imageDisplayZY = new ImageDisplay(awtImageZY);
		
		// encapsulate the displays into a scroll panel
        scrollXY = new JScrollPane(this.imageDisplayXY);
        scrollXY.setBackground(Color.RED);
        scrollXZ = new JScrollPane(this.imageDisplayXZ);
        scrollXZ.setBackground(Color.GREEN);
        scrollZY = new JScrollPane(this.imageDisplayZY);
        scrollZY.setBackground(Color.BLUE);

		// Setup the general layout
        this.panel.setLayout(new BorderLayout());
		
		JPanel displayPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		displayPanel.add(scrollXY, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        displayPanel.add(scrollZY, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        displayPanel.add(scrollXZ, c);

        this.panel.add(displayPanel, BorderLayout.CENTER);
		
		// Add listeners
        this.panel.addComponentListener(this);
	}
	
	// ===================================================================
	// General methods

	// ===================================================================
	// Display methods

	public double getZoom() 
	{
		return imageDisplayXY.getZoom();
	}
	
	public void setZoom(double zoom) 
	{
//		imageDisplayXY.setZoom(zoom);		
//		imageDisplayXY.invalidate();
//		validate();
//		imageDisplayXY.updateOffset();		
	}

	/**
	 * Computes the zoom factor that best fits the image within the limits of
	 * the panel.
	 */
	public void setBestZoom() 
	{
		Dimension dim0 = scrollXY.getSize();
		double ratioX = ((double) dim0.width - 5) / ((double) (image.getSize(0)));
		double ratioY = ((double) dim0.height - 5) / ((double) (image.getSize(1)));
		double zoom = Math.min(ratioX, ratioY);
		setZoom(zoom);
	}

	/**
	 * Recompute the AWT slice image from current slice positions.
	 */
	public void updateSliceImage() 
	{
        // create AWT images corresponding to each slice
        awtImageXY = BufferedImageUtils.createAwtImage(ImageSlicer.slice2d(image, 0, 1, this.slicesCenter));
        awtImageZY = BufferedImageUtils.createAwtImage(ImageSlicer.slice2d(image, 2, 1, this.slicesCenter));
        awtImageXZ = BufferedImageUtils.createAwtImage(ImageSlicer.slice2d(image, 0, 2, this.slicesCenter));
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
			imageDisplayXY.removeMouseListener(currentTool);
			imageDisplayXY.removeMouseMotionListener(currentTool);
		}
		currentTool = tool;
		
		if (currentTool != null)
		{
			imageDisplayXY.addMouseListener(currentTool);
			imageDisplayXY.addMouseMotionListener(currentTool);
		}
	}

    public void refreshDisplay()
    {
//        imageDisplay.updateOffset();
//        Image image = this.getImageToDisplay();
//        
//        this.awtImage = ImageUtils.createAwtImage(image);
//        this.imageDisplay.setBufferedImage(this.awtImage);
//        this.imageDisplay.repaint();
    }

    // ===================================================================
	// Implementation of StateListener interface

	@Override
	public void stateChanged(ChangeEvent evt)
	{
	}

	// ===================================================================
	// Implementation of ActionListener interface

	public void actionPerformed(ActionEvent evt) 
	{
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
