/**
 * 
 */
package imago.transform.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.stream.Stream;

import net.sci.geom.Transform;
import net.sci.geom.geom2d.MatrixAffineTransform2D;
import net.sci.geom.geom3d.impl.MatrixAffineTransform3D;

/**
 * Reads a 2D affine transform from a delimited text file containing the
 * coefficients of the transform on the first data line.
 */
public class DelimitedFileAffineTransformReader implements AutoCloseable 
{
    Reader reader;
    
    public DelimitedFileAffineTransformReader(File file) throws FileNotFoundException
    {
        this.reader = new FileReader(file);
    }
    
    public DelimitedFileAffineTransformReader(Reader reader)
    {
        this.reader = reader;
    }
    
    public Transform readTransform() throws IOException
    {
        LineNumberReader lineReader = new LineNumberReader(new BufferedReader(reader));
        
        // read (and discard) header line
        lineReader.readLine();
        
        // read coefficients line
        String line = lineReader.readLine();
        
        // split tokens
        String delimiters = " \t";
        String delimiterRegexp = "[" + delimiters + "]+";
        String[] tokens = line.split(delimiterRegexp);
        
        tokens = switch (tokens.length)
        {
            case 6 -> tokens;
            case 7 -> Arrays.copyOfRange(tokens, 1, 7);
            case 12 -> tokens;
            case 13 -> Arrays.copyOfRange(tokens, 1, 13);
            default -> throw new RuntimeException("Wrong number of coefficients");
        };
        
        double[] coeffs = Stream.of(tokens)
                .mapToDouble(s -> Double.parseDouble(s))
                .toArray();
        
        return switch (coeffs.length)
        {
            case 6 -> new MatrixAffineTransform2D(coeffs[0], coeffs[1], coeffs[2], coeffs[3], coeffs[4], coeffs[5]);
            case 12 -> new MatrixAffineTransform3D(
                    coeffs[0], coeffs[1], coeffs[2], coeffs[3], coeffs[4], coeffs[5],
                    coeffs[6], coeffs[7], coeffs[8], coeffs[9], coeffs[10], coeffs[11]);
            default -> throw new RuntimeException("Wrong number of coefficients");
        };
    }

    @Override
    public void close() throws Exception
    {
        this.reader.close();
    }
}
