/**
 * 
 */
package imago.table;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import imago.gui.FrameMenuBuilder;
import imago.gui.ImagoFrame;
import imago.gui.frames.ImagoEmptyFrame;
import imago.table.plugin.OpenDemoTable;

/**
 * Utility class for building menu bar of TableFrame instances.
 */
public class TableFrameMenuBuilder extends FrameMenuBuilder
{

    public TableFrameMenuBuilder(ImagoFrame frame)
    {
        super(frame);
    }
    
    // ===================================================================
    // menu creation methods
    
    public void setupMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createPlotMenu());
        menuBar.add(createProcessMenu());
        addSharedMenus(menuBar);
        
        frame.getWidget().setJMenuBar(menuBar);
    }
    
    /**
     * Creates the sub-menu for the "File" item in the main menu bar of Table frames.
     */
    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        
        addPlugin(fileMenu, imago.table.plugin.OpenTable.class, "Open Table...");
        JMenu demoTables = new JMenu("Demo Tables");
        addPlugin(demoTables, new OpenDemoTable("tables/fisherIris.csv"), "Fisher's Iris");
        addPlugin(demoTables, new OpenDemoTable("tables/penguins_clean.csv"), "Penguins");
        fileMenu.add(demoTables);

        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.table.plugin.SaveTable.class, "Save Table...");

        fileMenu.addSeparator();
        addPlugin(fileMenu, imago.gui.plugin.file.CloseCurrentFrame.class, "Close", !(frame instanceof ImagoEmptyFrame));
        addPlugin(fileMenu, imago.gui.plugin.file.QuitApplication.class, "Quit");

        return fileMenu;
    }
    /**
     * Creates the sub-menu for the "Edit" item in the main menu bar of Table
     * frames.
     */
    private JMenu createEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");
        
        addPlugin(editMenu, imago.table.plugin.edit.RenameTable.class, "Rename...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.table.plugin.edit.TableSelectColumns.class, "Select Columns...");
        addPlugin(editMenu, imago.table.plugin.edit.TableKeepNumericColumns.class, "Keep Numeric Columns");
        addPlugin(editMenu, imago.table.plugin.edit.ConcatenateTableColumns.class, "Concatenate Columns...");
        addPlugin(editMenu, imago.table.plugin.edit.TableParseGroupFromRowNames.class, "Parse Group From Row Names...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.table.plugin.edit.TableFilterRows.class, "Filter/Select Rows...");
        addPlugin(editMenu, imago.table.plugin.edit.TableSortRows.class, "Sort Rows...");
        addPlugin(editMenu, imago.table.plugin.edit.ConcatenateTableRows.class, "Concatenate Rows...");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.table.plugin.edit.TransposeTable.class, "Transpose");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.table.plugin.edit.FoldTableToImage.class, "Fold Table to Image");
        editMenu.addSeparator();
        addPlugin(editMenu, imago.table.plugin.edit.PrintTableInfo.class, "Print Table Info");
        addPlugin(editMenu, imago.table.plugin.edit.NumericTableSummary.class, "Table Summary");
        addPlugin(editMenu, imago.table.plugin.edit.PrintTableToConsole.class, "Print to Console");
        
        return editMenu;
    }

    /**
     * Creates the sub-menu for the "plot" item in the main menu bar of Table
     * frames.
     */
    private JMenu createPlotMenu()
    {
        JMenu plotMenu = new JMenu("Plot");
        addPlugin(plotMenu, imago.table.plugin.plot.TableScatterPlot.class, "Scatter Plot...");
        addPlugin(plotMenu, imago.table.plugin.plot.TableGroupScatterPlot.class, "Scatter Plot By Group...");
        addPlugin(plotMenu, imago.table.plugin.plot.TablePairPlot.class, "Pair Plot");
        plotMenu.addSeparator();
        addPlugin(plotMenu, imago.table.plugin.plot.TableLinePlot.class, "Line Plot...");
        plotMenu.addSeparator();
        addPlugin(plotMenu, imago.table.plugin.plot.PlotTableColumnHistogram.class, "Histogram...");
        return plotMenu;
    }
    
    /**
     * Creates the sub-menu for the "Process" item in the main menu bar of Table
     * frames.
     */
    private JMenu createProcessMenu()
    {
        JMenu processMenu = new JMenu("Process");
        
        addPlugin(processMenu, imago.table.plugin.process.ApplyFunctionToColumn.class, "Apply Function to Columns...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.table.plugin.process.AggregateTableWithColumn.class, "Aggregate by group...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.table.plugin.process.TablePca.class, "Principal Components Analysis");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.table.plugin.process.TableKMeans.class, "K-Means...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.table.plugin.process.TableConfusionMatrix.class, "Confusion Matrix...");
        processMenu.addSeparator();
        addPlugin(processMenu, imago.table.plugin.process.CreateLabelClassMap.class, "Create Label Class Map...");
        
        return processMenu;
    }



}
