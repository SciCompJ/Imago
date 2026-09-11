/**
 * 
 */
package imago.image;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import imago.gui.panels.CollapsiblePanel;
import imago.image.render.VectorImageChannelRenderer;
import imago.image.render.VectorImageMaxNormRenderer;
import imago.image.render.VectorImageNormRenderer;
import net.sci.array.Array;
import net.sci.array.numeric.Vector;
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
 * @see imago.image.ImageViewer
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

    int currentChannelIndex = 0;

    ValueSliderPanel channelIndexPanel;
    
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
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            
            panel.add(createVectorImageDisplayTypeCombo());
            panel.add(createChannelPanel());
            
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

    private JComboBox<String> createVectorImageDisplayTypeCombo()
    {
        String[] choices = new String[] {"Single Channel", "Norm", "Max Norm"};
        JComboBox<String> cb = new JComboBox<String>(choices);
        cb.addActionListener(evt -> {
            int index = cb.getSelectedIndex();
            ImageDataRenderer renderer = switch(index)
            {
                case 0 -> new VectorImageChannelRenderer().setChannelIndex(currentChannelIndex);
                case 1 -> new VectorImageNormRenderer();
                case 2 -> new VectorImageMaxNormRenderer();
                default -> throw new RuntimeException("Out of Bound index: " + index);
            };
            channelIndexPanel.setEnabled(index == 0);
            imageViewer.setRenderer(renderer);
            imageViewer.refreshDisplay();
        });
        return cb;
    }
    
    private JPanel createChannelPanel()
    {
        int nChannels = countChannels(image.getData());
        
        int channelIndex = 0;
        if (imageViewer.getRenderer() instanceof VectorImageChannelRenderer vicr)
        {
            currentChannelIndex  = vicr.getChannelIndex();
        }
        if (currentChannelIndex >= nChannels)
        {
            currentChannelIndex = nChannels - 1;
            setCurrentChannelIndex(currentChannelIndex);
        }
        this.channelIndexPanel = new ValueSliderPanel("Channel", 0, nChannels - 1, channelIndex);
        
        channelIndexPanel.slider.addChangeListener(evt ->
        {
            int index = this.channelIndexPanel.slider.getValue();
            String text = String.format("%d", index);
            channelIndexPanel.textField.setText(text);
            setCurrentChannelIndex(index);
            imageViewer.refreshDisplay();
        });
        
        channelIndexPanel.textField.addActionListener(evt -> 
        {
            String text = this.channelIndexPanel.textField.getText();
            int index = Integer.parseInt(text);
            this.channelIndexPanel.slider.setValue(index);
            setCurrentChannelIndex(index);
            imageViewer.refreshDisplay();
        });
        
        return this.channelIndexPanel;
    }
    
    private void setCurrentChannelIndex(int index)
    {
        this.currentChannelIndex = index;
        if (imageViewer.getRenderer() instanceof VectorImageChannelRenderer vicr)
        {
            vicr.setChannelIndex(currentChannelIndex);
        }
    }
    
    private int countChannels(Array<?> array)
    {
        if (array.sampleElement() instanceof Vector vect)
        {
            return vect.size();
        }
                
        throw new RuntimeException("Unable to count channels...");
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
        
        int value;
        
        int minValue;
        int maxValue;
        
        JLabel label;
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
            
            String text = String.format(Locale.ENGLISH, "%s (%d->%d)     ", title, minValue, maxValue);
            this.label = new JLabel(text);
            
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
            JPanel textLine = new JPanel();
            textLine.setLayout(new BoxLayout(textLine, BoxLayout.X_AXIS));
            textLine.add(label);
            textLine.add(textField);
            textLine.add(Box.createHorizontalGlue());

            this.add(textLine);
            this.add(slider);
        }
        
        public void setEnabled(boolean b)
        {
            super.setEnabled(b);
            
            this.label.setEnabled(b);
            this.textField.setEnabled(b);
            this.slider.setEnabled(b);
        }
    }
}
