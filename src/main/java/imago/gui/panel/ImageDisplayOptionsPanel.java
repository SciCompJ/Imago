/**
 * 
 */
package imago.gui.panel;

import imago.gui.ImageViewer;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import net.sci.image.Calibration;
import net.sci.image.Image;

/**
 * Display options for image display: current channel/slice/frame, contrast,
 * LUT...
 * 
 * Keeps a reference to the ImageViewer to setup.
 * 
 * Layout: contained within an ImageFrame. 
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
    
    ValueSliderPanel framePanel;
    
    JPanel[] axisPanels;
    
    
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
            JPanel panel = createChannelPanel();
            panel.setBorder(BorderFactory.createEtchedBorder());
            this.add(new CollapsiblePanel("Channels Display", panel));
        }
        
        int nd = this.image.getDimension();
        Calibration calib = image.getCalibration();
        this.axisPanels = new JPanel[nd];
        for (int d = 2; d < nd; d++)
        {
            axisPanels[d] = createAxisPanel(d);
            axisPanels[d].setBorder(BorderFactory.createEtchedBorder());
            this.add(new CollapsiblePanel(calib.getAxis(d).getName(), axisPanels[d]));
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
    
    private JPanel createAxisPanel(int d)
    {
        int vMax = image.getSize(d);
        if (vMax <= 1)
        {
            throw new RuntimeException("Requires an image with at least 2 element in direction " + d);
        }
        
        String name = String.format(Locale.ENGLISH, "Axis %d index", d);
        ValueSliderPanel panel = new ValueSliderPanel(name, 0, vMax-1, 0);

        panel.slider.addChangeListener(evt ->
        {
            int sliceIndex = panel.slider.getValue();
            String text = String.format("%d", sliceIndex);
            panel.textField.setText(text);

            imageViewer.setSlicingPosition(d, sliceIndex);
            imageViewer.updateSliceImage();
            imageViewer.refreshDisplay();
        });
        
        panel.textField.addActionListener(evt -> 
        {
            String text = panel.textField.getText();
            panel.slider.setValue(Integer.parseInt(text));
        });
        
        return panel;
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
            int spacing = (int) Math.max(Math.floor((maxValue + 1) / 8), 1);
            slider.setMajorTickSpacing(spacing);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);

            // setup global layout
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            String text = String.format(Locale.ENGLISH, "%s (%d->%d)     ", title, minValue, maxValue);
            JPanel textLine = new JPanel();
            textLine.setLayout(new BoxLayout(textLine, BoxLayout.X_AXIS));
            textLine.add(new JLabel(text));
            textLine.add(textField);
            textLine.add(Box.createHorizontalGlue());

            this.add(textLine);
            this.add(slider);
        }
    }
}