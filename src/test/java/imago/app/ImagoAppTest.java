package imago.app;

import static org.junit.Assert.assertEquals;
import net.sci.array.data.Array2D;
import net.sci.array.data.scalar2d.UInt8Array2D;
import net.sci.image.Image;

import org.junit.Test;

public class ImagoAppTest {

	@Test
	public void testAddDocument() {
		ImagoApp app = new ImagoApp();
		
		Array2D<?> array = UInt8Array2D.create(320, 200);
		Image img = new Image(array);
		ImagoDoc doc = new ImagoDoc("doc1", img);
		app.addDocument(doc);
		
		assertEquals(1, app.documentNumber());
	}
}
