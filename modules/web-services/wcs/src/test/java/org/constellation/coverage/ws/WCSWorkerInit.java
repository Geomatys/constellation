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
package org.constellation.coverage.ws;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;

import org.junit.AfterClass;

import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.test.utils.SpringTestRunner;
import org.junit.runner.RunWith;

/**
 * Initializes a {@link WCSWorker} for testing GetCapabilities, DescribeCoverage and GetCoverage
 * requests. Ensures that a PostGRID data preconfigured is handled by the {@link WCSWorker}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
@RunWith(SpringTestRunner.class)
public class WCSWorkerInit extends CoverageSQLTestCase {

    /**
     * The layer to test.
     */
    protected static final String LAYER_TEST = "SST_tests";

    protected static WCSWorker WORKER;

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
    public static boolean hasLocalDatabase() {
        return false; // TODO
    }

    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @PostConstruct
    public void setUpClass() {
        try {
            ConfigurationEngine.setupTestEnvironement("WCSWorkerInit");
            
            final LayerContext config = new LayerContext();
            config.getCustomParameters().put("shiroAccessible", "false");
            
            serviceBusiness.create("WCS", "default", config, null);
            layerBusiness.add("SST_tests", null, "coverageTestSrc", null, "default", "WCS");
            
            serviceBusiness.create("WCS", "test", config, null);
            layerBusiness.add("SST_tests", null, "coverageTestSrc", null, "test",    "WCS");
            
            final Configurator configurator = new AbstractConfigurator() {
                
                @Override
                public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                    final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                    
                    final ProviderFactory factory = DataProviders.getInstance().getFactory("coverage-sql");
                    
                    if (hasLocalDatabase()) {
                        final ParameterValueGroup config = factory.getProviderDescriptor().createValue();
                        // Defines a PostGrid data provider
                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                        srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://flupke.geomatys.com/coverages-test");
                        srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
                        final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                        srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                        srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
                        srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
                        srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("coverageTestSrc");
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("coverageTestSrc",config));
                    }
                    return lst;
                }
                
                @Override
                public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            DataProviders.getInstance().setConfigurator(configurator);
            
            
            WORKER = new DefaultWCSWorker("default");
            // Default instanciation of the worker' servlet context and uri context.
            WORKER.setServiceUrl("http://localhost:9090");
        } catch (ConfigurationException ex) {
            Logger.getLogger(WCSWorkerInit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WCSWorkerInit");
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }
}
