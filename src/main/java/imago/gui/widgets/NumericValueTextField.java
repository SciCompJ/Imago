/**
 * 
 */
package imago.gui.widgets;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Encapsulates a text field containing a numeric value, and manages key events
 * as well as conversion to numeric value.
 */
public class NumericValueTextField extends AbstractWidget
{
    double value;
    
    JTextField textField;
    
    /**
     * Creates a new value initialized with the specified value.
     * 
     * @param initialValue
     *            the initial value for this widget.
     */
    public NumericValueTextField(double initialValue)
    {
        this.value = initialValue;
        
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
                fireWidgetValueChangeEvent(new WidgetEvent(NumericValueTextField.this));
            }
        });
    }
    
    public double getValue()
    {
        return this.value;
    }

    @Override
    public JComponent getComponent()
    {
        return textField;
    }
    
    @Override
    public void setEnabled(boolean b)
    {
        this.textField.setEnabled(b);
    }

    private static final String doubleToString(double value)
    {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
}
