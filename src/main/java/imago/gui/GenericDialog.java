/**
 * 
 */
package imago.gui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * <p>
 * A generic dialog that allows easy addition of common widgets. The design of
 * this class is strongly inspired by the corresponding ImageJ class.
 * </p>
 * 
 * <p>
 * Widgets:
 * <ul>
 * <li>Text field for numeric values, eventually with unit</li>
 * <li>Check box for boolean values</li>
 * <li>Choice between several strings, or several enums</li>
 * <li>Text field</li>
 * </ul>
 * </p>
 * 
 * @author David Legland
 * 
 */
public class GenericDialog
		implements ActionListener, AdjustmentListener, CaretListener,
		FocusListener, InputMethodListener, KeyListener, WindowListener
{
	// TODO: thinking about splitting GUI and model parts ?

	
	private static final int MAX_SLIDERS = 25;

	
	/** The dialog instance containing widgets */
	private Dialog dialog;

	public enum Output {
		OK, CANCEL;
	};
	private Output output = Output.CANCEL;
	
	GridBagLayout gridLayout;
	GridBagConstraints c;
	private boolean firstNumericField = true;
		
	private int currentRow = 0;

	private int numericFieldIndex;
	private int textFieldIndex;
	private int checkBoxIndex;
	private int comboBoxIndex;

    ArrayList<JTextField> stringFields;
    ArrayList<JTextField> numericFields;
    ArrayList<JCheckBox> checkBoxes;
    ArrayList<JComboBox<String>> choices;
    ArrayList<JScrollBar> scrollBars;
    int[] sliderIndexes;
    double[] sliderScales;
    
    boolean buttonsCreated = false;
    JButton okButton;
    JButton cancelButton;
    
    boolean wasOKed;
    boolean wasCanceled;
    
    boolean updateWidgets = false;
   
	/**
	 * Creates a new GenericDialog, located with respect to parent frame, and
	 * with given title.
	 */
	public GenericDialog (JFrame parent, String title) 
	{
		this.dialog = new JDialog(parent, title, true);
		
		// setup global layout
		gridLayout = new GridBagLayout();
		c = new GridBagConstraints();
		this.dialog.setLayout(gridLayout);

		// add some listeners
		this.dialog.addKeyListener(this);
		this.dialog.addWindowListener(this);
		
		// setup location
		if (parent != null)
		{
			Point pos = parent.getLocation();
			Dimension dim = parent.getSize();
			int x = pos.x + dim.width / 4;
			int y = pos.y + dim.height / 4;
			this.dialog.setLocation(x, y);
		}
	}
	
	/**
	 * Kept for compatibility, but it is better to specify parent frame.
	 */
	public GenericDialog (String title) 
	{
		this((JFrame) null, title);
	}

	/**
	 * Adds a numeric field.
	 */
	public void addNumericField(String label, double defaultValue, int digits) 
	{
		addNumericField(label, defaultValue, digits, 6, null);
	}

	/**
	 * Adds a numeric field.
	 */
	public void addNumericField(String label, double defaultValue, int digits, 
			String tooltip) 
	{
		addNumericField(label, defaultValue, digits, 6, null);
	}

	public void addNumericField(String label, double defaultValue, int digits,
			int columns, String units)
	{
		addNumericField(label, defaultValue, digits, columns, units, null);
	}
		
	/** 
	 * Adds a numeric field. 
	 * @param label			the label
	 * @param defaultValue	state to be initially displayed
	 * @param digits			number of digits to right of decimal point
	 * @param columns		width of field in characters
	 * @param units			a string displayed to the right of the field
	 */
	public void addNumericField(String label, double defaultValue, int digits,
			int columns, String units, String toolTip) 
	{
		// creates the label widget
		JLabel labelItem = new JLabel(formatLabel(label));
//		if (toolTip != null) 
//		{
//			labelItem.setToolTipText(toolTip);
//		}
		
		c.gridx = 0;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		
//		if (firstNumericField)
//			c.insets = getInsets(5, 0, 3, 0);
//		else
//			c.insets = getInsets(0, 0, 3, 0);
		if (firstNumericField)
			c.insets = getInsets(5, 5, 3, 5);
		else
			c.insets = getInsets(0, 5, 3, 5);
		gridLayout.setConstraints(labelItem, c);
		this.dialog.add(labelItem);
		
		if (numericFields == null) 
		{
			numericFields = new ArrayList<JTextField>(5);
//			defaultValues = new Vector(5);
//			defaultText = new Vector(5);
		}

		String text = formatNumber(defaultValue, digits);
		JTextField tf = createNumericTextField(text, columns);
//		if (toolTip != null) 
//		{
//			labelItem.setToolTipText(toolTip);
//		}
		
		numericFields.add(tf);
//		defaultValues.addElement(new Double(defaultValue));
//		defaultText.addElement(tf.getText());
		c.gridx = 1;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.WEST;
		tf.setEditable(true);
		
		if (firstNumericField) tf.selectAll();
		firstNumericField = false;
		if (units == null || units.equals("")) 
		{
			gridLayout.setConstraints(tf, c);
			this.dialog.add(tf);
		} 
		else 
		{
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			panel.add(tf);
			panel.add(new JLabel(" " + units));
			gridLayout.setConstraints(panel, c);
			this.dialog.add(panel);
		}
//		if (Recorder.record || macro)
//			saveLabel(tf, label);
		currentRow++;
	}

	/** 
	 * Adds an 8 column text field.
	 * @param label			the label
	 * @param defaultText		the text initially displayed
	 */
	public void addTextField(String label, String text) 
	{
		addTextField(label, text, 8);
	}

	/**
	 * Adds a text field.
	 * 
	 * @param label
	 *            the label
	 * @param defaultText
	 *            text initially displayed
	 * @param columns
	 *            width of the text field
	 */
	public void addTextField(String label, String text, int columns) 
	{
		JLabel theLabel = new JLabel(formatLabel(label));

		c.gridx = 0;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		if (stringFields == null) 
		{
			stringFields = new ArrayList<JTextField>(4);
			c.insets = getInsets(5, 5, 5, 5);
		}
		else
		{
			c.insets = getInsets(0, 5, 5, 5);
		}
		gridLayout.setConstraints(theLabel, c);
		this.dialog.add(theLabel);

//		boolean custom = customInsets;
//		if (custom) {
//			if (stringFields.size() == 0)
//				c.insets = getInsets(5, 0, 5, 0);
//			else
//				c.insets = getInsets(0, 0, 5, 0);
//		}

		// creates text field component
		JTextField tf = createTextField(text, columns);
		// tf.setEchoChar(echoChar);
		// echoChar = 0;
		
		c.gridx = 1;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.WEST;
		gridLayout.setConstraints(tf, c);
		tf.setEditable(true);
		this.dialog.add(tf);
		stringFields.add(tf);

		// if (Recorder.record || macro)
		// saveLabel(tf, label);
		currentRow++;
	}

	/** 
	 * Adds a checkbox; does not make it recordable if isPreview is true.
     * With isPreview true, the checkbox can be referred to as previewCheckbox
     * from hereon.
     */
	public void addCheckBox(String label, boolean defaultValue) 
	{
		// Creates the new check box
		label = formatLabel(label);
		JCheckBox cb = new JCheckBox(label);
		cb.setSelected(defaultValue);
//		cb.addItemListener(this);
		cb.addKeyListener(this);

		// Configure layout depending on number of existing checkboxes
		if (checkBoxes == null)
		{
			checkBoxes = new ArrayList<JCheckBox>(4);
			c.insets = getInsets(15, 20, 0, 5);
		} 
		else
		{
			c.insets = getInsets(0, 20, 0, 5);
		}
		checkBoxes.add(cb);

		// add checkbox to layout
		c.gridx = 0;
		c.gridy = currentRow;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		gridLayout.setConstraints(cb, c);
		this.dialog.add(cb);
		
		// ij.IJ.write("addCheckbox: "+ y+" "+cbIndex);
//		if (!isPreview && (Recorder.record || macro)) // preview checkbox is not
//														// recordable
//			saveLabel(cb, label);
		currentRow++;
	}

	/**
	 * Adds a popup menu that contains different choices.
	 */
   public void addChoice(String label, String[] items, String defaultItem) 
   {
   		String label2 = formatLabel(label);
		JLabel theLabel = new JLabel(label2);
		
		c.gridx = 0;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;

		if (choices == null)
		{
			choices = new ArrayList<JComboBox<String>>(4);
			c.insets = getInsets(5, 5, 5, 5);
		} 
		else
		{
			c.insets = getInsets(0, 5, 5, 5);
		}
		
		gridLayout.setConstraints(theLabel, c);
		this.dialog.add(theLabel);
		
		JComboBox<String> combo = new JComboBox<String>();
		combo.addKeyListener(this);
//		thisChoice.addItemListener(this);
		for (String item : items)
			combo.addItem(item);
		combo.setSelectedItem(defaultItem);
		
		c.gridx = 1;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.WEST;
		gridLayout.setConstraints(combo, c);
		this.dialog.add(combo);

		choices.add(combo);
//		if (Recorder.record || macro)
//			saveLabel(thisChoice, label);
		currentRow++;
	}

	/**
	* Adds a slider (scroll bar) to the dialog box.
	* Floating point values will be used if (maxValue-minValue)<=5.0
	* and either minValue or maxValue are non-integer.
	* @param label	 the label
	* @param minValue  the minimum state of the slider
	* @param maxValue  the maximum state of the slider
	* @param defaultValue  the initial state of the slider
	*/
	public void addSlider(String label, double minValue, double maxValue,
			double defaultValue)
	{
		int columns = 4;
		int digits = 0;
		double scale = 1.0;

		if ((maxValue - minValue) <= 5.0
				&& (minValue != (int) minValue || maxValue != (int) maxValue || defaultValue != (int) defaultValue))
		{
			scale = 20.0;
			minValue *= scale;
			maxValue *= scale;
			defaultValue *= scale;
			digits = 2;
		}

		// Add the label
		String label2 = formatLabel(label);
		JLabel theLabel = new JLabel(label2);
		c.gridx = 0;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 3, 5);
		gridLayout.setConstraints(theLabel, c);
		this.dialog.add(theLabel);
		
		if (scrollBars == null) 
		{
			scrollBars = new ArrayList<JScrollBar>(5);
			sliderIndexes = new int[MAX_SLIDERS];
			sliderScales = new double[MAX_SLIDERS];
		}

		JScrollBar s = new JScrollBar(JScrollBar.HORIZONTAL, (int) defaultValue,
				1, (int) minValue, (int) maxValue + 1);

//		GUI.fix(s);
		scrollBars.add(s);
		s.addAdjustmentListener(this);
		s.setUnitIncrement(1);

		if (numericFields==null) 
		{
			numericFields = new ArrayList<JTextField>(5);
//			defaultValues = new Vector(5);
//			defaultText = new Vector(5);
		}
		
		String text = formatNumber(defaultValue / scale, digits);
		JTextField tf = createNumericTextField(text, columns);
		numericFields.add(tf);
		
		sliderIndexes[scrollBars.size() - 1] = numericFields.size() - 1;
		sliderScales[scrollBars.size() - 1] = scale;
		
//		defaultValues.addElement(new Double(defaultValue/scale));
//		defaultText.addElement(tf.getText());
		tf.setEditable(true);
		//if (firstNumericField && firstSlider) tf.selectAll();
//		firstSlider = false;
		
		// Create an inner panel containing slider and numeric field 
		JPanel panel = new JPanel();
		GridBagLayout pgrid = new GridBagLayout();
		panel.setLayout(pgrid);

		GridBagConstraints pc  = new GridBagConstraints();
		// label
		//pc.insets = new Insets(5, 0, 0, 0);
		//pc.gridx = 0; pc.gridy = 0;
		//pc.gridwidth = 1;
		//pc.anchor = GridBagConstraints.EAST;
		//pgrid.setConstraints(theLabel, pc);
		//panel.add(theLabel);
		// slider
		pc.insets = new Insets(5, 5, 0, 5);
		pc.gridx = 0; pc.gridy = 0;
		pc.gridwidth = 1;
		pc.ipadx = 75;
		pc.anchor = GridBagConstraints.WEST;
		pgrid.setConstraints(s, pc);
		panel.add(s);
		pc.ipadx = 0;  // reset
		// text field
		pc.gridx = 1;
		pc.insets = new Insets(5, 5, 0, 5);
		pc.anchor = GridBagConstraints.EAST;
		pgrid.setConstraints(tf, pc);
		panel.add(tf);
   	
		// Adds the inner panel to the global grid bag layout
		gridLayout.setConstraints(panel, c);
		c.gridx = 1; c.gridy = currentRow;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 0);
		gridLayout.setConstraints(panel, c);
		this.dialog.add(panel);
		
		currentRow++;
//		if (Recorder.record || macro)
//			saveLabel(tf, label);
   }


   
	/**
	 * Cleanup a label in case some special characters were added.
	 */
	private String formatLabel(String label)
	{
		String label2 = label;
		if (label2.indexOf('_') != -1)
			label2 = label2.replace('_', ' ');
		return label2;
	}
	
	/**
	 * Create text filed for numbers. Adds some pre-pressing on the number of columns.
	 */
	private JTextField createNumericTextField(String text, int columns) 
	{
//		if (IJ.isWindows())
//			columns -= 2;
		if (columns < 1)
			columns = 1;

		return createTextField(text, columns);
	}
	
	private JTextField createTextField(String text, int columns) 
	{
		JTextField tf = new JTextField(text, columns);		
//		if (IJ.isLinux())
//			tf.setBackground(Color.white);
		
		tf.addActionListener(this);
		tf.addCaretListener(this);
		tf.addFocusListener(this);
		tf.addKeyListener(this);

		return tf;
	}
	
    private static final String formatNumber(double value, int nDigits)
    {
		String format = "%." + nDigits + "f";
		return String.format(Locale.US, format, value);
	}

	/** Adds a message consisting of one or more lines of text. */
    public void addMessage(String text) 
    {
    	addMessage(text, null, null);
    }

    /** Adds a message consisting of one or more lines of text,
    	which will be displayed using the specified font. */
    public void addMessage(String text, Font font) 
    {
    	addMessage(text, font, null);
    }
    
    /** Adds a message consisting of one or more lines of text,
    	which will be displayed using the specified font and color. */
    public void addMessage(String text, Font font, Color color)
    {
    	// Creates a new label component
    	Component theLabel = null;
    	if (text.indexOf('\n')>=0)
			theLabel = new JMultiLineLabel(text);
		else
			theLabel = new JLabel(text);
		//theLabel.addKeyListener(this);
		if (font!=null)
			theLabel.setFont(font);
		if (color!=null)
			theLabel.setForeground(color);

		// add the label to the grid layout
		c.gridx = 0;
		c.gridy = currentRow;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = getInsets(text.equals("") ? 0 : 10, 20, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		gridLayout.setConstraints(theLabel, c);
		this.dialog.add(theLabel);

		c.fill = GridBagConstraints.NONE;
		currentRow++;
    }
    

	/**
	 * Helper functions that returns the insets of the next element to be added.
	 */
	private Insets getInsets(int top, int left, int bottom, int right) 
	{
//		if (customInsets) {
//			customInsets = false;
//			return new Insets(topInset, leftInset, bottomInset, 0);
//		} else
			return new Insets(top, left, bottom, right);
	}

	/**
	 * Shows the dialog.
	 */
	public void showDialog() 
	{
		if (!buttonsCreated)
		{
			createButtonPanel();
		}
		
		// computes optimal size of each subcomponent
		this.dialog.pack();

		// add some space to have a better visual impression
		Dimension dim = this.dialog.getPreferredSize();
		Dimension dim2 = new Dimension(dim.width + 20, dim.height + 20);
		this.dialog.setPreferredSize(dim2);
		this.dialog.setSize(dim2);
		
		this.dialog.setVisible(true);
		resetCounters();
	}
	
	/**
	 * Adds OK and Cancel buttons at the bottom of the dialog.
	 */
	private void createButtonPanel() 
	{
		// Create buttons
		okButton = addButton("OK");
		cancelButton = addButton("Cancel");
		
		// add buttons to a row panel
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		buttons.add(okButton);
		buttons.add(cancelButton);

		// add buttons panel to the grid layout
		c.gridx = 0;
		c.gridy = currentRow;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 2;
		c.insets = new Insets(15, 0, 0, 0);
		gridLayout.setConstraints(buttons, c);
		this.dialog.add(buttons);
		
		buttonsCreated = true;
	}
	
    /** Reset the counters before reading the dialog parameters */
    private void resetCounters()
    {
        numericFieldIndex = 0;        // prepare for readout
		textFieldIndex = 0;
		checkBoxIndex = 0;
		comboBoxIndex = 0;
//		textAreaIndex = 0;
//        invalidNumber = false;
    }


	/**
	 * Adds a button and add this as action and key listener.
	 * @param label
	 */
	private JButton addButton(String label) 
	{
		JButton button = new JButton(label);
		button.addActionListener(this);
		button.addKeyListener(this);
		return button;
	}
	
	/** Returns true if the user clicked on "Cancel". */
    public boolean wasCanceled() 
    {
//    	if (wasCanceled)
//    		Macro.abort();
    	return output == Output.CANCEL;
    }
    
	/** Returns true if the user has clicked on "OK" or a macro is running. */
    public boolean wasOKed() 
    {
    	return output == Output.OK;
    }

    public Output getOutput() 
    {
    	return output;
    }

    
	/**
	 * Returns the contents of the next numeric field, or NaN if the field does
	 * not contain a number.
	 */
	public double getNextNumber()
	{
		if (numericFields == null) 
		{
			throw new RuntimeException("no Numeric Field was added");
		}
			
		JTextField tf = numericFields.get(numericFieldIndex);
		
		double value = Double.parseDouble(tf.getText());
		
		numericFieldIndex++;
		return value;
	}

  	/** Returns the contents of the next text field. */
   public String getNextString() 
   {
		if (stringFields == null)
			return "";
		
		JTextField tf = stringFields.get(textFieldIndex);
		String text = tf.getText();

//		if (macro) {
//			String label = (String)labels.get((Object)tf);
//			theText = Macro.getValue(macroOptions, label, theText);
//			if (theText!=null && (theText.startsWith("&")||label.toLowerCase(Locale.US).startsWith(theText))) {
//				// Is the state a macro variable?
//				if (theText.startsWith("&")) theText = theText.substring(1);
//				Interpreter interp = Interpreter.getInstance();
//				String s = interp!=null?interp.getVariableAsString(theText):null;
//				if (s!=null) theText = s;
//			}
//		}
		
//		if (recorderOn) {
//			String s = theText;
//			if (s!=null&&s.length()>=3&&Character.isLetter(s.charAt(0))&&s.charAt(1)==':'&&s.charAt(2)=='\\')
//				s = s.replaceAll("\\\\", "\\\\\\\\");  // replace "\" with "\\" in Windows file paths
//			recordOption(tf, s);
//		}
		
		textFieldIndex++;
		return text;
    }
    
	/**
	 * Returns the contents of the next numeric field, or NaN if the field does
	 * not contain a number.
	 */
	public boolean getNextBoolean()
	{
		if (checkBoxes == null) 
		{
			throw new RuntimeException("no Check Box was added");
		}
			
		JCheckBox cb = checkBoxes.get(checkBoxIndex);
		
		boolean state = cb.isSelected();
		
		checkBoxIndex++;

		return state;
	}

  	/** 
  	 * Returns the selected item in the next popup menu. 
  	 */
    public String getNextChoice()
    {
		if (choices==null)
			return "";
		JComboBox<String> thisChoice = choices.get(comboBoxIndex);
		String item = (String) thisChoice.getSelectedItem();
//		if (macro) {
//			String label = (String)labels.get((Object)thisChoice);
//			item = Macro.getValue(macroOptions, label, item);
//			if (item!=null && item.startsWith("&")) // state is macro variable
//				item = getChoiceVariable(item);
//		}	
		
//		if (recorderOn)
//			recordOption(thisChoice, item);
		comboBoxIndex++;
		return item;
    }
    
  	/**
  	 *  Returns the index of the selected item in the next popup menu. 
  	 */
    public int getNextChoiceIndex() 
    {
		if (choices==null)
			return -1;
		JComboBox<String> thisChoice = choices.get(comboBoxIndex);
		int index = thisChoice.getSelectedIndex();
	
//		if (macro) {
//			String label = (String)labels.get((Object)thisChoice);
//			String oldItem = thisChoice.getSelectedItem();
//			int oldIndex = thisChoice.getSelectedIndex();
//			String item = Macro.getValue(macroOptions, label, oldItem);
//			if (item!=null && item.startsWith("&")) // state is macro variable
//				item = getChoiceVariable(item);
//			thisChoice.select(item);
//			index = thisChoice.getSelectedIndex();
//			if (index==oldIndex && !item.equals(oldItem)) {
//				// is state a macro variable?
//				Interpreter interp = Interpreter.getInstance();
//				String s = interp!=null?interp.getStringVariable(item):null;
//				if (s==null)
//					IJ.error(getTitle(), "\""+item+"\" is not a valid choice for \""+label+"\"");
//				else
//					item = s;
//			}
//		}
		
//		if (recorderOn) {
//			String item = thisChoice.getSelectedItem();
//			if (!(item.equals("*None*")&&getTitle().equals("Merge Channels")))
//				recordOption(thisChoice, thisChoice.getSelectedItem());
//		}
	
		comboBoxIndex++;
		return index;
    }
    

    
	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent arg0) {
		wasCanceled = true; 
		output = Output.CANCEL;
		this.dialog.dispose(); 
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}

	@Override
	public void keyPressed(KeyEvent evt) 
	{
		int keyCode = evt.getKeyCode(); 
//		IJ.setKeyDown(keyCode);
		
//		if (keyCode == KeyEvent.VK_ENTER && textArea1 == null) {
		if (keyCode == KeyEvent.VK_ENTER) {
			wasOKed = true;
			output = Output.OK;
//			if (IJ.isMacOSX() && IJ.isJava15())
//				accessTextFields();
			this.dialog.dispose();
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
			wasCanceled = true;
			output = Output.CANCEL;
			this.dialog.dispose();
//			IJ.resetEscape();
		} else if (keyCode == KeyEvent.VK_W
				&& (evt.getModifiers() & Toolkit.getDefaultToolkit()
						.getMenuShortcutKeyMask()) != 0) {
			wasCanceled = true;
			output = Output.CANCEL;
			this.dialog.dispose();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void focusGained(FocusEvent evt) {
		Component c = evt.getComponent();
		if (c instanceof JTextField)
			((JTextField) c).selectAll();

	}

	@Override
	public void focusLost(FocusEvent evt) {
		Component c = evt.getComponent();
		if (c instanceof JTextField)
			((JTextField) c).select(0, 0);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == okButton) {
			output = Output.OK;
			this.dialog.dispose();
		} else if (source == cancelButton) {
			output = Output.CANCEL;
			this.dialog.dispose();
		}
//		} else
//            notifyListeners(evt);
	}

	@Override
	public void caretPositionChanged(InputMethodEvent arg0) {
	}

	@Override
	public void inputMethodTextChanged(InputMethodEvent arg0) {
	}

	@Override
	public synchronized void caretUpdate(CaretEvent evt) {
//        notifyListeners(e); 
		if (updateWidgets)
			return;
		if (scrollBars == null)
			return;
		
		Object source = evt.getSource();
		for (int i = 0; i < scrollBars.size(); i++) {
			int index = sliderIndexes[i];
			if (source == numericFields.get(index)) {
				updateWidgets = true;

				JTextField tf = numericFields.get(index);
				// double state = Tools.parseDouble(tf.getText());
				double value = parseDouble(tf.getText());
				if (!Double.isNaN(value)) {
					JScrollBar sb = scrollBars.get(i);
					sb.setValue((int) (value * sliderScales[i]));
				}
				updateWidgets = false;

				// IJ.log(i+" "+tf.getText());
			}
		}
	}

	private static final double parseDouble(String text) 
	{
		if (text == null || text.isEmpty()) return Double.NaN;
		return Double.parseDouble(text);
	}
	
	@Override
	public synchronized void adjustmentValueChanged(AdjustmentEvent evt) 
	{
		if (updateWidgets)
			return;
		
		Object source = evt.getSource();
		for (int i = 0; i < scrollBars.size(); i++) {
			if (source == scrollBars.get(i)) {
				updateWidgets = true;
				JScrollBar sb = (JScrollBar) source;
				JTextField tf = numericFields.get(sliderIndexes[i]);
				
				double scale = sliderScales[i];
				int digits = scale == 1.0 ? 0 : 2;

				String text = formatNumber(sb.getValue() / scale, digits);
				tf.setText(text);
				updateWidgets = false;
			}
		}
	}

	public static final void main(String[] args) 
	{
		System.out.println("start main");
		 
		GenericDialog gd = new GenericDialog("Dialog Example");
		gd.addNumericField("Radius:", 12.3, 2, "Size is given by 2 * radius + 1");
		gd.addNumericField("Default Value:", 0, 2);
		gd.addTextField("New name:", "Truc");
		gd.addSlider("Slider:", 0, 100, 37);
		gd.addCheckBox("Show Result", true);
		gd.showDialog();
		
		if (gd.wasCanceled()) {
			System.out.println("canceled...");
		}
		
		System.out.println("radius: " + gd.getNextNumber());
		System.out.println("state: " + gd.getNextNumber());
		System.out.println("new name: " + gd.getNextString());
		System.out.println("slider: " + gd.getNextNumber());
		System.out.println("show result: " + gd.getNextBoolean());
	}
}
