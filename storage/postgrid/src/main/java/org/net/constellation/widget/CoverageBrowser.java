/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.widget;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gui.swing.ExceptionMonitor;
import org.geotools.gui.swing.image.OperationTreeBrowser;
import org.geotools.resources.Arguments;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;


/**
 * A browser over images in a layer. This is mostly a testing tool.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CoverageBrowser {
    /**
     * The list of layers that user can select.
     */
    private final ComboBoxModel layers;

    /**
     * The list of coverages for the selected layer.
     */
    private final CoverageTableModel coverages;

    /**
     * The desktop pane in which to display the table and the images.
     */
    private final JDesktopPane desktop;

    /**
     * The listeners of interest for the {@link CoverageBrowser} class.
     */
    private final class Listeners implements ItemListener, ListSelectionListener {
        public void itemStateChanged(final ItemEvent event) {
            layerSelected((Layer) event.getItem());
        }

        public void valueChanged(final ListSelectionEvent event) {
            coverageSelected(event.getFirstIndex());
        }
    }

    /**
     * Creates a browser for the specified database.
     */
    public CoverageBrowser(final Database database) throws CatalogException, SQLException {
        this(database.getTable(LayerTable.class));
    }

    /**
     * Creates a browser for the specified layer table.
     */
    public CoverageBrowser(final LayerTable layers) throws CatalogException, SQLException {
        this(layers.getEntries());
    }

    /**
     * Creates a browser for the specified collection of layers.
     */
    public CoverageBrowser(final Collection<Layer> layers) {
        final JComboBox layerBox = new JComboBox(layers.toArray(new Layer[layers.size()]));
        this.layers = layerBox.getModel();

        coverages = new CoverageTableModel();
        final JTable table = new JTable(coverages);
        final TableCellRenderer renderer = new CoverageTableModel.CellRenderer();
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Date.class, renderer);

        final JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(BorderLayout.NORTH, layerBox);
        tablePanel.add(BorderLayout.CENTER, new JScrollPane(table));

        final Listeners listeners = new Listeners();
        layerBox.addItemListener(listeners);
        table.getSelectionModel().addListSelectionListener(listeners);

        final JInternalFrame frame = new JInternalFrame("Images", true); // TODO: localize
        frame.add(tablePanel);
        frame.setSize(300, 500);
        frame.setVisible(true);
        desktop = new JDesktopPane();
        desktop.add(frame);
    }

    /**
     * Invoked when a layer has been selected.
     */
    final void layerSelected(final Layer layer) {
        try {
            coverages.setLayer(layer);
        } catch (CatalogException exception) {
            ExceptionMonitor.show(desktop, exception);
        }
    }

    /**
     * Invoked when a coverage has been selected.
     */
    final void coverageSelected(final int coverageIndex) {
        if (coverageIndex < 0) {
            return;
        }
        final CoverageReference reference = coverages.getCoverageReferenceAt(coverageIndex);
        final GridCoverage2D coverage;
        try {
            coverage = reference.getCoverage(null);
        } catch (IOException exception) {
            ExceptionMonitor.show(desktop, exception);
            return;
        }
        final String name = coverage.getName().toString(desktop.getLocale());
        for (final JInternalFrame candidate : desktop.getAllFrames()) {
            if (name.equals(candidate.getName())) try {
                candidate.setSelected(true);
                return;
            } catch (PropertyVetoException e) {
                ExceptionMonitor.show(desktop, e);
            }
        }
        final OperationTreeBrowser browser = new OperationTreeBrowser(coverage.geophysics(false).getRenderedImage());
        final JInternalFrame frame = new JInternalFrame(name, true, true, true, true);
        frame.add(browser);
        frame.setSize(400, 400);
        frame.setVisible(true);
        desktop.add(frame);
    }

    /**
     * Shows this browser in a frame.
     */
    public void show() {
        final JFrame frame = new JFrame("Navigateur"); // TODO: localize
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(desktop);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    /**
     * Shows this component using the default database.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore. We will use the default L&F.
        }
        final CoverageBrowser browser;
        try {
            final Database database = new Database();
            browser = new CoverageBrowser(database);
            // Do not close the database; we will rely on shutdown hook.
        } catch (Exception e) {
            e.printStackTrace(arguments.err);
            return;
        }
        browser.show();
    }
}
