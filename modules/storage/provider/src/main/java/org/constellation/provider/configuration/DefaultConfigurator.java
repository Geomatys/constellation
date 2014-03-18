/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2014, Geomatys
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

package org.constellation.provider.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.Data;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.w3c.dom.Node;

/**
 *
 * @author husky
 */
public final class DefaultConfigurator implements Configurator {

    DefaultConfigurator() {
    }

    @Override
    public ParameterValueGroup getConfiguration(final ProviderService service) {
        return ConfigurationEngine.getProviderConfiguration(service.getName(), service.getServiceDescriptor());
    }

    @Override
    public synchronized void saveConfiguration(final ProviderService service, final List<Provider> providers) {
        final ParameterValueGroup params = service.getServiceDescriptor().createValue();
        final String serviceName = service.getName();
        // Update administration database.
        try {
            // look for deleted providers
            final List<ProviderRecord> records = ConfigurationEngine.getProviders(serviceName);
            for (ProviderRecord record : records) {
                boolean remove = true;
                for (Provider provider : providers) {
                    if (record.getIdentifier().equals(provider.getId())) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    ConfigurationEngine.deleteProvider(record.getIdentifier());
                }
            }
            // look for new / updated providers
            for (Provider provider : providers) {
                params.values().add(provider.getSource());
                final ProviderRecord.ProviderType type = provider.getProviderType();
                ProviderRecord pr = ConfigurationEngine.getProvider(provider.getId());
                if (pr == null) {
                    pr = ConfigurationEngine.writeProvider(provider.getId(), type, serviceName, provider.getSource());
                } else {
                    // update
                    pr.setConfig(provider.getSource());
                }
                final List<DataRecord> list = pr.getData();
                if (type == ProviderRecord.ProviderType.LAYER) {
                    // Remove no longer existing layer.
                    final Map<String, InputStream> metadata = new HashMap<>(0);
                    for (final DataRecord data : list) {
                        boolean found = false;
                        for (final Object keyObj : provider.getKeys()) {
                            final Name key = (Name) keyObj;
                            if (data.getName().equals(key.getLocalPart())) {
                                found = true;
                                break;
                            } else if (key.getLocalPart().contains(data.getName()) && data.getProvider().getIdentifier().equalsIgnoreCase(provider.getId())) {
                                //save metadata
                                metadata.put(key.getLocalPart(), data.getMetadata());
                            }
                        }
                        if (!found) {
                            ConfigurationEngine.deleteData(data.getCompleteName(), provider.getId());
                        }
                    }
                    // Add new layer.
                    for (final Object keyObj : provider.getKeys()) {
                        final Name key = (Name) keyObj;
                        final QName name = new QName(key.getNamespaceURI(), key.getLocalPart());
                        boolean found = false;
                        for (final DataRecord data : list) {
                            if (name.equals(data.getCompleteName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            DataRecord record = ConfigurationEngine.writeData(name, pr, provider.getDataType());
                            final InputStream currentMetadata = metadata.get(record.getName());
                            if (currentMetadata != null) {
                                StringWriter writer = new StringWriter();
                                IOUtils.copy(currentMetadata, writer);
                                StringReader reader = new StringReader(writer.toString());
                                record.setMetadata(reader);
                            } else {
                                final Data layer = (Data) provider.get(new DefaultName(name));
                                final Object origin = layer.getOrigin();
                                if (origin instanceof CoverageReference) {
                                    final CoverageReference fcr = (CoverageReference) origin;
                                    try {
                                        int i = fcr.getImageIndex();
                                        final GridCoverageReader reader = fcr.acquireReader();
                                        final SpatialMetadata sm = reader.getCoverageMetadata(i);
                                        if (sm != null) {
                                            final String rootNodeName = sm.getNativeMetadataFormatName();
                                            final Node coverageRootNode = sm.getAsTree(rootNodeName);
                                            MetadataMapBuilder.setCounter(0);
                                            final List<SimplyMetadataTreeNode> coverageMetadataList = MetadataMapBuilder.createSpatialMetadataList(coverageRootNode, null, 11, i);
                                            final CoverageMetadataBean coverageMetadataBean = new CoverageMetadataBean(coverageMetadataList);
                                            final MarshallerPool mp = GenericDatabaseMarshallerPool.getInstance();
                                            final Marshaller marshaller = mp.acquireMarshaller();
                                            final StringWriter sw = new StringWriter();
                                            marshaller.marshal(coverageMetadataBean, sw);
                                            mp.recycle(marshaller);
                                            final StringReader sr = new StringReader(sw.toString());
                                            record.setMetadata(sr);
                                        }
                                    } catch (CoverageStoreException | JAXBException e) {
                                        LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    final List<StyleRecord> styles = pr.getStyles();
                    // Remove no longer existing style.
                    for (final StyleRecord style : styles) {
                        if (provider.get(style.getName()) == null) {
                            ConfigurationEngine.deleteStyle(style.getName(), provider.getId());
                        }
                    }
                    // Add not registered new data.
                    for (final Object key : provider.getKeys()) {
                        boolean found = false;
                        for (final StyleRecord style : styles) {
                            if (key.equals(style.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            StyleRecord.StyleType styleType = StyleRecord.StyleType.VECTOR;
                            MutableStyle style = (MutableStyle) provider.get(key);
                            fts:
                            for (MutableFeatureTypeStyle mutableFeatureTypeStyle : style.featureTypeStyles()) {
                                for (MutableRule mutableRule : mutableFeatureTypeStyle.rules()) {
                                    for (Symbolizer symbolizer : mutableRule.symbolizers()) {
                                        if (symbolizer instanceof RasterSymbolizer) {
                                            styleType = StyleRecord.StyleType.COVERAGE;
                                            break fts;
                                        }
                                    }
                                }
                            }
                            ConfigurationEngine.writeStyle((String) key, pr, styleType, style);
                        }
                    }
                }
            }
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating provider database", ex);
        }
    }
    
}
