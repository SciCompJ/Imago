/**
 * 
 */
package imago.app;

import java.util.ArrayList;
import java.util.Collection;

import net.sci.table.Table;

/**
 * An object handle containing a Table instance.
 * 
 * @author dlegland
 *
 */
public class TableHandle extends ObjectHandle
{
    // =============================================================
    // Static utility methods
    
    /**
     * Creates a new handle for a table, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param table
     *            the image instance.
     * @return the handle to manage the image.
     */
    public static final TableHandle create(ImagoApp app, Table table)
    {
        return create(app, table, null);
    }

    /**
     * Creates a new handle for a table, adds it to the workspace, and returns
     * the created handle.
     * 
     * @param image
     *            the image instance.
     * @param parent
     *            a parent handle, used to initialize handles fields.
     * @return the handle to manage the image.
     */
    public static final TableHandle create(ImagoApp app, Table table, TableHandle parent)
    {
        Workspace workspace = app.getWorkspace();
        String tag = workspace.findNextFreeTag("tab");
        String name = app.createHandleName(table.getName());
        TableHandle handle = new TableHandle(table, name, tag);
        workspace.addHandle(handle);
        return handle;
    }

    /**
     * Returns all the table handles contained in the application.
     * 
     * @param app
     *            the application to explore
     * @return the list of all table handles within the application workspace
     */
    public static final Collection<TableHandle> getAll(ImagoApp app)
    {
        ArrayList<TableHandle> res = new ArrayList<TableHandle>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof TableHandle)
            {
                res.add((TableHandle) handle);
            }
        }
        return res;
    }
    
    /**
     * Get the name of all image handles.
     * 
     * @return the list of names of handles containing images.
     */
    public static final Collection<String> getAllNames(ImagoApp app)
    {
        ArrayList<String> res = new ArrayList<String>();
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof TableHandle)
            {
                res.add(handle.getName());
            }
        }
        return res;
    }
    
    public static final TableHandle findFromName(ImagoApp app, String handleName)
    {
        for (ObjectHandle handle : app.getWorkspace().getHandles())
        {
            if (handle instanceof TableHandle)
            {
                if (handle.getName().equals(handleName))
                    return (TableHandle) handle;
            }
        }
        
        throw new IllegalArgumentException("App does not contain any table handle with name: " + handleName);
    }


    // =============================================================
    // Class variables
    
    Table table;
    
    
    // =============================================================
    // Constructor
    
    public TableHandle(Table table, String name, String tag)
    {
        super(tag);
        this.table = table;
        this.name = name;
    }
    
    
    // =============================================================
    // Access methods
    
    public Table getTable()
    {
        return table;
    }
    
    public Table getObject()
    {
        return this.table;
    }

}
