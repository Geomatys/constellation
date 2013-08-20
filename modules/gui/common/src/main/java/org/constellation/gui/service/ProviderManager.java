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

import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.gui.service.bean.LayerData;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.net.MalformedURLException;
import java.net.URL;
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
     * constellation server URL
     */
    private String constellationUrl;

    /**
     * constellation server user login
     */
    private String login;

    /**
     * constellation server user password
     */
    private String password;


    public ProviderManager() {
    }

    public void setConstellationUrl(String constellationUrl) {
        this.constellationUrl = constellationUrl;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @param type
     * @param fileIdentifier
     * @param path
     */
    public void createProvider(final String type, final String fileIdentifier, final String path) {
        try {
            final URL serverUrl = new URL(constellationUrl);
            final ConstellationServer cs = new ConstellationServer(serverUrl, login, password);
            final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup) cs.providers.getServiceDescriptor(type);
            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
            final ParameterValueGroup sources = sourceDesc.createValue();
            sources.parameter("id").setValue(fileIdentifier);

            final String folderPath = path.substring(0, path.lastIndexOf('/'));

            switch (type) {
                case "coverage-file":
                    sources.groups("coveragefile").get(0).parameter("path").setValue(folderPath);
                    break;
                case "sld":
                    sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                    break;
                default:
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "Provider type not known");
                    }
            }

            cs.providers.createProvider(type, sources);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "", e);
        }
    }

    /**
     *
     * @return
     * @param userLocale
     */
    public List<LayerData> getDataListing(final Locale userLocale){
        final List<LayerData> layerDatas = new ArrayList<>(0);
        try {
            final URL serverUrl = new URL(constellationUrl);
            final ConstellationServer cs = new ConstellationServer(serverUrl, login, password);
            final ProvidersReport report = cs.providers.listProviders();

            for (ProviderServiceReport providerServiceReport : report.getProviderServices()) {
                for (ProviderReport providerReport : providerServiceReport.getProviders()) {
                    String type;
                    switch (providerReport.getType()){
                        case "feature-store":
                            type = "vector";
                            break;
                        case "coverage-file":
                            type = "raster";
                            break;
                        default:
                            type = null;

                    }

                    if (type != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY X");
                        Date createDate = new Date();
                        try {
                            createDate = dateFormat.parse(providerReport.getDate());
                        } catch (ParseException e) {
                            LOGGER.log(Level.WARNING, "", e);
                        }

                        dateFormat = new SimpleDateFormat("dd-MM-YYYY", userLocale);
                        String date = dateFormat.format(createDate);

                        for (String name : providerReport.getItems()) {
                            int rightBracket = name.indexOf('}')+1;
                            name = name.substring(rightBracket);
                            LayerData layerData = new LayerData(providerReport.getId(), type, name, date);
                            layerDatas.add(layerData);
                        }
                    }
                }
            }
            return layerDatas;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "URL malformed", e);
        }
        return layerDatas;
    }
}
