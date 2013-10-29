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
package org.constellation.process.provider.style;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.process.AbstractProcessTest;
import org.constellation.provider.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapStyleTest extends AbstractProcessTest {

    protected static File configDirectory;
    protected static URL EMPTY_CSV;
    // dataStore service
    protected static ProviderService SLD_SERVICE;

    static {
        final Collection<StyleProviderService> availableLayerServices = StyleProviderProxy.getInstance().getServices();
        for (StyleProviderService tmpService : availableLayerServices) {
            if ("sld".equals(tmpService.getName())) {
                SLD_SERVICE = tmpService;
            }
        }
    }

    public AbstractMapStyleTest(final String str) {
        super(str);
    }

    @BeforeClass
    public static void initFolder() throws MalformedURLException {

        configDirectory = ConfigurationEngine.setupTestEnvironement("AbstractMapStyleTest");

        File providerDirectory = new File(configDirectory, "provider");
        providerDirectory.mkdir();
        File sld = new File(providerDirectory, "sld.xml");
        File csv = new File(configDirectory, "file.csv");
        EMPTY_CSV = csv.toURI().toURL();

    }

    @AfterClass
    public static void destroyFolder() {
        ConfigurationEngine.shutdownTestEnvironement("ProcessProviderTest");
    }

    /**
     *
     *
     * @param sercice
     * @param providerID
     * @return
     * @throws MalformedURLException
     */
    protected static ParameterValueGroup buildProvider(final String providerID, final boolean loadAll) throws MalformedURLException {

        ParameterDescriptorGroup desc = SLD_SERVICE.getServiceDescriptor();

        if (desc != null) {
            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) desc.descriptor("source");
            final ParameterValueGroup sourceValue = sourceDesc.createValue();
            sourceValue.parameter("id").setValue(providerID);
            sourceValue.parameter("load_all").setValue(loadAll);

            final ParameterValueGroup sldFolderValue = sourceValue.groups("sldFolder").get(0);
            final File sldFolder = new File(configDirectory, "sldDir");
            sldFolder.mkdir();
            sldFolderValue.parameter("path").setValue(sldFolder.getAbsolutePath());
            return sourceValue;
        } else {
            //error
            return null;
        }
    }

    /**
     * Register a provider.
     * @param providerSource
     */
    protected static void addProvider(ParameterValueGroup providerSource) {
        StyleProviderProxy.getInstance().createProvider((StyleProviderService) SLD_SERVICE, providerSource);
    }

    /**
     * Un-register a provider
     * @param id
     */
    protected static void removeProvider(String id) {

        StyleProvider provider = null;
        for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
            if (p.getId().equals(id)) {
            }
        }
        StyleProviderProxy.getInstance().removeProvider(provider);
    }

}
