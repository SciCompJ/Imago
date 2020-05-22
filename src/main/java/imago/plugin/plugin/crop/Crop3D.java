/**
 * 
 */
package imago.plugin.plugin.crop;

import imago.app.ImageHandle;
import imago.app.scene.GroupNode;
import imago.app.scene.ImageSerialSectionsNode;

/**
 * Utility methods for 3D Crop plugin.
 * 
 * @author dlegland
 *
 */
public class Crop3D
{
    public static final GroupNode getCrop3dNode(ImageHandle handle)
    {
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());

        if (rootNode.hasChildWithName("crop3d"))
        {
            return (GroupNode) rootNode.getChild("crop3d");
        }

        GroupNode cropNode = new GroupNode("crop3d");
        rootNode.addNode(cropNode);
        return cropNode;
    }
    
    
    public static final ImageSerialSectionsNode getPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("polygons"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("polygons");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("polygons");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
    public static final ImageSerialSectionsNode getSmoothPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("smooth"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("smooth");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("smooth");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
}
