/**
 * 
 */
package imago.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * 
 */
class FramePluginTest
{

    /**
     * Test method for {@link imago.gui.FramePlugin#parseOptionsString(java.lang.String)}.
     */
    @Test
    final void testParseOptionsString()
    {
        String optionsString = "integer=321, string= string value";
        
        Map<String, String> res = FramePlugin.parseOptionsString(optionsString);
        assertEquals(2, res.size());
        
        HashMap<String, String> exp = new HashMap<>();
        exp.put("string", "string value");
        exp.put("integer", "321");
        
        for (Map.Entry<String, String> entry : exp.entrySet())
        {
            String key = entry.getKey();
            assertTrue(res.containsKey(key));
            String expValue = exp.get(key); 
            assertEquals(expValue, res.get(key));
        }
        
        // check case insensitivity for keys
        assertTrue(res.containsKey("InTeGer"));
    }

}
