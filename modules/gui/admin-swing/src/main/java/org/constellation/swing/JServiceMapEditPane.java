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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.namespace.QName;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.Source;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.geotoolkit.gui.swing.util.ActionCell;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class JServiceMapEditPane extends JServiceEditionPane {

    private final ConstellationServer server;
    private final ConstellationClient serverV2;
    private final String serviceType;
    private final LayerContext configuration;
    private List<LayerModel> layerModelList;
    private List<SourceModel> sourceModelList;
    
    /**
     * Creates new form JServiceMapEditPane
     * @param server
     * @param serverV2
     * @param serviceType
     * @param configuration
     */
    public JServiceMapEditPane(final ConstellationServer server, final ConstellationClient serverV2, final String serviceType, final Object configuration) {
        this.server = server;
        this.serverV2 = serverV2;
        this.serviceType = serviceType;
        this.configuration = (configuration instanceof LayerContext) ? (LayerContext) configuration : null;
        initComponents();

        transactionnalBox.setVisible(serviceType.equals("WFS"));
        guiLayerTable.setDefaultRenderer(LayerRowModel.EditLayer.class, new ActionCell.Renderer(ICON_EDIT));
        guiLayerTable.setDefaultEditor(LayerRowModel.EditLayer.class, new ActionCell.Editor(ICON_EDIT) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                
                if (value instanceof LayerModel) {
                    final LayerModel oldLayerModel = (LayerModel) value;
                    final  LayerModel updateLayerModel = JEditLayerPane.showDialog(server, serverV2, serviceType, oldLayerModel);
                    if (updateLayerModel != null) {
                        final int pos = layerModelList.indexOf(oldLayerModel);
                        layerModelList.remove(pos);
                        layerModelList.add(pos, updateLayerModel);
                    }
                    updateLayerTableModel();
                }
            }
        });

        guiLayerTable.setDefaultRenderer(LayerRowModel.DeleteLayer.class, new ActionCell.Renderer(ICON_DELETE));
        guiLayerTable.setDefaultEditor(LayerRowModel.DeleteLayer.class, new ActionCell.Editor(ICON_DELETE) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                
                if (value instanceof LayerModel) {
                    final LayerModel oldLayerModel = (LayerModel) value;
                    layerModelList.remove(oldLayerModel);
                    updateLayerTableModel();
                }
            }
        });

        guiLayerTable.setShowVerticalLines(false);
        guiLayerTable.setFillsViewportHeight(true);

        guiSourceTable.setDefaultRenderer(SourceRowModel.EditSource.class, new ActionCell.Renderer(ICON_EDIT));
        guiSourceTable.setDefaultEditor(SourceRowModel.EditSource.class, new ActionCell.Editor(ICON_EDIT) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }

                if (value instanceof SourceModel) {
                    final SourceModel oldSourceModel = (SourceModel) value;
                    final SourceModel updateSourceModel = JEditSourcePane.showDialog(server, serverV2, serviceType, oldSourceModel);
                    if (updateSourceModel != null) {
                        final int pos = sourceModelList.indexOf(oldSourceModel);
                        sourceModelList.remove(pos);
                        sourceModelList.add(pos, updateSourceModel);
                    }
                    updateSourceTableModel();
                }
            }
        });

        guiSourceTable.setDefaultRenderer(SourceRowModel.DeleteSource.class, new ActionCell.Renderer(ICON_DELETE));
        guiSourceTable.setDefaultEditor(SourceRowModel.DeleteSource.class, new ActionCell.Editor(ICON_DELETE) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }

                if (value instanceof SourceModel) {
                    final SourceModel oldSourceModel = (SourceModel) value;
                    sourceModelList.remove(oldSourceModel);
                    updateSourceTableModel();
                }
            }
        });

        guiSourceTable.setShowVerticalLines(false);
        guiSourceTable.setFillsViewportHeight(true);
        
        try {
            initLayerSourceList();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        updateLayerTableModel();
        updateSourceTableModel();
    }

    /**
     * Create a list of layer model based on service configuration.
     */
    private void initLayerSourceList() throws IOException {
        layerModelList  = new ArrayList<>();
        sourceModelList = new ArrayList<>();
        final List<Source> sources = configuration.getLayers();

        for (final Source source : sources) {
            final String providerId = source.getId();

            sourceModelList.add(new SourceModel(providerId, source.getLoadAll()));

            if (!source.getLoadAll()) {
                final List<Layer> layers = source.getInclude();
                for (final Layer layer : layers) {
                    layerModelList.add(new LayerModel(layer, providerId));
                }

            } else {
                //get all layer from provider except exclude

                //Map providers and there layers
                ProviderReport provider = null;
                final ProvidersReport providersReport = serverV2.providers.listProviders();
                final List<ProviderServiceReport> servicesReport = providersReport.getProviderServices();
                for (final ProviderServiceReport serviceReport : servicesReport) {
                    if (serviceReport.getProvider(providerId) != null) {
                        provider = serviceReport.getProvider(providerId);
                    }
                }

                if (provider != null) {
                    final List<DataBrief> layers = provider.getItems();
                    for (final DataBrief layerName : layers) {
                        final QName layerQname = new QName(layerName.getName());
                        if (!source.isExcludedLayer(layerQname)) {
                            final Layer layer = new Layer(layerQname);
                            layerModelList.add(new LayerModel(layer, providerId));
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * Update GUI Outline layer model using the list of LayerModel.
     */
    private void updateLayerTableModel() {

        new Thread(){
            @Override
            public void run() {
                guiLoadLabel.setText(LayerRowModel.BUNDLE.getString("downloadingLayer"));
                guiLoadLabel.setIcon(IconBundle.getIcon("16_wait"));

                try {
                    final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                    final DefaultTreeModel treeModel = new org.geotoolkit.gui.swing.tree.DefaultTreeModel(root);
                    final RowModel model = new LayerRowModel();
               
                    for (final LayerModel layerModel : layerModelList) {
                        root.add(new DefaultMutableTreeNode(layerModel));
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            guiLayerTable.setRootVisible(false);
                            guiLayerTable.setModel(DefaultOutlineModel.createOutlineModel(treeModel, model));
                        }
                    });

                } finally{
                    guiLayerTable.repaint();
                    guiLoadLabel.setText("");
                    guiLoadLabel.setIcon(null);
                }
                guiLayerTable.setRenderDataProvider(new LayerRowRenderer());
            }
        }.start();

    }

    /**
     * Update GUI Outline layer model using the list of LayerModel.
     */
    private void updateSourceTableModel() {

        new Thread(){
            @Override
            public void run() {
                guiLoadLabel.setText(LayerRowModel.BUNDLE.getString("downloadingLayer"));
                guiLoadLabel.setIcon(IconBundle.getIcon("16_wait"));

                try {
                    final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                    final DefaultTreeModel treeModel = new org.geotoolkit.gui.swing.tree.DefaultTreeModel(root);
                    final RowModel model = new SourceRowModel();

                    for (final SourceModel sourceModel : sourceModelList) {
                        root.add(new DefaultMutableTreeNode(sourceModel));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            guiSourceTable.setRootVisible(false);
                            guiSourceTable.setModel(DefaultOutlineModel.createOutlineModel(treeModel, model));
                        }
                    });

                } finally{
                    guiSourceTable.repaint();
                    guiLoadLabel.setText("");
                    guiLoadLabel.setIcon(null);
                }
                guiSourceTable.setRenderDataProvider(new SourceRowRenderer());
            }
        }.start();

    }
    
    /**
     * Update layer defined in configuration using layerModelList.
     */
    private void updateConfiguration() {
        final List<Source> sources = new ArrayList<>();

        for (final SourceModel sourceModel : sourceModelList) {
            sources.add(new Source(sourceModel.getProviderId(), sourceModel.isLoadAll(), new ArrayList<Layer>(), null));
        }
        
        for (final LayerModel layerModel : layerModelList) {
            final String providerId = layerModel.getProviderId();
            
            Source src = getSourceFromId(sources, providerId);
            
            //create new source if not exist.
            if (src == null) {
                src = new Source(providerId, Boolean.FALSE, new ArrayList<Layer>(), null);
                sources.add(src);
            }
            
            // init include list if null
            if (src.getInclude() == null) {
                src.setInclude(new ArrayList<Layer>());
            }
            
            //add layer to source.
            src.getInclude().add(layerModel.getLayer());
        }
        
        //update configuration layers
        configuration.setLayers(sources);

        if (serviceType.equals("WFS")) {
            configuration.getCustomParameters().put("transactionnal", Boolean.toString(transactionnalBox.isSelected()));
        }
    }
    
    /**
     * Search a source in list from his identifier.
     * @param sources
     * @param providerId
     * @return source if found, null either.
     */
    private Source getSourceFromId(final List<Source> sources, final String providerId) {
        for (final Source source : sources) {
            if (source.getId().equals(providerId)) {
                return source;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public Object getConfiguration() {
        updateConfiguration();
        return configuration;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        guiLoadBar = new javax.swing.JToolBar();
        guiLoadLabel = new javax.swing.JLabel();
        guiLayerToolbar = new javax.swing.JToolBar();
        guiAddLayer = new javax.swing.JButton();
        guiAddSource = new javax.swing.JButton();
        transactionnalBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        guiLayerTable = new org.netbeans.swing.outline.Outline();
        jScrollPane2 = new javax.swing.JScrollPane();
        guiSourceTable = new org.netbeans.swing.outline.Outline();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(null);
        jPanel1.setLayout(new java.awt.BorderLayout());

        guiLoadBar.setFloatable(false);
        guiLoadBar.setRollover(true);
        guiLoadBar.add(guiLoadLabel);

        jPanel1.add(guiLoadBar, java.awt.BorderLayout.EAST);

        guiLayerToolbar.setFloatable(false);
        guiLayerToolbar.setRollover(true);

        guiAddLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/constellation/swing/edit_add.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        guiAddLayer.setText(bundle.getString("guiAddLayerBtn")); // NOI18N
        guiAddLayer.setFocusable(false);
        guiAddLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiAddLayerActionPerformed(evt);
            }
        });
        guiLayerToolbar.add(guiAddLayer);

        guiAddSource.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/constellation/swing/edit_add.png"))); // NOI18N
        guiAddSource.setText(org.openide.util.NbBundle.getMessage(JProviderEditPane.class, "guiAddSourceBtn")); // NOI18N
        guiAddSource.setFocusable(false);
        guiAddSource.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiAddSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiAddSourceActionPerformed(evt);
            }
        });
        guiLayerToolbar.add(guiAddSource);

        transactionnalBox.setText(org.openide.util.NbBundle.getMessage(JProviderMetadataPane.class, "transactionnal")); // NOI18N
        transactionnalBox.setFocusable(false);
        transactionnalBox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        guiLayerToolbar.add(transactionnalBox);

        jPanel1.add(guiLayerToolbar, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(2, 1, 5, 5));

        jScrollPane1.setViewportView(guiLayerTable);

        jPanel2.add(jScrollPane1);

        jScrollPane2.setViewportView(guiSourceTable);

        jPanel2.add(jScrollPane2);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void guiAddLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiAddLayerActionPerformed
        final  LayerModel layerModel = JEditLayerPane.showDialog(server, serverV2, serviceType, null);
        if (layerModel != null) {
            layerModelList.add(layerModel);
            updateLayerTableModel();
        }
    }//GEN-LAST:event_guiAddLayerActionPerformed

    private void guiAddSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiAddSourceActionPerformed
        final  SourceModel sourceModel = JEditSourcePane.showDialog(server, serverV2, serviceType, null);
        if (sourceModel != null) {
            sourceModelList.add(sourceModel);
            updateSourceTableModel();
        }
    }//GEN-LAST:event_guiAddSourceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiAddLayer;
    private javax.swing.JButton guiAddSource;
    private org.netbeans.swing.outline.Outline guiLayerTable;
    private javax.swing.JToolBar guiLayerToolbar;
    private javax.swing.JToolBar guiLoadBar;
    private javax.swing.JLabel guiLoadLabel;
    private org.netbeans.swing.outline.Outline guiSourceTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox transactionnalBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Rendering of layer row in Outline table.
     */
    private static class LayerRowRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            if(o instanceof DefaultMutableTreeNode){
                o = ((DefaultMutableTreeNode)o).getUserObject();
            }
            if (o instanceof LayerModel) {
                final LayerModel layer = (LayerModel) o;
                if (layer.getLayer() != null && layer.getLayer().getAlias() != null) {
                    return layer.getLayer().getAlias();
                } else {
                    return layer.getLayer().getName().getLocalPart();
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

    /**
     * Rendering of layer row in Outline table.
     */
    private static class SourceRowRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            if(o instanceof DefaultMutableTreeNode){
                o = ((DefaultMutableTreeNode)o).getUserObject();
            }
            if (o instanceof SourceModel) {
                final SourceModel source = (SourceModel) o;
                return source.getProviderId();
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
