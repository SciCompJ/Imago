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
