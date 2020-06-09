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
	/**
	 * Reset the nodes associated to a Crop3D plugin. 
	 * 
	 * @param handle the ImageHandle containing the nodes to reset.
	 */
	public static final void initializeCrop3dNodes(ImageHandle handle)
	{
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());
        
        // remove old crop node if it exists
        if (rootNode.hasChildWithName("crop3d"))
        {
            rootNode.removeNode(rootNode.getChild("crop3d"));
        }
        
        // create new crop node
        GroupNode cropNode = new GroupNode("crop3d");
        rootNode.addNode(cropNode);

        // add child nodes
        cropNode.addNode(new ImageSerialSectionsNode("polygons"));
        cropNode.addNode(new ImageSerialSectionsNode("smooth"));
        cropNode.addNode(new ImageSerialSectionsNode("interp"));
	}
	
	/**
	 * @param handle
	 *            the ImageHandle containing the nodes.
	 * @return true if the image handle contains the necessary scene nodes for
	 *         performing Crop3D.
	 */
	public static final boolean hasCrop3dNodes(ImageHandle handle)
	{
        // get root node
        GroupNode rootNode = ((GroupNode) handle.getRootNode());
        
        // remove old crop node if it exists
        if (!rootNode.hasChildWithName("crop3d"))
        {
            return false;
        }
        
        // create new crop node
        GroupNode cropNode = (GroupNode) rootNode.getChild("crop3d");

        // add child nodes
        if (!(cropNode.hasChildWithName("polygons"))) return false;
        if (!(cropNode.hasChildWithName("smooth"))) return false;
        if (!(cropNode.hasChildWithName("interp"))) return false;
        
        // if all conditions are checked, return true
        return true;
	}
	
	
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
    
    public static final ImageSerialSectionsNode getInterpolatedPolygonsNode(ImageHandle handle)
    {
        GroupNode cropNode = getCrop3dNode(handle);
        
        if (cropNode.hasChildWithName("interp"))
        {
            return (ImageSerialSectionsNode) cropNode.getChild("interp");
        }
        
        ImageSerialSectionsNode polyNode = new ImageSerialSectionsNode("interp");
        cropNode.addNode(polyNode);
        return polyNode;
    }
    
}
