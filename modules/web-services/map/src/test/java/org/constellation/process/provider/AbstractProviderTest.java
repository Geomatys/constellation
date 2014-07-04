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
package org.constellation.process.provider;


import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.AbstractProcessTest;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractProviderTest extends AbstractProcessTest {

    protected static URL EMPTY_CSV;
    // dataStore service
    protected static ProviderFactory DATASTORE_SERVICE;

    protected AbstractProviderTest(final String processName) {
        super(processName);
    }
    
    @PostConstruct
    public void fillDatastoreService() {
        final Collection<DataProviderFactory> availableLayerServices = DataProviders.getInstance().getFactories();
        for (DataProviderFactory tmpService: availableLayerServices) {
            if ("feature-store".equals(tmpService.getName())) {
                DATASTORE_SERVICE = tmpService;
            }
        }
    }

    @BeforeClass
    public static void initFolder() throws MalformedURLException {

        final File configDirectory = ConfigurationEngine.setupTestEnvironement("ProcessProviderTest");
        final File providerDirectory = new File(configDirectory, "provider");
        providerDirectory.mkdir();

        File csv = new File(configDirectory, "file.csv");
        EMPTY_CSV = csv.toURI().toURL();

    }

    @AfterClass
    public static void destroyFolder() {
        ConfigurationEngine.shutdownTestEnvironement("ProcessProviderTest");
    }

    /**
     * Create a CSV provider for test purpose.
     * @param sercice
     * @param providerID
     * @return
     * @throws MalformedURLException
     */
    protected static ParameterValueGroup buildCSVProvider(final ProviderFactory sercice, final String providerID, final boolean loadAll,
            final URL url) throws MalformedURLException {

        ParameterDescriptorGroup desc = sercice.getProviderDescriptor();

        if (desc != null) {
            final ParameterDescriptorGroup sourceDesc = desc;
            final ParameterValueGroup sourceValue = sourceDesc.createValue();
            sourceValue.parameter("id").setValue(providerID);
            sourceValue.parameter("load_all").setValue(loadAll);

            final ParameterValueGroup choiceValue = sourceValue.groups("choice").get(0);
            final ParameterValueGroup csvValue = (ParameterValueGroup) choiceValue.addGroup("CSVParameters");
            csvValue.parameter("identifier").setValue("csv");
            csvValue.parameter("url").setValue(url);
            csvValue.parameter("namespace").setValue(null);
            csvValue.parameter("separator").setValue(new Character(';'));

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
        DataProviders.getInstance().createProvider(id, (DataProviderFactory) DATASTORE_SERVICE, providerSource);
    }

    /**
     * Un-register a provider
     * @param id
     */
    protected static void removeProvider(String id) throws ConfigurationException {

        DataProvider provider = null;
        for (DataProvider p : DataProviders.getInstance().getProviders()) {
            if (p.getId().equals(id)) {
                provider = p;
            }
        }
        DataProviders.getInstance().removeProvider(provider);
    }
}
