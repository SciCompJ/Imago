/**
 * 
 */
package imago.util;

/**
 * Utility methods for Strings.
 */
public class StringUtils
{
    /**
     * Creates a new String based on the specified String, by incrementing the
     * trailing digits if they exist, or by adding the arbitrary "01" digits at
     * the end.
     * 
     * Examples:
     * <ul>
     * <li>myRef -> myRef01</li>
     * <li>tag03 -> tag04</li>
     * <li>name_10 -> name_11</li>
     * </ul>
     * 
     * @param name
     *            the String used as basis for creating the new name
     * @returnt the new name
     */
    public static final String addNumericIncrement(String name)
    {
        // remove trailing digits
        int endIndex = name.length();
        while (endIndex > 0 && Character.isDigit(name.charAt(endIndex-1))) endIndex--;
        
        // determine current number at the end of the string
        int count = 0;
        if (endIndex != name.length())
        {
            String subString = name.substring(endIndex);
            count = Integer.parseInt(subString);
        }

        return  String.format(name.substring(0, endIndex) + "%02d", count+1);
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private StringUtils()
    {
    }
}
