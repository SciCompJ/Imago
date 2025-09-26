/**
 * 
 */
package imago.image.viewers;

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

import imago.image.ImageHandle;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import net.sci.geom.Geometry;
import net.sci.image.ImageType;
import net.sci.image.shape.ImageSlicer;

/**
 * A Panel that displays three orthogonal slices of the current 3D image.
 * 
 * @author David Legland
 *
 */
public class OrthoSlicesViewer extends ImageViewer
        implements ChangeListener, ActionListener, ComponentListener
{
    // ===================================================================
    // Class variables

    JPanel panel;

    BufferedImage awtImageXY; // upper left display
    BufferedImage awtImageZY; // upper right display
    BufferedImage awtImageXZ; // lower left display

    ZoomMode zoomMode = ZoomMode.FILL;

    int[] slicesCenter = new int[3];

    // GUI handles
    JScrollPane scrollXY;
    JScrollPane scrollZY;
    JScrollPane scrollXZ;
    ImageDisplay imageDisplayXY;
    ImageDisplay imageDisplayZY;
    ImageDisplay imageDisplayXZ;

    protected ImageTool currentTool = null;

    /**
     * The shape of the current selection, usually a polyline or a rectangle, in
     * pixels coordinates.
     */
    protected Geometry selection = null;
    

    // ===================================================================
    // Constructors

    public OrthoSlicesViewer(ImageHandle handle)
    {
        super(handle);
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
    // Display methods

    public double getZoom()
    {
        return imageDisplayXY.getZoom();
    }

    public void setZoom(double zoom)
    {
        // imageDisplayXY.setZoom(zoom);
        // imageDisplayXY.invalidate();
        // validate();
        // imageDisplayXY.updateOffset();
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
        ImageType type = image.getType();
        awtImageXY = type.createAwtImage(ImageSlicer.slice2d(image, 0, 1, this.slicesCenter));
        awtImageZY = type.createAwtImage(ImageSlicer.slice2d(image, 2, 1, this.slicesCenter));
        awtImageXZ = type.createAwtImage(ImageSlicer.slice2d(image, 0, 2, this.slicesCenter));
    }


    // ===================================================================
    // selection management

    public Geometry getSelection()
    {
        return this.selection;
    }

    public void setSelection(Geometry shape)
    {
        this.selection = shape;
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
        // imageDisplay.updateOffset();
        // Image image = this.getImageToDisplay();
        //
        // this.awtImage = ImageUtils.createAwtImage(image);
        // this.imageDisplay.setBufferedImage(this.awtImage);
        // this.imageDisplay.repaint();
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
        if (zoomMode == ZoomMode.FILL)
        {
            setBestZoom();
            refreshDisplay();
        }
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
}
