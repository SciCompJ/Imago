/**
 * 
 */
package imago.plugin.plugin.crop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.stream.JsonReader;

import imago.app.ImageHandle;
import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.ImageSliceNode;
import imago.app.scene.Node;
import imago.app.scene.io.JsonSceneReader;
import imago.gui.ImageFrame;
import imago.gui.ImageViewer;
import imago.gui.ImagoFrame;
import imago.gui.Plugin;
import imago.gui.viewer.StackSliceViewer;

/**
 * @author dlegland
 *
 */
public class Crop3D_LoadPolygonsFromJson implements Plugin
{
	private JFileChooser openWindow = null;


	@Override
	public void run(ImagoFrame frame, String args)
	{
        // Check type is image frame
        if (!(frame instanceof ImageFrame))
            return;

        ImageViewer viewer = ((ImageFrame) frame).getImageView();
        if (!(viewer instanceof StackSliceViewer))
        {
            System.out.println("requires an instance of stack slice viewer");
            return;
        }
        
        // get handle to the document
        ImageHandle handle = ((ImageFrame) frame).getImageHandle();
		
        // create file dialog using last open path
		String lastPath = ".";
		openWindow = new JFileChooser(lastPath);
		openWindow.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

		// Open dialog to choose the file
		int ret = openWindow.showOpenDialog(frame.getWidget());
		if (ret != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		// Check the chosen file is state
		File file = openWindow.getSelectedFile();
		if (!file.exists())
		{
			return;
		}
		
		JsonSceneReader sceneReader;
		
		try
		{
			FileReader fileReader = new FileReader(file);
			JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
			sceneReader = new JsonSceneReader(jsonReader);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}

		try 
		{
			// expect a group node...
			Node node = sceneReader.readNode();
			if (!(node instanceof ImageSerialSectionsNode))
			{
				throw new RuntimeException("JSON file should contains a single ImageSerialSectionsNode instance.");
			}
			
	        ImageSerialSectionsNode polyNode = Crop3D.getPolygonsNode(handle);
	        
	        for(ImageSliceNode child : ((ImageSerialSectionsNode) node).children())
	        {
	        	polyNode.addSliceNode(child);
	        }

		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}

        System.out.println("reading polygons terminated.");

        // need to call this to update items to display 
        viewer.refreshDisplay(); 
        
        viewer.repaint();

	}
	
}
