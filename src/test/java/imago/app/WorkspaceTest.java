/**
 * 
 */
package imago.app;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class WorkspaceTest
{

    /**
     * Test method for {@link imago.app.Workspace#addNewItem(java.lang.Object)}.
     */
    @Test
    public final void testAddNewItem()
    {
        Workspace ws = new Workspace();
        ws.addHandle(new GenericHandle("hello!", "string", "str"));
        
        assertTrue(ws.hasHandle("str"));
    }

    /**
     * Test method for {@link imago.app.Workspace#getHandle(java.lang.String)}.
     */
    @Test
    public final void testGetItem()
    {
        Workspace ws = new Workspace();
        ws.addHandle(new StringHandle("hello!", "string", "str"));
        StringHandle item = (StringHandle) ws.getHandle("str");
        assertEquals(item.getString(), "hello!");
    }

}
