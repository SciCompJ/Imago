/**
 * 
 */
package imago.image.viewers;

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

import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.ShapeNode;
import imago.app.shape.Shape;
import imago.image.ImageHandle;
import imago.image.ImageTool;
import imago.image.ImageViewer;
import net.sci.geom.Geometry;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.image.Image;
import net.sci.image.shape.ImageSlicer;

/**
 * A Panel that displays a single slice of the current 3D image.
 * 
 * @author David Legland
 *
 */
public class StackSliceViewer extends ImageViewer
        implements ChangeListener, ActionListener, ComponentListener
{
    // ===================================================================
    // Class variables

    JPanel panel;

    BufferedImage awtImage;

    ZoomMode zoomMode = ZoomMode.FILL;

    // GUI handles
    JScrollPane scroll;
    ImageDisplay imageDisplay;
    JSlider sliceSlider;
    JTextField sliceEdit;

    /**
     * The shape of the current selection, usually a polyline or a rectangle, in
     * pixels coordinates.
     */
    protected Geometry selection = null;

    protected ImageTool currentTool = null;

    
    // ===================================================================
    // Constructors

    public StackSliceViewer(ImageHandle handle)
    {
        super(handle);

        recomputeAwtImage();
        setupLayout();
    }

    public StackSliceViewer(ImageHandle handle, int sliceIndex)
    {
        super(handle);
        this.slicingPosition[2] = sliceIndex;

        recomputeAwtImage();
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
        sliceSlider = new JSlider(JSlider.VERTICAL, 0, nz - 1, z0);
        sliceSlider.setInverted(true);
        // sliceSlider.setMinorTickSpacing(10);
        // sliceSlider.setMajorTickSpacing(nz-1);
        sliceSlider.setPaintTicks(true);
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

    public void setSlicingPosition(int index, int pos)
    {
        this.slicingPosition[index] = pos;

        // update widgets
        String txt = Integer.toString(pos);
        this.sliceSlider.setValue(pos);
        this.sliceEdit.setText(txt);
    }

    public ImageDisplay getImageDisplay()
    {
        return imageDisplay;
    }

    // ===================================================================
    // Selection management

    public Geometry getSelection()
    {
        return this.selection;
    }

    public void setSelection(Geometry selection)
    {
        this.selection = selection;
        if (selection == null || selection instanceof Geometry2D)
        {
            this.imageDisplay.selection = (Geometry2D) selection;
        }
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
        // System.out.println("update slice image");
        recomputeAwtImage();
        imageDisplay.setBufferedImage(this.awtImage);
    }

    private void recomputeAwtImage()
    {
        Image image = this.getImageToDisplay();
        Image slice = ImageSlicer.slice2d(image, this.slicingPosition[2]);
        this.awtImage = slice.getType().createAwtImage(slice);
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
        updateSliceImage();
        this.imageDisplay.repaint();

        refreshSceneGraph();
    }

    private void refreshSceneGraph()
    {
        if (this.imageHandle == null)
        {
            return;
        }

        this.imageDisplay.clearSceneGraphItems();
        if (!this.displaySceneGraph)
        {
            return;
        }

        Node rootNode = this.imageHandle.getRootNode();
        if (rootNode == null)
        {
            return;
        }

        displaySceneGraphNode(rootNode);
    }

    private void displaySceneGraphNode(Node node)
    {
        if (!node.isVisible())
        {
            return;
        }

        if (node instanceof ImageSliceNode)
        {
            // check slice index to display only items of current slice
            int index = ((ImageSliceNode) node).getSliceIndex();
            if (index != this.getSlicingPosition(2))
            {
                return;
            }
            for (Node child : node.children())
            {
                displaySceneGraphNode(child);
            }
        }
        else if (!node.isLeaf())
        {
            for (Node child : node.children())
            {
                displaySceneGraphNode(child);
            }
        }
        else if (node instanceof ShapeNode)
        {
            Geometry geom = ((ShapeNode) node).getGeometry();
            if (geom instanceof Geometry2D)
            {
                Shape shape = new Shape(geom, ((ShapeNode) node).getStyle());
                this.imageDisplay.addSceneGraphItem(shape);
            }
        }
        else
        {
            return;
        }
    }
    

    // ===================================================================
    // Implementation of StateListener interface

    @Override
    public void stateChanged(ChangeEvent evt)
    {
        int index = sliceSlider.getValue();
        this.setSlicingPosition(2, index);
        updateSliceImage();
        this.panel.repaint();
        refreshSceneGraph();// TODO: split refresh graph and draw graph
    }

    
    // ===================================================================
    // Implementation of ActionListener interface

    public void actionPerformed(ActionEvent evt)
    {
        String text = sliceEdit.getText();
        int index = Integer.parseInt(text);
        if (index < 0 || index >= this.image.getSize(2))
        {
            return;
        }

        this.setSlicingPosition(2, index);
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
