/**
 * 
 */
package imago.gui;

/**
 * An Exception thrown when a Frame plugin could not be instantiated.  
 */
public class FramePluginInstantiationException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public FramePluginInstantiationException(String errorMessage)
    {
        super(errorMessage);
    }

    public FramePluginInstantiationException(String errorMessage, Throwable err)
    {
        super(errorMessage, err);
    }
}
