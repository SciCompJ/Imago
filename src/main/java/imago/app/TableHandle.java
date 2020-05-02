/**
 * 
 */
package imago.app;

import net.sci.table.Table;

/**
 * @author dlegland
 *
 */
public class TableHandle extends ObjectHandle
{
    Table table;
    
    public TableHandle(Table table, String name, String tag)
    {
        super(tag);
        this.table = table;
        this.name = name;
    }
    
    public Table getTable()
    {
        return table;
    }
    
    public Object getObject()
    {
        return this.table;
    }

}
