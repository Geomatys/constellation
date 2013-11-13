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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.namespace.QName;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Instance;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.Source;
import org.geotoolkit.gui.swing.misc.ActionCell;
import org.geotoolkit.gui.swing.resource.IconBundle;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class JServiceMapEditPane extends JServiceEditionPane {

    //icones
    private static final ImageIcon ICON_EDIT = new ImageIcon(JServiceMapEditPane.class.getResource("/org/constellation/swing/edit.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(JServiceMapEditPane.class.getResource("/org/constellation/swing/edit_remove.png"));
    
    private ConstellationServer server;
    private String serviceType;
    private Instance serviceInstance;
    private LayerContext configuration;
    private List<LayerModel> layerModelList;
    
    /**
     * Creates new form JServiceMapEditPane
     */
    public JServiceMapEditPane(final ConstellationServer server, final String serviceType, final Instance serviceInstance, final Object configuration) {
        this.server = server;
        this.serviceType = serviceType;
        this.serviceInstance = serviceInstance;
        this.configuration = (configuration instanceof LayerContext) ? (LayerContext) configuration : null;
        initComponents();
        
        guiLayerTable.setDefaultRenderer(LayerRowModel.EditLayer.class, new ActionCell.Renderer(ICON_EDIT));
        guiLayerTable.setDefaultEditor(LayerRowModel.EditLayer.class, new ActionCell.Editor(ICON_EDIT) {
            @Override
            public void actionPerformed(final ActionEvent e, Object value) {
                if(value instanceof DefaultMutableTreeNode){
                    value = ((DefaultMutableTreeNode)value).getUserObject();
                }
                
                if (value instanceof LayerModel) {
                    final LayerModel oldLayerModel = (LayerModel) value;
                    final  LayerModel updateLayerModel = JEditLayerPane.showDialog(server, configuration, serviceType, oldLayerModel);
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
        
        initLayerList();
        
        updateLayerTableModel();
    }

    /**
     * Create a list of layer model based on service configuration.
     */
    private void initLayerList() {
        layerModelList = new ArrayList<LayerModel>();
        final List<Source> sources = configuration.getLayers();

        for (final Source source : sources) {
            final String providerId = source.getId();

            if (!source.getLoadAll()) {
                final List<Layer> layers = source.getInclude();
                for (final Layer layer : layers) {
                    layerModelList.add(new LayerModel(layer, providerId));
                }

            } else {
                //get all layer from provider exept exclude

                //Map providers and there layers
                ProviderReport provider = null;
                final ProvidersReport providersReport = server.providers.listProviders();
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
     * Update layer defined in configuration using layerModelList.
     */
    private void updateConfiguration() {
        final List<Source> sources = new ArrayList<Source>();
        
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
        jScrollPane1 = new javax.swing.JScrollPane();
        guiLayerTable = new org.netbeans.swing.outline.Outline();

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

        jPanel1.add(guiLayerToolbar, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(guiLayerTable);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void guiAddLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiAddLayerActionPerformed
        
        final  LayerModel layerModel = JEditLayerPane.showDialog(server, configuration, serviceType, null);
        if (layerModel != null) {
            layerModelList.add(layerModel);
            updateLayerTableModel();
        }
    }//GEN-LAST:event_guiAddLayerActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton guiAddLayer;
    private org.netbeans.swing.outline.Outline guiLayerTable;
    private javax.swing.JToolBar guiLayerToolbar;
    private javax.swing.JToolBar guiLoadBar;
    private javax.swing.JLabel guiLoadLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
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
}
