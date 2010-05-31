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

import com.sun.grizzly.http.servlet.ServletContextImpl;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.server.impl.application.WebApplicationContext;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import java.io.File;
import java.net.URI;
import java.util.List;
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.coveragesql.CoverageSQLProvider;
import org.constellation.provider.coveragesql.CoverageSQLProviderService;
import org.constellation.register.RegisterException;
import org.geotoolkit.xml.MarshallerPool;
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
     * A list of available layers to be requested in WMS.
     */
    protected static List<LayerDetails> LAYERS;

    /**
     * The layer to test.
     */
    protected static final String LAYER_TEST = "SST_tests";

    protected static WCSWorker WORKER;

    protected static MarshallerPool POOL;

    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        POOL = new AnchoredMarshallerPool("",
                    "org.constellation.ws:" +
                    "org.geotoolkit.ows.xml.v100:" +
                    "org.geotoolkit.wcs.xml.v100:" +
                    "org.geotoolkit.wcs.xml.v111:" +
                    "org.geotoolkit.gml.xml.v311");
        WORKER = new WCSWorker(POOL);
        // Default instanciation of the worker' servlet context and uri context.
        WORKER.initServletContext(new ServletContextImpl());
        WORKER.initUriContext(new WebApplicationContext(
                new WebApplicationImpl(),
                new ContainerRequest(new WebApplicationImpl(), "GET", new URI("http://localhost:9090/"),
                                     new URI("http://localhost:9090/wcs?request=GetCapabilities&service=WCS&version=1.0.0"),
                                     new InBoundHeaders(), null),
                null));

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
        source.loadAll = true;

        final ProviderConfig config = new ProviderConfig();
        config.sources.add(source);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgrid data provider defined previously
            if (service instanceof CoverageSQLProviderService) {
                service.setConfiguration(config);
                assumeTrue(!(service.getProviders().isEmpty()));
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        try {
            LAYERS = Cstl.getRegister().getAllLayerReferences(ServiceDef.WCS_1_0_0);
        } catch (RegisterException ex) {
            LAYERS = null;
            assumeNoException(ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    /**
     * Returns {@code true} if the {@code SST_tests} layer is found in the list of
     * available layers. It means the postgrid database, pointed by the postgrid.xml
     * file in the configuration directory, contains this layer and can then be requested
     * in WMS.
     */
    protected static boolean containsTestLayer() {
        for (LayerDetails layer : LAYERS) {
            if (layer.getName().equals(LAYER_TEST)) {
                return true;
            }
        }
        return false;
    }
}
