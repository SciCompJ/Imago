/**
 * 
 */
package imago.gui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * A frame that displays a block of text.
 * 
 * @author dlegland
 *
 */
public class ImagoTextFrame extends ImagoFrame
{
    JTextArea textArea;
    
    ArrayList<String> textLines;
    
    public ImagoTextFrame(ImagoFrame parent, String title, String[] textLines)
    {
        super(parent, title);
        
        this.textLines = new ArrayList<String>(textLines.length);
        for (String line : textLines)
            this.textLines.add(line);
        
        setupLayout();
    }
    
    public ImagoTextFrame(ImagoFrame parent, String title, Collection<String> textLines)
    {
        super(parent, title);
        
        this.textLines = new ArrayList<String>(textLines.size());
        this.textLines.addAll(textLines);
        
        setupLayout();
    }
    
    private void setupLayout()
    {
        // creates text area
        textArea = new JTextArea(15, 80);
//        textArea.setForeground(Color.RED);
        textArea.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        // populates text area with text content
        for (String line : textLines)
        {
            textArea.append(line + "\n");
        }
        
        // add Text area in the middle panel
        this.jFrame.add(scroll);
    }
}
