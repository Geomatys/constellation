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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.geotoolkit.gui.swing.misc.ActionCell;
import org.geotoolkit.gui.swing.propertyedit.JFeatureOutLine;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JAdvancedStylePanel;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Edit a service.
 *
 * @author Johann Sorel (geomatys)
 */
public class JProviderEditPane extends javax.swing.JPanel {

    private static final ImageIcon ICON_EDIT =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceEditBlanc.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceCross.png"));

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/constellation/swing/Bundle");

    private final ConstellationServer server;
    private final String providerType;
    private ProviderReport providerReport;
    private final ParameterDescriptorGroup configDesc;
    private final ParameterDescriptorGroup sourceDesc;
    private final ParameterValueGroup sourceParam;
    private final ParameterDescriptorGroup dataDesc;
    private final ParameterValueGroup dataParam;
    private ParameterDescriptorGroup subdataDesc = null;
    private ParameterValueGroup subdataParam;
    private final JFeatureOutLine guiParameterEditor = new JFeatureOutLine();

    public JProviderEditPane(final ConstellationServer server, final String serviceType, final ProviderReport providerReport) {
        this.server = server;
        this.providerType = serviceType;
        this.providerReport = providerReport;

        configDesc = (ParameterDescriptorGroup) server.providers.getServiceDescriptor(providerType);
        sourceDesc = (ParameterDescriptorGroup) configDesc.descriptor("source");
        sourceParam = (ParameterValueGroup) server.providers.getProviderConfiguration(providerReport.getId(), sourceDesc);
        dataDesc = (ParameterDescriptorGroup) server.providers.getSourceDescriptor(providerType);
        dataParam = sourceParam.groups(dataDesc.getName().getCode()).get(0);

        if("choice".equalsIgnoreCase(dataDesc.getName().getCode())){
            for(GeneralParameterValue sub : dataParam.values()){
                subdataParam = (ParameterValueGroup) sub;
                subdataDesc = subdataParam.getDescriptor();
            }
        }

        initComponents();
        guiParameterEditor.setEdited((subdataParam==null) ? dataParam : subdataParam);
        guiParameters.setViewportView(guiParameterEditor);

        guiIdentifier.setText(providerReport.getId());
        final boolean styleType = "sld".equals(serviceType);
        guiAdd.setVisible(styleType);

        //data list
        updateDataModel();

    }

    private void updateDataModel(){
        providerReport = server.providers.listProviders().getProviderService(providerType).getProvider(providerReport.getId());
        final boolean styleType = "sld".equals(providerType);

        final Font fontBig = new Font("Monospaced", Font.BOLD, 16);
        final Font fontNormal = new Font("Monospaced", Font.PLAIN, 12);
        final ImageIcon editIcon = new ImageIcon(JServicesPane.createImage("",
                ICON_EDIT, Color.BLACK, fontNormal, Color.DARK_GRAY));
        final ImageIcon deleteIcon = new ImageIcon(JServicesPane.createImage("",
                ICON_DELETE, Color.WHITE, fontNormal, Color.DARK_GRAY));

        guiData.setModel(new DataModel(providerReport.getItems(),styleType));

        if(styleType){
            guiData.getColumn(1).setCellRenderer(new ActionCell.Renderer(editIcon));
            guiData.getColumn(1).setCellEditor(new ActionCell.Editor(editIcon) {
                @Override
                public void actionPerformed(final ActionEvent e, Object value) {
                    final String styleName = (String) value;
                    MutableStyle style = server.providers.downloadStyle(providerReport.getId(), styleName);
                    editStyle(style, styleName,false);

                }
            });

            guiData.getColumn(2).setCellRenderer(new ActionCell.Renderer(deleteIcon));
            guiData.getColumn(2).setCellEditor(new ActionCell.Editor(deleteIcon) {
                @Override
                public void actionPerformed(final ActionEvent e, Object value) {
                    final String styleName = (String) value;
                    server.providers.deleteStyle(providerReport.getId(), styleName);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateDataModel();
                        }
                    });
                }
            });
        }

        guiData.setTableHeader(null);
        guiData.setRowHeight(37);
        guiData.setFillsViewportHeight(true);
        guiData.setBackground(Color.WHITE);
        guiData.setShowGrid(true);
        guiData.setShowHorizontalLines(true);
        guiData.setShowVerticalLines(false);
        guiData.revalidate();
        guiData.repaint();
    }

    public String getProviderType() {
        return providerType;
    }

    public ProviderReport getProviderReport() {
        return providerReport;
    }

    private void correctName(){
        int pos = guiIdentifier.getCaretPosition();
        guiIdentifier.setText(guiIdentifier.getText().replace(' ', '_'));
        guiIdentifier.setCaretPosition(pos);
    }

    private void editStyle(MutableStyle style, String styleName,final boolean isNew){
        final String oldName = styleName;

        final JDialog dialog = new JDialog();
        final JPanel pane = new JPanel(new BorderLayout());
        final JLabel lbl = new JLabel(BUNDLE.getString("name"));
        final JTextField textField = new JTextField(styleName);
        final JAdvancedStylePanel editor = new JAdvancedStylePanel();
        editor.parse(style);

        final JPanel north = new JPanel(new BorderLayout());
        north.add(BorderLayout.WEST,lbl);
        north.add(BorderLayout.CENTER,textField);

        pane.add(BorderLayout.NORTH,north);
        pane.add(BorderLayout.CENTER,editor);


        dialog.setContentPane(pane);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        style = (MutableStyle) editor.create();
        style.setName(textField.getText());

        if(isNew){
            //ensure name does not exist
            final String baseName = styleName;
            int i=0;
            while(providerReport.getItems().contains(styleName)){
                styleName = baseName + i++;
            }
        }else{
            if(providerReport.getItems().contains(oldName)){
                //delete previous if it existed
                server.providers.deleteStyle(providerReport.getId(), oldName);
            }
        }

        server.providers.createStyle(providerReport.getId(), styleName, style);
        updateDataModel();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        guiDelete = new javax.swing.JButton();
        guiSave = new javax.swing.JButton();
        guiIdentifier = new javax.swing.JTextField();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        guiParameters = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        guiAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        guiData = new org.jdesktop.swingx.JXTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("id")); // NOI18N

        guiDelete.setText(bundle.getString("delete")); // NOI18N
        guiDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiDeleteActionPerformed(evt);
            }
        });

        guiSave.setText(bundle.getString("save")); // NOI18N
        guiSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiSaveActionPerformed(evt);
            }
        });

        guiIdentifier.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                guiIdentifierKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                guiIdentifierKeyTyped(evt);
            }
        });

        jSplitPane1.setBorder(null);

        jLabel3.setText(bundle.getString("parameters")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(145, Short.MAX_VALUE))
            .addComponent(guiParameters, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(guiParameters, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jLabel2.setText(bundle.getString("datas")); // NOI18N

        guiAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/constellation/swing/edit_add.png"))); // NOI18N
        guiAdd.setText(bundle.getString("newstyle")); // NOI18N
        guiAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiAddActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(guiData);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 184, Short.MAX_VALUE)
                .addComponent(guiAdd))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(guiAdd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(guiDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(guiSave))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiIdentifier)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiDelete, guiSave});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(guiIdentifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiDelete)
                    .addComponent(guiSave))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiSaveActionPerformed
        correctName();

        final String id = providerReport.getId();
        final ParameterValueGroup config = guiParameterEditor.getEditedAsParameter(
                (subdataDesc==null) ? dataDesc : subdataDesc );

        final ParameterValueGroup params = sourceParam;
        params.parameter("id").setValue(id);

        if(subdataDesc == null){
            Parameters.copy(config, dataParam);
        }else{
            Parameters.copy(config, subdataParam);
        }

        server.providers.updateProvider(providerType, id, sourceParam);

        firePropertyChange("update", 0, 1);
    }//GEN-LAST:event_guiSaveActionPerformed

    private void guiDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiDeleteActionPerformed
        server.providers.deleteProvider(providerReport.getId());
        firePropertyChange("update", 0, 1);
    }//GEN-LAST:event_guiDeleteActionPerformed

    private void guiIdentifierKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_guiIdentifierKeyTyped
        correctName();
    }//GEN-LAST:event_guiIdentifierKeyTyped

    private void guiIdentifierKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_guiIdentifierKeyPressed
        correctName();
    }//GEN-LAST:event_guiIdentifierKeyPressed

    private void guiAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiAddActionPerformed

        final MutableStyleFactory MSF = new DefaultStyleFactory();
        MutableStyle style = MSF.style();
        String name = "unnamed";
        editStyle(style,name,true);

    }//GEN-LAST:event_guiAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiAdd;
    private org.jdesktop.swingx.JXTable guiData;
    private javax.swing.JButton guiDelete;
    private javax.swing.JTextField guiIdentifier;
    private javax.swing.JScrollPane guiParameters;
    private javax.swing.JButton guiSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    private class DataModel extends  AbstractTableModel{

        private final List<String> datas;
        private final boolean styleType;

        public DataModel(final List<String> datas, final boolean styleType) {
            this.datas = datas;
            this.styleType = styleType;
        }

        @Override
        public int getRowCount() {
            return datas.size();
        }

        @Override
        public int getColumnCount() {
            return (styleType)? 3 : 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex>0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return datas.get(rowIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

    }

}
