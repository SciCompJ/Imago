/**
 * 
 */
package imago.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import imago.image.plugins.ImageArrayOperatorPlugin;
import imago.image.plugins.ImageOperatorPlugin;
import net.sci.array.ArrayOperator;
import net.sci.image.ImageOperator;

/**
 * Manages the plugins loaded by the application. This concerns the built-in
 * plugins embedded with the application, as well as user plugins that can be
 * added with the "plugins" directory.
 */
public class PluginManager
{
    // ===================================================================
    // Class variables

    /**
     * A list of plugins that have been loaded.
     */
    Map<Class<?>, FramePlugin> plugins = new HashMap<>();

    /**
     * A list of plugin handlers, used for creating user plugins.
     * (may need some refactoring in the future)
     */
    ArrayList<PluginHandler> pluginHandlers = new ArrayList<PluginHandler>();

    
    // ===================================================================
    // Constructor
    
    /**
     * Default empty constructor.
     */
    public PluginManager()
    {
    }

    // ===================================================================
    // General methods
    
    public Collection<PluginHandler> pluginHandlers()
    {
        return Collections.unmodifiableList(this.pluginHandlers);
    }

    /**
     * Returns an instance of the plugin associated to the specified class. If
     * the plugin has already been loaded, it is returned. Otherwise, a new
     * FramePlugin is created, using a strategy that depends on the class:
     * <ul>
     * <li>if the class implements {@code FramePlugin}, calls the empty
     * constructor</li>
     * <li>if the class implements {@code ArrayOperator}, calls the empty
     * constructor and encapsulated the operator within an instance of
     * {@code ImageArrayOperatorPlugin}</li>
     * <li>if the class implements {@code ImageOperator}, calls the empty
     * constructor and encapsulates the operator within an instance of {@code
     * ImageOperatorPlugin}</li>
     * </ul>
     * If the plugin could not be created or loaded, return null, and prints the
     * encountered error messages on the error stream.
     * 
     * @param itemClass
     *            the class of the item used to retrieve or create a plugin
     * @return an instance of the plugin from the specified class
     */
    @SuppressWarnings("unchecked")
    public FramePlugin retrievePlugin(Class<?> itemClass)
    {
        FramePlugin plugin = plugins.get(itemClass);
        if (plugin != null)
        {
            return plugin;
        }
        // If there was already an attempt to create the plugin, do not try again 
        if (plugins.containsKey(itemClass))
        {
            return null;
        }

        try
        {
            if (FramePlugin.class.isAssignableFrom(itemClass))
            {
                // most common case: the class corresponds to an implementation
                // of FramePlugin
                // simply try to instantiate a new plugin from the constructor
                plugin = createPluginInstance((Class<? extends FramePlugin>) itemClass);
            }
            else if (ArrayOperator.class.isAssignableFrom(itemClass))
            {
                // Instantiate a new plugin from the constructor
                plugin = createArrayOperatorPluginInstance((Class<? extends ArrayOperator>) itemClass);
            }
            else if (ImageOperator.class.isAssignableFrom(itemClass))
            {
                // Instantiate a new plugin from the constructor
                plugin = createImageOperatorPluginInstance((Class<? extends ImageOperator>) itemClass);
            }
            else
            {
                System.err.println("Could not find how to create plugin for item with class: " + itemClass.getName());
            }
        }
        catch (FramePluginInstantiationException ex)
        {
            // If the plugin could not be instantiated, displays the error,
            // but do not break the application flow.
            // Returns a "null" plugin that will be ignored by the GUI builder.
            ex.printStackTrace(System.err);
            plugin = null;
        }

        plugins.put(itemClass, plugin);
        return plugin;
    }

    private FramePlugin createPluginInstance(Class<? extends FramePlugin> pluginClass) throws FramePluginInstantiationException
    {
        Constructor<? extends FramePlugin> cons;
        try
        {
            // retrieve empty constructor of the plugin
            cons = pluginClass.getConstructor();
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not create constructor for Plugin: " + pluginClass.getName(), ex);
        }

        try
        {
            // Instantiate a new plugin from the constructor
            return (FramePlugin) cons.newInstance();
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not instantiate Plugin: " + pluginClass.getName(), ex);
        }
    }

    private ImageArrayOperatorPlugin createArrayOperatorPluginInstance(Class<? extends ArrayOperator> opClass) throws FramePluginInstantiationException
    {
        Constructor<? extends ArrayOperator> cons;
        try
        {
            // retrieve empty constructor of the operator
            cons = opClass.getConstructor();
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not create empty constructor for ArrayOperator: " + opClass.getName(), ex);
        }

        try
        {
            // Instantiate a new plugin from the constructor
            ArrayOperator op = (ArrayOperator) cons.newInstance();

            // encapsulates the operator within a Plugin, such that the
            // operator can be run on the array of the image contained within
            // the frame calling this plugin.
            return new ImageArrayOperatorPlugin(op);
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not instantiate Plugin: " + opClass.getName(), ex);
        }
    }

    private ImageOperatorPlugin createImageOperatorPluginInstance(Class<? extends ImageOperator> opClass) throws FramePluginInstantiationException
    {
        Constructor<? extends ImageOperator> cons;
        try
        {
            // retrieve empty constructor of the operator
            cons = opClass.getConstructor();
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not create empty constructor for ImageOperator: " + opClass.getName(), ex);
        }

        try
        {
            // Instantiate a new plugin from the constructor
            ImageOperator op = (ImageOperator) cons.newInstance();

            // encapsulates the operator within a Plugin, such that the
            // operator can be run on the array of the image contained within
            // the frame calling this plugin.
            return new ImageOperatorPlugin(op);
        }
        catch (Exception ex)
        {
            throw new FramePluginInstantiationException("Could not instantiate Plugin: " + opClass.getName(), ex);
        }
    }


    // ===================================================================
    // General methods

    /**
     * Loads the user plugins, or refresh the list of plugins loaded by the GUI.
     * 
     * @throws IOException
     *             in case of I/O error
     * @throws ClassNotFoundException
     *             if a plugin class could not be found
     */
    public void loadPlugins() throws ClassNotFoundException, IOException
    {
        String currentDir = System.getProperty("user.dir");
        Path pluginsDir = Paths.get(currentDir, "plugins");
        if (Files.exists(pluginsDir))
        {
            System.out.println("import plugins from directory: " + pluginsDir);
            loadPluginsFromDirectory(pluginsDir);
        }
        
        String homeDir = System.getProperty("user.home");
        Path userPluginsDir = Paths.get(homeDir, ".imago", "addons", "plugins");
        if (Files.exists(userPluginsDir))
        {
            System.out.println("import plugins from directory: " + userPluginsDir);
            loadPluginsFromDirectory(userPluginsDir);
        }
    }
    
    private void loadPluginsFromDirectory(Path pluginsDirPath) throws IOException
    {
        if (!Files.exists(pluginsDirPath))
        {
            return;
        }
        
        // iterate over jar files within the directory
        for (Path path : Files.newDirectoryStream(pluginsDirPath))
        {
            if (!path.getFileName().toString().endsWith(".jar"))
            {
                continue;
            }
            
            try
            {
                loadPluginsFromJarFile(path);
            }
            catch (Exception ex)
            {
                System.err.println("Failed to load plugin from file: " + path);
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
     *            the path to the jar file containing the plugin(s) and the
     *            configuration file.
     */
    private void loadPluginsFromJarFile(Path file) throws IOException, ClassNotFoundException
    {
        ArrayList<String> entries = readPluginEntries(file);

        // create plugin handle for each entry of configuration file
        try
        {
            for (String entry : entries)
            {
                PluginHandler handler = createPluginHandler(file, entry);
                if (handler != null)
                {
                    this.pluginHandlers.add(handler);
                }
            }
        }
        catch (Exception ex)
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
     * @param pathToFile
     *            the path to the file containing plugins and configuration
     *            file.
     * @throws IOException
     */
    private ArrayList<String> readPluginEntries(Path pathToFile) throws IOException
    {
        ArrayList<String> entries = new ArrayList<String>();

        try (JarFile jarFile = new JarFile(pathToFile.toFile()))
        {
            JarEntry entry = jarFile.getJarEntry("plugins.config");
            if (entry == null)
            {
                throw new RuntimeException("Could not find configuration file into jar file: " + pathToFile);
            }

            InputStream is = jarFile.getInputStream(entry);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));

            // read lines from configuration file
            reader.lines()
                .filter(s -> s.length() >= 3 )      // Do not process empty lines
                .filter(s -> !s.startsWith("#"))    // Do not process comment lines
                .forEach(entries::add);
            
            is.close();
        }

        return entries;
    }

    /**
     * Creates a new PluginHandler from a jarFile and a configuration entry.
     * Each configuration entry is composed of three tokens:
     * <ol>
     * <li>the path within menu</li>
     * <li>the label of the menu entry</li>
     * <li>the class name</li>
     * </ol>
     * If the label is equal to "-", this should be interpreted as a menu
     * separator
     * 
     * Example:
     * {@snippet :
     * Plugins>Demos, "Hello Imago", imago.plugins.HelloImagoPlugin
     * }
     *
     * 
     * @param jarFile
     *            the jar file containing plugin class
     * @param entry
     *            a onle-line string with menu location, menu item name, and
     *            class name.
     * @return a new PluginHandler
     * @throws IOException
     */
    private PluginHandler createPluginHandler(Path jarFile, String entry) throws IOException
    {
        entry = entry.trim();
        String[] tokens = entry.split(",", 3);
        
        // retrieve path and label of the menu entry
        String menuPath = tokens[0].trim();
        String label = removeEndingDoubleQuotes(tokens[1].trim());

        // process the special case of separator, by creating a plugin handler with a null plugin
        if (tokens.length >= 2 && label.equals("-"))
        {
            return new PluginHandler(null, label, menuPath);
        }
        
        if (tokens.length != 3)
        {
            throw new RuntimeException("Unable to parse plugin entry: " + entry);
        }

        // create the plugin from its class name
        String className = tokens[2].trim();
        FramePlugin plugin = loadPluginFromJar(jarFile.toFile(), className);

        // encapsulate the plugin within a Handler
        return new PluginHandler(plugin, label, menuPath);
    }
    
    private static final String removeEndingDoubleQuotes(String str)
    {
        if (str.startsWith("\"") && str.endsWith("\""))
        {
            return str.substring(1, str.length()-1);
        }
        return str;
    }

    private FramePlugin loadPluginFromJar(File file, String className) throws IOException
    {
        // try-with-resources statement to ensure file is correctly closed
        try (JarFile jarFile = new JarFile(file))
        {
            // prepare for reading data from jar file
            URL url = file.toURI().toURL();
            URLClassLoader cl = URLClassLoader.newInstance(new URL[] {url});

            Class<?> pluginClass;
            try
            {
                pluginClass = cl.loadClass(className);
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException("Could not load plugin class with name: " + className, ex);
            }

            if (!FramePlugin.class.isAssignableFrom(pluginClass))
            {
                throw new RuntimeException("Plugin class must implement the FramePlugin interface: " + className);
            }

            // retrieve empty constructor of the plugin
            Constructor<?> cons;
            try
            {
                cons = pluginClass.getConstructor();
            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException("Could not find empty constructor for plugin: "  + className, ex);
            }
            catch (SecurityException ex)
            {
                throw new RuntimeException("Security exception when accessing plugin constructor: " + className, ex);
            }
            catch (java.lang.NoClassDefFoundError err)
            {
                throw new RuntimeException("No definition class could be found for plugin: " + className, err);
            }

            // Instantiate a new plugin from the constructor
            try
            {
                return (FramePlugin) cons.newInstance();
            }
            catch (InstantiationException ex)
            {
                throw new RuntimeException("Could not instantiate plugin from empty constructor: " + className, ex);
            }
            catch (IllegalAccessException ex)
            {
                throw new RuntimeException("Could not access constructor for plugin: "  + className, ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new RuntimeException("Wrong arguments when creating plugin: "  + className, ex);
            }
            catch (InvocationTargetException ex)
            {
                throw new RuntimeException("Invocation exception when creating plugin: " + className, ex);
            }
        }
    }
}
