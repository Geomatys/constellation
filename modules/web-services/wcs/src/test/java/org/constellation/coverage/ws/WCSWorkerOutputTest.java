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

import org.constellation.configuration.ConfigurationException;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.GridLimitsType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.v100.CoverageDescription;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingBriefType;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingType;
import org.geotoolkit.wcs.xml.v100.DescribeCoverageType;
import org.geotoolkit.wcs.xml.v100.DomainSubsetType;
import org.geotoolkit.wcs.xml.v100.GetCapabilitiesType;
import org.geotoolkit.wcs.xml.v100.GetCoverageType;
import org.geotoolkit.wcs.xml.v100.OutputType;
import org.geotoolkit.wcs.xml.v100.SpatialDomainType;
import org.geotoolkit.wcs.xml.v100.SpatialSubsetType;
import org.geotoolkit.wcs.xml.v100.TimeSequenceType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilitiesType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.awt.image.RenderedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.NAMESPACE_DESCRIPTOR;
import org.constellation.ws.embedded.AbstractGrizzlyServer;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.jai.Registry;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateValue;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;


/**
 * Testing class for WCS requests.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class WCSWorkerOutputTest implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.coverage.ws");

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * The layer to test.
     */
    private static final String LAYER_TEST = "SSTMDE200305";

    private static WCSWorker WORKER;

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    private ILayerBusiness layerBusiness;

    @Inject
    private IProviderBusiness providerBusiness;

    @Inject
    private IDataBusiness dataBusiness;

    private static boolean initialized = false;

    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("WCSWorkerOutputTest");
    }

    /**
     * Initialisation of the worker and the PostGRID data provider before launching
     * the different tests.
     */
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();

                // coverage-sql datastore
                final File rootDir = AbstractGrizzlyServer.initDataDirectory();

                final ProviderFactory covFilefactory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup sourceCF = covFilefactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceCF, "id").setValue("coverageTestSrc");
                getOrCreateValue(sourceCF, "load_all").setValue(true);
                final ParameterValueGroup choice3 = getOrCreateGroup(sourceCF, "choice");

                final ParameterValueGroup srcCFConfig = getOrCreateGroup(choice3, "FileCoverageStoreParameters");

                getOrCreateValue(srcCFConfig, "path").setValue(new URL("file:" + rootDir.getAbsolutePath() + "/org/constellation/data/SSTMDE200305.png"));
                getOrCreateValue(srcCFConfig, "type").setValue("AUTO");
                getOrCreateValue(srcCFConfig, NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");

                providerBusiness.storeProvider("coverageTestSrc", null, ProviderType.LAYER, "coverage-store", sourceCF);

                dataBusiness.create(new QName("SSTMDE200305"), "coverageTestSrc", "COVERAGE", false, true, null, null);

                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wcs", "default", config, null);
                layerBusiness.add("SSTMDE200305", null, "coverageTestSrc", null, "default", "wcs", null);

                serviceBusiness.create("wcs", "test", config, null);
                layerBusiness.add("SSTMDE200305", null, "coverageTestSrc", null, "test",    "wcs", null);

                DataProviders.getInstance().reload();

                WORKER = new DefaultWCSWorker("default");
                // Default instanciation of the worker' servlet context and uri context.
                WORKER.setServiceUrl("http://localhost:9090");
                initialized = true;

                WorldFileImageReader.Spi.registerDefaults(null);

                //reset values, only allow pure java readers
                for(String jn : ImageIO.getReaderFormatNames()){
                    Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
                }

                //reset values, only allow pure java writers
                for(String jn : ImageIO.getWriterFormatNames()){
                    Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
                }

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            final ILayerBusiness layerBean = SpringHelper.getBean(ILayerBusiness.class);
            if (layerBean != null) {
                layerBean.removeAll();
            }
            final IServiceBusiness service = SpringHelper.getBean(IServiceBusiness.class);
            if (service != null) {
                service.deleteAll();
            }
            final IDataBusiness dataBean = SpringHelper.getBean(IDataBusiness.class);
            if (dataBean != null) {
                dataBean.deleteAll();
            }
            final IProviderBusiness provider = SpringHelper.getBean(IProviderBusiness.class);
            if (provider != null) {
                provider.removeAll();
            }
        } catch (ConfigurationException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage());
        }
        ConfigDirectory.shutdownTestEnvironement("WCSWorkerOutputTest");
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    /**
     * Ensures that a PostGRID layer preconfigured is found in the GetCapabilities document
     * returned by the {@link WCSWorker}.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testGetCapabilities() throws JAXBException, CstlServiceException {

        GetCapabilities request = new GetCapabilitiesType("1.0.0", "WCS", null, null);
        GetCapabilitiesResponse response = WORKER.getCapabilities(request);

        assertNotNull(response);
        assertTrue(response instanceof WCSCapabilitiesType);
        WCSCapabilitiesType getCaps = (WCSCapabilitiesType) response;


        // Verifies that the test layer is present into the GetCapabilities response.
        boolean find = false;
        final List<CoverageOfferingBriefType> offerings = getCaps.getContentMetadata().getCoverageOfferingBrief();
        assertFalse(offerings.isEmpty());
        for (CoverageOfferingBriefType offering : offerings) {
            for (JAXBElement<String> string : offering.getRest()) {
                if (string.getName().getLocalPart().equalsIgnoreCase("name") &&
                    string.getValue().equals(LAYER_TEST))
                {
                    find = true;
                }
            }
        }
        // Not found in the list of coverage offerings, there is a mistake here.
        if (!find) {
            fail("Unable to find the layer "+ LAYER_TEST +" in the GetCapabilities document.");
        }

        request = new GetCapabilitiesType("1.0.0", "WCS", "/WCS_Capabilities/Capability", null);
        getCaps = (WCSCapabilitiesType) WORKER.getCapabilities(request);

        assertNotNull(getCaps.getCapability());
        assertNull(getCaps.getContentMetadata());
        assertNull(getCaps.getService());

        request = new GetCapabilitiesType("1.0.0", "WCS", "/WCS_Capabilities/Service", null);
        getCaps = (WCSCapabilitiesType) WORKER.getCapabilities(request);

        assertNull(getCaps.getCapability());
        assertNull(getCaps.getContentMetadata());
        assertNotNull(getCaps.getService());

        request = new GetCapabilitiesType("1.0.0", "WCS", "/WCS_Capabilities/ContentMetadata", null);
        getCaps = (WCSCapabilitiesType) WORKER.getCapabilities(request);

        assertNull(getCaps.getCapability());
        assertNotNull(getCaps.getContentMetadata());
        assertNull(getCaps.getService());
    }

    /**
     * Ensures that a PostGRID layer preconfigured can be requested with a DescribeCoverage request,
     * and that the output document contains all data information.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testDescribeCoverage() throws JAXBException, CstlServiceException {

        final DescribeCoverage request = new DescribeCoverageType(LAYER_TEST);
        final DescribeCoverageResponse response = WORKER.describeCoverage(request);
        assertNotNull(response);
        assertTrue(response instanceof CoverageDescription);

        final CoverageDescription descCov = (CoverageDescription) response;
        // Verifies that the test layer is present into the DescribeCoverage response.
        for (CoverageOfferingType offering : descCov.getCoverageOffering()) {
            for (JAXBElement<String> string : offering.getRest()) {
                if (string.getName().getLocalPart().equalsIgnoreCase("name") &&
                    string.getValue().equals(LAYER_TEST))
                {
                    final SpatialDomainType spatialDomain = (SpatialDomainType) offering.getDomainSet()
                            .getContent().get(0).getValue();
                    final TimeSequenceType temporalDomain = (TimeSequenceType) offering.getDomainSet()
                            .getContent().get(1).getValue();
                    // Builds expected spatial domain
                    final List<DirectPositionType> pos = new ArrayList<>();
                    pos.add(new DirectPositionType(-180.0, -90.0));
                    pos.add(new DirectPositionType(180.0, 90.0));
                    final EnvelopeType expectedEnvelope = new EnvelopeType(pos, "EPSG:4326");
                    // Builds expected temporal domain
                    final List<TimePositionType> expectedTimes =
                            Collections.singletonList(new TimePositionType("2003-05-16T00:00:00Z"));
                    // Do assertions
                    assertEquals(expectedEnvelope, spatialDomain.getEnvelope());
                    // assertEquals(expectedTimes, temporalDomain.getTimePositionOrTimePeriod());
                    /*
                     * All tests have succeed on that specific layer, we can now stop this test.
                     */
                    return;
                }
            }
        }
        fail("Unable to find the layer "+ LAYER_TEST +" in the DescribeCoverage document.");
    }

    /**
     * Ensures that a PostGRID layer preconfigured can be requested with a GetCoverage request.
     *
     * TODO: do a checksum on the output image.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testGetCoverage() throws JAXBException, CstlServiceException {

        // Builds the GetCoverage request
        final List<String> axis = new ArrayList<>();
        axis.add("width");
        axis.add("height");
        final int[] low  = new int[2];
        low[0] = 0;
        low[1] = 0;
        final int[] high = new int[2];
        high[0] = 1024;
        high[1] = 512;
        final GridLimitsType limits = new GridLimitsType(low, high);
        final GridType grid = new GridType(limits, axis);
        final List<DirectPositionType> pos = new ArrayList<>();
        pos.add(new DirectPositionType(-180.0, -90.0));
        pos.add(new DirectPositionType(180.0, 90.0));
        final EnvelopeType envelope = new EnvelopeType(pos, "CRS:84");
        final DomainSubsetType domain = new DomainSubsetType(null, new SpatialSubsetType(envelope, grid));
        GetCoverage request = new GetCoverageType(LAYER_TEST, domain, null, null, new OutputType(MimeType.IMAGE_PNG, "CRS:84"));

        // Finally execute the request on the worker.
        final RenderedImage image = (RenderedImage) WORKER.getCoverage(request);
        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        // Test the checksum of the image, if the image is indexed (and its values of type byte).
        // TODO: the image should have indexed colors. Find the origin of the conversion from
        //       indexed color to RGB (int values).
//        assertEquals(Commons.checksum(image), 3183786073L);


        request = new GetCoverageType(LAYER_TEST, domain, null, "WCS_INTERPLATION_METHOD_INVALID", new OutputType(MimeType.IMAGE_PNG, "CRS:84"));
        boolean exLaunched = false;
        try {
            WORKER.getCoverage(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
        }
        assertTrue(exLaunched);
    }
}
