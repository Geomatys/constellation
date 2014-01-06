/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import org.apache.commons.io.IOUtils;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.constellation.admin.dao.Session;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.admin.dao.StyleRecord.StyleType;
import org.constellation.admin.dao.UserRecord;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.security.NoSecurityManagerException;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Johann Sorel (Geomatys)
 */
public interface Configurator {

    public static final Logger LOGGER = Logging.getLogger(Configurator.class);

    public static final Configurator DEFAULT = new DefaultConfigurator();

    ParameterValueGroup getConfiguration(final ProviderService service);

    void saveConfiguration(final ProviderService service, final List<Provider> providers);

    static final class DefaultConfigurator implements Configurator {

        private DefaultConfigurator() {
        }

        @Override
        public ParameterValueGroup getConfiguration(final ProviderService service) {
            return ConfigurationEngine.getProviderConfiguration(service.getName(), service.getServiceDescriptor());
        }

        @Override
        public void saveConfiguration(final ProviderService service, final List<Provider> providers) {
            final ParameterValueGroup params = service.getServiceDescriptor().createValue();
            final String serviceName = service.getName();


            // Update administration database.
            Session session = null;
            try {
                session = EmbeddedDatabase.createSession();
                final UserRecord userRecord;
                String login = null;
                try {
                    login = SecurityManagerHolder.getInstance().getCurrentUserLogin();
                } catch (NoSecurityManagerException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage());
                }

                if (login != null) {
                    userRecord = session.readUser(login);
                } else {
                    userRecord = session.readUser("admin");
                }

                // look for deleted providers
                final List<ProviderRecord> records = session.readProviders(serviceName);
                for (ProviderRecord record : records) {
                    boolean remove = true;
                    for (Provider provider : providers) {
                        if (record.getIdentifier().equals(provider.getId())) {
                            remove = false;
                            break;
                        }
                    }
                    if (remove) {
                        session.deleteProvider(record.getIdentifier());
                    }
                }

                // look for new / updated providers
                for (Provider provider : providers) {
                    params.values().add(provider.getSource());

                    final ProviderType type = provider.getProviderType();
                    ProviderRecord pr = session.readProvider(provider.getId());
                    if (pr == null) {
                        pr = session.writeProvider(provider.getId(), type, serviceName, provider.getSource(), userRecord);
                    } else {
                        // update
                        pr.setConfig(provider.getSource());
                    }
                    final List<DataRecord> list = pr.getData();

                    if (type == ProviderType.LAYER) {
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
                                session.deleteData(data.getCompleteName(), provider.getId());
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
                                DataRecord record = session.writeData(name, pr, provider.getDataType(), userRecord);
                                final InputStream currentMetadata = metadata.get(record.getName());
                                if (currentMetadata != null) {
                                    StringWriter writer = new StringWriter();
                                    IOUtils.copy(currentMetadata, writer);
                                    StringReader reader = new StringReader(writer.toString());
                                    record.setMetadata(reader);
                                } else {

                                    final LayerDetails layer = (LayerDetails) provider.get(new DefaultName(name));
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
                                session.deleteStyle(style.getName(), provider.getId());
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
                                session.writeStyle((String) key, pr, StyleType.VECTOR, (MutableStyle) provider.get(key), userRecord);
                            }
                        }
                    }
                }
            } catch (IOException | SQLException ex) {
                LOGGER.log(Level.WARNING, "An error occurred while updating provider database", ex);
            } finally {
                if (session != null) session.close();
            }
        }
    }
}