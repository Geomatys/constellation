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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import static org.constellation.swing.JServiceEditionPane.ICON_EDIT;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.util.ActionCell;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.NbBundle;

/**
 *
 * @author guilhem
 */
public class JServiceWpsPane extends JServiceEditionPane {

    private final ProcessContext configuration;

    private List<ProcessFactoryModel> fatoryModelModelList;

    private final ConstellationClient serverV2;
    
    /**
     * Creates new form JServiceWpsPane
     * @param serverV2
     * @param configuration
     */
    public JServiceWpsPane(final ConstellationClient serverV2, final Object configuration) {
        this.configuration = (configuration instanceof ProcessContext) ? (ProcessContext) configuration : null;
        this.serverV2 = serverV2;
        initComponents();

        guiProcessFactoryTable.setDefaultRenderer(ProcessFactoryRowModel.EditProcessFactory.class, new ActionCell.Renderer(ICON_EDIT));
        guiProcessFactoryTable.setDefaultEditor(ProcessFactoryRowModel.EditProcessFactory.class, new ActionCell.Editor(ICON_EDIT) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }

                if (value instanceof ProcessFactoryModel) {
                    final ProcessFactoryModel oldProcessFactoryModel = (ProcessFactoryModel) value;
                    final  ProcessFactoryModel updateProcessFactoryModel = JEditProcessFactoryPane.showDialog(serverV2, oldProcessFactoryModel);
                    if (updateProcessFactoryModel != null) {
                        final int pos = fatoryModelModelList.indexOf(oldProcessFactoryModel);
                        fatoryModelModelList.remove(pos);
                        fatoryModelModelList.add(pos, updateProcessFactoryModel);
                    }
                    updateProcessTableModel();
                }
            }
        });

        guiProcessFactoryTable.setDefaultRenderer(ProcessFactoryRowModel.DeleteProcessFactory.class, new ActionCell.Renderer(ICON_DELETE));
        guiProcessFactoryTable.setDefaultEditor(ProcessFactoryRowModel.DeleteProcessFactory.class, new ActionCell.Editor(ICON_DELETE) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }

                if (value instanceof ProcessFactoryModel) {
                    final ProcessFactoryModel oldProcessFactoryModel = (ProcessFactoryModel) value;
                    fatoryModelModelList.remove(oldProcessFactoryModel);
                    updateProcessTableModel();
                }
            }
        });

        guiProcessFactoryTable.setShowVerticalLines(false);
        guiProcessFactoryTable.setFillsViewportHeight(true);

        initProcessList();

        updateProcessTableModel();
    }

    /**
     * Create a list of process factory model based on service configuration.
     */
    private void initProcessList() {
        fatoryModelModelList = new ArrayList<>();
        if (configuration.getProcesses().getLoadAll()) {
            allProcessCheck.setSelected(true);
        } else {
            final List<ProcessFactory> factories = configuration.getProcessFactories();
            for (final ProcessFactory factory : factories) {
                fatoryModelModelList.add(new ProcessFactoryModel(factory));
            }
            allProcessCheck.setSelected(false);
        }
        guiTextFCProviderID.setText(configuration.getFileCoverageProviderId());
        guiTextWMSInstance.setText(configuration.getWmsInstanceName());
        guiTextWebavDir.setText(configuration.getWebdavDirectory());
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
                    final RowModel model = new ProcessFactoryRowModel();

                    for (final ProcessFactoryModel factoryModel : fatoryModelModelList) {
                        root.add(new DefaultMutableTreeNode(factoryModel));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            guiProcessFactoryTable.setRootVisible(false);
                            guiProcessFactoryTable.setModel(DefaultOutlineModel.createOutlineModel(treeModel, model));
                        }
                    });

                } finally{
                    guiProcessFactoryTable.repaint();
                    guiLoadLabel.setText("");
                    guiLoadLabel.setIcon(null);
                }
                guiProcessFactoryTable.setRenderDataProvider(new ProcessFactoryRowRenderer());
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

        allProcessCheck = new JCheckBox();
        jScrollPane1 = new JScrollPane();
        guiProcessFactoryTable = new Outline();
        guiLoadBar = new JToolBar();
        guiLoadLabel = new JLabel();
        guiAddLayer = new JButton();
        jLabel1 = new JLabel();
        guiTextWebavDir = new JTextField();
        jLabel2 = new JLabel();
        guiTextWMSInstance = new JTextField();
        jLabel3 = new JLabel();
        guiTextFCProviderID = new JTextField();

        ResourceBundle bundle = ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        allProcessCheck.setText(bundle.getString("loadAllProcess")); // NOI18N
        allProcessCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                allProcessCheckItemStateChanged(evt);
            }
        });

        jScrollPane1.setViewportView(guiProcessFactoryTable);

        guiLoadBar.setFloatable(false);
        guiLoadBar.setRollover(true);
        guiLoadBar.add(guiLoadLabel);

        guiAddLayer.setIcon(new ImageIcon(getClass().getResource("/org/constellation/swing/edit_add.png"))); // NOI18N
        guiAddLayer.setText(bundle.getString("guiAddProcessFactoryBtn")); // NOI18N
        guiAddLayer.setFocusable(false);
        guiAddLayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                guiAddProcessFactoryActionPerformed(evt);
            }
        });

        jLabel1.setText(NbBundle.getMessage(JServiceWpsPane.class, "webdavDir")); // NOI18N

        jLabel2.setText(NbBundle.getMessage(JServiceWpsPane.class, "wmsInstance")); // NOI18N

        jLabel3.setText(NbBundle.getMessage(JServiceWpsPane.class, "fcProviderID")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(guiTextWebavDir, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(guiTextWMSInstance, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(guiTextFCProviderID, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(guiAddLayer)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(guiLoadBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(allProcessCheck, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 493, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {guiTextFCProviderID, guiTextWMSInstance, guiTextWebavDir});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel1, jLabel2, jLabel3});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(guiAddLayer)
                    .addComponent(guiLoadBar, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(allProcessCheck)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 227, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guiTextWebavDir))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guiTextWMSInstance))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(guiTextFCProviderID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiAddProcessFactoryActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guiAddProcessFactoryActionPerformed

        final ProcessFactoryModel processFactoryModel = JEditProcessFactoryPane.showDialog(serverV2, null);
        if (processFactoryModel != null) {
            guiProcessFactoryTable.setEnabled(true);
            fatoryModelModelList.add(processFactoryModel);
            updateProcessTableModel();
        }
    }//GEN-LAST:event_guiAddProcessFactoryActionPerformed

    private void allProcessCheckItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_allProcessCheckItemStateChanged
        if (allProcessCheck.isSelected()) {
            fatoryModelModelList = new ArrayList<>();
        }
        guiProcessFactoryTable.setEnabled(!allProcessCheck.isSelected());
        updateProcessTableModel();
    }//GEN-LAST:event_allProcessCheckItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox allProcessCheck;
    private JButton guiAddLayer;
    private JToolBar guiLoadBar;
    private JLabel guiLoadLabel;
    private Outline guiProcessFactoryTable;
    private JTextField guiTextFCProviderID;
    private JTextField guiTextWMSInstance;
    private JTextField guiTextWebavDir;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public AbstractConfigurationObject getConfiguration() {
        updateConfiguration();
        return configuration;
    }

    /**
     * Update process defined in configuration using fatoryModelModelList.
     */
    private void updateConfiguration() {
        final List<ProcessFactory> factories = new ArrayList<>();
        if (!allProcessCheck.isSelected()) {
            for (final ProcessFactoryModel factoryModel : fatoryModelModelList) {
                factories.add(factoryModel.getFactory());
            }
            configuration.getProcesses().setLoadAll(Boolean.FALSE);
        } else {
            configuration.getProcesses().setLoadAll(Boolean.TRUE);
        }
        //update configuration processes
        configuration.getProcesses().setFactory(factories);

        final String wdDirectory = guiTextWebavDir.getText();
        if (wdDirectory != null && !wdDirectory.isEmpty()) {
            configuration.setWebdavDirectory(wdDirectory);
        } else {
            configuration.setWebdavDirectory(null);
        }

        final String fcProviderID = guiTextFCProviderID.getText();
        if (fcProviderID != null && !fcProviderID.isEmpty()) {
            configuration.setFileCoverageProviderId(fcProviderID);
        } else {
            configuration.setFileCoverageProviderId(null);
        }

        final String wmsInstance = guiTextWMSInstance.getText();
        if (wmsInstance != null && !wmsInstance.isEmpty()) {
            configuration.setWmsInstanceName(wmsInstance);
        } else {
            configuration.setWmsInstanceName(null);
        }
    }

    /**
     * Rendering of process factory row in Outline table.
     */
    private static class ProcessFactoryRowRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            if(o instanceof DefaultMutableTreeNode){
                o = ((DefaultMutableTreeNode)o).getUserObject();
            }
            if (o instanceof ProcessFactoryModel) {
                final ProcessFactoryModel processFactory = (ProcessFactoryModel) o;
                if (processFactory.getFactory() != null) {
                    return processFactory.getFactory().getAutorityCode();
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
    
    @Override
    public DataSourceType getDatasourceType() {
        throw new UnsupportedOperationException("Not supported on this panel.");
    }
}
