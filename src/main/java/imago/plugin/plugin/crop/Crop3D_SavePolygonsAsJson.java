/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.stream.JsonWriter;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.io.JsonSceneWriter;
import imago.gui.ImageFrame;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;

/**
 * @author dlegland
 *
 */
public class Crop3D_SavePolygonsAsJson implements Plugin
{
	private JFileChooser saveWindow = null;


	@Override
	public void run(ImagoFrame frame, String args)
	{
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;

        // get handle to the document
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		
        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(handle);
        if (polyNode == null)
        {
        	System.err.println("Current image does not contain Crop3D polygon information");
        	return;
        }
        
        // create file dialog using last open path
//		String lastPath = ".";
		String imageName = ((ImageFrame) frame).getImage().getName();
		saveWindow = new JFileChooser(new File(imageName + ".json"));
		saveWindow.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

		// Open dialog to choose the file
		int ret = saveWindow.showSaveDialog(frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		// Check the chosen file is state
		File file = saveWindow.getSelectedFile();
		if (!file.getName().endsWith(".json"))
		{
			File parent = file.getParentFile();
			file = new File(parent, file.getName() + ".json");
		}
		
		try 
		{
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			JsonWriter jsonWriter = new JsonWriter(new PrintWriter(fileWriter));
			jsonWriter.setIndent("  ");
			JsonSceneWriter writer = new JsonSceneWriter(jsonWriter);

			writer.writeNode(polyNode);

			fileWriter.close();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
		
		System.out.println("Saving polygon terminated.");
	}
	
}
