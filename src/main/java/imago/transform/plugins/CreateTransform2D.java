/**
 * 
 */
package imago.transform.plugins;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Transform2D;

/**
 * 
 */
public class CreateTransform2D implements FramePlugin
{
    // ===================================================================
    // Class properties
    
    ImagoFrame parentFrame;
    
    /** the translation vector (in pixels) */
    NumericProperty xShiftProperty;
    NumericProperty yShiftProperty;
    NumericProperty rotAngleProperty;
    NumericProperty logScaleProperty;

    /** The transform to create */
    Transform2D transform = AffineTransform2D.IDENTITY;
   
    JFrame pluginFrame = null;

    JComboBox<String> transformTypeCombo;
    List<JComponent> xShiftWidgets;
    List<JComponent> yShiftWidgets;
    List<JComponent> rotAngleWidgets;
    List<JComponent> logScaleWidgets;

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        this.parentFrame = frame;
        
        // build control frame
        this.pluginFrame = new JFrame("Create Transform");
        if (parentFrame != null)
        {
            Point pos = parentFrame.getWidget().getLocation();
            this.pluginFrame.setLocation(pos.x + 30, pos.y + 20);
        }
        
        initWidgets();
        setupLayout(pluginFrame);
        
        pluginFrame.pack();
        pluginFrame.setVisible(true);
    }
    
    public void onOKButtonClicked()
    {
        int transfoIndex = this.transformTypeCombo.getSelectedIndex();
        double xShift = this.xShiftProperty.getValue();
        double yShift = this.yShiftProperty.getValue();
        double rotAngle = this.rotAngleProperty.getValue();
        double logScale = this.logScaleProperty.getValue();
        double scaling = Math.pow(2.0, logScale);
        
        AffineTransform2D tra = AffineTransform2D.createTranslation(xShift, yShift);
        this.transform = switch (transfoIndex)
        {
            case 0 -> tra;
            case 1 -> tra.compose(AffineTransform2D.createRotation(rotAngle));
            case 2 -> tra.compose(AffineTransform2D.createRotation(rotAngle))
                    .compose(AffineTransform2D.createScaling(scaling, scaling));
            default -> throw new RuntimeException(
                    "This transformation is not implemented: " + this.transformTypeCombo.getSelectedItem());
        };
        
        ImagoGui gui = this.parentFrame.getGui();
        String name = this.transformTypeCombo.getSelectedItem().toString();
        TransformHandle.create(gui.getAppli(), transform, name);
        
        if (TransformManager.hasInstance(gui))
        {
            TransformManager mgr = TransformManager.getInstance(gui);
            mgr.repaint();
        }
        
        closeFrame();
    }
    
    public void closeFrame()
    {
        pluginFrame.setVisible(false);
        pluginFrame.dispose();
    }


    private void initWidgets()
    {
        this.transformTypeCombo = new JComboBox<String>();
        this.transformTypeCombo.addItem("Translation");
        this.transformTypeCombo.addItem("Rotation");
        this.transformTypeCombo.addItem("Scaling");
        this.transformTypeCombo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) 
            {
                updateWidgets();
            }
        });
        
        xShiftProperty = new NumericProperty("Shift X (pixels)", 0.0, 2, 1.0);
        yShiftProperty = new NumericProperty("Shift Y (pixels)", 0.0, 2, 1.0);
        rotAngleProperty = new NumericProperty("Rotation angle (degrees)", 0.0, 2, 1.0);
        logScaleProperty = new NumericProperty("Log_2 of scaling factor", 0.0, 3, 0.01);
    }
    
    private void updateWidgets()
    {
        enable(rotAngleWidgets, transformTypeCombo.getSelectedIndex() > 0);
        enable(logScaleWidgets, transformTypeCombo.getSelectedIndex() > 1);
    }
    
    private void enable(List<JComponent> widgets, boolean state)
    {
        for (JComponent w : widgets)
        {
            w.setEnabled(state);
        }
    }

    private JTextField createNumericTextField(NumericProperty prop)
    {
        String format = "%." + prop.nDigits + "f"; 
        return createNumericTextField(prop, format);
    }
    
    private JTextField createNumericTextField(NumericProperty prop, String format)
    {
        String text = String.format(Locale.ENGLISH, format, prop.getValue());
        JTextField textField = new JTextField(text, 10);
        
        textField.addActionListener(evt -> 
        {
            double value = Double.parseDouble(textField.getText());
            if (Double.isFinite(value))
            {
                prop.setValue(value);
            }
        });
        
        textField.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e) 
            {
                // set the whole text as selected
                textField.setSelectionStart(0);
                textField.setSelectionEnd(textField.getText().length());
            }
            
            @Override
            public void focusLost(FocusEvent e)
            {
                double value = Double.parseDouble(textField.getText());
                if (Double.isFinite(value))
                {
                    prop.setValue(value);
                }
            }
        });
        
        prop.addPropertyListener(
                (prp, oldValue, newValue) -> textField.setText(String.format(Locale.ENGLISH, format, newValue)));
        
        return textField;
    }

    private void setupLayout(JFrame frame)
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(5, 2));
        optionsPanel.add(new JLabel("Transform Type:"));
        optionsPanel.add(transformTypeCombo);
        this.xShiftWidgets = addNumericPropertyPanel(optionsPanel, xShiftProperty);
        this.yShiftWidgets = addNumericPropertyPanel(optionsPanel, yShiftProperty);
        this.rotAngleWidgets = addNumericPropertyPanel(optionsPanel, rotAngleProperty);
        this.logScaleWidgets = addNumericPropertyPanel(optionsPanel, logScaleProperty);
        updateWidgets();
        
        mainPanel.add(optionsPanel);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(createButton("OK", evt -> this.onOKButtonClicked()));
        buttonsPanel.add(createButton("Cancel", evt -> closeFrame()));
        
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private List<JComponent> addNumericPropertyPanel(JPanel optionsPanel, NumericProperty prop)
    {
        // create widgets
        JLabel label = new JLabel(prop.label);
        JTextField tf = createNumericTextField(prop); 
        JButton decButton = createPlusMinusButton("-", evt -> prop.setValue(prop.getValue() - prop.step));
        JButton incButton = createPlusMinusButton("+", evt -> prop.setValue(prop.getValue() + prop.step));
        
        // add widgets to container, by separating the label and the other widgets
        JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subPanel.add(tf);
        subPanel.add(decButton);
        subPanel.add(incButton);
        optionsPanel.add(label);
        optionsPanel.add(subPanel);
        
        // returns the list of created widgets
        return List.of(label, subPanel, tf, decButton, incButton);
    }
        
    private static final JButton createPlusMinusButton(String label, ActionListener lst)
    {
        JButton button = createButton(label, lst);
        Insets insets = button.getMargin();
        insets.left = insets.right = 7;
        button.setMargin(insets);
        return button;
    }
    
    private static final JButton createButton(String label, ActionListener lst)
    {
        JButton button = new JButton(label);
        button.addActionListener(lst);
        return button;
    }
    
    class Property<T>
    {
        ArrayList<PropertyListener<T>> listeners;
        
        protected Property()
        {
            this.listeners = new ArrayList<>();
        }
        
        public void addPropertyListener(PropertyListener<T> lst)
        {
            this.listeners.add(lst);
        }
        
        public void removePropertyListener(PropertyListener<T> lst)
        {
            this.listeners.remove(lst);
        }

    }
    
    class NumericProperty extends Property<Double>
    {
        String label;
        double value;
        int nDigits = 0;
        double step = 1.0;
        
        public NumericProperty(String label, double value)
        {
            this(label, value, 0, 1.0);
        }
        
        public NumericProperty(String label, double value, int nDigits, double incStep)
        {
            this.label = label;
            this.value = value;
            this.nDigits = nDigits;
            this.step = incStep;
        }
        
        public double getValue()
        {
            return this.value;
        }
        
        public void setValue(double newValue)
        {
            double oldValue = this.value;
            this.value = newValue;
            for (PropertyListener<Double> lst : listeners)
            {
                lst.propertyValueChanged(this, oldValue, newValue);
            }
        }
    }
    
    /**
     * A basic interface of listening property changes, based on JavaFX
     * {@code ChangeListener} interface.
     * 
     * @param <T>
     *            the type of data stored by the property
     */
    interface PropertyListener<T>
    {
        public void propertyValueChanged(Property<T> prop, T oldValue, T newValue);
    }
}
