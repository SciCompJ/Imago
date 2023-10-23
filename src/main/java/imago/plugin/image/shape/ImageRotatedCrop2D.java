/**
 * 
 */
package imago.plugin.image.shape;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImageFrame;
import imago.gui.util.GuiHelper;
import imago.gui.viewer.PlanarImageViewer;
import imago.gui.viewer.ImageDisplay;
import net.sci.array.Array;
import net.sci.array.scalar.ScalarArray;
import net.sci.array.scalar.ScalarArray2D;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.geom.geom2d.AffineTransform2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.register.image.TransformedImage2D;

/**
 * Plugin for generating a rotated crop from an image.
 * 
 * The main job of the plugin is the create and display an instance of the
 * SettingsFrame. The SettingsFrame will be responsible for storing the state
 * and calling the necessary processing methods.
 * 
 * @author dlegland
 *
 * @deprecated (?) replaced by ImageRotatedCr�p 
 */
@Deprecated
public class ImageRotatedCrop2D implements FramePlugin
{
    public static final ScalarArray2D<?> rotatedCrop(ScalarArray2D<?> image, int[] dims, Point2D refPoint, double angleInDegrees)
    {
        // retrieve image dimensions
        int sizeX = dims[0];
        int sizeY = dims[1];

        // create elementary transforms
        AffineTransform2D trBoxCenter = AffineTransform2D.createTranslation(-sizeX / 2, -sizeY / 2);
        AffineTransform2D rot = AffineTransform2D.createRotation(Math.toRadians(angleInDegrees));
        AffineTransform2D trRefPoint = AffineTransform2D.createTranslation(refPoint);

        // concatenate into global display-image-to-source-image transform
        AffineTransform2D transfo = trRefPoint.compose(rot).compose(trBoxCenter);

        // Create interpolation class, that encapsulates both the image and the
        // transform
        TransformedImage2D interp = new TransformedImage2D(image, transfo);

        // allocate result image
        UInt8Array2D res = UInt8Array2D.create(sizeX, sizeY);
        res.fillValues((x, y) -> interp.evaluate(x, y));

        return res;
    }
    

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current image data
        ImageFrame imageFrame = (ImageFrame) frame;
        Image image = imageFrame.getImageHandle().getImage();
        Array<?> array = image.getData();

        int nd = array.dimensionality();
        if (nd != 2)
        {
            frame.showErrorDialog("Requires a 2D image as input", "Input Image error");
            return;
        }
        
        // use center of image as default position for box center
        int sizeX = array.size(0);
        int sizeY = array.size(1);
        Point2D refPoint = new Point2D(sizeX * 0.5, sizeY * 0.5);
        
        int[] dims = new int[] {sizeX, sizeY};
        SettingsFrame settingsFrame = new SettingsFrame(imageFrame, dims, refPoint, 0.0);
        settingsFrame.setVisible(true);
        
        // TODO: should try to avoid class cast
        ImageDisplay imageDisplay = ((PlanarImageViewer) imageFrame.getImageView()).getImageDisplay();
        imageDisplay.addMouseListener(settingsFrame);
    }
    
    public class SettingsFrame extends JFrame implements MouseListener
    {
        // ====================================================
        // Static fields

        /**
         * Version ID.
         */
        private static final long serialVersionUID = 1L;

        // ====================================================
        // Class properties
        
        ImageFrame parentFrame;
       
        Image image;
        
        ScalarArray2D<?> array;
        
        int boxSizeX;
        int boxSizeY;
        double boxCenterX;
        double boxCenterY;
        double boxAngle;
        // TODO: create a "Box"/"OrientedBox" inner class?
        
        // ====================================================
        // GUI Widgets
        
        JSpinner sizeXWidget;
        JSpinner sizeYWidget;
        JSpinner boxCenterXWidget;
        JSpinner boxCenterYWidget;
        JSpinner boxAngleWidget;
        
        JCheckBox autoUpdateCheckBox;
        JButton previewButton;
        JButton runButton;
        
        /** The frame used to display the result of rotated crop. */
        ImageFrame resultFrame = null;
        
        
        public SettingsFrame(ImageFrame parentFrame, int[] boxSize, Point2D refPoint, double rotAngle)
        {
            super("Crop Oriented Box");
            
            this.parentFrame = parentFrame;
            this.image = parentFrame.getImage();
            this.array = ScalarArray2D.wrapScalar2d((ScalarArray<?>) image.getData());
            
            // init default values
            boxSizeX = boxSize[0];
            boxSizeY = boxSize[1];
            boxCenterX = refPoint.x();
            boxCenterY = refPoint.y();
            boxAngle = rotAngle;

            setupWidgets();
            setupLayout();

            this.pack();
            this.centerFrame();
            
            setVisible(true);
        }
        
        private void setupWidgets()
        {
            sizeXWidget = GuiHelper.createNumberSpinner(boxSizeX, 0, 10000, 1);
            sizeXWidget.addChangeListener(evt -> 
            {
                this.boxSizeX = ((SpinnerNumberModel) sizeXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            sizeYWidget = GuiHelper.createNumberSpinner(boxSizeY, 0, 10000, 1);
            sizeYWidget.addChangeListener(evt -> 
            {
                this.boxSizeY = ((SpinnerNumberModel) sizeYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterXWidget = GuiHelper.createNumberSpinner(boxCenterX, 0, 10000, 1);
            boxCenterXWidget.addChangeListener(evt -> 
            {
                this.boxCenterX = ((SpinnerNumberModel) boxCenterXWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            boxCenterYWidget = GuiHelper.createNumberSpinner(boxCenterY, 0, 10000, 1);
            boxCenterYWidget.addChangeListener(evt -> 
            {
                this.boxCenterY = ((SpinnerNumberModel) boxCenterYWidget.getModel()).getNumber().intValue();
                updatePreviewIfNeeded();
            });
            
            
            boxAngleWidget = GuiHelper.createNumberSpinner(boxAngle, -180, 180, 1);
            boxAngleWidget.addChangeListener(evt -> 
            {
                this.boxAngle = ((SpinnerNumberModel) boxAngleWidget.getModel()).getNumber().doubleValue();
                updatePreviewIfNeeded();
            });

            autoUpdateCheckBox = new JCheckBox("Auto-Update", false);
            autoUpdateCheckBox.addItemListener(evt -> updatePreviewIfNeeded());
            
            runButton = new JButton("Run!");
            runButton.addActionListener(evt -> updateCrop());
        }

        private void setupLayout()
        {
            GuiHelper gh = new GuiHelper();
            // encapsulate into a main panel
            JPanel mainPanel = new JPanel();
            mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

            JPanel sizePanel = gh.createOptionsPanel("Result Size");
            sizePanel.setLayout(new GridLayout(2, 2));
            sizePanel.add(new JLabel("Size X:"));
            sizePanel.add(sizeXWidget);
            sizePanel.add(new JLabel("Size Y:"));
            sizePanel.add(sizeYWidget);
            mainPanel.add(sizePanel);
            
            JPanel boxPanel = gh.createOptionsPanel("Rotated Box");
            boxPanel.setLayout(new GridLayout(3, 2));
            boxPanel.add(new JLabel("Center X:"));
            boxPanel.add(boxCenterXWidget);
            boxPanel.add(new JLabel("Center Y:"));
            boxPanel.add(boxCenterYWidget);
            boxPanel.add(new JLabel("Angle (degrees):"));
            boxPanel.add(boxAngleWidget);
            mainPanel.add(boxPanel);
            
            // also add buttons
            gh.addInLine(mainPanel, FlowLayout.CENTER, autoUpdateCheckBox, runButton);
            
            // put main panel in the middle of frame
            this.setLayout(new BorderLayout());
            this.add(mainPanel, BorderLayout.CENTER);
        }
        
        /**
         * Puts this frame in the center of the screen.
         */
        private void centerFrame()
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = this.getSize();
            
            int posX = screenSize.width / 2 - frameSize.width / 2;
            int posY = Math.max((screenSize.height - frameSize.height) / 4, 0);
            this.setLocation(posX, posY);
        }
        
        
        public void updateCrop()
        {
            int[] dims = new int[] {boxSizeX, boxSizeY};
            Point2D cropCenter = new Point2D(boxCenterX, boxCenterY);
            
            ScalarArray2D<?> res = rotatedCrop(this.array, dims, cropCenter, boxAngle);
            Image resultImage = new Image(res, image);
            
            // retrieve frame for displaying result
            if (this.resultFrame == null)
            {
                this.resultFrame = this.parentFrame.createImageFrame(resultImage);
            }
            
            // update display frame
            this.resultFrame.getImageView().setPreviewImage(resultImage);
            this.resultFrame.getImageView().refreshDisplay();
            this.resultFrame.setVisible(true);
        }

        private void updatePreviewIfNeeded()
        {
            if (this.autoUpdateCheckBox.isSelected())
            {
                updateCrop();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
        }

        @Override
        public void mousePressed(MouseEvent evt)
        {
            ImageDisplay imageDisplay = (ImageDisplay) evt.getComponent();
            Point p = evt.getPoint();
            Point2D point = imageDisplay.displayToImage(p);
            
            this.boxCenterX = point.x();
            ((SpinnerNumberModel) this.boxCenterXWidget.getModel()).setValue(this.boxCenterX);
            this.boxCenterY = point.y();
            ((SpinnerNumberModel) this.boxCenterYWidget.getModel()).setValue(this.boxCenterY);
            
            if (this.autoUpdateCheckBox.isSelected())
            {
                updateCrop();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
    }

}
