/**
 * 
 */
package imago.plugin.plugin.crop;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.google.gson.stream.JsonReader;

import imago.app.scene.ImageSerialSectionsNode;
import imago.app.scene.io.JsonSceneReader;
import net.sci.array.Array3D;
import net.sci.array.scalar.ScalarArray;
import net.sci.image.Image;
import net.sci.image.io.TiffImageReader;

/**
 * @author dlegland
 *
 */
public class Crop3D_CropImageTest
{

	/**
	 * Test method for {@link imago.plugin.plugin.crop.Crop3D_CropImage#process(net.sci.array.Array, imago.app.scene.ImageSerialSectionsNode)}.
	 * @throws IOException 
	 */
	@Test
	public void testProcess() throws IOException
	{
		String imageFileName = getClass().getResource("/images/tomStack.tif").getFile();
		Image image = new TiffImageReader(imageFileName).readImage();
		
		String fileName = getClass().getResource("/json/tomStackLinearRings.json").getFile();
		File file = new File(fileName);
		assertTrue(file.exists());
		assertTrue(file.canRead());
		
		FileReader fileReader = new FileReader(file);
		JsonReader jsonReader = new JsonReader(new BufferedReader(fileReader));
		
		JsonSceneReader sceneReader = new JsonSceneReader(jsonReader);
		ImageSerialSectionsNode node = (ImageSerialSectionsNode) sceneReader.readNode();

		Crop3D_CropImage plugin = new Crop3D_CropImage();
		Array3D<?> res = (Array3D<?>) plugin.process((ScalarArray<?>) image.getData(), node);
		
		assertNotNull(res);
	}

}
