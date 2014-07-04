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
package org.constellation.process.provider.style;

import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.AbstractProcessTest;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderFactory;
import org.constellation.provider.StyleProviders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapStyleTest extends AbstractProcessTest {

    protected static File configDirectory;
    protected static URL EMPTY_CSV;
    // dataStore service
    protected static ProviderFactory SLD_SERVICE;

    static {
        final Collection<StyleProviderFactory> availableLayerServices = StyleProviders.getInstance().getFactories();
        for (StyleProviderFactory tmpService : availableLayerServices) {
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
        ConfigurationEngine.shutdownTestEnvironement("AbstractMapStyleTest");
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

        ParameterDescriptorGroup desc = SLD_SERVICE.getProviderDescriptor();

        if (desc != null) {
            final ParameterDescriptorGroup sourceDesc = desc;
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
    protected static void addProvider(String id,ParameterValueGroup providerSource) throws ConfigurationException {
        StyleProviders.getInstance().createProvider(id, (StyleProviderFactory) SLD_SERVICE, providerSource);
    }

    /**
     * Un-register a provider
     * @param id
     */
    protected static void removeProvider(String id) throws ConfigurationException {

        StyleProvider provider = null;
        for (StyleProvider p : StyleProviders.getInstance().getProviders()) {
            if (p.getId().equals(id)) {
                provider = p;
            }
        }
        StyleProviders.getInstance().removeProvider(provider);
    }

}
