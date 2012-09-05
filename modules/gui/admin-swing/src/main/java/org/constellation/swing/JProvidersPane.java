/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.security.DefaultRoleController;
import org.constellation.security.RoleController;
import static org.constellation.security.ActionPermissions.*;
import org.geotoolkit.gui.swing.misc.ActionCell;
import org.jdesktop.swingx.JXTable;

/**
 * Top component to manage constellation providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class JProvidersPane extends JPanel implements ActionListener {

    private static final Comparator<ProviderServiceReport> SERVICE_COMPARATOR = new Comparator<ProviderServiceReport>() {

        @Override
        public int compare(ProviderServiceReport o1, ProviderServiceReport o2) {
            return o1.getType().compareTo(o2.getType());
        }
    };

    private static final ImageIcon ICON_SERVICE_EDIT =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceEdit.png"));
    private static final ImageIcon ICON_SERVICE_RELOAD =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceReload.png"));

    private final JXTable guiTable = new JXTable();
    private final ConstellationServer cstl;
    private final FrameDisplayer displayer;
    private final RoleController roleController;

    public JProvidersPane(final ConstellationServer cstl, final FrameDisplayer displayer) {
        this(cstl, displayer, new DefaultRoleController());
    }

    public JProvidersPane(final ConstellationServer cstl, final FrameDisplayer displayer, final RoleController roleController) {
        initComponents();

        this.cstl = cstl;
        if(displayer == null){
            this.displayer = new DefaultFrameDisplayer();
        } else {
            this.displayer = displayer;
        }
        this.roleController = roleController;
        //list all providers
        guiAll.addActionListener(this);
        final ProvidersReport providersReport = cstl.providers.listProviders();
        final List<ProviderServiceReport> servicesReport = providersReport.getProviderServices();
        Collections.sort(servicesReport,SERVICE_COMPARATOR);
        for (final ProviderServiceReport serviceReport : servicesReport) {
            //add a button for each type
            final JToggleButton btn = new JToggleButton(serviceReport.getType());
            btn.setActionCommand(serviceReport.getType());
            btn.addActionListener(this);
            guiTypeGroup.add(btn);
            guiToolBar.add(btn);
        }
        guiNew.setVisible(roleController.hasPermission(NEW_PROVIDER));

        add(BorderLayout.CENTER,new JScrollPane(guiTable));
        updateInstanceList();
    }

    public FrameDisplayer getDisplayer() {
        return displayer;
    }

    private void updateInstanceList(){

        String action = "all";
        if(guiTypeGroup.getSelection() != null){
            action = guiTypeGroup.getSelection().getActionCommand();
        }

        //list all providers
        final ProvidersReport providersReport = cstl.providers.listProviders();
        final List<ProviderServiceReport> servicesReport = providersReport.getProviderServices();
        final List<Entry<String,ProviderReport>> instances = new ArrayList<Entry<String,ProviderReport>> ();

        for (final ProviderServiceReport serviceReport : servicesReport) {
            final String type = serviceReport.getType();
            if("all".equals(action) || action.equalsIgnoreCase(type)){
                final List<ProviderReport> providers = serviceReport.getProviders();

                for(ProviderReport report : providers){
                    instances.add(new AbstractMap.SimpleEntry<String,ProviderReport>(type,report));
                }
            }
        }

        Collections.sort(instances,new Comparator<Entry<String,ProviderReport>>(){
            @Override
            public int compare(Entry<String,ProviderReport> o1, Entry<String,ProviderReport> o2) {
                if(o1.getKey().equals(o2.getKey())){
                    //compare instance names
                    return o1.getValue().getId().compareTo(o2.getValue().getId());
                }else{
                    //compare types
                    return o1.getKey().compareTo(o2.getKey());
                }
            }
        });

        final TableModel model = new InstanceModel(instances);
        guiTable.setModel(model);

        final Font fontBig = new Font("Monospaced", Font.BOLD, 16);
        final Font fontNormal = new Font("Monospaced", Font.PLAIN, 12);
        final ImageIcon viewIcon = new ImageIcon(JServicesPane.createImage(LayerRowModel.BUNDLE.getString("view"),
                null, Color.BLACK, fontNormal, Color.LIGHT_GRAY));
        final ImageIcon editIcon = new ImageIcon(JServicesPane.createImage(LayerRowModel.BUNDLE.getString("edit"),
                ICON_SERVICE_EDIT, Color.BLACK, fontNormal, Color.LIGHT_GRAY));
        final ImageIcon reloadIcon = new ImageIcon(JServicesPane.createImage(LayerRowModel.BUNDLE.getString("reload"),
                ICON_SERVICE_RELOAD, Color.WHITE, fontNormal, new Color(65,150,190)));

        guiTable.getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){

            @Override
            public Component getTableCellRendererComponent(JTable table, Object o,
            boolean isSelected, boolean hasFocus, int row, int column) {
                final JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, o, isSelected, hasFocus, row, column);

                lbl.setIcon(null);

                if(o instanceof DefaultMutableTreeNode){
                    o = ((DefaultMutableTreeNode)o).getUserObject();
                }
                if(o instanceof Entry){
                    final Entry entry = (Entry) o;
                    final String type = (String) entry.getKey();
                    final ProviderReport inst = (ProviderReport) entry.getValue();
                    final Color bgColor = new Color(130, 160, 50);

                    final BufferedImage img = JServicesPane.createImage(type, null, Color.WHITE,fontBig, bgColor);
                    lbl.setIcon(new ImageIcon(img));
                    lbl.setText(inst.getId());
                }

                return lbl;
            }

        });

        guiTable.getColumn(1).setCellRenderer(new ActionCell.Renderer(viewIcon){

            @Override
            public Icon getIcon(Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Entry entry = (Entry) value;
                    final String type = (String) entry.getKey();
                    if("sld".equalsIgnoreCase(type)){
                        return null;
                    }
                }
                return super.getIcon(value);
            }

        });
        guiTable.getColumn(1).setCellEditor(new ActionCell.Editor(viewIcon) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if (value instanceof DefaultMutableTreeNode) {
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                if (value instanceof Entry) {
                    final Entry entry = (Entry) value;
                    final String type = (String) entry.getKey();
                    final ProviderReport inst = (ProviderReport) entry.getValue();

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getDisplayer().display(cstl, type, inst);
                        }
                    });

                }
            }
        });

        int columIndex = 2;
        if (roleController.hasPermission(EDIT_PROVIDER)) {
            guiTable.getColumn(columIndex).setCellRenderer(new ActionCell.Renderer(editIcon));
            guiTable.getColumn(columIndex).setCellEditor(new ActionCell.Editor(editIcon) {
                @Override
                public void actionPerformed(final ActionEvent e, Object value) {
                    if (value instanceof DefaultMutableTreeNode) {
                        value = ((DefaultMutableTreeNode)value).getUserObject();
                    }
                    if (value instanceof Entry) {
                        final Entry entry = (Entry) value;
                        final String type = (String) entry.getKey();
                        final ProviderReport inst = (ProviderReport) entry.getValue();


                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {

                                final JProviderEditPane edit = new JProviderEditPane(cstl, type, inst);
                                final PropertyChangeListener cl = new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if ("update".equals(evt.getPropertyName())) {
                                            updateInstanceList();
                                            edit.removePropertyChangeListener(this);
                                        }
                                    }
                                };

                                edit.addPropertyChangeListener(cl);

                                getDisplayer().display(edit);
                            }
                        });

                    }
                }
            });
            columIndex++;
        }

        if (roleController.hasPermission(RELOAD_PROVIDER)) {
            guiTable.getColumn(columIndex).setCellRenderer(new ActionCell.Renderer(reloadIcon));
            guiTable.getColumn(columIndex).setCellEditor(new ActionCell.Editor(reloadIcon) {
                @Override
                public void actionPerformed(final ActionEvent e, Object value) {
                    if(value instanceof DefaultMutableTreeNode){
                        value =((DefaultMutableTreeNode)value).getUserObject();
                    }
                    if(value instanceof Entry){
                        final Entry entry = (Entry) value;
                        final String type = (String) entry.getKey();
                        final ProviderReport inst = (ProviderReport) entry.getValue();

                         cstl.providers.restartProvider(inst.getId());
                         SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                updateInstanceList();
                            }
                        });
                    }
                }
            });
        }

        final int width = 140;
        for (int i = 1; i < guiTable.getColumnCount(); i++) {
            guiTable.getColumn(i).setMinWidth(width);
            guiTable.getColumn(i).setPreferredWidth(width);
            guiTable.getColumn(i).setMaxWidth(width);
        }
        guiTable.setTableHeader(null);
        guiTable.setRowHeight(37);
        guiTable.setFillsViewportHeight(true);
        guiTable.setBackground(Color.WHITE);
        guiTable.setShowGrid(true);
        guiTable.setShowHorizontalLines(true);
        guiTable.setShowVerticalLines(false);
        guiTable.revalidate();
        guiTable.repaint();

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiTypeGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jToolBar0 = new javax.swing.JToolBar();
        guiRefresh = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        guiToolBar = new javax.swing.JToolBar();
        guiAll = new javax.swing.JToggleButton();
        jToolBar1 = new javax.swing.JToolBar();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        guiNew = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar0.setFloatable(false);

        guiRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/constellation/swing/reload.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        guiRefresh.setText(bundle.getString("refresh")); // NOI18N
        guiRefresh.setFocusable(false);
        guiRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiRefreshActionPerformed(evt);
            }
        });
        jToolBar0.add(guiRefresh);
        jToolBar0.add(jSeparator1);

        jPanel1.add(jToolBar0, java.awt.BorderLayout.WEST);

        guiToolBar.setFloatable(false);

        guiTypeGroup.add(guiAll);
        guiAll.setSelected(true);
        guiAll.setText(bundle.getString("all")); // NOI18N
        guiAll.setActionCommand("all");
        guiAll.setFocusable(false);
        guiAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        guiAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiToolBar.add(guiAll);

        jPanel1.add(guiToolBar, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.add(jSeparator2);

        guiNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/constellation/swing/edit_add.png"))); // NOI18N
        guiNew.setText(bundle.getString("creation")); // NOI18N
        guiNew.setFocusable(false);
        guiNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiNewActionPerformed(evt);
            }
        });
        jToolBar1.add(guiNew);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.EAST);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void guiRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiRefreshActionPerformed
        updateInstanceList();
    }//GEN-LAST:event_guiRefreshActionPerformed

    private void guiNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiNewActionPerformed

        JProviderCreationPane.showDialog(cstl);
        updateInstanceList();

    }//GEN-LAST:event_guiNewActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton guiAll;
    private javax.swing.JButton guiNew;
    private javax.swing.JButton guiRefresh;
    private javax.swing.JToolBar guiToolBar;
    private javax.swing.ButtonGroup guiTypeGroup;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar0;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        updateInstanceList();
    }

    private class InstanceModel extends AbstractTableModel{

        private final List<Entry<String,ProviderReport>> entries;

        public InstanceModel(final List<Entry<String,ProviderReport>> entries) {
            this.entries = entries;
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            int nbColumn = 2; // instance name + viewButton
            if (roleController.hasPermission(EDIT_PROVIDER)) {
                nbColumn++;
            }
            if (roleController.hasPermission(RELOAD_PROVIDER)) {
                nbColumn++;
            }
            return nbColumn;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return entries.get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if(columnIndex == 1){
                Object value = getValueAt(rowIndex, columnIndex);
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Entry entry = (Entry) value;
                    final String type = (String) entry.getKey();
                    if("sld".equalsIgnoreCase(type)){
                        return false;
                    }
                }
            }

            return columnIndex>0;
        }

    }

}
