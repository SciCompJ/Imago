/**
 * 
 */
package imago.plugin.plugin.crop;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.Node;
import imago.app.scene.io.JsonSceneReader;

/**
 * @author dlegland
 *
 */
public class Crop3D_LoadPolygonsFromJsonTest
{

	/**
	 * Test method for {@link imago.plugin.plugin.crop.Crop3D_LoadPolygonsFromJson#run(imago.gui.ImagoFrame, java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void test_() throws IOException
	{
		String fileName = getClass().getResource("/json/tomStackLinearRings.json").getFile();
		File file = new File(fileName);
		assertTrue(file.exists());
		assertTrue(file.canRead());
		
		FileReader fileReader = new FileReader(file);
		JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
		
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);

		// expect a group node...
		Node node = sceneReader.readNode();

		assertTrue(node instanceof ImageSerialSectionsNode);
	}

}
