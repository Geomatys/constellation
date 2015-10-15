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

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.util.DataReference;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.opengis.filter.Filter;
import org.opengis.util.FactoryException;
import org.openide.util.Exceptions;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class JEditLayerPane extends javax.swing.JPanel {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.swing");

    private static final String EMPTY_ITEM = "empty";

    private LayerModel layerModel;

    /**
     * Creates new form JEditLayerPane
     * @param server
     * @param serviceType
     * @param layerModel
     */
    public JEditLayerPane(final ConstellationClient serverV2, final String serviceType, final LayerModel layerModel) {
        this.layerModel = layerModel;
        initComponents();

        guiCQLError.getParent().setVisible(false);

        //create combobox items (dataReference string)
        final List<String> providerList = new ArrayList<>();
        final List<DataReference> styleList = new ArrayList<>();
        styleList.add(null);

        try {
            final ProvidersReport providersReport = serverV2.providers.listProviders();
            final List<ProviderServiceReport> servicesReport = providersReport.getProviderServices();
            for(final ProviderServiceReport serviceReport : servicesReport) {
                final String serviceProviderType = serviceReport.getType();

                final List<ProviderReport> providers = serviceReport.getProviders();
                for (final ProviderReport providerReport : providers) {

                    final String providerID = providerReport.getId();
                    final List<DataBrief> layers = providerReport.getItems();
                    for (final DataBrief item : layers) {
                        final String fullName;
                        if (item.getNamespace() != null) {
                            fullName = '{' + item.getNamespace() + '}' + item.getName();
                        } else {
                            fullName = item.getName();
                        }
                        if ("sld".equals(serviceProviderType)) {
                            styleList.add(DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, providerID, fullName));
                        } else {
                            boolean addProviderToList = false;
                            //WFS -> data-store
                            if ( ("WFS".equals(serviceType) && "feature-store".equals(serviceProviderType)) ) {
                                addProviderToList = true;
                            }

                            //WMTS or WCS -> coverage-store
                            if ( ("WMTS".equals(serviceType) || "WCS".equals(serviceType) ) && "coverage-store".equals(serviceProviderType)) {
                                addProviderToList = true;
                            }

                            //WMS -> all layer provider
                            if ("WMS".equals(serviceType)) {
                                addProviderToList = true;
                            }

                            if (addProviderToList) {
                                providerList.add(DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, providerID, fullName).getReference());
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        guiLayerDataCBox.setModel(new ListComboBoxModel(providerList));
        Collections.sort(styleList);
        guiLayerStyleCBox.setModel(new ListComboBoxModel(styleList));
        final ListCellRenderer renderer = new CBoxRenderer();
        guiLayerStyleCBox.setRenderer(renderer);
        guiLayerDataCBox.setRenderer(renderer);

        //enable style choice only for WMS
        if (!"WMS".equals(serviceType)) {
            guiLayerStyleCBox.setEnabled(false);
            guiLayerStyleLabel.setEnabled(false);
            guiLayerStyleCBox.setPreferredSize(new Dimension(1, 1));
            guiLayerStyleLabel.setPreferredSize(new Dimension(1, 1));
            guiLayerStyleCBox.setVisible(false);
            guiLayerStyleLabel.setVisible(false);
        }

        if (layerModel != null) {
            final Layer layer = layerModel.getLayer();
            //init alias name
            if (layer.getAlias() != null) {
                guiLayerAliasText.setText(layer.getAlias());
            }

            //filter
            if (layer.getFilter() != null) {
                try {
                    final FilterType filterType = layer.getFilter();
                    final StyleXmlIO xmlUtils = new StyleXmlIO();
                    final Filter filter = xmlUtils.getTransformer110().visitFilter(filterType);
                    guiFilterArea.setText(CQL.write(filter));
                } catch (FactoryException ex) {
                    LOGGER.log(Level.INFO, ex.getMessage(),ex);
                }
            }

            //data
            final String providerId = layerModel.getProviderId();
            final QName layerName = layer.getName();
            final String dataRef = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, providerId, layerName.toString()).getReference();
            guiLayerDataCBox.setSelectedItem(dataRef);

            //style only the first !!! TODO handle a list of styles in GUI.
            if (!layer.getStyles().isEmpty()) {

                final DataReference styleRef = layer.getStyles().get(0);
                guiLayerStyleCBox.setSelectedItem(styleRef);
            }

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        guiLayerAliasLabel = new javax.swing.JLabel();
        guiLayerAliasText = new javax.swing.JTextField();
        guiLayerDataLabel = new javax.swing.JLabel();
        guiLayerDataCBox = new javax.swing.JComboBox();
        guiLayerStyleLabel = new javax.swing.JLabel();
        guiLayerFilterLabel = new javax.swing.JLabel();
        guiLayerStyleCBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        guiFilterArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        guiCQLError = new javax.swing.JTextArea();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        guiLayerAliasLabel.setText(bundle.getString("guiLayerAliasLabel")); // NOI18N

        guiLayerDataLabel.setText(bundle.getString("guiLayerDataLabel")); // NOI18N

        guiLayerStyleLabel.setText(bundle.getString("guiLayerStyleLabel")); // NOI18N

        guiLayerFilterLabel.setText(bundle.getString("guiLayerFilterLabel")); // NOI18N

        guiFilterArea.setColumns(20);
        guiFilterArea.setRows(5);
        jScrollPane1.setViewportView(guiFilterArea);

        guiCQLError.setEditable(false);
        guiCQLError.setColumns(20);
        guiCQLError.setLineWrap(true);
        guiCQLError.setRows(3);
        guiCQLError.setWrapStyleWord(true);
        jScrollPane2.setViewportView(guiCQLError);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(guiLayerAliasLabel)
                            .addComponent(guiLayerDataLabel)
                            .addComponent(guiLayerStyleLabel)
                            .addComponent(guiLayerFilterLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(guiLayerStyleCBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(guiLayerDataCBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(guiLayerAliasText, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiLayerAliasLabel)
                    .addComponent(guiLayerAliasText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiLayerDataLabel)
                    .addComponent(guiLayerDataCBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(guiLayerStyleCBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(guiLayerStyleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(guiLayerFilterLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea guiCQLError;
    private javax.swing.JTextArea guiFilterArea;
    private javax.swing.JLabel guiLayerAliasLabel;
    private javax.swing.JTextField guiLayerAliasText;
    private javax.swing.JComboBox guiLayerDataCBox;
    private javax.swing.JLabel guiLayerDataLabel;
    private javax.swing.JLabel guiLayerFilterLabel;
    private javax.swing.JComboBox guiLayerStyleCBox;
    private javax.swing.JLabel guiLayerStyleLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    public void setCQLError (final String error) {
        guiCQLError.setText(error);
        guiCQLError.getParent().setVisible(true);
    }

    public LayerModel getLayerEntry() throws CQLException {
        if (layerModel == null) {
            layerModel = new LayerModel(new Layer(), null);
        }

        //data
        final DataReference data = new DataReference((String) guiLayerDataCBox.getSelectedItem());
        final QName qname = new QName(data.getLayerId().getNamespaceURI(), data.getLayerId().getLocalPart());
        layerModel.getLayer().setName(qname);
        layerModel.setProviderId(data.getProviderOrServiceId());

        //alias
        final String alias = guiLayerAliasText.getText();
        if (alias != null && !alias.isEmpty()) {
            layerModel.getLayer().setAlias(alias);
        } else {
            layerModel.getLayer().setAlias(null);
        }

        //style
        final DataReference style = (DataReference) guiLayerStyleCBox.getSelectedItem();
        if (!EMPTY_ITEM.equals(style)) {
            if (layerModel.getLayer().getStyles() == null) {
                layerModel.getLayer().setStyles(new ArrayList<DataReference>());
            }
            if (!layerModel.getLayer().getStyles().contains(style)) {
                //TODO handle multi style with the default one.
                //remove old style and add the new one

                //currently new style replace the old one
                layerModel.getLayer().getStyles().clear();
                layerModel.getLayer().getStyles().add(style);
            }
        }

        //filter
        final String cql = guiFilterArea.getText();
        if (cql != null && !cql.trim().isEmpty()) {
            final Filter filter = CQL.parseFilter(cql);
            final StyleXmlIO xmlUtils = new StyleXmlIO();
            final FilterType filterType = xmlUtils.getTransformerXMLv110().visit(filter);
            layerModel.getLayer().setFilter(filterType);
        } else {
            layerModel.getLayer().setFilter(null);
        }

        return layerModel;
    }

    /**
     *
     * @param server
     * @param serviceType
     * @param layer
     * @return
     */
    public static LayerModel showDialog(final ConstellationClient serverV2, final String serviceType, final LayerModel layer){

        final JEditLayerPane pane = new JEditLayerPane(serverV2, serviceType, layer);

        int res = JOptionPane.showOptionDialog(null, new Object[]{pane},
                LayerRowModel.BUNDLE.getString("createLayerMsg"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                null);


        LayerModel layerModel = null;
        boolean cqlValid = false;
        while (!cqlValid) {
            if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
                break;
            }

            try {
                layerModel = pane.getLayerEntry();
                cqlValid = true;
            } catch (CQLException ex) {
                cqlValid = false;
                pane.setCQLError(ex.getMessage());
                res = JOptionPane.showOptionDialog(null, new Object[]{pane},
                        LayerRowModel.BUNDLE.getString("createLayerMsg"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                        null);
            }
        }

        return layerModel;
    }

    /**
     * Combobox renderer.
     */
    private class CBoxRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String) {
                final String val = (String) value;
                if (!val.trim().isEmpty() && !val.equals(EMPTY_ITEM)) {
                    final DataReference data = new DataReference((String) val);
                    label.setText(data.getProviderOrServiceId() + " - " +data.getLayerId().getLocalPart());
                } else {
                    label.setText(" ----- ");
                }
            }
            return label;
        }

    }
}
