/**
 * 
 */
package imago.gui.panel;

import imago.gui.ImageViewer;
import imago.gui.viewer.StackSliceViewer;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import net.sci.image.Image;

/**
 * Display options for image display: current channel/slice/frame, contrast,
 * LUT...
 * 
 * Keeps a reference to the ImageViewer to setup.
 * 
 * Layout: contained within an ImagoDocViewer. 
 *
 * @see imago.gui.ImageViewer
 * @see imago.gui.ImageDocViewer
 * 
 * @author dlegland
 *
 */
public class ImageDisplayOptionsPanel extends JPanel
{
    /**
     * to comply with Swing conventions
     */
    private static final long serialVersionUID = 1L;

    /** 
     * The instance of ImageViewer to setup
     */
    ImageViewer imageViewer;
    
    /** 
     * The Image displayed within the ImageViewer
     */
    Image image;

    
    ValueSliderPanel channelPanel;
    
    ValueSliderPanel zSlicePanel;
    
    
    
    public ImageDisplayOptionsPanel(ImageViewer imageViewer)
    {
        this.imageViewer = imageViewer;
        this.image = imageViewer.getImage();
        
        setupLayout();
    }
    
    private void setupLayout()
    {
        // layout panels onto main panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Intensity, contrast and brightness
        this.add(new JLabel("Image Display Options"));
        
        if (this.image.isVectorImage())
        {
            this.add(createChannelPanel());
        }
        if (this.image.getDimension() > 2)
        {
            this.add(createZSlicePanel());
        }
        this.add(Box.createVerticalGlue());

        this.invalidate();
    }

    private JPanel createChannelPanel()
    {
        // TODO: need to create a specific panel
        this.channelPanel = new ValueSliderPanel("Channel", 0, 2, 2);
        
        channelPanel.slider.addChangeListener(evt ->
        {
            int value = this.channelPanel.slider.getValue();
            String text = String.format("%d", value);
            channelPanel.textField.setText(text);
        });
        
        channelPanel.textField.addActionListener(evt -> 
        {
            String text = this.channelPanel.textField.getText();
            int value = Integer.parseInt(text);
            this.channelPanel.slider.setValue(value);
        });
        
        return this.channelPanel;
    }

    private JPanel createZSlicePanel()
    {
        int zMax = image.getSize(2);
        if (zMax == 1)
        {
            throw new RuntimeException("Requires an image with at least 2 slices");
        }
        
        this.zSlicePanel = new ValueSliderPanel("Z-Slice", 0, zMax-1, 50);
        
        zSlicePanel.slider.addChangeListener(evt ->
        {
//            System.out.println("update slice from slider");
            int sliceIndex = this.zSlicePanel.slider.getValue();
            String text = String.format("%d", sliceIndex);
            zSlicePanel.textField.setText(text);

            StackSliceViewer image3dViewer = (StackSliceViewer) imageViewer;
            image3dViewer.setSliceIndex(sliceIndex);
            image3dViewer.updateSliceImage();
            image3dViewer.refreshDisplay();
//            image3dViewer.repaint();
        });
        
        zSlicePanel.textField.addActionListener(evt -> 
        {
//            System.out.println("update slice from text");
            String text = this.zSlicePanel.textField.getText();
            this.zSlicePanel.slider.setValue(Integer.parseInt(text));
        });
        
        return this.zSlicePanel;
    }

    class ValueSliderPanel extends JPanel
    {
        /**
         * serial ID.
         */
        private static final long serialVersionUID = 1L;
        
        JPanel panel;
        
        int value;
        
        int minValue;
        int maxValue;
        
        JTextField textField;
        
        JSlider slider;

        ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        public ValueSliderPanel(String title, int minValue, int maxValue, int currentValue)
        {
            // init
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.value = currentValue;
            
            // creates widgets
            textField = new JTextField(String.format("%d",  this.value), 5);
            textField.setMaximumSize(new Dimension(80, 20));
            
            slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, value);
            // TODO: determine tick spacing
            slider.setMajorTickSpacing(20);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);

            // setup global layout
            this.setBorder( BorderFactory.createTitledBorder( title ) );
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel textLine = new JPanel();
            textLine.setLayout(new BoxLayout(textLine, BoxLayout.X_AXIS));
            textLine.add(new JLabel("0"));
            textLine.add(Box.createHorizontalGlue());
            textLine.add(textField);
            textLine.add(Box.createHorizontalGlue());
            textLine.add(new JLabel(String.format("%d",  this.maxValue)));

            this.add(textLine);
            this.add(slider);
        }
    }
}
