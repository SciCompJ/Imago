package imago.app;

import static org.junit.Assert.assertEquals;

import net.sci.array.Array2D;
import net.sci.array.scalar.UInt8Array2D;
import net.sci.image.Image;

import org.junit.Test;

public class ImagoAppTest {

	@Test
	public void testAddDocument() {
		ImagoApp app = new ImagoApp();
		
		Array2D<?> array = UInt8Array2D.create(320, 200);
		Image image = new Image(array);
		
		ImageHandle.create(app, image);
		
        assertEquals(1, ImageHandle.getAll(app).size());
	}
}
