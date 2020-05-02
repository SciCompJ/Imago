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
        ObjectHandle handle = ws.createHandle("hello!", "string", "str");
        assertNotNull(handle.tag);
    }

    /**
     * Test method for {@link imago.app.Workspace#getHandle(java.lang.String)}.
     */
    @Test
    public final void testGetItem()
    {
        Workspace ws = new Workspace();
        ObjectHandle handle = ws.createHandle("hello!", "string", "str");
        assertNotNull(handle.tag);
        StringHandle item = (StringHandle) ws.getHandle(handle.tag);
        assertEquals(item.getString(), "hello!");
    }

}
