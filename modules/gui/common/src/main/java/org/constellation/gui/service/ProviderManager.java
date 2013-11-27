/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.service;

import org.constellation.ServiceDef.Specification;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.dto.AddLayer;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.Database;
import org.constellation.dto.FileBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.gui.service.bean.LayerData;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 * Juzu service to call constellation services server side about providers
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class ProviderManager {

    private static final Logger LOGGER = Logger.getLogger(ProviderManager.class.getName());
    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * @param type
     * @param identifier
     * @param path
     * @param database
     */
    public void createProvider(final String type, final String identifier, final String path, final String dataType, final Database database, final String subType) {
        final ConstellationServer cs = cstl.openServer(true);

        final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup) cs.providers.getServiceDescriptor(type);
        final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
        final ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(identifier);
        sources.parameter("providerType").setValue(dataType);
        String folderPath;

        switch (type) {
            case "sld":
                folderPath = path.substring(0, path.lastIndexOf('/'));
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;
            case "feature-store":
                final URL url;

                if (path != null) {
                    try {
                        url = new URL("file:" + path);
                        ParameterValueGroup shapeFileParametersFolder = sources.groups("choice").get(0).addGroup("ShapeFileParametersFolder");
                        shapeFileParametersFolder.parameter("url").setValue(url);
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.WARNING, "", e);
                    }
                } else {
                    //database connection
                    ParameterValueGroup postGresParametersFolder = sources.groups("choice").get(0).addGroup("PostgresParameters");
                    int port = Integer.parseInt(database.getPort());
                    postGresParametersFolder.parameter("identifier").setValue("postgresql");
                    postGresParametersFolder.parameter("host").setValue(database.getHost());
                    postGresParametersFolder.parameter("port").setValue(port);
                    postGresParametersFolder.parameter("user").setValue(database.getLogin());
                    postGresParametersFolder.parameter("password").setValue(database.getPassword());
                    postGresParametersFolder.parameter("database").setValue(database.getName());
                    postGresParametersFolder.parameter("simple types").setValue(true);
                }
                break;
            case "coverage-store":
                URL fileUrl = null;

                switch (subType) {
                    case "coverage-xml-pyramid":
                        try {
                            fileUrl = URI.create(path).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unnable to create url from path", e);
                        }
                        ParameterValueGroup xmlCoverageStoreParameters = sources.groups("choice").get(0).addGroup("XMLCoverageStoreParameters");
                        xmlCoverageStoreParameters.parameter("identifier").setValue("coverage-xml-pyramid");
                        xmlCoverageStoreParameters.parameter("path").setValue(fileUrl);
                        xmlCoverageStoreParameters.parameter("type").setValue("AUTO");
                        break;
                    case "coverage-file":
                        try {
                            fileUrl = URI.create("file:"+path).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unnable to create url from path", e);
                        }

                        ParameterValueGroup fileCoverageStoreParameters = sources.groups("choice").get(0).addGroup("FileCoverageStoreParameters");
                        fileCoverageStoreParameters.parameter("identifier").setValue("coverage-file");
                        fileCoverageStoreParameters.parameter("path").setValue(fileUrl);
                        fileCoverageStoreParameters.parameter("type").setValue("AUTO");
                        break;
                    case "pgraster":
                        ParameterValueGroup postGresParametersFolder = sources.groups("choice").get(0).addGroup("PGRasterParameters");
                        int port = Integer.parseInt(database.getPort());

                        postGresParametersFolder.parameter("identifier").setValue("postgresql");
                        postGresParametersFolder.parameter("host").setValue(database.getHost());
                        postGresParametersFolder.parameter("port").setValue(port);
                        postGresParametersFolder.parameter("user").setValue(database.getLogin());
                        postGresParametersFolder.parameter("password").setValue(database.getPassword());
                        postGresParametersFolder.parameter("database").setValue(database.getName());
                        postGresParametersFolder.parameter("simple types").setValue(true);
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "error on subtype definition");
                }
            default:
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Provider type not known");
                }
        }
        cs.providers.createProvider(type, sources);
    }

    /**
     * @param providerTypes
     * @return
     */
    public List<LayerData> getDataListing(final List<String> providerTypes) {
        final List<LayerData> layerDatas = new ArrayList<>(0);

        final ProvidersReport report = cstl.openServer(true).providers.listProviders();

        for (ProviderServiceReport providerServiceReport : report.getProviderServices()) {
            for (ProviderReport providerReport : providerServiceReport.getProviders()) {
                String type = providerReport.getAbstractType();

                if (providerTypes==null || providerTypes.contains(type)) {
                    for (DataBrief dataBrief : providerReport.getItems()) {
                        String name = dataBrief.getName();
                        int rightBracket = name.indexOf('}') + 1;
                        name = name.substring(rightBracket);
                        LayerData layerData = new LayerData(providerReport.getId(), type, name, dataBrief.getDate(), dataBrief.getOwner());
                        layerDatas.add(layerData);
                    }
                }
            }
        }
        return layerDatas;
    }

    public void addLayer(final AddLayer toAddLayer) {
        try {
            cstl.openClient().services.addLayer(Specification.fromShortName(toAddLayer.getServiceType()),
                    toAddLayer.getServiceId(), toAddLayer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when try to add layer on service", e);
        }
    }

    /**
     * ask server to a specific folder
     *
     * @param path folder search; if "/", it will be root from data_directory configuration.
     * @return folder child list name
     */
    public List<FileBean> getDataFolder(final String path) {
        try {
            return cstl.openClient().providers.getDataFolder(path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when try to get subfolder list", e);
        }
        return new ArrayList<>(0);
    }

    public DataInformation loadData(final String filePath, final String dataType) {
        try {
            return cstl.openClient().providers.loadData(filePath, dataType);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when try to found data on file", e);
        }
        return new DataInformation();
    }

    public MetadataLists getMetadataCodeLists(final String locale) {
        try {
            return cstl.openClient().providers.getMetadataCodeLists(locale);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "MetadataCodeList service isn't accessible", e);
        }
        return null;
    }

    public void saveISO19115Metadata(final DataMetadata metadataToSave) {
        try {
            cstl.openClient().providers.saveISO19115Metadata(metadataToSave);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to access to service to save metadata", e);
        }
    }

    public void pyramidData(final String name, final String path) {
        try {
            cstl.openClient().providers.pyramidData(name, path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when ask pyramidal data ", e);
        }
    }

    public String getPyramidPath(final String providerName) {
        try {
            return cstl.openClient().providers.getPyramidPath(providerName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to know pyramid data path", e);
        }
        return null;
    }

    public ParameterValues getCoverageList(final String providerId) {
        try {
            return cstl.openClient().providers.getCoverageList(providerId);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to find coverage list", e);
        }
        return null;
    }

    public void saveCRSModifications(final Map<String, String> dataCRSModified, final String providerId) {
        dataCRSModified.put("providerId", providerId);
        final ParameterValues values = new ParameterValues(dataCRSModified);
        try {
            cstl.openClient().providers.saveCRSModification(values);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to save CRS modifications", e);
        }
    }

    public DataBrief getDataSummary(final QName name, final String providerId) {
        try {
            return cstl.openClient().providers.getDataSummary(name, providerId);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to access to data summary", e);
        }
        return null;
    }

    public DataBrief getLayerSummary(final String layerAlias, final String providerId) {
        try {
            return cstl.openClient().providers.getLayerSummary(layerAlias, providerId);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to access to data summary", e);
        }
        return null;
    }

    public DataInformation getMetadata(final String providerId, final String dataId, final String dataType) {
        try {
            return cstl.openClient().providers.getMetadata(providerId, dataId, dataType);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when call web service to get metadata from a layer", e);
        }
        return null;
    }
}
