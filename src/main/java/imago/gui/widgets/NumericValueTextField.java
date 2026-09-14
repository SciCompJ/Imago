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
    
    String format;
    
    JTextField textField;
    
    /**
     * Creates a new numeric textfield initialized with the specified value.
     * 
     * @param initialValue
     *            the initial value for this widget.
     */
    public NumericValueTextField(double initialValue)
    {
        this(initialValue, 2);
    }
    
    /**
     * Creates a new numeric textfield initialized with the specified value, and
     * using the specified number of digits for representing the numeric value.
     * 
     * @param initialValue
     *            the initial value for this widget.
     * @param nDigits
     *            the number of decimal digits (0 for integer number)
     */
    public NumericValueTextField(double initialValue, int nDigits)
    {
        this.value = initialValue;
        this.format = "%." + nDigits + "f";
        
        // create widgets
        this.textField = new JTextField(formatNumber(initialValue), 10);
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

    private String formatNumber(double value)
    {
        return String.format(Locale.ENGLISH, format, value);
    }
}
