/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Superclass for all the entities manipulated by the Imago application.
 * Instances of {@code ObjectHandle} can encapsulate images, tables,
 * geometries...
 *
 * Manages several generic variables:
 * <ul>
 * <li>the item; storage is left to subclasses</li>
 * <li>a tag to identify the item uniquely within the application</li>
 * <li>a name, to identify item within GUI</li>
 * <li>a flag for modification.</li>
 * </ul>
 * 
 * The {@code GenericHandle} class provides a default implementation for
 * encapsulating items of any type. The different sub-modules (image, table...)
 * can manage specific handles.
 */
public abstract class ObjectHandle
{
    // =============================================================
    // Static methods
    
    /**
     * Retrieves the list of names of all the handles within the application.
     * @param handles
     * @return
     */
    public static final Collection<String> getNames(Collection<? extends ObjectHandle> handles)
    {
        ArrayList<String> names = new ArrayList<String>(handles.size());
        for (ObjectHandle handle : handles)
        {
            if (!handle.getName().isEmpty())
            {
                names.add(handle.getName());
            }
        }
        return names;
    }
    
    /**
     * Creates a new name by appending a suffix to another name. If the base
     * name is null or composed of only blank characters, the new name is
     * corresponds to the suffix alone. Otherwise, the new name is created by
     * inserting a hyphen character ("-") between the base name and the suffix.
     * 
     * @param baseName
     *            the radical used to create the new name
     * @param suffix
     *            the string to append at the end of the suffix
     * @return a new name composed of the base name and a suffix.
     */
    public static final String appendSuffix(String baseName, String suffix)
    {
        if (baseName != null && !baseName.isBlank())
        {
            return baseName + "-" + suffix;
        }
        else
        {
            return suffix;
        }
    }
    
    
    // =============================================================
    // Class variables
    
    /**
     * The tag of the object, used to identify it in the workspace. Should be
     * unique in the workspace.
     */
    protected String tag;
    
    /**
     * The name of the object (used for display in GUI).
     * Can be updated. The ImagoApp class contains methods to ensure handles keep unique names.
     */
    protected String name;
    
    /**
     * A boolean flag indicating whether the underlying item has been modified.
     * Default value is false.
     */
    protected boolean modified = false;
    
    
    // =============================================================
    // Constructor
    
    protected ObjectHandle(String tag)
    {
        this.tag = tag;
    }
    
    // =============================================================
    // Accessors
    
    public abstract Object getObject();
    
    public String getName() 
    {
        return this.name;
    }
    
    /**
     * Changes the name of this handle. To avoid two or moerz handle to share
     * the same name, the ImagoApp class contains methods to create unique
     * names.
     * 
     * @see ImagoApp#createHandleName(String)
     * @param newName the new name for this handle.
     */
    public void setName(String newName) 
    {
        this.name = newName;
        this.modified = true;
    }
    
    public String getTag() 
    {
        return this.tag;
    }
    

    public boolean isModified() 
    {
        return this.modified;
    }
    
    public void setModified(boolean b) 
    {
        this.modified = b;
    }
    
}
