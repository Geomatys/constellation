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
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.admin.service.ConstellationServer.Services;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.security.RoleController;
import static org.constellation.security.ActionPermissions.*;
import org.constellation.swing.action.Action;
import org.constellation.swing.action.ActionEditor;
import org.constellation.swing.action.ActionRenderer;
import org.jdesktop.swingx.JXTable;

/**
 * Top component to manage constellation services.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class JServicesPane extends JPanel implements ActionListener, PropertyChangeListener {

    private static final Logger LOGGER = Logging.getLogger(JServicesPane.class);
    
    private final List<Action> actions = new ArrayList<>();
    private final JXTable guiTable = new JXTable();
    private final ConstellationServer cstl;
    private final FrameDisplayer displayer;

    public JServicesPane(final ConstellationServer cstl, final FrameDisplayer displayer) {
        this(cstl, displayer, null);
    }

    public JServicesPane(final ConstellationServer cstl, final FrameDisplayer displayer,
            RoleController roleController, Action ... actions) {
        initComponents();

        for(Action act : actions){
            if(roleController == null || roleController.hasPermission(act.getName())){
                this.actions.add(act);
                act.addPropertyChangeListener(this);
            }
        }

        this.cstl = cstl;
        if (displayer == null) {
            this.displayer = new DefaultFrameDisplayer();
        } else {
            this.displayer = displayer;
        }
        final Services services = cstl.services;
        final Map<String,List<String>> listServices = services.getAvailableService();
        final List<String> types = new ArrayList<>(listServices.keySet());
        Collections.sort(types);

        guiAll.addActionListener(this);
        for(String type : types) {
            final JToggleButton btn = new JToggleButton(type);
            btn.setActionCommand(type);
            btn.addActionListener(this);
            guiTypeGroup.add(btn);
            guiToolBar.add(btn);
        }

        guiNew.setVisible(roleController == null || roleController.hasPermission(NEW_SERVICE));

        guiTable.setDefaultRenderer(Action.class, new ActionRenderer(cstl));
        guiTable.setDefaultEditor(Action.class, new ActionEditor(cstl));

        guiTable.setDefaultRenderer(Entry.class, new DefaultTableCellRenderer(){

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

                    final Font fontBig = new Font("Monospaced", Font.BOLD, 16);
                    final BufferedImage img = JServicesPane.createImage(type, null, Color.WHITE,fontBig, bgColor);
                    lbl.setIcon(new ImageIcon(img));
                    lbl.setText(inst.getName());
                }

                return lbl;
            }

        });


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
        final List<Entry<Instance,String>> instances = new ArrayList<> ();


        final Map<String,List<String>> services = cstl.services.getAvailableService();
        for(Map.Entry<String,List<String>> service : services.entrySet()){

            if("all".equals(action) || action.equalsIgnoreCase(service.getKey())){
                final InstanceReport report = cstl.services.listInstance(service.getKey());
                if (report != null) {
                    if (report.getInstances() == null) continue;
                    for(Instance instance : report.getInstances()){
                        instances.add(new AbstractMap.SimpleImmutableEntry<>(instance, service.getKey()));
                    }
                } else {
                    LOGGER.warning("Unable to get the report for service: " + service.getKey());
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateInstanceList();
    }

    private class InstanceModel extends AbstractTableModel{

        private final List<Entry<Instance,String>> entries;

        public InstanceModel(final List<Entry<Instance,String>> entries) {
            this.entries = entries;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if(columnIndex == 0){
                return Entry.class;
            }else{
                return Action.class;
            }
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 1+actions.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex>0){
                final Action act = actions.get(columnIndex-1).clone();
                act.setTarget(entries.get(rowIndex));
                act.setDisplayer(displayer);
                return act;
            }

            return entries.get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex>0;
        }

    }

    public static BufferedImage createImage(String text, ImageIcon icon, Color textColor, Font font,Color bgColor){

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
