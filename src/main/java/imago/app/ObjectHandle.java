/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Superclass for all the entities manipulated by the Imago application.
 *
 * Manages a name, a tag.
 * 
 * @author dlegland
 *
 */
public abstract class ObjectHandle
{
    // =============================================================
    // Static methods
    
    public static final Collection<String> getNames(Collection<ObjectHandle> handles)
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
