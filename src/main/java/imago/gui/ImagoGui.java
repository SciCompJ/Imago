/**
 * 
 */
package imago.gui;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import imago.app.ImagoApp;
import imago.app.ImageHandle;
import imago.app.TableHandle;
import imago.gui.frames.ImageFrame;
import imago.gui.frames.ImagoEmptyFrame;
import imago.gui.frames.TableFrame;
import net.sci.image.Image;
import net.sci.table.Table;


/**
 * The GUI Manager, that create frames, stores the set of open frames, 
 * and keeps a reference to the current application.
 * 
 * @author David Legland
 *
 */
public class ImagoGui 
{
    // ===================================================================
    // Static methods

    public static final void showMessage(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(
                frame.getWidget(), message, title, 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static final void showErrorDialog(ImagoFrame frame, String message)
    {
        JOptionPane.showMessageDialog(
                frame.getWidget(), message, "Error", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    public static final void showErrorDialog(ImagoFrame frame, String message, String title)
    {
        JOptionPane.showMessageDialog(
                frame.getWidget(), message, title, 
                JOptionPane.ERROR_MESSAGE);
    }
    
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
	 * Some global settings / preferences for current user.
	 */
	public UserPreferences userPreferences = new UserPreferences();
	
    ArrayList<PluginHandler> pluginHandlers = new ArrayList<PluginHandler>();

    
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
		
		try 
		{
			loadPlugins();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		catch(IOException ex)
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
	 * Loads the plugins, or refresh the list of plugins loaded by the GUI.
	 * 
	 * @throws IOException in case of I/O error 
	 * @throws ClassNotFoundException  if a plugin class could not be found
	 */
	public void loadPlugins() throws ClassNotFoundException, IOException
	{
	    System.out.println("Load plugins");
	    
        String baseDirName = System.getProperty("user.dir");
        
	    File baseDir = new File(System.getProperty("user.dir"));
        System.out.println("base directory: " + baseDir.getAbsolutePath());
        
	    String pluginsDirName = baseDirName + File.separator + "plugins";
	    File pluginsDir = new File(pluginsDirName);
        System.out.println("plugins directory: " + pluginsDir.getAbsolutePath());
	    if (!pluginsDir.exists())
	    {
	        System.out.println("No plugin directory, abort.");
	        return;
	    }
	    
	    // Find all jar files
        File[] subFiles = pluginsDir.listFiles((file, name) -> name.endsWith(".jar"));
        
        // For each jar, find the plugins within
        for (File file : subFiles)
        {
            System.out.println("Found plugin file: " + file.getName());
            try 
            {
                loadPluginsFromJarFile(file);
            }
            catch(Exception ex)
            {
                System.err.println("Failed to load plugin: " + file);
                ex.printStackTrace();
                continue; 
            }
        }
	}
	
	/**
     * Loads plugins from the jar files located in the "plugins" directory, and
     * populates the "pluginHandlers" variable.
     * 
     * @param file
     *            the jar file containing the plugin(s) and the configuration
     *            file.
     */
	private void loadPluginsFromJarFile(File file) throws IOException, ClassNotFoundException
	{
        ArrayList<String> entries = readPluginEntries(file);
        
        // create plugin handle for each entry of configuration file
        try
        {
            for (String entry : entries)
            {
                System.out.println("installing plugin: " + entry);

                PluginHandler handler = createPluginHandler(file, entry);
                if (handler != null && handler.plugin != null)
                {
                    this.pluginHandlers.add(handler);
                }
            }
        }
        catch(Exception ex)
        {
            System.err.println("Failed to load plugin: " + file);
            ex.printStackTrace();
            return;
        }
	}
	
    /**
     * Opens the configuration file ("plugins.config") from a JAR file and
     * returns the list of plugin entries it contains.
     * 
     * @param fileName the name of the file containing plugins and configuration file.
     * @throws IOException 
     */
	private ArrayList<String> readPluginEntries(File file) throws IOException
	{
        ArrayList<String> entries = new ArrayList<String>();

        try(JarFile jarFile = new JarFile(file))
	    {
	        JarEntry entry = jarFile.getJarEntry("plugins.config");
	        if (entry == null)
	        {
	            throw new RuntimeException("Could not find configuration file into jar file: " + file);
	        }

	        InputStream is = jarFile.getInputStream(entry);
	        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));

	        // read lines from configuration file
	        String s;
	        while ((s = reader.readLine()) != null)
	        {
	            // Do not process empty lines or comment lines
	            if (s.length() >= 3 && !s.startsWith("#"))
	            {
	                entries.add(s);
	            }
	        }
	    }

        return entries;
	}

    /**
     * Creates a new PluginHandler from a jarFile and a configuration entry.
     * 
     * @param jarFile
     *            the jar file
     * @param entry
     *            a string with menu location, menu item name, and class name.
     * @return a new PluginHandler
     * @throws IOException 
     */
    private PluginHandler createPluginHandler(File file, String entry) throws IOException 
    {
        entry = entry.trim();
        String[] tokens = entry.split(",", 3);
        if (tokens.length != 3)
        {
            throw new RuntimeException("Unable to parse plugin entry: " + entry);
        }
        
        String menuPath = tokens[0].trim();
        String menuName = tokens[1].trim();
        String className = tokens[2].trim();
        
        FramePlugin plugin = loadPluginFromJar(file, className);
        
        PluginHandler handler = new PluginHandler(plugin, menuName, menuPath);
        return handler;
    }
    
    private FramePlugin loadPluginFromJar(File file, String className) throws IOException
    {
        // try-with-resources statement to ensure file is correctly closed
        try (JarFile jarFile = new JarFile(file))
        {
            // prepare for reading data from jar file
            URL[] urls = { new URL("jar:file:" + file + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            // iterate over items in the jar file
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                JarEntry je = entries.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class"))
                {
                    continue;
                }

                // -6 because of .class
                String className2 = je.getName().substring(0,je.getName().length()-6);
                className2 = className2.replace('/', '.');

                if (!className2.equals(className))
                {
                    continue;
                }

                System.out.println("Checking for class " + className2);
                Class<?> pluginClass;
                try
                {
                    pluginClass = cl.loadClass(className2);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new RuntimeException("Could not load plugin class.", ex);
                }

                System.out.println("Class object " + pluginClass.getName());

                if (! FramePlugin.class.isAssignableFrom(pluginClass))
                {
                    throw new RuntimeException("Plugin class must inherit the FramePlugin class.");
                }

                System.out.println("Found a plugin: " + pluginClass.getName());

                // retrieve empty constructor of the plugin
                Constructor<?> cons;
                try
                {
                    cons = pluginClass.getConstructor();
                }
                catch (NoSuchMethodException ex)
                {
                    throw new RuntimeException("Could not find empty constructor of plugin.", ex);
                }
                catch (SecurityException ex)
                {
                    throw new RuntimeException("Security exception when accessing plugin constructor.", ex);
                }
                
                // Instantiate a new plugin from the constructor
                FramePlugin plugin;
                try
                {
                    plugin = (FramePlugin) cons.newInstance();
                }
                catch (InstantiationException ex)
                {
                    throw new RuntimeException("Could not instantiate plugin from empty constructor.", ex);
                }
                catch (IllegalAccessException ex)
                {
                    throw new RuntimeException("Could not access plugin constructor.", ex);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new RuntimeException("Wrong arguments when creating plugin.", ex);
                }
                catch (InvocationTargetException ex)
                {
                    throw new RuntimeException("Invocation exception when creating plugin.", ex);
                }
                
                return plugin;
            }
        }

        throw new RuntimeException("Plugin class could not be found in jar file: " + className);
    }
 	
	// ===================================================================
    // Creation of dialogs
    
    /**
     * Creates a new JFileChooser instance to open a file. The dialog
     * automatically opens within the last open directory.
     * 
     * @param title
     *            the title of the dialog
     * @param fileFilters
     *            an optional list of file filters
     * @return the reference to the JFileChooser
     */
    public JFileChooser createOpenFileDialog(String title, FileFilter... fileFilters)
    {
        // create dialog using last open path
        JFileChooser dlg = new JFileChooser(this.userPreferences.lastOpenPath);
        
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
        
        // add an action listener to keep path for future opening
        dlg.addActionListener(evt -> 
        {
            if (evt.getActionCommand() == JFileChooser.APPROVE_SELECTION)
            {
                // update path for future opening
                File file = dlg.getSelectedFile();
                String path = file.getParent();
                this.userPreferences.lastOpenPath = path;
            }
        });
        return dlg;
    }

    /**
     * Creates a new JFileChooser instance to save a file. The dialog
     * automatically opens within the last directory used for saving a file.
     * 
     * @param title
     *            the title of the dialog
     * @param fileFilters
     *            an optional list of file filters
     * @return the reference to the JFileChooser
     */
    public JFileChooser createSaveFileDialog(String title, FileFilter... fileFilters)
    {
        // create dialog using last open path
        JFileChooser dlg = new JFileChooser(this.userPreferences.lastSavePath);
        
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
        
        // add an action listener to keep path for future opening
        dlg.addActionListener(evt -> 
        {
            if (evt.getActionCommand() == JFileChooser.APPROVE_SELECTION)
            {
                // update path for future opening
                File file = dlg.getSelectedFile();
                String path = file.getParent();
                this.userPreferences.lastSavePath = path;
            }
        });
        return dlg;
    }

    
	// ===================================================================
    // Creation and management of new frames for specific objects
	
    public TableFrame createTableFrame(Table table, ImagoFrame parentFrame)
	{
	    // Create the new handle, keeping the maximum of settings
        TableHandle handle = this.app.createTableHandle(table);
        
        // create the frame
        TableFrame frame = new TableFrame(parentFrame, handle);

        if (parentFrame != null)
        {
			Point pos = parentFrame.getWidget().getLocation();
			frame.getWidget().setLocation(pos.x + FRAME_OFFSET_X, pos.y + FRAME_OFFSET_Y);
        }
        
        // add to frame manager
        this.addFrame(frame); 
        return frame;
	}
	
    /** 
	 * Creates a new document from an image, adds it to the application, 
	 * and returns a new frame associated to this document. 
	 */
	public ImageFrame createImageFrame(Image image)
	{
		return createImageFrame(image, null);
	}

    /** 
     * Creates a new document from an image, adds it to the application, 
     * and returns a new frame associated to this document. 
     */
    public ImageFrame createImageFrame(Image image, ImagoFrame parentFrame)
    {
    	// First create a handle for the image
    	ImageHandle parentHandle = null;
    	if (parentFrame != null && parentFrame instanceof ImageFrame)
    	{
    		parentHandle = ((ImageFrame) parentFrame).getImageHandle();
    	}
    	ImageHandle handle = app.createImageHandle(image, parentHandle);

		// Create the frame
		ImageFrame frame = new ImageFrame(this, handle);
		
        if (parentFrame != null)
        {
			Point pos = parentFrame.getWidget().getLocation();
			frame.getWidget().setLocation(pos.x + FRAME_OFFSET_X, pos.y + FRAME_OFFSET_Y);
        }
			
        // link the frames
		this.frames.add(frame);
    	if (parentFrame != null)
    	{
    		parentFrame.addChild(frame);
    	}
		
		// add the frame to the list of frames associated to the document
		String key = handle.getTag();
		if (imageFrames.containsKey(key))
		{
		    imageFrames.get(key).add(frame);
		}
		else
		{
		    ArrayList<ImageFrame> frameList = new ArrayList<>(1);
		    frameList.add(frame);
		    imageFrames.put(key, frameList);
		}
		
		frame.setVisible(true);
        return frame;
    }
	
    // ===================================================================
    // Frame management
    
	public Collection<ImageFrame> getImageFrames()
	{
		ArrayList<ImageFrame> viewers = new ArrayList<ImageFrame>(this.frames.size());
		for (ImagoFrame frame : this.frames)
		{
			if (frame instanceof ImageFrame)
			{
				viewers.add((ImageFrame) frame);
			}
		}
		
		return viewers;
	}
	
	/**
	 * Finds the frame corresponding to he viewer of a given document.
	 * 
	 * @param handle the document
	 * @return an instance of ImageFrame associated to this document
	 */
	public ImageFrame getImageFrame(ImageHandle handle)
	{
	    for (ImageFrame frame : getImageFrames())
	    {
	        if (handle == frame.getImageHandle())
	        {
	            return frame;
	        }
	    }
	    
	    throw new RuntimeException("Could not find any image frame for handle with name: " + handle.getName());
	}

	
    public Collection<ImagoFrame> getFrames()
    {
    	return Collections.unmodifiableCollection(this.frames);
    }

    public boolean addFrame(ImagoFrame frame)
    {
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
    
    public void showEmptyFrame(boolean b) 
    {
        if (this.emptyFrame == null) 
        {
            this.emptyFrame = new ImagoEmptyFrame(this);
        }
    
        this.emptyFrame.setVisible(b);
    }

    public void disposeEmptyFrame()
    {
        this.emptyFrame.getWidget().dispose();
    }

    
    // ===================================================================
    // Creation of dialogs
    
    
    

	// ===================================================================
    // Getters / setters

    public ImagoApp getAppli()
    {
    	return this.app;
    }
 }
