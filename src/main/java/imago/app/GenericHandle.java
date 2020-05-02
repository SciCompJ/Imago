/**
 * 
 */
package imago.app;


/**
 * Handle class for any type of objects.
 * 
 * @author dlegland
 *
 */
public class GenericHandle extends ObjectHandle
{
    Object object;
    
    public GenericHandle(Object object, String name, String tag)
    {
        super(tag);
        this.object = object;
        this.name = name;
    }
    
    public Object getObject()
    {
        return this.object;
    }

}
