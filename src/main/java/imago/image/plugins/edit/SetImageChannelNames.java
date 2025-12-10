/**
 * 
 */
package imago.image.plugins.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.ImagoGui;
import imago.image.ImageFrame;
import imago.image.ImageHandle;
import net.sci.array.Array;
import net.sci.array.numeric.VectorArray;
import net.sci.axis.CategoricalAxis;
import net.sci.image.Image;

/**
 * Opens a (modal) dialog to edit the names of the channels of the current
 * image. Can also save and load channel names from text files.
 */
public class SetImageChannelNames implements FramePlugin
{
    public SetImageChannelNames()
    {
    }

    @Override
    public void run(ImagoFrame frame, String args)
    {
        // get current frame
        ImageHandle doc = ((ImageFrame) frame).getImageHandle();
        Image image = doc.getImage();
        
        if (image == null)
        {
            return;
        }
        Array<?> array = image.getData();
        if (array == null)
        {
            return;
        }
        if (!(array instanceof VectorArray))
        {
            ImagoGui.showErrorDialog(frame, "Requires an image containing a vector array", "Data Type Error");
            return;
        }
        
        new CustomDialog((ImageFrame) frame);
    }
    
    private class CustomDialog
    {
        ImageFrame parentFrame;
        JDialog dialog;
        Image image;

        // the widget that displays the name of the channels
        DefaultListModel<String> channelListModel;
        JList<String> channelNameList;
        
        JButton renameButton;
        JButton importButton;
        JButton exportButton;
        
        JButton okButton;
        JButton cancelButton;

        private static FileFilter textFileFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        
        public CustomDialog(ImageFrame parentFrame)
        {
            this.parentFrame = parentFrame;
            this.image = parentFrame.getImageHandle().getImage();
            
            this.dialog = new JDialog(parentFrame.getWidget(), "Set Channel Names", true);
            
            // initialize widgets and setup layout
            initializeWidgets();
            setupLayout(dialog);
            
            // setup frame location according to parent frame location
            java.awt.Point pos = parentFrame.getWidget().getLocation();
            dialog.setLocation(pos.x + 20, pos.y + 20);
            dialog.setVisible(true);
        }
        
        private void initializeWidgets()
        {
            renameButton = createButton("Rename", evt -> onRename());
            importButton = createButton("Import", evt -> onImport());
            exportButton = createButton("Export", evt -> onExport());
            
            okButton = createButton("OK", evt -> onOK());
            cancelButton = createButton("Cancel", evt -> onCancel());
            
            channelListModel = new DefaultListModel<String>();
            String[] names = image.getCalibration().getChannelAxis().itemNames();
            for (String name : names)
            {
                channelListModel.addElement(name);
            }
            
            channelNameList = new JList<String>(channelListModel);
            channelNameList.setVisibleRowCount(10);
            channelNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        private void setupLayout(JDialog frame)
        {
            // layout:
            // one panel to display the list of channels
            // one panel for common options
            // a control panels with validation buttons
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            // 3. The panel for the channel names
            JPanel channelNamesPanel = new JPanel();
            channelNamesPanel.setLayout(new GridLayout(1, 2, 5, 5));
            channelNamesPanel.setBorder(BorderFactory.createTitledBorder("Channels"));
            
            // 3.1 the list of channels
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BorderLayout());
            listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JScrollPane listScroller = new JScrollPane(channelNameList);
            listScroller.setPreferredSize(new Dimension(80, 80));
            listPanel.add(listScroller, BorderLayout.CENTER);
            channelNamesPanel.add(listPanel);
            
            // 3.2 The control panel for the channels
            JPanel polyButtonsPanel = new JPanel(new GridLayout(5, 1));
            polyButtonsPanel.add(new JLabel(""));
            polyButtonsPanel.add(renameButton);
            polyButtonsPanel.add(importButton);
            polyButtonsPanel.add(exportButton);
            polyButtonsPanel.add(new JLabel(""));

            channelNamesPanel.add(polyButtonsPanel);
            mainPanel.add(channelNamesPanel, BorderLayout.CENTER);
            
            JPanel controlsPanel = new JPanel();
            controlsPanel.setLayout(new FlowLayout());
            controlsPanel.add(okButton);
            controlsPanel.add(cancelButton);
            mainPanel.add(controlsPanel, BorderLayout.SOUTH);
            
            
            frame.setLayout(new BorderLayout());
            frame.add(mainPanel, BorderLayout.CENTER);
            frame.setSize(250, 300);
        }
        
        private JButton createButton(String label, Consumer<ActionEvent> action)
        {
            JButton button = new JButton(label);
            button.addActionListener(evt -> action.accept(evt));
            return button;
        }
        
        public void onOK()
        {
            int n = channelListModel.size();
            String[] newNames = new String[n];
            for (int i = 0; i < n; i++)
            {
                newNames[i] = channelListModel.get(i);
            }
            
            CategoricalAxis channelAxis = image.getCalibration().getChannelAxis();
            for (int i = 0; i < n; i++)
            {
                channelAxis.setItemName(i, newNames[i]);
            }   
            
            dialog.setVisible(false);
            dialog.dispose();
        }

        public void onCancel()
        {
            dialog.setVisible(false);
            dialog.dispose();
        }

        public void onRename()
        {
            // retrieve index of selected slice polygon
            int index = channelNameList.getSelectedIndex();
            if (index == -1)
            {
                return;
            }
            
            String name = channelListModel.get(index);
            
            String newName = (String) JOptionPane.showInputDialog(
                        this.dialog,
                        "New name:",
                        "Rename Channel",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        name);
            if (name == null || name.isBlank()) return;
            if (name.equals(newName)) return;

            channelListModel.set(index, newName);
            
            // refresh display
            channelNameList.invalidate();
            dialog.repaint();
        }

        public void onImport()
        {
            // open a dialog to choose a text file
            File file = parentFrame.getGui().chooseFileToOpen(parentFrame,
                    "Read Channel Names", textFileFilter);

            // Check validity of the chosen file
            if (file == null) return;
            if (!file.exists()) return;
            
            ArrayList<String> tokens = new ArrayList<String>();
            try (Scanner scanner = new Scanner(file))
            {
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty())
                    {
                        tokens.add(line);
                    }
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }

            int nChannels = this.image.getCalibration().getChannelAxis().length();
            int nTokens = tokens.size();
            if (nTokens != nChannels)
            {
                String msg = String.format("The number of channels within text file (%d)\nmust match the number of channels within image (%d)", nTokens, nChannels);
                ImagoGui.showErrorDialog(parentFrame, msg, "Data Import Error");
                return;
            }
            
            for (int i = 0; i < nTokens; i++)
            {
                channelListModel.set(i, tokens.get(i));
            }
            
            // refresh display
            channelNameList.invalidate();
            dialog.repaint();
        }

        public void onExport() 
        {
            // open a dialog to choose a text file
            File file = parentFrame.getGui().chooseFileToSave(parentFrame,
                    "Write Channel Names", "channels.txt", textFileFilter);

            // Check validity of the chosen file
            if (file == null) return;

            // print header into header file
            try (FileOutputStream stream = new FileOutputStream(file))
            {
                PrintStream ps = new PrintStream(stream);
                int nChannels = channelListModel.getSize();
                for (int i = 0; i < nChannels; i++)
                {
                    ps.printf("%s\n", channelListModel.get(i));
                }
                ps.close();
            }
            catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
//    /**
//     * Static method used for debugging purpose.
//     * 
//     * @param args
//     *            optional arguments not used in practice.
//     */
//    public static final void main(String... args)
//    {
//        // setup app
//        ImagoFrame parentFrame = new ImagoEmptyFrame(new ImagoGui(new ImagoApp()));
//        
//        // read image
//        String fileName = SetImageChannelNames.class.getResource("/images/peppers.png").getFile();
//        Image image = Imago.readImage(new File(fileName), parentFrame);
//        ImageFrame frame = ImageFrame.create(image, parentFrame);
//        
//        SetImageChannelNames plugin = new SetImageChannelNames();
//        plugin.run(frame, "");
//    }
}
