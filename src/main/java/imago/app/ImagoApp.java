/**
 * 
 */
package imago.app;

import java.io.File;

import net.sci.geom.Geometry;
import net.sci.image.Image;
import net.sci.table.Table;

/**
 * The main manager of the application. Contains the workspace, and 
 * various global settings.
 * 
 * @author David Legland
 *
 */
public class ImagoApp 
{
	// =============================================================
	// class variables
    
    /**
     * Contains handles to the different entities manipulated by the
     * application: Image, Table, Geometry...
     */
    Workspace workspace = new Workspace();

    /**
     * Some global settings / preferences for current user.
     */
    public UserPreferences userPreferences = new UserPreferences();
	
	
	// =============================================================
	// Constructor

	/**
	 * Empty constructor by default.
	 */
	public ImagoApp()
	{
	    this.userPreferences = loadUserPreferences();
	}
	
	private UserPreferences loadUserPreferences()
	{
	    // retrieve imago directory within user home directory
        String userHome = System.getProperty("user.home");
        File prefsDir = new File(userHome, ".imago");
        if (!prefsDir.exists())
        {
            return new UserPreferences();
        }
        
        // identify property file containing preferences
        File initFile = new File(prefsDir, "imago_prefs.txt");
        if (!initFile.exists())
        {
            return new UserPreferences();
        }
        
        return UserPreferences.read(initFile);
	}

	
    // =============================================================
    // Global management of handles

    /**
     * Creates a new handle for the input item, by creating the appropriate name
     * and tag, and adds the handle to the list of handles in the workspace.
     * <p>
     * 
     * This methods tries to create the appropriate sub-class of
     * {@code ObjectHandle}, depending on the class of the input item. For
     * example if {@code item} is an instance of the {@code Image} class, an
     * instance of {@code ImageHandle} will be created.
     * 
     * @param item
     *            the item to add.
     * @param name
     *            the name associated to this object.
     * @param baseTag
     *            the string pattern used to build handle tag.
     * @return the handle created for this object.
     */
    public ObjectHandle createHandle(Object item, String name, String baseTag)
    {
        // setup indexation variables
        name = createHandleName(name != null ? name : "NoName");
        String tag = workspace.findNextFreeTag(baseTag);
        
        // create handle based on class of item
        ObjectHandle handle = switch(item)
        {
            case Image image -> new ImageHandle(image, name, tag);
            case Table table -> new TableHandle(table, name, tag);
            case Geometry geom -> new GeometryHandle(geom, name, tag);
            case String string -> new StringHandle(string, name, tag);
            default -> new GenericHandle(item, name, tag);
        };
        
        workspace.addHandle(handle);
        
        return handle;
    }
    
	public void removeHandle(ObjectHandle handle)
	{
	    workspace.removeHandle(handle.tag);
	}


    // =============================================================
    // Management of handle names

    /**
     * Creates a unique name for a handle, given a base name (typically a file name). If
     * application already contains a document with same base name, an index is
     * added to make the name unique.
     * 
     * @param name
     *            a base name for the handle, for example the file name.
     * @return a unique name based on proposed name.
     */
    public String createHandleName(String name)
    {
        // avoid empty name
        if (name == null || name.isEmpty())
        {
            name = "NoName";
        }
        
        if (!workspace.hasHandleWithName(name))
        {
            return name;
        }
        
        // extract base name (before extension if present)
        String[] fileParts = splitFileNameParts(name);
        String baseName = fileParts[0];
        
        // remove trailing suffix if present
        baseName = removeTrailingDigits(baseName);
            
        // create names with the pattern until we found a non existing one
        int index = 1;
        do
        {
            name = buildFileName(String.format("%s-%d", baseName, index++), fileParts[1]);
        } while (workspace.hasHandleWithName(name));
        
        return name;
    }
    
    private static final String[] splitFileNameParts(String filename) 
    {
        // identifies position of extension
        int extensionIndex = filename.lastIndexOf(".");

        // Case of no extension.
        if (extensionIndex == -1)
        {
            return new String[] {filename, ""};
        }
        
        String baseName = filename.substring(0, extensionIndex); 
        String extensionName = filename.substring(extensionIndex+1); 
        
        return new String[] {baseName, extensionName};
    }
    
    private static final String removeTrailingDigits(String name)
    {
        // basic check-up
        if (name.isEmpty()) 
            return name;
        
        // if last character is not a digit, return the base name
        if (!name.substring(name.length()-1).matches("[0-9]"))
            return name;

        // remove trailing digits
        while (!name.isEmpty() && name.substring(name.length()-1).matches("[0-9]"))
            name = name.substring(0, name.length()-1);
        // remove trailing '-' characters
        while (!name.isEmpty() && name.endsWith("-"))
            name = name.substring(0, name.length()-1);
        
        return name;
    }
    
    private String buildFileName(String baseName, String extensionName)
    {
        if (extensionName == null || extensionName.isEmpty())
            return baseName;
        else
            return baseName + "." + extensionName;
    }
    
    // =============================================================
    // Management of user preferences
    
    /**
     * Save user preferences within default file in
     * "[user.home]/.imago/imago_prefs.txt".
     */
    public void saveUserPreferences()
    {
        // retrieve imago directory within user home directory
        String userHome = System.getProperty("user.home");
        File prefsDir = new File(userHome, ".imago");
        if (!prefsDir.exists())
        {
            prefsDir.mkdir();
        }
        
        // identify property file containing preferences
        File initFile = new File(prefsDir, "imago_prefs.txt");
        
        // save preferences
        this.userPreferences.write(initFile);
    }


    // =============================================================
    // Management of workspace

    /**
     * @return the workspace
     */
    public Workspace getWorkspace()
    {
        return workspace;
    }

}
