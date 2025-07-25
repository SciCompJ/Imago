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
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import imago.plugin.image.ImageArrayOperatorPlugin;
import imago.plugin.image.ImageOperatorPlugin;
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
     * constructor and encapsulated the operator within an instance of {@code
     * ImageOperatorPlugin}</li>
     * </ul>
     * If the plugin could not be created or loaded, return null, and prints the
     * encountered error messages on the error stream.
     * 
     * @param clazz
     *            the class of the item used to retrieve or create a plugin
     * @return an instance of the plugin from the specified class
     */
    @SuppressWarnings("unchecked")
    public FramePlugin retrievePlugin(Class<?> clazz)
    {
        FramePlugin plugin = plugins.get(clazz);
        if (plugin != null)
        {
            return plugin;
        }
        // If there was already an attempt to create the plugin, do not try again 
        if (plugins.containsKey(clazz))
        {
            return null;
        }

        try
        {
            if (FramePlugin.class.isAssignableFrom(clazz))
            {
                // most common case: the class corresponds to an implementation
                // of FramePlugin
                // simply try to instantiate a new plugin from the constructor
                plugin = createPluginInstance((Class<? extends FramePlugin>) clazz);
            }
            else if (ArrayOperator.class.isAssignableFrom(clazz))
            {
                // Instantiate a new plugin from the constructor
                plugin = createArrayOperatorPluginInstance((Class<? extends ArrayOperator>) clazz);
            }
            else if (ImageOperator.class.isAssignableFrom(clazz))
            {
                // Instantiate a new plugin from the constructor
                plugin = createImageOperatorPluginInstance((Class<? extends ImageOperator>) clazz);
            }
            else
            {
                System.err.println("Could not find how to create plugin for item with class: " + clazz.getName());
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

        plugins.put(clazz, plugin);
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
        String baseDirName = System.getProperty("user.dir");
        String pluginsDirName = baseDirName + File.separator + "plugins";
        File pluginsDir = new File(pluginsDirName);
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
            try
            {
                loadPluginsFromJarFile(file);
            }
            catch (Exception ex)
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
                PluginHandler handler = createPluginHandler(file, entry);
                if (handler != null && handler.plugin != null)
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
     * @param fileName
     *            the name of the file containing plugins and configuration
     *            file.
     * @throws IOException
     */
    private ArrayList<String> readPluginEntries(File file) throws IOException
    {
        ArrayList<String> entries = new ArrayList<String>();

        try (JarFile jarFile = new JarFile(file))
        {
            JarEntry entry = jarFile.getJarEntry("plugins.config");
            if (entry == null)
            { throw new RuntimeException("Could not find configuration file into jar file: " + file); }

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
        { throw new RuntimeException("Unable to parse plugin entry: " + entry); }

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
            URI uri = Paths.get("jar:file:" + file + "!/").toUri();
            URL[] urls = { uri.toURL() };
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            // iterate over items in the jar file
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                JarEntry je = entries.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class"))
                {
                    continue;
                }

                // remove six characters corresponding to ".class"
                String className2 = je.getName().substring(0, je.getName().length() - 6);
                className2 = className2.replace('/', '.');

                if (!className2.equals(className))
                {
                    continue;
                }

                Class<?> pluginClass;
                try
                {
                    pluginClass = cl.loadClass(className2);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new RuntimeException("Could not load plugin class.", ex);
                }

                if (!FramePlugin.class.isAssignableFrom(pluginClass))
                { throw new RuntimeException("Plugin class must implement the FramePlugin interface."); }

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
                catch (java.lang.NoClassDefFoundError err)
                {
                    throw new RuntimeException("Error encoutered during plugin creation.", err);
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
}
