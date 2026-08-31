/**
 * 
 */
package imago.gui.widgets;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A widget for choosing a numeric value, using either a text field, or one of
 * the two buttons for incrementing or decrementing the value.
 */
public class NumericValueTextIncDecWidget extends AbstractWidget
{
    double value;
    double increment = 1.0;
    
    JTextField textField;
    JButton decButton;
    JButton incButton;
    JPanel panel;
    
    /**
     * Creates a new value initialized with the specified value.
     * 
     * @param initialValue
     *            the initial value for this widget.
     */
    public NumericValueTextIncDecWidget(double initialValue)
    {
        this(initialValue, 1.0);
    }
    
    /**
     * Creates a new value initialized with the specified value and an
     * increment/decrement value.
     * 
     * @param initialValue
     *            the initial value for this widget.
     * @param incrementValue
     *            the value for incrementing or decrementing the widget value
     *            when clicking on the corresponding button.
     */
    public NumericValueTextIncDecWidget(double initialValue, double incrementValue)
    {
        this.value = initialValue;
        this.increment = incrementValue;
        
        // create widgets
        this.textField = new JTextField(doubleToString(initialValue), 10);
        textField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent evt)
            {
                try
                {
                    value = Double.parseDouble(textField.getText());
                }
                catch (NumberFormatException ex)
                {
                    return;
                }
            }
        });
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent evt)
            {
                fireWidgetValueChangeEvent(new WidgetEvent(NumericValueTextIncDecWidget.this));
            }
        });
        
        // decrement value button
        this.decButton = createPlusMinusButton("-", evt -> {
            this.value = this.value - increment;
            this.textField.setText(doubleToString(this.value));
            this.fireWidgetValueChangeEvent(new WidgetEvent(this));
        });
        
        // increment value button
        this.incButton = createPlusMinusButton("+", evt -> {
            this.value = this.value + increment;
            this.textField.setText(doubleToString(this.value));
            this.fireWidgetValueChangeEvent(new WidgetEvent(this));
        });
        
        this.panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(textField);
        panel.add(decButton);
        panel.add(incButton);
    }
    
    private JButton createPlusMinusButton(String label, ActionListener lst)
    {
        JButton button = new JButton(label);
        button.addActionListener(lst);
        return button;
    }
    
    public double getValue()
    {
        return this.value;
    }

    @Override
    public JComponent getComponent()
    {
        return panel;
    }
    
    @Override
    public void setEnabled(boolean b)
    {
        this.textField.setEnabled(b);
        this.decButton.setEnabled(b);
        this.incButton.setEnabled(b);
    }

    private static final String doubleToString(double value)
    {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
}
