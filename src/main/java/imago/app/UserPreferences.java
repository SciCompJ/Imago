/**
 * 
 */
package imago.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Global settings for GUI interactions, such as brush size or color.
 * 
 * @author dlegland
 *
 */
public class UserPreferences
{
    // ===================================================================
    // Static methods
    
    /**
     * Reads user preferences from a file using classical key=value parameter
     * pairs.
     * 
     * @param file
     *            the file containing the preferences to read
     * @return a new UserPreferences instance
     */
    public final static UserPreferences read(File file)
    {
        Properties props = new Properties();
        
        try (InputStream input = new FileInputStream(file)) 
        {
            // load a properties file
            props.load(input);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Could not parse property file", ex);
        }
        
        
        UserPreferences prefs = new UserPreferences();
        
        if (props.containsKey("imago.files.lastOpenPath"))
        {
            prefs.lastOpenPath = (String) props.get("imago.files.lastOpenPath");
        }
        if (props.containsKey("imago.files.lastSavePath"))
        {
            prefs.lastSavePath = (String) props.get("imago.files.lastSavePath");
        }
        if (props.containsKey("imago.tools.brush.radius"))
        {
            prefs.brushRadius = Double.parseDouble((String) props.get("imago.tools.brush.radius"));
        }
        if (props.containsKey("imago.tools.brush.value"))
        {
            prefs.brushValue = Double.parseDouble((String) props.get("imago.tools.brush.value"));
        }
        
        return prefs;
    }
    
    
    // ===================================================================
    // The public fields
    
    /**
     * The path to the last directory that was used to open a file.
     */
    public String lastOpenPath = ".";
    
    /**
     * The path to the last directory that was used to save a file.
     */
    public String lastSavePath = ".";
    
    /**
     * The value of the brush for intensity images.
     * Default is 255, corresponding to white for UInt8 images.
     */
    public double brushValue = 255;
    
    /**
     * The radius of the brush.
     */
    public double brushRadius = 2.0;

    
    // ===================================================================
    // General methods
    
    public void write(File file)
    {
        // convert user preferences to property instance, by converting to
        // pseudo-hierarchic coding
        Properties props = new Properties();
        props.put("imago.files.lastOpenPath", this.lastOpenPath);
        props.put("imago.files.lastSavePath", this.lastSavePath);
        props.put("imago.tools.brush.radius", Double.toString(this.brushRadius));
        props.put("imago.tools.brush.value", Double.toString(this.brushValue));
        
        // use Property class to save user preferences
        try (OutputStream output = new FileOutputStream(file)) 
        {
            // save properties to project root folder
            props.store(output, "Imago User Preferences");
        } 
        catch (IOException io) 
        {
            io.printStackTrace();
        }
    }
    
}
