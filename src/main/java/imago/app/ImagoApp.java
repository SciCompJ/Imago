/**
 * 
 */
package imago.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

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
    // Management of image handles
    
    /**
     * Creates a new handle for an image, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param image
     *            the image instance.
     * @return the handle to manage the image.
     */
    public ImageHandle createImageHandle(Image image)
    {
        return createImageHandle(image, null);
    }

    /**
     * Creates a new handle for an image, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param image
     *            the image instance.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the image.
     */
    public ImageHandle createImageHandle(Image image, ImageHandle parent)
    {
        String tag = workspace.findNextFreeTag("img");
        String name = createHandleName(image.getName());
        ImageHandle handle = new ImageHandle(image, name, tag);
        if (parent != null)
        {
        	handle.copyDisplaySettings(parent);
        }
        workspace.addHandle(handle);
        return handle;
    }

	public int imageHandleNumber()
	{
		return getImageHandles().size();
	}
	
	public Collection<ImageHandle> getImageHandles()
	{
	    ArrayList<ImageHandle> res = new ArrayList<ImageHandle>();
	    for (ObjectHandle handle : workspace.getHandles())
	    {
	        if (handle instanceof ImageHandle)
	        {
	            res.add((ImageHandle) handle);
	        }
	    }
	    return res;
	}
	

	/**
	 * Get the names of all open image documents.
	 * 
	 * @return the list of names of documents containing images.
	 */
	public Collection<String> getImageHandleNames()
	{
        ArrayList<String> res = new ArrayList<String>();
        for (ObjectHandle handle : workspace.getHandles())
        {
            if (handle instanceof ImageHandle)
            {
                res.add(handle.getName());
            }
        }
        return res;
	}
	
	public ImageHandle getImageHandleFromName(String handleName)
	{
		for (ImageHandle handle : getImageHandles())
		{
			if (handle.getName().equals(handleName))
				return handle;
		}
		
		throw new IllegalArgumentException("App does not contain any image handle with name: " + handleName);
	}


	// =============================================================
    // Management of Table handles
    
    /**
     * Creates a new handle for a table, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param table
     *            the table instance.
     * @return the handle to manage the table.
     */
    public TableHandle createTableHandle(Table table)
    {
        return createTableHandle(table, null);
    }
    
    /**
     * Creates a new handle for a table, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param table
     *            the table instance.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the image.
     */
    public TableHandle createTableHandle(Table table, TableHandle parent)
    {
        String tag = workspace.findNextFreeTag("tab");
        String name = createHandleName(table.getName());
        TableHandle handle = new TableHandle(table, name, tag);
        workspace.addHandle(handle);
        return handle;
    }

	public Collection<TableHandle> getTableHandles()
	{
	    ArrayList<TableHandle> res = new ArrayList<TableHandle>();
        for (ObjectHandle handle : workspace.getHandles())
	    {
	        if (handle instanceof TableHandle)
	        {
	            res.add((TableHandle) handle);
	        }
	    }
	    return res;
	}
	
	
    // =============================================================
    // Management of geometry handles

    /**
     * Creates a new handle for a geometry, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param geom
     *            the geometry instance.
     * @return the handle to manage the table.
     */
    public GeometryHandle createGeometryHandle(Geometry geom)
    {
        String baseTag = GeometryHandle.createTag(geom);
        String name = baseTag;
        String tag = workspace.findNextFreeTag(baseTag);
        GeometryHandle handle = new GeometryHandle(geom, name, tag);
        workspace.addHandle(handle);
        return handle;
    }
    
    public Collection<GeometryHandle> getGeometryHandles()
    {
        ArrayList<GeometryHandle> res = new ArrayList<GeometryHandle>();
        for (ObjectHandle handle : workspace.getHandles())
        {
            if (handle instanceof GeometryHandle)
            {
                res.add((GeometryHandle) handle);
            }
        }
        return res;
    }
    
	
    // =============================================================
    // Global management of handles

    /**
     * Creates a new handle to the input argument, creates the appropriate tag,
     * and adds it to the list of handles in the workspace.
     * <p>
     * 
     * This methods tries to create the appropriate sub-class of ObjectHAndle,
     * depending on the class of the input object. For example if
     * <code>object</code> is an instance of the <code>Image</code> class, an
     * instance of <code>ImageHandle</code> will be created.
     * 
     * @param object
     *            the object to add.
     * @param name
     *            the name associated to this object.
     * @param baseTag
     *            the string pattern used to build handle tag.
     * @return the handle created for this object.
     */
    public ObjectHandle createHandle(Object object, String name, String baseTag)
    {
        if (name == null)
        {
            name = "NoName";
        }
        
        ObjectHandle handle;
        String tag = workspace.findNextFreeTag(baseTag);
        name = createHandleName(name);
        
        if (object instanceof Image)
        {
            handle = new ImageHandle((Image) object, name, tag);
        }
        else if (object instanceof Table)
        {
            handle = new TableHandle((Table) object, name, tag);
        }
        else if (object instanceof Geometry)
        {
            handle = new GeometryHandle((Geometry) object, name, tag);
        }
        else if (object instanceof String)
        {
            handle = new StringHandle((String) object, name, tag);
        }
        else
        {
            handle  = new GenericHandle(object, name, tag);
        }
        
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
