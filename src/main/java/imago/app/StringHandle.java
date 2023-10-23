/**
 * 
 */
package imago.app;


/**
 * An handle to a string.
 * 
 * @author dlegland
 *
 */
public class StringHandle extends ObjectHandle
{
    String string;
    
    public StringHandle(String string, String name, String tag)
    {
        super(tag);
        this.string = string;
        this.name = name;
    }
    
    public String getString()
    {
        return string;
    }

    public void setString(String string)
    {
        this.string= string ;
    }
    
    public String getObject()
    {
        return this.string;
    }

}
