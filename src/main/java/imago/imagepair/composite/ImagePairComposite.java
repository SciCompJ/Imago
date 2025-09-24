/**
 * 
 */
package imago.imagepair.composite;

import net.sci.array.Array;
import net.sci.image.Image;

/**
 * Computes a synthetic image by combining two images with the same size. Input
 * images are typically a reference image and the result of the registration of
 * a moving image onto the reference image.
 */
public interface ImagePairComposite
{
    public default Image process(Image refImage, Image otherImage)
    {
        Array<?> array = process(refImage.getData(), otherImage.getData());
        return new Image(array, refImage);
    }
    
    public <T> Array<?> process(Array<T> refArray, Array<?> otherArray);
    
    /**
     * Utility method that determines if a position is contained within the
     * bounds of the array.
     * 
     * @param array
     *            the array to test
     * @param pos
     *            an array of coordinates, with same length as array
     *            dimensionality
     * @return true if the position array corresponds to an element within the
     *         array
     */
    public static boolean containsPosition(Array<?> array, int[] pos)
    {
        int[] dims = array.size();
        for (int d = 0; d < dims.length; d++)
        {
            if (pos[d] < 0 || pos[d] >= dims[d]) return false;
        }
        return true;
    }
}
