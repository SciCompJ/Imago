/**
 * 
 */
package imago.plugin.image.process;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import imago.app.ImageHandle;
import imago.app.ImagoApp;
import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.gui.imagepair.ImagePairFrame;
import imago.gui.imagepair.ImagePairViewer;
import imago.gui.util.GuiHelper;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.geom.geom2d.Transform2D;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;
import net.sci.register.transform.CenteredMotion2D;
import net.sci.register.transform.CenteredSimilarity2D;
import net.sci.register.transform.TranslationModel2D;

/**
 * 
 */
public class ImagePair2DRegister implements FramePlugin, KeyListener
{
    // ===================================================================
    // Class properties
    
    ImagoFrame parentFrame;
    
    Image refImage;
    
    Image movingImage;
    
    /** the translation vector (in pixels) */
    double xShift = 0.0;
    double yShift = 0.0;
    
    /** rotation angle (degrees)*/
    double rotationAngle = 0.0;
    
    /** binary logarithm of the scaling factor (for Similarity transform) */
    double logScaling = 0.0;
    
    boolean validParams = true;

    /** The transform model from reference space to moving image space */
    Transform2D transform = new TranslationModel2D();
    
    /** The result of the transform applied on the moving image */
    Image registeredImage; 
    
    ImagePairFrame resultDisplay = null;
    
    Image resultImage = null;
    
    JFrame pluginFrame = null;

    
    // ----------------------------------------------------
    // Menu items
    
    MenuItem saveRegistrationItem;
    
    
    // ----------------------------------------------------
    // GUI Widgets
    
    JComboBox<String> imageNames1Combo;
    JComboBox<String> imageNames2Combo;
    
    JComboBox<String> transformModelCombo;

    JLabel xShiftLabel;
    JTextField xShiftTextField;
    JButton xShiftDec;
    JButton xShiftInc;
    JLabel yShiftLabel;
    JButton yShiftDec;
    JButton yShiftInc;
    JTextField yShiftTextField;
    JLabel rotationAngleLabel;
    JTextField rotationAngleTextField;
    JButton rotAngleDec;
    JButton rotAngleInc;
    JLabel logScalingLabel;
    JTextField logScalingTextField;
    JButton scalingDec;
    JButton scalingInc;
    
    JCheckBox autoUpdateCheckBox;
    JButton runButton;

    
    // ====================================================
    // Main processing methods
 
    // ===================================================================
        // Implementation of the Plugin interface    
    
        @Override
        public void run(ImagoFrame frame, String args)
        {
            this.parentFrame = frame;
            
            // build control frame
            this.pluginFrame = new JFrame("Simple Registration");
            if (parentFrame != null)
            {
                Point pos = parentFrame.getWidget().getLocation();
                this.pluginFrame.setLocation(pos.x + 30, pos.y + 20);
            }
            
            initWidgets();
            setupLayout(pluginFrame);
    //        setupMenu(pluginFrame);
            
            pluginFrame.pack();
            pluginFrame.setVisible(true);
        }

    /**
     * The main processing method. It applies several processing steps:
     * <ul>
     * <li> Retrieve input arguments </li>
     * <li> Compute the transform </li>
     * <li> Apply transform to moving image</li>
     * <li> Compute result image showing result</li>
     * </ul>
     */
    private void runRegistration()
    {
        updateInputImages();
        
        // need to update transform after updating images (to compute center)
        updateTransform();
        
        // apply transform on moving image
        updateRegisteredImage();
        
        updateResultDisplay();
    }
    
    private void updateInputImages()
    {
        // retrieve name of images
        String imageName1 = (String) this.imageNames1Combo.getSelectedItem();
        String imageName2 = (String) this.imageNames2Combo.getSelectedItem();
        
        // retrieve image data
        ImagoApp app = this.parentFrame.getGui().getAppli();
        this.refImage = ImageHandle.findFromName(app, imageName1).getImage();
        this.movingImage = ImageHandle.findFromName(app, imageName2).getImage();
    }
    
    public void updateTransform()
    {
        int transfoIndex = this.transformModelCombo.getSelectedIndex();
        
        switch (transfoIndex)
        {
        case 0:
            this.transform = new TranslationModel2D(this.xShift, this.yShift);
            break;
            
        case 1:
        {
            double sizeX = this.refImage.getSize(0);
            double sizeY = this.refImage.getSize(1);
            Point2D center = new Point2D(sizeX/2, sizeY/2);
            this.transform = new CenteredMotion2D(center, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        case 2:
        {
            double sizeX = this.refImage.getSize(0);
            double sizeY = this.refImage.getSize(1);
            Point2D center = new Point2D(sizeX/2, sizeY/2);
            this.transform = new CenteredSimilarity2D(center, this.logScaling, this.rotationAngle, this.xShift, this.yShift);
            break;
        }
        default:
            throw new RuntimeException("This transformation is not implemented: " + this.transform.getClass().getName());
        }
    }

    /**
     * Applies the current transform on the moving image.
     */
    public void updateRegisteredImage()
    {
        // apply transform on moving image
        registeredImage = computeTransformedImage2d(this.refImage, this.transform, this.movingImage);
    }
    
    private static final Image computeTransformedImage2d(Image refImage, Transform2D transfo, Image movingImage)
    {
        ScalarArray2D<?> refArray = (ScalarArray2D<?>) refImage.getData();
        ScalarArray2D<?> movArray = (ScalarArray2D<?>) movingImage.getData();
        TransformedImage2D transformed = new TransformedImage2D(movArray, transfo);
        
        int[] dims = refArray.size();
        ScalarArray2D<?> resArray = ScalarArray2D.wrapScalar2d(refArray.newInstance(dims));
        for (int[] pos : resArray.positions())
        {
            resArray.setValue(pos, transformed.evaluate(pos[0], pos[1]));
        }
        
        return new Image(resArray, movingImage);
    }
    
    
    /**
     * Updates the current display of result, by combining the result of
     * registration with the reference image.
     */
    public void updateResultDisplay()
    {
        if (resultDisplay == null)
        {
            this.resultDisplay = ImagePairFrame.create(refImage, registeredImage, this.parentFrame);
        } 
        else
        {
            ImagePairViewer viewer = this.resultDisplay.getViewer();
            viewer.setReferenceImage(refImage);
            viewer.setMovingImage(registeredImage);
            viewer.refreshDisplay();
            this.resultDisplay.repaint();
        }
    }
    
    /**
     * Callback for the "Save Registration" menu item.
     */
    public void saveRegistration()
    {
//        // create file dialog using last save path
//        String imageName = referenceImagePlus.getShortTitle();
//        saveWindow = new JFileChooser(new File(imageName + ".json"));
//        saveWindow.setDialogTitle("Save Registration Data");
//        saveWindow.addChoosableFileFilter(regFileFilter);
//        saveWindow.addChoosableFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
//        saveWindow.addChoosableFileFilter(new FileNameExtensionFilter("All files (*.*)", "*"));
//        saveWindow.setFileFilter(regFileFilter);
//
//        // Open dialog to choose the file
//        int ret = saveWindow.showSaveDialog(this);
//        if (ret != JFileChooser.APPROVE_OPTION) 
//        {
//            return;
//        }
//
//        // Check the chosen file is valid
//        File file = saveWindow.getSelectedFile();
//        if (!file.getName().endsWith(".json"))
//        {
//            File parent = file.getParentFile();
//            file = new File(parent, file.getName() + ".json");
//        }
//        
//        try 
//        {
//            Registration.saveRegistration(file, referenceImagePlus, movingImagePlus, transform);
//        }
//        catch (IOException ex)
//        {
//            throw new RuntimeException(ex);
//        }
    }


    // ===================================================================
    // Implementation of the Plugin interface    

    private void initWidgets()
    {
        ImagoApp app = this.parentFrame.getGui().getAppli();
        String[] imageNames = ImageHandle.getAllNames(app).toArray(new String[]{});
        this.imageNames1Combo = new JComboBox<String>(imageNames);
        this.imageNames1Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        this.imageNames2Combo = new JComboBox<String>(imageNames);
        this.imageNames2Combo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                updateInputImages();
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        
        this.transformModelCombo = new JComboBox<String>();
        this.transformModelCombo.addItem("Translation");
        this.transformModelCombo.addItem("Motion (Translation+Rotation)");
        this.transformModelCombo.addItem("Similarity (Tr.+Rot.+Scaling)");
        this.transformModelCombo.addItemListener(evt -> {
            if (evt.getStateChange() == ItemEvent.SELECTED) 
            {
                updateEnabledRegistrationWidgets();;
                if (this.autoUpdateCheckBox.isSelected()) runRegistration();
            }
        });
        
        this.xShiftLabel = new JLabel("Shift X (pixels):");
        this.xShiftTextField = createNumericTextField(0.0);
        this.xShiftDec = createPlusMinusButton("-", evt -> {
            setXShift(this.xShift - 1.0);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });
        this.xShiftInc = createPlusMinusButton("+", evt -> {
            setXShift(this.xShift + 1.0);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });

        this.yShiftLabel = new JLabel("Shift Y (pixels):");
        this.yShiftTextField = createNumericTextField(0.0);
        this.yShiftDec = createPlusMinusButton("-", evt -> {
            setYShift(this.yShift - 1.0);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });
        this.yShiftInc = createPlusMinusButton("+", evt -> {
            setYShift(this.yShift + 1.0);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });

        this.rotationAngleLabel = new JLabel("Rotation angle (degrees):");
        this.rotationAngleTextField = createNumericTextField(0.0);
        this.rotAngleDec = createPlusMinusButton("-", evt -> {
            setRotationAngle(this.rotationAngle - 1.0);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });
        this.rotAngleInc = createPlusMinusButton("+", evt -> {
            setRotationAngle(this.rotationAngle + 1.0);   
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });

        this.logScalingLabel = new JLabel("Log_2 of scaling factor:");
        this.logScalingTextField = createNumericTextField(0.0);
        this.scalingDec = createPlusMinusButton("-", evt -> {
            setLogScaling(this.logScaling - 0.01);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });
        this.scalingInc = createPlusMinusButton("+", evt -> {
            setLogScaling(this.logScaling + 0.01);
            if (this.autoUpdateCheckBox.isSelected()) runRegistration();
        });
        
        this.autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
        this.autoUpdateCheckBox.addActionListener(evt -> {
            runRegistration();
        });

        this.runButton = new JButton("Run");
        this.runButton.addActionListener(evt -> runRegistration());
    }
    
    private void updateEnabledRegistrationWidgets()
    {
        if (transformModelCombo.getSelectedIndex() == 0)
        {
            this.rotationAngleLabel.setEnabled(false);
            this.rotationAngleTextField.setText("0.0");
            this.rotationAngleTextField.setEnabled(false);
            this.logScalingLabel.setEnabled(false);
            this.logScalingTextField.setText("0.0");
            this.logScalingTextField.setEnabled(false);
        }
        else if (transformModelCombo.getSelectedIndex() == 1)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleTextField.setText(doubleToString(this.rotationAngle));
            this.rotationAngleTextField.setEnabled(true);
            this.logScalingLabel.setEnabled(false);
            this.logScalingTextField.setText("0.0");
            this.logScalingTextField.setEnabled(false);
        }
        else if (transformModelCombo.getSelectedIndex() == 2)
        {
            this.rotationAngleLabel.setEnabled(true);
            this.rotationAngleTextField.setText(doubleToString(this.rotationAngle));
            this.rotationAngleTextField.setEnabled(true);
            this.logScalingLabel.setEnabled(true);
            this.logScalingTextField.setText(doubleToString(this.logScaling));
            this.logScalingTextField.setEnabled(true);
        }
    }
    
    private void setXShift(double value)
    {
        this.xShift = value;
        xShiftTextField.setText(doubleToString(value));
    }
    
    private void setYShift(double value)
    {
        this.yShift = value;
        yShiftTextField.setText(doubleToString(value));
    }
    
    private void setRotationAngle(double value)
    {
        this.rotationAngle = value;
        rotationAngleTextField.setText(doubleToString(value));
    }
    
    private void setLogScaling(double value)
    {
        this.logScaling = value;
        logScalingTextField.setText(doubleToString(value));
    }
    
    private JTextField createNumericTextField(double initialValue)
    {
        String text = doubleToString(initialValue);
        JTextField textField = new JTextField(text, 10);
        textField.addKeyListener(this);
        return textField;
    }

    private JButton createPlusMinusButton(String label, ActionListener act)
    {
        JButton button = new JButton(label);
        button.addActionListener(act);
        return button;
    }
    
    private void setupLayout(JFrame frame)
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        
        JPanel imagesPanel = GuiHelper.createOptionsPanel("Images");
        imagesPanel.setLayout(new GridLayout(2, 2));
        imagesPanel.add(new JLabel("Reference Image: "));
        imagesPanel.add(this.imageNames1Combo);
        imagesPanel.add(new JLabel("Moving Image: "));
        imagesPanel.add(this.imageNames2Combo);

        JPanel registrationPanel = GuiHelper.createOptionsPanel("Registration");
        registrationPanel.setLayout(new GridLayout(5, 2));
        registrationPanel.add(new JLabel("Registration Type:"));
        registrationPanel.add(transformModelCombo);
        registrationPanel.add(xShiftLabel);
        registrationPanel.add(createPanel(xShiftTextField, xShiftDec, xShiftInc));
        registrationPanel.add(yShiftLabel);
        registrationPanel.add(createPanel(yShiftTextField, yShiftDec, yShiftInc));
        registrationPanel.add(rotationAngleLabel);
        registrationPanel.add(createPanel(rotationAngleTextField, rotAngleDec, rotAngleInc));
        registrationPanel.add(logScalingLabel);
        registrationPanel.add(createPanel(logScalingTextField, scalingDec, scalingInc));
        updateEnabledRegistrationWidgets();
        
        mainPanel.add(imagesPanel);
        mainPanel.add(registrationPanel);
         
        GuiHelper.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, runButton);
        
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
    }
    
//    private void setupMenu(JFrame frame)
//        {
//    //        // init menu items
//    //        saveRegistrationItem = new MenuItem("Save Registration...");
//    //        saveRegistrationItem.addActionListener(this);
//    
//            MenuBar menuBar = new MenuBar();
//            Menu fileMenu = new Menu("File");
//    //        fileMenu.add(saveRegistrationItem);
//            
//            menuBar.add(fileMenu);
//            frame.setMenuBar(menuBar);
//        }

    private JPanel createPanel(JComponent comp, JButton button1, JButton button2)
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(comp);
        panel.add(button1);
        panel.add(button2);
        return panel;
    }
    
    // ====================================================
    // Implementation of KeyListener (for Text fields)
    
    @Override
    public void keyTyped(KeyEvent evt)
    {
        if (evt.getSource() instanceof JTextField)
        {
            processTextUpdate((JTextField) evt.getSource());
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
    
    private void processTextUpdate(JTextField textField)
    {
        try
        {
            if(textField == xShiftTextField)
            {
                this.xShift = Double.parseDouble(xShiftTextField.getText());
            }
            else if(textField == yShiftTextField)
            {
                this.yShift = Double.parseDouble(yShiftTextField.getText());
            }
            else if(textField == rotationAngleTextField)
            {
                this.rotationAngle = Double.parseDouble(rotationAngleTextField.getText());
            }
            else if(textField == logScalingTextField)
            {
                this.logScaling = Double.parseDouble(logScalingTextField.getText());
            }
        }
        catch (NumberFormatException ex)
        {
            return;
        }
        
        if (this.autoUpdateCheckBox.isSelected())
        {
            runRegistration();
            textField.requestFocus();
        }
    }
    
    private static final String doubleToString(double value)
    {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }
    
    public static final void main(String... args)
    {
        System.out.println("hello");
        
        ImagoGui gui = new ImagoGui(new ImagoApp());
        ImagoFrame baseFrame = gui.getEmptyFrame();
        
        ImagePair2DRegister plugin = new ImagePair2DRegister();
        plugin.run(baseFrame, "");
        
    }
}
