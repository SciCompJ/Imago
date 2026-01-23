/**
 * 
 */
package imago.gui;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import imago.app.ImagoApp;
import imago.app.Workspace;
import imago.gui.frames.ImagoEmptyFrame;
import imago.image.ImageFrame;
import imago.image.ImageHandle;

/**
 * The GUI Manager, that create frames, stores the set of open frames, and keeps
 * a reference to the current application.
 * 
 * @author David Legland
 *
 */
public class ImagoGui
{
    // ===================================================================
    // Static methods

    /**
     * Opens a dialog to display a message.
     * 
     * @param frame
     *            an instance of ImagoFrame to align the dialog with
     * @param message
     *            the message to display in the dialog
     * @param title
     *            the title of the dialog frame
     */
    public static final void showMessage(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(frame.getWidget(), message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Opens a dialog to enter a text string.
     * 
     * @param frame
     *            an instance of ImagoFrame to align the dialog with
     * @param message
     *            the message to display in the dialog
     * @param title
     *            the title of the dialog frame
     * @param defaultAnswer
     *            the initial content of the text field to populate
     * @String return the content of the text field after edition
     */
    public static final String showInputDialog(ImagoFrame frame, String message, String title, String defaultAnswer)
    {
        return (String) JOptionPane.showInputDialog(
                frame.getWidget(),
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultAnswer);
    }

    /**
     * Opens a dialog to display an error message.
     * 
     * @param frame
     *            an instance of ImagoFrame to align the dialog with
     * @param message
     *            the message to display in the dialog
     */
    public static final void showErrorDialog(ImagoFrame frame, String message)
    {
        JOptionPane.showMessageDialog(frame.getWidget(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Opens a dialog to display an error message.
     * 
     * @param frame
     *            an instance of ImagoFrame to align the dialog with
     * @param message
     *            the message to display in the dialog
     * @param title
     *            the title of the dialog frame
     */
    public static final void showErrorDialog(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(frame.getWidget(), message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Opens a dialog to display an exception message together with its stack trace.
     * 
     * @param frame
     *            an instance of ImagoFrame to align the dialog with
     * @param ex
     *            the exception to display (both error message and stack trace)
     * @param title
     *            the title of the dialog frame
     */
    public static final void showExceptionDialog(ImagoFrame frame, Exception ex, String title)
    {
        // create error frame
        JFrame errorFrame = new JFrame(title);
        errorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // creates text area
        JTextArea textArea = new JTextArea(15, 80);
        textArea.setForeground(Color.RED);
        textArea.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // populates text area with stack trace
        textArea.append(ex.toString());
        for (StackTraceElement item : ex.getStackTrace())
        {
            textArea.append("\n    at " + item.toString());
        }

        // add Text area in the middle panel
        errorFrame.add(scroll);

        // display error frame
        errorFrame.setLocationRelativeTo(frame.getWidget());
        errorFrame.pack();
        errorFrame.setVisible(true);
    }
    

    // ===================================================================
    // Class variables

    /**
     * The parent application
     */
    ImagoApp app;

    /**
     * The list of frames associated to this GUI.
     */
    ArrayList<ImagoFrame> frames = new ArrayList<ImagoFrame>();

    /**
     * The list of frames associated to each document.
     * 
     * Used to remove the document from the app instance when the last frame
     * referring to a document is closed.
     */
    private Map<String, ArrayList<ImageFrame>> imageFrames = new HashMap<>();

    /**
     * An empty frame without document, displayed at startup.
     */
    ImagoFrame emptyFrame = null;

    /**
     * The class that manages the plugins.
     */
    PluginManager pluginManager;
   
    
    // ===================================================================
    // Private constants

    /**
     * The amount of displacement in the x-direction to locate a new frame with
     * respect to the parent one
     */
    private static final int FRAME_OFFSET_X = 20;

    /**
     * The amount of displacement in the y-direction to locate a new frame with
     * respect to the parent one
     */
    private static final int FRAME_OFFSET_Y = 30;
    

    // ===================================================================
    // Constructor

    public ImagoGui(ImagoApp app)
    {
        this.app = app;
        
        setupLookAndFeel();

        pluginManager = new PluginManager();
        try
        {
            pluginManager.loadPlugins();
        }
        catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void setupLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        // set up default font
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.put("ComboBox.background", UIManager.get("TextArea.background"));
    }
    

    // ===================================================================
    // General methods
    
    /**
     * Returns a reference to the inner plugin manager.
     * 
     * @return a reference to the inner plugin manager
     */
    public PluginManager getPluginManager()
    {
        return this.pluginManager;
    }


    // ===================================================================
    // Creation of dialogs

    /**
     * Creates a new JFileChooser instance to open a file. The dialog
     * automatically opens within the last open directory.
     * 
     * Usage:
     * 
     * <pre>
     * {@code
        ImagoFrame frame = ...
        File file = frame.getGui().chooseFileToOpen(frame, "Open Image");
     * }
     * </pre>
     * 
     * @param frame
     *            the reference frame for positioning the file chooser dialog
     *            (may be null)
     * @param title
     *            the title of the dialog
     * @param fileFilters
     *            an optional list of file filters. The first one of the file
     *            filters is selected as current filter.
     * @return the selected file, or null if the dialog was canceled
     */
    public File chooseFileToOpen(ImagoFrame frame, String title, FileFilter... fileFilters)
    {
        if (app.userPreferences.useFileOpenSystemDialog)
        {
            return chooseFileToOpen_awt(frame, title, fileFilters);
        }
        
        // create dialog using last open path
        JFileChooser dlg = new JFileChooser(this.app.userPreferences.lastOpenPath);

        // setup dialog title
        if (title != null)
        {
            dlg.setDialogTitle(title);
        }

        // add optional file filters
        for (FileFilter filter : fileFilters)
        {
            dlg.addChoosableFileFilter(filter);
        }
        if (fileFilters.length > 0)
        {
            dlg.setFileFilter(fileFilters[0]);
        }

        // add an action listener to keep path for future opening
        dlg.addActionListener(evt -> {
            if (evt.getActionCommand() == JFileChooser.APPROVE_SELECTION)
            {
                // update path for future opening
                File file = dlg.getSelectedFile();
                String path = file.getParent();
                this.app.userPreferences.lastOpenPath = path;
            }
        });

        // Open dialog to choose the file
        int ret = dlg.showOpenDialog(frame == null ? null : frame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION)
        { return null; }

        // return the selected file
        return dlg.getSelectedFile();
    }
    
    private File chooseFileToOpen_awt(ImagoFrame frame, String title, FileFilter... fileFilters)
    {
        FileDialog dlg = new FileDialog(frame == null ? null : frame.getWidget(), "Choose a file", FileDialog.LOAD);
        dlg.setDirectory(this.app.userPreferences.lastOpenPath);
        if (fileFilters.length > 0)
        {
            FileFilter filter = fileFilters[0];
            dlg.setFilenameFilter((dir, name) -> filter.accept(new File(dir, name)));
        }
        
        dlg.setVisible(true);
        
        String file = dlg.getFile();
        if (file == null) return null;
        String dir = dlg.getDirectory();
        this.app.userPreferences.lastOpenPath = dir;
        
        return new File(dir, file);
    }

    /**
     * Creates a new JFileChooser instance to open a file. The dialog
     * automatically opens within the last directory used for saving.
     * 
     * @param frame
     *            the reference frame for positioning the file chooser dialog
     *            (may be null)
     * @param title
     *            the title of the dialog
     * @param fileFilters
     *            an optional list of file filters. The first one of the file
     *            filters is selected as current filter.
     * @return the selected file, or null if the dialog was canceled
     */
    public File chooseFileToSave(ImagoFrame frame, String title, String defaultName, FileFilter... fileFilters)
    {
        if (app.userPreferences.useSaveFileSystemDialog)
        {
            return chooseFileToSave_awt(frame, title, defaultName, fileFilters);
        }
        
        // create dialog using last open path
        JFileChooser dlg = new JFileChooser(this.app.userPreferences.lastSavePath);

        // setup dialog title
        if (title != null)
        {
            dlg.setDialogTitle(title);
        }

        // add optional file filters
        for (FileFilter filter : fileFilters)
        {
            dlg.addChoosableFileFilter(filter);
        }
        if (fileFilters.length > 0)
        {
            dlg.setFileFilter(fileFilters[0]);
        }

        // if a name is selected, use it as default file
        if (defaultName != null)
        {
            dlg.setSelectedFile(new File(this.app.userPreferences.lastSavePath, defaultName));
        }

        // add an action listener to keep path for future opening
        dlg.addActionListener(evt -> {
            if (evt.getActionCommand() == JFileChooser.APPROVE_SELECTION)
            {
                // update path for future opening
                File file = dlg.getSelectedFile();
                String path = file.getParent();
                this.app.userPreferences.lastSavePath = path;
            }
        });

        // Open dialog to choose the file
        int ret = dlg.showSaveDialog(frame == null ? null : frame.getWidget());
        if (ret != JFileChooser.APPROVE_OPTION)
        { return null; }

        // return the selected file
        return dlg.getSelectedFile();
    }
    
    private File chooseFileToSave_awt(ImagoFrame frame, String title, String defaultName, FileFilter... fileFilters)
    {
        FileDialog dlg = new FileDialog(frame == null ? null : frame.getWidget(), "Choose a file", FileDialog.SAVE);
        dlg.setDirectory(this.app.userPreferences.lastSavePath);
        if (fileFilters.length > 0)
        {
            FileFilter filter = fileFilters[0];
            dlg.setFilenameFilter((dir, name) -> filter.accept(new File(dir, name)));
        }
        dlg.setFile(defaultName);
        
        dlg.setVisible(true);
        
        String file = dlg.getFile();
        if (file == null) return null;
        
        String dir = dlg.getDirectory();
        this.app.userPreferences.lastSavePath = dir;
        return new File(dir, file);
    }
    
    public void updateFrameLocation(ImagoFrame frame, ImagoFrame parentFrame)
    {
        if (parentFrame != null)
        {
            Point pos = parentFrame.getWidget().getLocation();
            frame.getWidget().setLocation(pos.x + FRAME_OFFSET_X, pos.y + FRAME_OFFSET_Y);
        }
    }
    

    // ===================================================================
    // Frame management

    public Collection<ImagoFrame> getFrames(Function<ImagoFrame, Boolean> filter)
    {
        return this.frames.stream()
                .filter(filter::apply)
                .toList();
    }

    public Collection<ImagoFrame> getFrames()
    {
        return Collections.unmodifiableCollection(this.frames);
    }

    public boolean addFrame(ImagoFrame frame)
    {
        // specific processing for image frames
        if (frame instanceof ImageFrame)
        {
            // add the frame to the list of frames associated to the document
            String key = ((ImageFrame) frame).getImageHandle().getTag();
            if (imageFrames.containsKey(key))
            {
                imageFrames.get(key).add((ImageFrame) frame);
            }
            else
            {
                ArrayList<ImageFrame> frameList = new ArrayList<>(1);
                frameList.add((ImageFrame) frame);
                imageFrames.put(key, frameList);
            }

        }

        // add to the general list of frames
        return this.frames.add(frame);
    }

    public boolean removeFrame(ImagoFrame frame)
    {
        if (frame instanceof ImageFrame)
        {
            ImageHandle handle = ((ImageFrame) frame).getImageHandle();
            ArrayList<ImageFrame> frameList = imageFrames.get(handle.getTag());
            if (!frameList.contains(frame))
            {
                System.err.println("Warning: frame " + frame.getWidget().getName() + " is not referenced by image handle " + handle.getName());
                return false;
            }

            frameList.remove(frame);

            if (frameList.size() == 0)
            {
                app.removeHandle(handle);
            }
        }
        return this.frames.remove(frame);
    }

    public boolean containsFrame(ImagoFrame frame)
    {
        return this.frames.contains(frame);
    }

    /**
     * Returns the current instance of the EmptyFrame, creating it if necessary.
     * 
     * @return the current empty frame instance.
     */
    public ImagoFrame getEmptyFrame()
    {
        if (this.emptyFrame == null)
        {
            this.emptyFrame = new ImagoEmptyFrame(this);
            this.emptyFrame.setVisible(true);
        }
        return emptyFrame;
    }

    public void showEmptyFrame(boolean b)
    {
        if (this.emptyFrame == null)
        {
            this.emptyFrame = new ImagoEmptyFrame(this);
        }

        this.emptyFrame.setVisible(b);
    }

    /**
     * Disposes the current instance of empty frame. Should be called only when
     * closing application.
     */
    public void disposeEmptyFrame()
    {
        if (this.emptyFrame != null)
        {
            this.emptyFrame.getWidget().dispose();
            this.emptyFrame = null;
        }
    }

    // ===================================================================
    // Creation of dialogs

    // ===================================================================
    // Getters / setters

    public Workspace getWorkspace()
    {
        return this.app.getWorkspace();
    }
    
    public ImagoApp getAppli()
    {
        return this.app;
    }
}
