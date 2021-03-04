package imago.plugin.plugin.crop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	Crop3D_AddPolygonTest.class,
    Crop3D_CropImageTest.class,
    Crop3D_LoadPolygonsFromJsonTest.class,
    CroppedUInt8Array3DTest.class,
    })
public class AllTests {
  //nothing
}
