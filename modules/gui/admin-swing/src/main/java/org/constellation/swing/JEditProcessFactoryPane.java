/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Process;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JEditProcessFactoryPane extends javax.swing.JPanel {

    private static final Logger LOGGER = Logging.getLogger(JEditProcessFactoryPane.class);

    private static final String EMPTY_ITEM = "empty";

    private ProcessFactoryModel processFactoryModel;

    private List<ProcessModel> processModelList;

    private final ConstellationServer server;

    /**
     * Creates new form JEditProcessFactoryPane
     * @param server
     * @param processFactoryModel
     */
    public JEditProcessFactoryPane(final ConstellationServer server, final ProcessFactoryModel processFactoryModel) {
        this.processFactoryModel = processFactoryModel;
        this.server = server;
        initComponents();

        processFactoryComboBox.setModel(new ListComboBoxModel(new ArrayList<>(server.tasks.listProcessFactories().getList())));
        guiProcessTable.setDefaultRenderer(ProcessRowModel.SelectProcess.class, new SelectionRenderer());
        guiProcessTable.setDefaultEditor(ProcessRowModel.SelectProcess.class, new SelectionEditor());

        guiProcessTable.setShowVerticalLines(false);
        guiProcessTable.setFillsViewportHeight(true);

        if (processFactoryModel != null) {
            processFactoryComboBox.setSelectedItem(processFactoryModel.getFactory().getAutorityCode());
        }
        initProcessList();

        updateProcessTableModel();
    }

    public ProcessFactoryModel getProcessFactoryEntry() {
        if (processFactoryModel == null) {
            processFactoryModel = new ProcessFactoryModel(new ProcessFactory());
        }
        processFactoryModel.getFactory().setAutorityCode((String)processFactoryComboBox.getSelectedItem());
        processFactoryModel.getFactory().setLoadAll(Boolean.FALSE);
        processFactoryModel.getFactory().getInclude().getProcess().clear();
        for (ProcessModel pm : processModelList) {
            if (pm.isSelected()) {
                processFactoryModel.getFactory().getInclude().getProcess().add(pm.getProcess());
            }
        }
        return processFactoryModel;
    }

    /**
     * Create a list of process factory model based on service configuration.
     */
    private void initProcessList() {
        processModelList = new ArrayList<>();
        if (processFactoryModel != null) {
            final ProcessFactory factory = processFactoryModel.getFactory();
            final Collection<String> processes = server.tasks.listProcessForFactory(processFactoryModel.getFactory().getAutorityCode()).getList();
            if (factory.getLoadAll()) {
                for (final String process : processes) {
                    processModelList.add(new ProcessModel(new Process(process), !factory.getExclude().contains(process)));
                }
            } else {
                for (final String process : processes) {
                    processModelList.add(new ProcessModel(new Process(process), factory.getInclude().contains(process)));
                }
            }
        } else {
            final String authorityCode = (String)processFactoryComboBox.getSelectedItem();
            final Collection<String> processes = server.tasks.listProcessForFactory(authorityCode).getList();
            for (final String process : processes) {
                processModelList.add(new ProcessModel(new Process(process), true));
            }
        }
    }

    /**
     * Update GUI Outline process model using the list of ProcessFactoryModel.
     */
    private void updateProcessTableModel() {

        new Thread(){
            @Override
            public void run() {
                guiLoadLabel.setText(ProcessFactoryRowModel.BUNDLE.getString("downloadingProcess"));
                guiLoadLabel.setIcon(IconBundle.getIcon("16_wait"));

                try {
                    final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                    final DefaultTreeModel treeModel = new org.geotoolkit.gui.swing.tree.DefaultTreeModel(root);
                    final RowModel model = new ProcessRowModel();

                    for (final ProcessModel processModel : processModelList) {
                        root.add(new DefaultMutableTreeNode(processModel));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            guiProcessTable.setRootVisible(false);
                            guiProcessTable.setModel(DefaultOutlineModel.createOutlineModel(treeModel, model));
                        }
                    });

                } finally{
                    guiProcessTable.repaint();
                    guiLoadLabel.setText("");
                    guiLoadLabel.setIcon(null);
                }
                guiProcessTable.setRenderDataProvider(new ProcessRowRenderer());
            }
        }.start();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        processFactoryComboBox = new JComboBox();
        jScrollPane1 = new JScrollPane();
        guiProcessTable = new Outline();
        guiLoadBar = new JToolBar();
        guiLoadLabel = new JLabel();

        jLabel1.setText(NbBundle.getMessage(JEditProcessFactoryPane.class, "processFactory")); // NOI18N

        processFactoryComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                processFactoryComboBoxItemStateChanged(evt);
            }
        });

        jScrollPane1.setViewportView(guiProcessTable);

        guiLoadBar.setFloatable(false);
        guiLoadBar.setRollover(true);
        guiLoadBar.add(guiLoadLabel);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processFactoryComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiLoadBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 375, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(processFactoryComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(guiLoadBar, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void processFactoryComboBoxItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_processFactoryComboBoxItemStateChanged
        initProcessList();
        updateProcessTableModel();
    }//GEN-LAST:event_processFactoryComboBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JToolBar guiLoadBar;
    private JLabel guiLoadLabel;
    private Outline guiProcessTable;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JComboBox processFactoryComboBox;
    // End of variables declaration//GEN-END:variables

    /**
     * 
     * @param server
     * @param processFactory
     * @return 
     */
    public static ProcessFactoryModel showDialog(final ConstellationServer server, final ProcessFactoryModel processFactory){
        
        final JEditProcessFactoryPane pane = new JEditProcessFactoryPane(server, processFactory);
                
        int res = JOptionPane.showOptionDialog(null, new Object[]{pane}, 
                ProcessFactoryRowModel.BUNDLE.getString("createProcessFactoryMsg"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 
                null);
        
        ProcessFactoryModel processFactoryModel = null;
        if (res != JOptionPane.CANCEL_OPTION && res != JOptionPane.CLOSED_OPTION) {
            processFactoryModel = pane.getProcessFactoryEntry();
        }
        return processFactoryModel;
    }

    private static class SelectionRenderer extends DefaultTableCellRenderer{

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if(value instanceof DefaultMutableTreeNode){
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            if (value instanceof ProcessModel) {
                final ProcessModel n = (ProcessModel) value;
                final JCheckBox box = new JCheckBox();
                box.setSelected(n.isSelected());
                box.setHorizontalTextPosition(SwingConstants.CENTER);
                box.setHorizontalAlignment(SwingConstants.CENTER);
                return box;
            }
            // root
            return null;
        }
    }

    private static class SelectionEditor extends AbstractCellEditor implements TableCellEditor {

        private JCheckBox box;

        @Override
        public Object getCellEditorValue() {
            if (box != null) {
                return box.isSelected();
            }
            return false;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if(value instanceof DefaultMutableTreeNode){
                value = ((DefaultMutableTreeNode)value).getUserObject();
            }
            if (value instanceof ProcessModel) {
                final ProcessModel n = (ProcessModel) value;
                box = new JCheckBox();
                box.setSelected(n.isSelected());
                box.setHorizontalTextPosition(SwingConstants.CENTER);
                box.setHorizontalAlignment(SwingConstants.CENTER);
                box.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        n.setSelected(box.isSelected());
                    }
                });
                return box;
            }
            // root
            return null;
        }
    }

    /**
     * Rendering of process factory row in Outline table.
     */
    private static class ProcessRowRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            if(o instanceof DefaultMutableTreeNode){
                o = ((DefaultMutableTreeNode)o).getUserObject();
            }
            if (o instanceof ProcessModel) {
                final ProcessModel process = (ProcessModel) o;
                if (process.getProcess()!= null) {
                    return process.getProcess().getId();
                }
            }
            return String.valueOf(o);
        }

         @Override
        public boolean isHtmlDisplayName(Object o) {
            return true;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return null;
        }

        @Override
        public Icon getIcon(Object o) {
            return IconBundle.EMPTY_ICON_16;
        }
    }
}
