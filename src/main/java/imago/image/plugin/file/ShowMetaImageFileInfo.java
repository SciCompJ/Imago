/**
 * 
 */
package imago.image.plugin.file;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import imago.gui.FramePlugin;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoTextFrame;
import net.sci.image.io.MetaImageInfo;
import net.sci.image.io.MetaImageReader;

/**
 * Opens a dialog to choose a meta-image file, and displays the content of the
 * resulting FileInfo class.
 * 
 * @author David Legland
 */
public class ShowMetaImageFileInfo implements FramePlugin
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void run(ImagoFrame frame, String args)
	{
        // choose the file to open
	    File file = frame.getGui().chooseFileToOpen(frame, "Open MetaImage File", ImageFileFilters.META_IMAGE);
	    
		// Check the chosen file is valid
		if (file == null || !file.isFile())
		{
			return;
		}

		// Create a MetaImage Format reader with the chosen file
		MetaImageInfo info;
		try
		{
		    info = MetaImageReader.readFileInfo(file);
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("Unable to find input file: " + file.getName(), ex);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Unable to open file: " + file.getName(), ex);
		}

		// initialize array of text lines to display
        ArrayList<String> textLines = new ArrayList<String>();
        textLines.add("MetaImage File Info");
        
        // image info tags
        textLines.add("NDims: " + info.nDims);
        textLines.add("DimSize: " + printIntArray(info.dimSize));
        textLines.add("ElementType: " + info.elementType);
        textLines.add("ElementNumberOfChannels: " + info.elementNumberOfChannels);
        // file storage tags
        textLines.add("ElementDataFile: " + info.elementDataFile);
        textLines.add("HeaderSize: " + info.headerSize);
        textLines.add("BinaryData: " + info.binaryData);
        textLines.add("BinaryDataByteOrderMSB: " + info.binaryDataByteOrderMSB);
        textLines.add("CompressedData: " + info.compressedData);

        // also add calibration tags (if present)
        if (info.elementSpacing != null)
        {
            textLines.add("ElementSpacing: " + printFloatArray(info.elementSpacing));
        }
        if (info.elementSize != null)
        {
            textLines.add("ElementSize: " + printFloatArray(info.elementSize));
        }
        if (info.offset != null)
        {
            textLines.add("Offset: " + printFloatArray(info.offset));
        }
        
		// add the image document to GUI
        ImagoTextFrame newFrame = new ImagoTextFrame(frame, "MetaImage Info", textLines);
        newFrame.getWidget().pack();
        newFrame.getWidget().setSize(new Dimension(600, 400));
        newFrame.setVisible(true);
	}
	
	private static final String printIntArray(int[] array)
	{
	    StringBuilder sb = new StringBuilder();
	    sb.append("[");
	    if (array.length > 0) 
	    {
	        sb.append(array[0]);
	    }
        for (int i = 1; i < array.length; i++)
        {
            sb.append(", " + array[i]);
        }
        sb.append("]");
        return sb.toString();
	}
	
    private static final String printFloatArray(double[] array)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (array.length > 0) 
        {
            sb.append(array[0]);
        }
        for (int i = 1; i < array.length; i++)
        {
            sb.append(", ").append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
