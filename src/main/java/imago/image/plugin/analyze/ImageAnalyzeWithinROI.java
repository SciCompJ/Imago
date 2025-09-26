/**
 * 
 */
package imago.image.plugin.analyze;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.image.ImageFrame;
import imago.image.ImageViewer;
import imago.image.viewers.PlanarImageViewer;
import imago.table.TableFrame;
import imago.util.StringUtils;
import net.sci.array.Array;
import net.sci.array.numeric.ScalarArray;
import net.sci.array.numeric.ScalarArray2D;
import net.sci.array.numeric.VectorArray;
import net.sci.array.numeric.VectorArray2D;
import net.sci.geom.geom2d.Bounds2D;
import net.sci.geom.geom2d.Curve2D;
import net.sci.geom.geom2d.Domain2D;
import net.sci.geom.geom2d.Geometry2D;
import net.sci.geom.geom2d.Point2D;
import net.sci.image.Image;
import net.sci.table.NumericTable;

/**
 * Opens a dialog with a simple push button, that will populate a table each
 * time the button is pushed.
 */
public class ImageAnalyzeWithinROI implements FramePlugin
{
    /**
     * Empty default constructor.
     */
    public ImageAnalyzeWithinROI()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;
        ImageFrame imageFrame = (ImageFrame) frame;
        
        // create analyzer frame
        new Analyzer(imageFrame);
    }
    
    public class Analyzer
    {
        // =============================================================
        // Class members
        
        /**
         * The image to measure data from.
         */
        ImageFrame imageFrame;
        
        /**
         * The table frame used to display measurement results.
         */
        TableFrame tableFrame;
        
        JFrame jFrame;
        
        JTextField rowNameTextField;
        JButton pushButton;
        JButton closeButton;
        
        
        // =============================================================
        // Constructor and related methods
        
        public Analyzer(ImageFrame imageFrame)
        {
            this.imageFrame = imageFrame;
            
            setupLayout();
        }
        
        private void setupLayout()
        {
            this.jFrame = new JFrame();
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            JPanel centerPanel = new JPanel(new GridLayout(3, 1));
            pushButton = createButton("Add Measurement", evt ->
            {
                performMeasurement();
                String currentName = this.rowNameTextField.getText();
                String newName = StringUtils.addNumericIncrement(currentName);
                this.rowNameTextField.setText(newName);
                this.jFrame.repaint();
            });
            JPanel rowNamePanel = new JPanel(new FlowLayout());
            rowNamePanel.add(new JLabel("ROI Name:"));
            rowNameTextField = new JTextField("roi-01", 12);
            rowNamePanel.add(rowNameTextField);
            centerPanel.add(rowNamePanel);
            addWithinFlowLayout(pushButton, centerPanel);
            centerPanel.add(new JLabel(""));
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel(new FlowLayout());
            closeButton = createButton("close", evt -> this.jFrame.dispose());
            bottomPanel.add(closeButton);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            jFrame.setContentPane(mainPanel);
            jFrame.setSize(300, 200);
            if (imageFrame != null)
            {
                Point pos = imageFrame.getWidget().getLocation();
                jFrame.setLocation(pos.x + 30, pos.y + 25);
            }
            jFrame.setTitle(imageFrame.getImageHandle().getName() + " Measure");
            jFrame.setVisible(true);
        }
        
        private JButton createButton(String label, ActionListener listener)
        {
            JButton button = new JButton(label);
            button.addActionListener(listener);
            return button;
        }
        
        private void addWithinFlowLayout(JComponent comp, Container container)
        {
            JPanel wrapper = new JPanel(new FlowLayout());
            wrapper.add(comp);
            container.add(wrapper);
        }
        
        
        // =============================================================
        // Processing methods
        
        private void performMeasurement()
        {
            // retrieve current selection
            ImageViewer viewer = imageFrame.getImageViewer();
            if (!(viewer instanceof PlanarImageViewer))
            {
                System.out.println("requires an instance of planar image viewer");
                return;
            }
            PlanarImageViewer piv = (PlanarImageViewer) viewer;
            Geometry2D selection = piv.getSelection();

            // create results table, or retrieve the existing one
            NumericTable table = createOrRetrieveTable();
            
            // create a name for new ROI
            String rowName = rowNameTextField.getText();
            
            // retrieve image data
            Image image = imageFrame.getImageHandle().getImage();
            Array<?> array = image.getData();
            
            // measure values within ROI and update table
            if (array instanceof ScalarArray2D array2d)
            {
                double value = averageWithinRoi(array2d, selection);
                table.addRow(rowName, new double[] {value});
            }
            else if (array instanceof VectorArray2D array2d)
            {
                double[] values = averageWithinRoi(array2d, selection);
                table.addRow(rowName, values);
            }
            
            // ensure table frame is created
            if (this.tableFrame == null)
            {
                this.tableFrame = TableFrame.create(table, imageFrame);
            }
            
            // update table display
            this.tableFrame.updateTableDisplay();
            this.tableFrame.updateTitle();
            this.tableFrame.getWidget().repaint();
        }
        
        private NumericTable createOrRetrieveTable()
        {
            // return current table if already created
            if (this.tableFrame != null) return (NumericTable) tableFrame.getTable();
            
            // retrieve image data
            Image image = imageFrame.getImageHandle().getImage();
            Array<?> array = image.getData();
            
            // determine number of columns of table
            int nCols = switch (array)
            {
                case ScalarArray<?> scalarArray -> 1;
                case VectorArray<?, ?> vectorArray -> vectorArray.channelCount();
                default -> throw new RuntimeException(
                        "Type of image data is not managed: " + array.getClass().getName());
            };
            
            // create new table
            NumericTable table = NumericTable.create(0, nCols);
            // use description of image channels as description of table columns
            table.setColumnAxis(image.getCalibration().getChannelAxis());
            
            // setup table metadata
            table.setName(imageFrame.getImageHandle().getName() + "-measures");
            return table;
        }
    }

    private static final double averageWithinRoi(ScalarArray2D<?> array, Geometry2D roi)
    {
        Bounds2D bounds = roi.bounds();
        int xmin = Math.max((int) bounds.xMin(), 0);
        int xmax = Math.max((int) bounds.xMax(), array.size(0) - 1);
        int ymin = Math.max((int) bounds.yMin(), 0);
        int ymax = Math.max((int) bounds.yMax(), array.size(1) - 1);

        return switch (roi)
        {
            case Domain2D domain -> {
                double sum = 0.0;
                int count = 0;
                for (int y = ymin; y <= ymax; y++)
                {
                    for (int x = xmin; x <= xmax; x++)
                    {
                        if (domain.contains(x, y))
                        {
                            sum += array.getValue(x, y);
                            count++;
                        }
                    }
                }
                yield sum / count;
            }
            case Curve2D curve -> {
                double sum = 0.0;
                int count = 0;
                for (int y = ymin; y <= ymax; y++)
                {
                    for (int x = xmin; x <= xmax; x++)
                    {
                        if (curve.distance(x, y) <= 0.5)
                        {
                            sum += array.getValue(x, y);
                            count++;
                        }
                    }
                }
                yield sum / count;
            }
            case Point2D point -> array.getValue((int) point.x(), (int) point.y());
            default -> throw new RuntimeException("can not process geometry: " + roi.getClass());
        };
    }

    private static final double[] averageWithinRoi(VectorArray2D<?, ?> array, Geometry2D roi)
    {
        Bounds2D bounds = roi.bounds();
        int xmin = Math.max((int) bounds.xMin(), 0);
        int xmax = Math.max((int) bounds.xMax(), array.size(0) - 1);
        int ymin = Math.max((int) bounds.yMin(), 0);
        int ymax = Math.max((int) bounds.yMax(), array.size(1) - 1);

        int nc = array.channelCount();
        return switch (roi)
        {
            case Domain2D domain -> {
                // iterate over the points contained within the domain
                double[] values = new double[nc];
                int count = 0;
                for (int y = ymin; y <= ymax; y++)
                {
                    for (int x = xmin; x <= xmax; x++)
                    {
                        if (domain.contains(x, y))
                        {
                            for (int i = 0; i < nc; i++)
                            {
                                values[i] += array.getValue(x, y, i);
                            }
                            count++;
                        }
                    }
                }

                for (int i = 0; i < nc; i++)
                {
                    values[i] /= count;
                }
                yield values;
            }
            case Curve2D curve -> {
                // iterate over the points located close enough from the curve
                double[] values = new double[nc];
                int count = 0;
                for (int y = ymin; y <= ymax; y++)
                {
                    for (int x = xmin; x <= xmax; x++)
                    {
                        if (curve.distance(x, y) <= 0.5)
                        {
                            for (int i = 0; i < nc; i++)
                            {
                                values[i] += array.getValue(x, y, i);
                            }
                            count++;
                        }
                    }
                }
                for (int i = 0; i < nc; i++)
                {
                    values[i] /= count;
                }
                yield values;
            }
            case Point2D point -> array.getValues((int) point.x(), (int) point.y());
            default -> throw new RuntimeException("can not process geometry: " + roi.getClass());
        };
    }
}
