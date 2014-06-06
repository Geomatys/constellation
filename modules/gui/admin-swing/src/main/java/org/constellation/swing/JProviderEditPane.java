/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProviderReport;
import org.geotoolkit.gui.swing.style.JColorMapPane;
import org.geotoolkit.gui.swing.util.ActionCell;
import org.geotoolkit.gui.swing.util.JOptionDialog;
import org.geotoolkit.gui.swing.propertyedit.JFeatureOutLine;
import org.geotoolkit.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JAdvancedStylePanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JClassificationIntervalStylePanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JClassificationSingleStylePanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JSLDImportExportPanel;
import org.geotoolkit.gui.swing.propertyedit.styleproperty.JSimpleStylePanel;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.openide.util.Exceptions;

/**
 * Edit a provider.
 *
 * @author Johann Sorel (geomatys)
 */
public class JProviderEditPane extends javax.swing.JPanel {

    private static final ImageIcon ICON_EDIT =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceEditBlanc.png"));
    private static final ImageIcon ICON_COPY =  new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/edit_copy.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(JServicesPane.class.getResource("/org/constellation/swing/serviceCross.png"));

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/constellation/swing/Bundle");

    private final ConstellationServer server;
    private final ConstellationClient serverV2;
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

    public JProviderEditPane(final ConstellationServer server, final ConstellationClient serverV2, final String serviceType, final ProviderReport providerReport) throws IOException, XMLStreamException, ClassNotFoundException {
        this.server         = server;
        this.serverV2       = serverV2;
        this.providerType   = serviceType;
        this.providerReport = providerReport;

        configDesc = (ParameterDescriptorGroup) serverV2.providers.getServiceDescriptor(providerType);
        ParameterDescriptorGroup sourceCandidate = null; 
        try {
            sourceCandidate = (ParameterDescriptorGroup) configDesc.descriptor("source");
        } catch (ParameterNotFoundException ex) {
            sourceCandidate = configDesc;
        }
        sourceDesc = sourceCandidate;
        ParameterValueGroup source = null;
        try {
            source = (ParameterValueGroup) serverV2.providers.getProviderConfiguration(providerReport.getId(), sourceDesc);
        } catch (IOException | XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        sourceParam = source;
        dataDesc = (ParameterDescriptorGroup) serverV2.providers.getSourceDescriptor(providerType);
        final List<ParameterValueGroup> dataGroups = sourceParam.groups(dataDesc.getName().getCode());
        dataParam = (dataGroups.isEmpty()) ? null : dataGroups.get(0);

        if("choice".equalsIgnoreCase(dataDesc.getName().getCode())){
            for(GeneralParameterValue sub : dataParam.values()){
                subdataParam = (ParameterValueGroup) sub;
                subdataDesc  = subdataParam.getDescriptor();
            }
        }

        initComponents();
        if(dataParam != null || subdataParam != null){
            guiParameterEditor.setEdited((subdataParam==null) ? dataParam : subdataParam);
            guiParameters.setViewportView(guiParameterEditor);
        }

        final String providerType = sourceParam.parameter("providerType").stringValue();

        guiIdentifier.setText(providerReport.getId());
        guiCategory.setSelectedItem(providerType);
        
        final boolean styleType = "sld".equals(serviceType);
        guiAdd.setVisible(styleType);

        //data list
        updateDataModel();

    }

    private void updateDataModel(){
        try {
            providerReport = serverV2.providers.listProviders().getProviderService(providerType).getProvider(providerReport.getId());
            final boolean styleType = "sld".equals(providerType);

            final Font fontBig = new Font("Monospaced", Font.BOLD, 16);
            final Font fontNormal = new Font("Monospaced", Font.PLAIN, 12);
            final ImageIcon editIcon = new ImageIcon(JServicesPane.createImage("",
                    ICON_EDIT, Color.BLACK, fontNormal, Color.DARK_GRAY));
            final ImageIcon copyIcon = new ImageIcon(JServicesPane.createImage("",
                    ICON_COPY, Color.BLACK, fontNormal, Color.DARK_GRAY));
            final ImageIcon deleteIcon = new ImageIcon(JServicesPane.createImage("",
                    ICON_DELETE, Color.WHITE, fontNormal, Color.DARK_GRAY));

            final List<DataBrief> layers = providerReport.getItems();

            Collections.sort(layers, new Comparator<DataBrief>() {
                @Override
                public int compare(DataBrief o1, DataBrief o2) {
                    String l1 = o1.getName();
                    String l2 = o2.getName();
                    return l1.toLowerCase().compareTo(l2.toLowerCase());
                }
            });

            final List<String> itemNames = new ArrayList<>(0);
            for (DataBrief dataBrief : layers) {
                itemNames.add(dataBrief.getName());
            }

            guiData.setModel(new DataModel(itemNames,styleType));

            if(styleType){
                guiData.getColumn(1).setCellRenderer(new ActionCell.Renderer(editIcon));
                guiData.getColumn(1).setCellEditor(new ActionCell.Editor(editIcon) {
                    @Override
                    public void actionPerformed(final ActionEvent e, Object value) {
                        try {
                            final String styleName = (String) value;
                            MutableStyle style = serverV2.providers.getStyle(providerReport.getId(), styleName);
                            editStyle(style, styleName,false);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    }
                });
                guiData.getColumn(1).setMaxWidth(40);
                guiData.getColumn(1).setWidth(40);
                guiData.getColumn(1).setPreferredWidth(40);

                guiData.getColumn(2).setCellRenderer(new ActionCell.Renderer(copyIcon));
                guiData.getColumn(2).setCellEditor(new ActionCell.Editor(copyIcon) {
                    @Override
                    public void actionPerformed(final ActionEvent e, Object value) {
                        try {
                            final String styleName = (String) value;
                            final MutableStyle style = serverV2.providers.getStyle(providerReport.getId(), styleName);
                            final String newName = styleName+"(copy)";
                            style.setName(newName);
                            serverV2.providers.createStyle(providerReport.getId(), style);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateDataModel();
                                }
                            });
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                guiData.getColumn(2).setMaxWidth(40);
                guiData.getColumn(2).setWidth(40);
                guiData.getColumn(2).setPreferredWidth(40);

                guiData.getColumn(3).setCellRenderer(new ActionCell.Renderer(deleteIcon));
                guiData.getColumn(3).setCellEditor(new ActionCell.Editor(deleteIcon) {
                    @Override
                    public void actionPerformed(final ActionEvent e, Object value) {
                        try {
                            final String styleName = (String) value;
                            serverV2.providers.deleteStyle(providerReport.getId(), styleName);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateDataModel();
                                }
                            });
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                guiData.getColumn(3).setMaxWidth(40);
                guiData.getColumn(3).setWidth(40);
                guiData.getColumn(3).setPreferredWidth(40);
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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getProviderType() {
        return providerType;
    }

    public ProviderReport getProviderReport() {
        return providerReport;
    }
    
    public void setIdentifierTextFieldEnable(boolean enable) {
        guiIdentifier.setEnabled(enable);
    }

    private void correctName(){
        int pos = guiIdentifier.getCaretPosition();
        guiIdentifier.setText(guiIdentifier.getText().replace(' ', '_'));
        guiIdentifier.setCaretPosition(pos);
    }

    private void editStyle(MutableStyle style, String styleName,final boolean isNew){
        final String oldName = styleName;

        final JPanel pane = new JPanel(new BorderLayout());
        final JLabel lbl = new JLabel(BUNDLE.getString("name"));
        final JTextField textField = new JTextField(styleName);
        
        final MapLayer layer = MapBuilder.createEmptyMapLayer();
        layer.setStyle(style);

        LayerStylePropertyPanel editors = new LayerStylePropertyPanel();
        editors.addPropertyPanel(MessageBundle.getString("analyze"),new JSimpleStylePanel());
        editors.addPropertyPanel(MessageBundle.getString("analyze_vector"),new JClassificationSingleStylePanel());
        editors.addPropertyPanel(MessageBundle.getString("analyze_vector"),new JClassificationIntervalStylePanel());
        editors.addPropertyPanel(MessageBundle.getString("analyze_raster"),new JColorMapPane());
        editors.addPropertyPanel(MessageBundle.getString("sld"),new JAdvancedStylePanel());
        editors.addPropertyPanel(MessageBundle.getString("sld"),new JSLDImportExportPanel());
        editors.setTarget(layer);
        
        final JPanel north = new JPanel(new BorderLayout());
        north.add(BorderLayout.WEST,lbl);
        north.add(BorderLayout.CENTER,textField);

        pane.add(BorderLayout.NORTH,north);
        pane.add(BorderLayout.CENTER,editors);

        int res = JOptionDialog.show(null, pane, JOptionPane.OK_CANCEL_OPTION);
        if(JOptionPane.OK_OPTION != res) return;
        
        styleName = textField.getText();

        editors.apply();
        style = layer.getStyle();
        style.setName(textField.getText());

        if (isNew) {
            //ensure name does not exist
            final String baseName = styleName;
            int i=0;
            while (providerReport.getItems().contains(styleName)) {
                styleName = baseName + i++;
            }
        } else {
            if (providerReport.getItems().contains(oldName)) {
                try {
                    //delete previous if it existed
                    serverV2.providers.deleteStyle(providerReport.getId(), oldName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        try {
            serverV2.providers.createStyle(providerReport.getId(), style);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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
        metadataButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        guiCategory = new javax.swing.JComboBox();

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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel2);

        metadataButton.setText(org.openide.util.NbBundle.getMessage(JProviderEditPane.class, "metadata")); // NOI18N
        metadataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metadataButtonActionPerformed(evt);
            }
        });

        jLabel4.setText(org.openide.util.NbBundle.getMessage(JProviderEditPane.class, "providerCategory")); // NOI18N

        guiCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "vector", "raster" }));

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
                        .addComponent(metadataButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(guiSave))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(guiCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(guiIdentifier))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(guiCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiDelete)
                    .addComponent(guiSave)
                    .addComponent(metadataButton))
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
        params.parameter("providerType").setValue(guiCategory.getSelectedItem());

        if(subdataDesc == null){
            Parameters.copy(config, dataParam);
        }else{
            Parameters.copy(config, subdataParam);
        }

        try {
            serverV2.providers.updateProvider(providerType, id, sourceParam);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        firePropertyChange("update", 0, 1);
    }//GEN-LAST:event_guiSaveActionPerformed

    private void guiDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiDeleteActionPerformed
        try {
            serverV2.providers.deleteProvider(providerReport.getId(), false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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

    private void metadataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metadataButtonActionPerformed
        final JComponent edit = new JProviderMetadataPane(serverV2, providerReport.getId());
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setContentPane(edit);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setTitle(providerReport.getId());

        final PropertyChangeListener cl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    dialog.dispose();
                }
            }
        };
        edit.addPropertyChangeListener(cl);
        dialog.setVisible(true);
    }//GEN-LAST:event_metadataButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiAdd;
    private javax.swing.JComboBox guiCategory;
    private org.jdesktop.swingx.JXTable guiData;
    private javax.swing.JButton guiDelete;
    private javax.swing.JTextField guiIdentifier;
    private javax.swing.JScrollPane guiParameters;
    private javax.swing.JButton guiSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton metadataButton;
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
            return (styleType)? 4 : 1;
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
