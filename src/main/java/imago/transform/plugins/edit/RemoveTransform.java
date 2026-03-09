/**
 * 
 */
package imago.transform.plugins.edit;

import imago.app.ImagoApp;
import imago.gui.ImagoFrame;
import imago.transform.TransformHandle;
import imago.transform.TransformManager;
import imago.transform.plugins.TransformManagerPlugin;

/**
 * Renames the selected transform. Requires selection to have only one element.
 */
public class RemoveTransform implements TransformManagerPlugin
{

    @Override
    public void run(ImagoFrame frame, String optionsString)
    {
        TransformManager tm = (TransformManager) frame;
        ImagoApp appli = tm.getGui().getAppli();
        for (TransformHandle handle : tm.getSelectedHandles())
        {
            appli.removeHandle(handle);
        }
        
        tm.updateInfoTable();
    }
}