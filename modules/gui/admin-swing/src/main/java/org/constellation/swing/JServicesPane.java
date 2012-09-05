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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.constellation.admin.service.ConstellationServer.Services;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.security.DefaultRoleController;
import org.constellation.security.RoleController;
import static org.constellation.security.ActionPermissions.*;
import org.geotoolkit.gui.swing.misc.ActionCell;
import org.jdesktop.swingx.JXTable;

/**
 * Top component to manage constellation services.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class JServicesPane extends JPanel implements ActionListener {

    private static final ImageIcon ICON_SERVICE_EDIT =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceEdit.png"));
    private static final ImageIcon ICON_SERVICE_START = new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceStart.png"));
    private static final ImageIcon ICON_SERVICE_RELOAD =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceReload.png"));
    private static final ImageIcon ICON_SERVICE_STOP =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceStop.png"));

    private final JXTable guiTable = new JXTable();
    private final ConstellationServer cstl;
    private final FrameDisplayer displayer;
    private final RoleController roleController;

    public JServicesPane(final ConstellationServer cstl, final FrameDisplayer displayer) {
        this(cstl, displayer, new DefaultRoleController());
    }

    public JServicesPane(final ConstellationServer cstl, final FrameDisplayer displayer, final RoleController roleController) {
        initComponents();

        this.cstl = cstl;
        if (displayer == null) {
            this.displayer = new DefaultFrameDisplayer();
        } else {
            this.displayer = displayer;
        }
        this.roleController = roleController;
        final Services services = cstl.services;
        final Map<String,List<String>> listServices = services.getAvailableService();
        final List<String> types = new ArrayList<String>(listServices.keySet());
        Collections.sort(types);

        guiAll.addActionListener(this);
        for(String type : types) {
            final JToggleButton btn = new JToggleButton(type);
            btn.setActionCommand(type);
            btn.addActionListener(this);
            guiTypeGroup.add(btn);
            guiToolBar.add(btn);
        }

        guiNew.setVisible(roleController.hasPermission(NEW_SERVICE));

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

        //list all services
        final List<Entry<Instance,String>> instances = new ArrayList<Entry<Instance, String>> ();


        final Map<String,List<String>> services = cstl.services.getAvailableService();
        for(Map.Entry<String,List<String>> service : services.entrySet()){

            if("all".equals(action) || action.equalsIgnoreCase(service.getKey())){
                final InstanceReport report = cstl.services.listInstance(service.getKey());
                if(report.getInstances() == null) continue;
                for(Instance instance : report.getInstances()){
                    instances.add(new AbstractMap.SimpleImmutableEntry<Instance, String>(instance, service.getKey()));
                }
            }
        }

        Collections.sort(instances,new Comparator<Entry<Instance,String>>(){
            @Override
            public int compare(Entry<Instance, String> o1, Entry<Instance, String> o2) {
                if(o1.getValue().equals(o2.getValue())){
                    //compare instance names
                    return o1.getKey().getName().compareTo(o2.getKey().getName());
                }else{
                    //compare types
                    return o1.getValue().compareTo(o2.getValue());
                }
            }
        });

        final TableModel model = new InstanceModel(instances);
        guiTable.setModel(model);

        final Font fontBig = new Font("Monospaced", Font.BOLD, 16);
        final Font fontNormal = new Font("Monospaced", Font.PLAIN, 12);
        final ImageIcon viewIcon = new ImageIcon(createImage(LayerRowModel.BUNDLE.getString("view"),
                null, Color.BLACK, fontNormal, Color.LIGHT_GRAY));
        final ImageIcon editIcon = new ImageIcon(createImage(LayerRowModel.BUNDLE.getString("edit"),
                ICON_SERVICE_EDIT, Color.BLACK, fontNormal, Color.LIGHT_GRAY));
        final ImageIcon reloadIcon = new ImageIcon(createImage(LayerRowModel.BUNDLE.getString("reload"),
                ICON_SERVICE_RELOAD, Color.WHITE, fontNormal, new Color(65,150,190)));
        final ImageIcon startIcon = new ImageIcon(createImage(LayerRowModel.BUNDLE.getString("start"),
                ICON_SERVICE_START, Color.WHITE, fontNormal, new Color(130, 160, 50)));
        final ImageIcon stopIcon = new ImageIcon(createImage(LayerRowModel.BUNDLE.getString("stop"),
                ICON_SERVICE_STOP, Color.WHITE, fontNormal, new Color(180,60,60)));

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
                    final Instance inst = (Instance) entry.getKey();
                    final String type = (String) entry.getValue();

                    final Color bgColor;
                    if(ServiceStatus.WORKING.equals(inst.getStatus())){
                        bgColor = new Color(130, 160, 50);
                    }else if(ServiceStatus.NOT_STARTED.equals(inst.getStatus())){
                        bgColor = Color.GRAY;
                    }else{
                        bgColor = new Color(180,60,60);
                    }

                    final BufferedImage img = JServicesPane.createImage(type, null, Color.WHITE,fontBig, bgColor);
                    lbl.setIcon(new ImageIcon(img));
                    lbl.setText(inst.getName());
                }

                return lbl;
            }

        });

        guiTable.getColumn(1).setCellRenderer(new ActionCell.Renderer(viewIcon){

            @Override
            public Icon getIcon(Object value){
                if (value instanceof DefaultMutableTreeNode) {
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                if (value instanceof Entry) {
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    final String type = (String) ((Entry)value).getValue();
                    final String lowerType = type.toLowerCase();

                    if(!(lowerType.equals("wms") || lowerType.equals("wmts") || lowerType.equals("wfs"))){
                        //not a viewable type
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
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    final String type = (String) ((Entry)value).getValue();
                    final String lowerType = type.toLowerCase();

                    if(!(lowerType.equals("wms") || lowerType.equals("wmts") || lowerType.equals("wfs"))){
                        //not a viewable type
                        return;
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getDisplayer().display(cstl, type, inst);
                        }
                    });

                }
            }
        });

        guiTable.getColumn(2).setCellRenderer(new ActionCell.Renderer(editIcon));
        guiTable.getColumn(2).setCellEditor(new ActionCell.Editor(editIcon) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if (value instanceof DefaultMutableTreeNode) {
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                if (value instanceof Entry) {
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    final String type = (String) ((Entry)value).getValue();


                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            final JServiceEditPane edit = new JServiceEditPane(cstl, type, inst);
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

        guiTable.getColumn(3).setCellRenderer(new ActionCell.Renderer(reloadIcon));
        guiTable.getColumn(3).setCellEditor(new ActionCell.Editor(reloadIcon) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    final String type = (String) ((Entry)value).getValue();
                     cstl.services.restartInstance(type, inst.getName());
                     SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateInstanceList();
                        }
                    });
                }
            }
        });

        guiTable.getColumn(4).setCellRenderer(new ActionCell.Renderer(null){

            @Override
            public Icon getIcon(Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    if(ServiceStatus.WORKING.equals(inst.getStatus())){
                        return stopIcon;
                    }else{
                        return startIcon;
                    }
                }
                return super.getIcon(value);
            }
        });
        guiTable.getColumn(4).setCellEditor(new ActionCell.Editor(null) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    final String type = (String) ((Entry)value).getValue();

                    if(ServiceStatus.WORKING.equals(inst.getStatus())){
                        cstl.services.stopInstance(type, inst.getName());
                    }else{
                        cstl.services.startInstance(type, inst.getName());
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateInstanceList();
                        }
                    });
                }
            }

            @Override
            public Icon getIcon(Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value =((DefaultMutableTreeNode)value).getUserObject();
                }
                if(value instanceof Entry){
                    final Instance inst = (Instance) ((Entry)value).getKey();
                    if(ServiceStatus.WORKING.equals(inst.getStatus())){
                        return stopIcon;
                    }else{
                        return startIcon;
                    }
                }
                return super.getIcon(value);
            }

        });

        final int width = 140;
        guiTable.getColumn(1).setMinWidth(width);
        guiTable.getColumn(1).setPreferredWidth(width);
        guiTable.getColumn(1).setMaxWidth(width);
        guiTable.getColumn(2).setMinWidth(width);
        guiTable.getColumn(2).setPreferredWidth(width);
        guiTable.getColumn(2).setMaxWidth(width);
        guiTable.getColumn(3).setMinWidth(width);
        guiTable.getColumn(3).setPreferredWidth(width);
        guiTable.getColumn(3).setMaxWidth(width);
        guiTable.getColumn(4).setMinWidth(width);
        guiTable.getColumn(4).setPreferredWidth(width);
        guiTable.getColumn(4).setMaxWidth(width);
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

        final String[] params = JServiceCreationPane.showDialog(cstl);
        if(params != null){
            cstl.services.newInstance(params[0], params[1]);
            updateInstanceList();
        }

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

        private final List<Entry<Instance,String>> entries;

        public InstanceModel(final List<Entry<Instance,String>> entries) {
            this.entries = entries;

        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return entries.get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {

            if(columnIndex == 1){
                Object value = getValueAt(rowIndex, columnIndex);

                if (value instanceof DefaultMutableTreeNode) {
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                if (value instanceof Entry) {
                    final String type = (String) ((Entry)value).getValue();
                    final String lowerType = type.toLowerCase();

                    if(!(lowerType.equals("wms") || lowerType.equals("wmts") || lowerType.equals("wfs"))){
                        //not a viewable type : disable edition
                        return false;
                    }
                }
            }

            return columnIndex>0;
        }

    }

    static BufferedImage createImage(String text, ImageIcon icon, Color textColor, Font font,Color bgColor){

        final int border = 5;
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        final FontMetrics fm = g.getFontMetrics(font);
        final int textSize = fm.stringWidth(text);
        int width = textSize+border*2;
        int height = fm.getHeight()+border*2;
        if(icon != null){
            width += icon.getIconWidth() + 2;
            height = Math.max(height, icon.getIconHeight());
        }

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width-1, img.getHeight()-1, border, border);

        g.setColor(bgColor);
        g.fill(rect);

        int x = border;
        //draw icon
        if(icon != null){
            g.drawImage(icon.getImage(), x, (height-icon.getIconHeight())/2, null);
            x += icon.getIconWidth()+2;
        }

        //draw text
        g.setColor(textColor);
        g.setFont(font);
        g.drawString(text, x, fm.getMaxAscent()+border);

        return img;
    }

}
