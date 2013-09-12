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
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.dto.AddLayer;
import org.constellation.dto.DataInformation;
import org.constellation.dto.FileBean;
import org.constellation.gui.service.bean.LayerData;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Juzu service to call constellation services server side
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
     * @param fileIdentifier
     * @param path
     */
    public void createProvider(final String type, final String fileIdentifier, final String path, final String dataType) {
        final ConstellationServer cs = cstl.openServer(true);

        final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup) cs.providers.getServiceDescriptor(type);
        final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
        final ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(fileIdentifier);

        final String folderPath = path.substring(0, path.lastIndexOf('/'));
        sources.parameter("providerType").setValue(dataType);
        switch (type) {
            case "coverage-file":
                sources.groups("coveragefile").get(0).parameter("path").setValue(folderPath);
                break;
            case "sld":
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;
            case "feature-store":
                final URL url;
                try {
                    url = new URL("file:" + path);
                    ParameterValueGroup shapeFileParametersFolder = sources.groups("choice").get(0).addGroup("ShapeFileParametersFolder");
                    shapeFileParametersFolder.parameter("url").setValue(url);
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "", e);
                }
                break;
            default:
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Provider type not known");
                }
        }
        cs.providers.createProvider(type, sources);
    }

    /**
     * @param userLocale
     * @param providerTypes
     * @return
     */
    public List<LayerData> getDataListing(final Locale userLocale, final List<String> providerTypes) {
        final List<LayerData> layerDatas = new ArrayList<>(0);

        final ProvidersReport report = cstl.openServer(true).providers.listProviders();

        for (ProviderServiceReport providerServiceReport : report.getProviderServices()) {
            for (ProviderReport providerReport : providerServiceReport.getProviders()) {
                String type = providerReport.getAbstractType();

                if (providerTypes.contains(type)) {
                    String date = "";

                    if (providerReport.getDate() != null) {
                        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy X");
                        Date createDate = new Date();
                        try {
                            createDate = dateFormat.parse(providerReport.getDate());
                        } catch (ParseException e) {
                            LOGGER.log(Level.WARNING, "", e);
                        }

                        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, userLocale);
                        date = dateFormat.format(createDate);
                    }

                    for (String name : providerReport.getItems()) {
                        int rightBracket = name.indexOf('}') + 1;
                        name = name.substring(rightBracket);
                        LayerData layerData = new LayerData(providerReport.getId(), type, name, date);
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

    public DataInformation loadData(final String filePath, final String name, final String dataType) {
        try {
            return cstl.openClient().providers.loadData(filePath, name, dataType);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when try to found data on file", e);
        }
        return new DataInformation();
    }
}
