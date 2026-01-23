/**
 * 
 */
package imago.app;

import java.util.Collection;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Contains a set of {@code ObjectHandle} instances, indexed by their tag.
 * 
 * Each tag is unique by definition. Each Handle name is also expected to be
 * unique. For this, several methods are provided to check whether a handle
 * exists with a given name, or to generate new unique names.
 * 
 * @see ObjectHandle
 * 
 * @author dlegland
 */
public class Workspace
{
    // =============================================================
    // class variables

    /**
     * The list of handles managed by the application, indexed by their tag.
     */
    private TreeMap<String, ObjectHandle> handles = new TreeMap<String, ObjectHandle>();
    
    
    // =============================================================
    // Query handles from name or class
    
    public Collection<ObjectHandle> getHandles(Predicate<ObjectHandle> filter)
    {
        return handles.values().stream()
                .filter(filter)
                .toList();
    }
    
    public Collection<String> getHandleNames(Predicate<ObjectHandle> filter)
    {
        return handles.values().stream()
                .filter(filter)
                .map(h -> h.getName())
                .toList();
    }
    
    /**
     * Returns the first handle whose name matches the specified name.
     * 
     * @param name
     *            the name of the handle.
     * @return the first handle whose name matches the specified name.
     */
    public ObjectHandle findHandleWithName(String name)
    {
        return handles.values().stream()
                .filter(handle -> handle.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Workspace does not contain any item with name: " + name));
    }
    
    /**
     * Checks whether this workspace contains a handle with the specified name.
     * 
     * @param name
     *            the name of the handle
     * @return true if this workspace contains an handle with the specified
     *         name.
     */
    public boolean hasHandleWithName(String name)
    {
        return handles.values().stream().anyMatch(handle -> handle.getName().equals(name));
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
        
        if (!hasHandleWithName(name))
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
        } while (hasHandleWithName(name));
        
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
    
    private static final String buildFileName(String baseName, String extensionName)
    {
        if (extensionName == null || extensionName.isEmpty())
            return baseName;
        else
            return baseName + "." + extensionName;
    }
    

    // =============================================================
    // get handles from the workspace

    /**
     * Returns the item associated with the specified tag.
     * 
     * @param tag
     *            the tag.
     * @return the tag associated to the item.
     */
    public ObjectHandle getHandle(String tag)
    {
        if (this.handles.containsKey(tag))
            return this.handles.get(tag);
        else
            throw new RuntimeException("Workspace does not contain any item with tag: " + tag);
    }
    
    public Collection<ObjectHandle> getHandles()
    {
        return this.handles.values();
    }
    
    public boolean hasHandle(String tag)
    {
        return this.handles.containsKey(tag);
    }


    // =============================================================
    // Management of tags

    public Collection<String> getTags()
    {
        return this.handles.keySet();
    }
    
    /**
     * Creates a unique tag for an item, given a base name (file name). If
     * application already contains an item with same base tag, an index is
     * added to make the tag unique.
     * 
     * @param baseTag
     *            a base tag for the item, for example based on its class.
     * @return a unique tag.
     */
    public String findNextFreeTag(String baseTag)
    {
        // avoid empty tags
        if (baseTag == null || baseTag.isEmpty())
        {
            baseTag = "obj";
        }
        
        // create names with the pattern until we found a non existing one
        String tag;
        int index = 0;
        do
        {
            tag = String.format(baseTag + "%02d", index++);
        } while (handles.containsKey(tag));
        
        return tag;
    }
    
    
    // =============================================================
    // update list of handles in the workspace

    /**
     * Adds the specified handle to the workspace. If another handle with the
     * same tag was stored in the workspace, it is replaced.
     * 
     * @param handle
     *            the handle to add.
     */
    public void addHandle(ObjectHandle handle)
    {
        String tag = handle.tag;
        this.handles.put(tag, handle);
    }

    /**
     * Removes the specified handle from the workspace, or throws an exception.
     * 
     * @param handle
     *            the handle to remove
     */
    public void removeHandle(ObjectHandle handle)
    {
        removeHandle(handle.tag);
    }
    
    /**
     * Removes the handle identified by its tag from the workspace, or throws an
     * exception if there is no handle with the specified tag.
     * 
     * @param tag
     *            the tag of the handle to remove
     */
    public void removeHandle(String tag)
    {
        if (this.handles.containsKey(tag))
            this.handles.remove(tag);
        else
            throw new RuntimeException("Workspace does not contain any item with tag: " + tag);
    }

    /**
     * Removes all handles within workspace.
     */
    public void clear()
    {
        this.handles.clear();
    }

}
