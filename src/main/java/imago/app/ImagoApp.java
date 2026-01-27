/**
 * 
 */
package imago.app;

import java.io.File;

import imago.image.ImageHandle;
import imago.shape.GeometryHandle;
import imago.table.TableHandle;
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
        name = workspace.createHandleName(name != null ? name : "NoName");
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
        return workspace.createHandleName(name);
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
