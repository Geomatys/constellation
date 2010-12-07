/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.coverage.ws;

import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.Layers;
import org.constellation.configuration.LayerContext;
import java.util.Arrays;
import org.constellation.configuration.Source;
import org.geotoolkit.util.FileUtilities;
import java.io.File;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.coveragesql.CoverageSQLProvider;
import org.constellation.provider.coveragesql.CoverageSQLProviderService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assume.*;


/**
 * Initializes a {@link WCSWorker} for testing GetCapabilities, DescribeCoverage and GetCoverage
 * requests. Ensures that a PostGRID data preconfigured is handled by the {@link WCSWorker}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
public class WCSWorkerInit extends CoverageSQLTestCase {

    /**
     * The layer to test.
     */
    protected static final String LAYER_TEST = "SST_tests";

    protected static WCSWorker WORKER;

    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        final Configurator config = new Configurator() {
            @Override
            public ProviderConfig getConfiguration(String serviceName) {
                final ProviderConfig config = new ProviderConfig();

                if("coverage-sql".equals(serviceName)){
                    // Defines a PostGrid data provider
                    final ProviderSource source = new ProviderSource();
                    source.parameters.put(CoverageSQLProvider.KEY_DATABASE, "jdbc:postgresql://db.geomatys.com/coverages-test");
                    source.parameters.put(CoverageSQLProvider.KEY_DRIVER,   "org.postgresql.Driver");
                    source.parameters.put(CoverageSQLProvider.KEY_PASSWORD, "test");
                    source.parameters.put(CoverageSQLProvider.KEY_READONLY, "true");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    source.parameters.put(CoverageSQLProvider.KEY_ROOT_DIRECTORY, rootDir);
                    source.parameters.put(CoverageSQLProvider.KEY_USER,     "test");
                    source.parameters.put(CoverageSQLProvider.KEY_SCHEMA,   "coverages");
                    source.parameters.put(CoverageSQLProvider.KEY_NAMESPACE,   "no namespace");
                    source.loadAll = true;
                    source.id = "src";
                    config.sources.add(source);
                }
                
                return config;
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(config);


        File configDir = new File("WCSWorkerTest");
        if (configDir.exists()) {
            FileUtilities.deleteDirectory(new File("WCSWorkerTest"));
        }

        try {
            if (!configDir.exists()) {
                configDir.mkdir();
                Source s1 = new Source("src", Boolean.TRUE, null, null);
                
                LayerContext lc = new LayerContext(new Layers(Arrays.asList(s1)));

                //we write the configuration file
                File configFile = new File(configDir, "layerContext.xml");
                final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(lc, configFile);
                GenericDatabaseMarshallerPool.getInstance().release(marshaller);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        WORKER = new WCSWorker("default", configDir);
        // Default instanciation of the worker' servlet context and uri context.
        WORKER.setServiceUrl("http://localhost:9090");
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtilities.deleteDirectory(new File("WCSWorkerTest"));
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }
}
