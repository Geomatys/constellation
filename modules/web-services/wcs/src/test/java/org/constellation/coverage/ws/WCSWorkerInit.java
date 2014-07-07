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

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.LayerContext;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.utils.TestDatabaseHandler;
import org.junit.AfterClass;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.COVERAGESQL_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.NAMESPACE_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.PASSWORD_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.ROOT_DIRECTORY_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.SCHEMA_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.URL_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.USER_DESCRIPTOR;


/**
 * Initializes a {@link WCSWorker} for testing GetCapabilities, DescribeCoverage and GetCoverage
 * requests. Ensures that a PostGRID data preconfigured is handled by the {@link WCSWorker}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
public class WCSWorkerInit extends CoverageSQLTestCase implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(WCSWorkerInit.class);
    
    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * The layer to test.
     */
    protected static final String LAYER_TEST = "SST_tests";

    protected static WCSWorker WORKER;

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
    @Inject
    protected ProviderBusiness providerBusiness;
    
    @Inject
    protected DataBusiness dataBusiness;
    
    private static boolean initialized = false;
    
    protected static boolean localdb_active = true;
    
    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                ConfigurationEngine.setupTestEnvironement("WCSWorkerInit");
                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();

                // coverage-sql datastore
                localdb_active = TestDatabaseHandler.hasLocalDatabase();
                if (localdb_active) {
                    final String rootDir                = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    final ProviderFactory factory       = DataProviders.getInstance().getFactory("coverage-sql");
                    final ParameterValueGroup source    = factory.getProviderDescriptor().createValue();
                    final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                    
                    srcconfig.parameter(URL_DESCRIPTOR           .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("coverage_db_url"));
                    srcconfig.parameter(PASSWORD_DESCRIPTOR      .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("coverage_db_pass"));
                    srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                    srcconfig.parameter(USER_DESCRIPTOR          .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("coverage_db_user"));
                    srcconfig.parameter(SCHEMA_DESCRIPTOR        .getName().getCode()).setValue(TestDatabaseHandler.testProperties.getProperty("coverage_db_schema"));
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR     .getName().getCode()).setValue("no namespace");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR   .getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR        .getName().getCode()).setValue("coverageTestSrc");
                    providerBusiness.createProvider("coverageTestSrc", null, ProviderRecord.ProviderType.LAYER, "coverage-sql", source);

                    dataBusiness.create(new QName("SST_tests"), "coverageTestSrc", rootDir, false, true, null, null);
                } else {
                    LOGGER.log(Level.WARNING, "-- SOME TEST WILL BE SKIPPED BECAUSE THE LOCAL DATABASE IS MISSING --");
                }

                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wcs", "default", config, null, null);
                if (localdb_active) layerBusiness.add("SST_tests", null, "coverageTestSrc", null, "default", "wcs", null);

                serviceBusiness.create("wcs", "test", config, null, null);
                if (localdb_active) layerBusiness.add("SST_tests", null, "coverageTestSrc", null, "test",    "wcs", null);

                DataProviders.getInstance().reload();
                
                WORKER = new DefaultWCSWorker("default");
                // Default instanciation of the worker' servlet context and uri context.
                WORKER.setServiceUrl("http://localhost:9090");
                initialized = true;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WCSWorkerInit");
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }
}
