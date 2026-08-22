/**
 * 
 */
package imago.developer.plugins;

import java.io.PrintStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;

/**
 * Prints user preferences on the console.
 */
public class PrintUserPreferences implements FramePlugin
{
    /**
     * Default empty construtor.
     */
    public PrintUserPreferences()
    {
    }
    
    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        Preferences prefs = Preferences.userRoot().node("imago");

        try
        {
            printNode(prefs, System.out, 0);
        }
        catch (BackingStoreException e)
        {
            e.printStackTrace();
        }
    }
    
    private void printNode(Preferences prefs, PrintStream os, int indentLevel) throws BackingStoreException
    {
        String indentString = "";
        for (int i = 0; i < indentLevel; i++)
        {
            indentString += "  ";
        }
        
        for (String k : prefs.keys())
        {
            os.format("%s%s=%s\n", indentString, k, prefs.get(k, ""));
        }
        
        for (String childName : prefs.childrenNames())
        {
            os.format("%sNode %s:\n", indentString, childName);
            printNode(prefs.node(childName), os, indentLevel+1);
        }
    }
}
