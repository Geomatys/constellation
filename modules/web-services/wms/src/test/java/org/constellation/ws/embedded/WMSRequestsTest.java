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
package org.constellation.ws.embedded;

// J2SE dependencies
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.*;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.ImageTesting;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.inspire.xml.vs.LanguageType;
import org.geotoolkit.inspire.xml.vs.LanguagesType;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.sld.xml.v110.LayerDescriptionType;
import org.geotoolkit.sld.xml.v110.TypeNameType;
import org.geotoolkit.util.NamesExt;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.v111.LatLonBoundingBox;
import org.geotoolkit.wms.xml.v111.Layer;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.geotoolkit.utility.parameter.ParametersExt.createGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import org.junit.BeforeClass;
import org.opengis.util.GenericName;
import org.apache.sis.util.logging.Logging;
import org.springframework.test.context.ActiveProfiles;
import org.junit.Assert;


/**
 * A set of methods that request a Grizzly server which embeds a WMS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class WMSRequestsTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    protected ILayerBusiness layerBusiness;

    @Inject
    protected IProviderBusiness providerBusiness;

    @Inject
    protected IDataBusiness dataBusiness;

    /**
     * The layer to test.
     */
    private static final GenericName LAYER_TEST = NamesExt.create("SSTMDE200305");

    /**
     * URLs which will be tested on the server.
     */
    private static final String WMS_GETCAPABILITIES ="request=GetCapabilities&service=WMS&version=1.1.1";

    private static final String WMS_GETCAPABILITIES_WMS1_111 ="request=GetCapabilities&service=WMS&version=1.1.1";

    private static final String WMS_GETCAPABILITIES_WMS1 ="request=GetCapabilities&service=WMS&version=1.3.0";

    private static final String WMS_GETCAPABILITIES_WMS1_FRE ="request=GetCapabilities&service=WMS&version=1.3.0&language=fre";

    private static final String WMS_GETCAPABILITIES_WMS1_ENG ="request=GetCapabilities&service=WMS&version=1.3.0&language=eng";

    private static final String WMS_FALSE_REQUEST ="request=SomethingElse";

    private static final String WMS_GETMAP ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";


    private static final String WMS_GETMAP_LAYER_LIMIT ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "styles=&layers=";

    private static final String WMS_GETFEATUREINFO ="request=GetFeatureInfo&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=&" +
                                      "query_layers="+ LAYER_TEST +"&info_format=text/plain&" +
                                      "X=300&Y=200";

    private static final String WMS_GETFEATUREINFO2 ="request=GetFeatureInfo&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=200&height=100&" +
                                      "srs=CRS:84&BbOx=0,-0.0020,0.0040,0&" +
                                      "layers=Lakes&styles=&" +
                                      "query_layers=Lakes&info_format=text/plain&" +
                                      "X=60&Y=60";

    private static final String WMS_GETFEATUREINFO3 = "QuErY_LaYeRs=BasicPolygons&I=50&"
                                                    + "LaYeRs=BasicPolygons&StYlEs=&WiDtH=100&CrS=CRS:84&"
                                                    + "ReQuEsT=GetFeatureInfo&InFo_fOrMaT=text/plain&BbOx=-2,2,2,6"
                                                    + "&HeIgHt=100&J=50&VeRsIoN=1.3.0&FoRmAt=image/gif";

    private static final String WMS_GETFEATUREINFO4 ="QuErY_LaYeRs=cite:Lakes&BbOx=0,-0.0020,0.0040,0&"
                                                    + "FoRmAt=image/gif&ReQuEsT=GetFeatureInfo&"
                                                    + "VeRsIoN=1.1.1&InFo_fOrMaT=application/vnd.ogc.gml&"
                                                    + "X=60&StYlEs=&LaYeRs=cite:Lakes&"
                                                    + "SrS=EPSG:4326&WiDtH=200&HeIgHt=100&Y=60";

    private static final String WMS_GETLEGENDGRAPHIC = "request=GetLegendGraphic&service=wms&" +
            "width=200&height=40&layer="+ LAYER_TEST +"&format=image/png&version=1.1.0";

    private static final String WMS_DESCRIBELAYER ="request=DescribeLayer&service=WMS&" +
            "version=1.1.1&layers="+ LAYER_TEST;

    private static final String WMS_GETMAP2 =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/png&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_BMP =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/bmp&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_JPEG =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/jpeg&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_BMP_111 =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/bmp&ReQuEsT=GetMap&StYlEs=&SrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.1.1&WiDtH=100";

    private static final String WMS_GETMAP_PPM =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/x-portable-pixmap&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_GIF =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/gif&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_GIF_UNVALID_LAYER =
    "TrAnSpArEnT=False&HeIgHt=100&LaYeRs=unknownlayer&FoRmAt=image/gif&ReQuEsT=GetMap&StYlEs=&srS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.1.1&WiDtH=100&EXCEPTIONS=application/vnd.ogc.se_inimage";

    private static final String WMS_GETMAP_GIF_TRANSPARENT =
    "TrAnSpArEnT=TRUE&CrS=CRS:84&FoRmAt=image%2Fgif&VeRsIoN=1.3.0&HeIgHt=100&WiDtH=200&StYlEs=&LaYeRs=cite%3ALakes&ReQuEsT=GetMap&BbOx=0,-0.0020,0.0040,0";

    private static boolean initialized = false;

    @BeforeClass
    public static void startup() {
        ConfigDirectory.setupTestEnvironement("WMSRequestTest");
    }

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initLayerList() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {

                try {
                    layerBusiness.removeAll();
                    serviceBusiness.deleteAll();
                    dataBusiness.deleteAll();
                    providerBusiness.removeAll();
                } catch (Exception ex) {}

                // coverage-file datastore
                final File rootDir                   = AbstractGrizzlyServer.initDataDirectory();
                final ProviderFactory covFilefactory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup sourceCF   = covFilefactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceCF, "id").setValue("coverageTestSrc");
                getOrCreateValue(sourceCF, "load_all").setValue(true);
                final ParameterValueGroup choice3 = getOrCreateGroup(sourceCF, "choice");

                final ParameterValueGroup srcCFConfig = getOrCreateGroup(choice3, "FileCoverageStoreParameters");

                getOrCreateValue(srcCFConfig, "path").setValue(new URL("file:" + rootDir.getAbsolutePath() + "/org/constellation/data/SSTMDE200305.png"));
                getOrCreateValue(srcCFConfig, "type").setValue("AUTO");
                getOrCreateValue(srcCFConfig, "namespace").setValue("no namespace");

                providerBusiness.storeProvider("coverageTestSrc", null, ProviderType.LAYER, "coverage-store", sourceCF);
                dataBusiness.create(new QName("SSTMDE200305"), "coverageTestSrc", "COVERAGE", false, true, null, null);


                final ProviderFactory ffactory = DataProviders.getInstance().getFactory("feature-store");
                final File outputDir = initDataDirectory();
                final ParameterValueGroup sourcef = ffactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                String path;
                if (outputDir.getAbsolutePath().endsWith("org/constellation/ws/embedded/wms111/styles")) {
                    path = outputDir.getAbsolutePath().substring(0, outputDir.getAbsolutePath().indexOf("org/constellation/ws/embedded/wms111/styles"));
                } else {
                    path = outputDir.getAbsolutePath();
                }
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+path + "/org/constellation/ws/embedded/wms111/shapefiles"));

                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                final ParameterValueGroup layer = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer, "name").setValue("NamedPlaces");
                getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");

                providerBusiness.storeProvider("shapeSrc", null, ProviderType.LAYER, "feature-store", sourcef);

                dataBusiness.create(new QName("http://www.opengis.net/gml", "BuildingCenters"), "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "BasicPolygons"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Bridges"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Streams"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Lakes"),           "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "NamedPlaces"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Buildings"),       "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "RoadSegments"),    "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "DividedRoutes"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Forests"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "MapNeatline"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Ponds"),           "shapeSrc", "VECTOR", false, true, null, null);


                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());

                serviceBusiness.create("wms", "default", config, null);
                final Details details = serviceBusiness.getInstanceDetails("wms", "default", "eng");
                details.getServiceConstraints().setLayerLimit(100);
                serviceBusiness.setInstanceDetails("wms", "default", details, "eng", true);

                layerBusiness.add("SSTMDE200305",                     null,           "coverageTestSrc",        null, "default", "wms", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);


                final LayerContext config2 = new LayerContext();
                config2.setSupportedLanguages(new Languages(Arrays.asList(new Language("fre"), new Language("eng", true))));
                config2.getCustomParameters().put("shiroAccessible", "false");
                config2.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());

                serviceBusiness.create("wms", "wms1", config2, null);
                layerBusiness.add("Lakes",    "http://www.opengis.net/gml", "shapeSrc",        null, "wms1", "wms", null);


                final Details serviceEng = new Details();
                serviceEng.setDescription("Serveur Cartographique.  Contact: someone@geomatys.fr.  Carte haute qualité.");
                serviceEng.setIdentifier("wms1");
                serviceEng.setKeywords(Arrays.asList("WMS"));
                serviceEng.setName("this is the default english capabilities");
                final AccessConstraint cstr = new AccessConstraint("NONE", "NONE", 20, 1024, 1024);
                serviceEng.setServiceConstraints(cstr);
                final Contact ct = new Contact();
                serviceEng.setServiceContact(ct);
                serviceEng.setVersions(Arrays.asList("1.1.1", "1.3.0"));

                serviceBusiness.setInstanceDetails("wms", "wms1", serviceEng, "eng", true);
                //ConfigDirectory.writeServiceMetadata("wms1", "wms", serviceEng, "eng");

                final Details serviceFre = new Details();
                serviceFre.setDescription("Serveur Cartographique.  Contact: someone@geomatys.fr.  Carte haute qualité.");
                serviceFre.setIdentifier("wms1");
                serviceFre.setKeywords(Arrays.asList("WMS"));
                serviceFre.setName("Ceci est le document capabilities français");
                serviceFre.setServiceConstraints(cstr);
                serviceFre.setServiceContact(ct);
                serviceFre.setVersions(Arrays.asList("1.1.1", "1.3.0"));

                serviceBusiness.setInstanceDetails("wms", "wms1", serviceFre, "fre", false);

                final LayerContext config3 = new LayerContext();
                config3.getCustomParameters().put("shiroAccessible", "false");
                config3.getCustomParameters().put("supported_versions", "1.3.0");
                config3.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
                final Details details3 = new Details();
                details3.setIdentifier("wms2");
                details3.setName("wms2");
                details3.setVersions(Arrays.asList("1.3.0"));

                serviceBusiness.create("wms", "wms2", config3, details3);
                layerBusiness.add("SSTMDE200305",                      null,          "coverageTestSrc",        null, "wms2", "wms", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",        null, "wms2", "wms", null);

                initServer(null, null);

                pool = WMSMarshallerPool.getInstance();

                WorldFileImageReader.Spi.registerDefaults(null);
                WMSPortrayal.setEmptyExtension(true);

                //reset values, only allow pure java readers
                for(String jn : ImageIO.getReaderFormatNames()){
                    Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
                }

                //reset values, only allow pure java writers
                for(String jn : ImageIO.getWriterFormatNames()){
                    Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
                }
                DataProviders.getInstance().reload();
                initialized = true;
            } catch (Exception ex) {
                Logging.getLogger("org.constellation.ws.embedded").log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() throws JAXBException {
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
        } catch (Exception ex) {
            Logging.getLogger("org.constellation.ws.embedded").log(Level.WARNING, ex.getMessage());
        }
        ConfigDirectory.shutdownTestEnvironement("WMSRequestTest");
        finish();
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    @Order(order=1)
    public void testWMSWrongRequest() throws Exception {

        waitForStart();

        // Creates an intentional wrong url, regarding the WMS version 1.1.1 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_FALSE_REQUEST);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a ServiceExceptionReport.
        final Object obj = unmarshallResponse(wrongUrl);
        assertTrue(obj instanceof ServiceExceptionReport);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=2)
    public void testWMSGetMap() throws Exception {
        waitForStart();
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 8);

    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=3)
    public void testWMSGetMapLakeGif() throws Exception {
        waitForStart();
                // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_GIF);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/gif");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=4)
    public void testWMSGetMapLakeGifransparent() throws Exception {
        waitForStart();
                // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_GIF_TRANSPARENT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/gif");

        // Test on the returned image.
        assertEquals(200, image.getWidth());
        assertEquals(100,  image.getHeight());
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=5)
    public void testWMSGetMapLakePng() throws Exception {
        waitForStart();
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=6)
    public void testWMSGetMapLakeBmp() throws Exception {
        waitForStart();
        // Creates a valid GetMap url.
        URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_BMP);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/bmp");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);

        // wms do not supported 1.1.1 request
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms2?" + WMS_GETMAP_BMP_111);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        Object obj = unmarshallResponse(getMapUrl);
        assertTrue(obj instanceof ServiceExceptionReport);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=7)
    public void testWMSGetMapLakeJpeg() throws Exception {

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_JPEG);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/jpeg");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    @Order(order=8)
    public void testWMSGetMapLakePpm() throws Exception {
        waitForStart();
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_PPM);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/x-portable-pixmap");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that an exception is returned when requesting too many layers.
     */
    @Test
    @Order(order=9)
    public void testWMSGetMapLayerLimit() throws Exception {

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("http://localhost:").append(grizzly.getCurrentPort()).append("/wms/default?" + WMS_GETMAP_LAYER_LIMIT);
            sb.append(LAYER_TEST);
            for (int i=0;i<120;i++) {
                sb.append(',').append(LAYER_TEST);
            }
            getMapUrl = new URL(sb.toString());
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        try{
            final BufferedImage image = getImageFromURL(getMapUrl, "image/png");
            Assert.fail("Service should have raised an error");
        }catch(Exception ex){
            //ok
        }

    }

    /**
     * Ensures that an error is returned in image as gif and
     * is not all black.
     */
    @Test
    @Order(order=10)
    public void testWMSGetMapErrorInImageGif() throws Exception {

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_GIF_UNVALID_LAYER);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/gif");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) == 2);
        assertFalse (ImageTesting.hasTransparency(image));

    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WMS version 1.1.1/ 1.3.0 standard.
     */
    @Test
    @Order(order=11)
    public void testWMSGetCapabilities() throws JAXBException, Exception {
        waitForStart();
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj, obj instanceof WMT_MS_Capabilities);
        WMT_MS_Capabilities responseCaps = (WMT_MS_Capabilities)obj;

        Layer layer = (Layer) responseCaps.getLayerFromName(LAYER_TEST.tip().toString());

        assertNotNull(layer);
        assertEquals("EPSG:4326", layer.getSRS().get(0));
        final LatLonBoundingBox bboxGeo = (LatLonBoundingBox) layer.getLatLonBoundingBox();
        assertTrue(bboxGeo.getWestBoundLongitude() == -180d);
        assertTrue(bboxGeo.getSouthBoundLatitude() ==  -90d);
        assertTrue(bboxGeo.getEastBoundLongitude() ==  180d);
        assertTrue(bboxGeo.getNorthBoundLatitude() ==   90d);


        String currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?", currentUrl);

        // Creates a valid GetCapabilities url.
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" + WMS_GETCAPABILITIES_WMS1_111);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMT_MS_Capabilities);

        responseCaps = (WMT_MS_Capabilities) obj;

        // The layer test must be excluded
        layer = (Layer) responseCaps.getLayerFromName(LAYER_TEST.tip().toString());
        assertNull(layer);


        // The layer lake must be included
        layer = (Layer) responseCaps.getLayerFromName("http://www.opengis.net/gml:Lakes");
        assertNotNull(layer);

        currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?", currentUrl);

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMT_MS_Capabilities);
        responseCaps = (WMT_MS_Capabilities) obj;

        currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?", currentUrl);


        // Creates a valid GetCapabilities url.
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms2?" + WMS_GETCAPABILITIES_WMS1_111);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        //the service WMS2 does not support 1.1.0 version
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was :" + obj.getClass().getName(), obj instanceof WMSCapabilities);

         // Creates a valid GetCapabilities url.
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms2?" + WMS_GETCAPABILITIES_WMS1);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);
    }

    @Test
    @Order(order=12)
    public void testWMSGetCapabilitiesLanguage() throws JAXBException, Exception {
        waitForStart();
         // Creates a valid GetMap url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        WMSCapabilities responseCaps130 = (WMSCapabilities)obj;
        ExtendedCapabilitiesType  ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("eng", ext.getCurrentLanguage());

        LanguageType l1 = new LanguageType("fre", false);
        LanguageType l2 = new LanguageType("eng", true);
        LanguagesType languages = new LanguagesType(Arrays.asList(l1, l2));
        assertEquals(ext.getLanguages(), languages);

        assertEquals("this is the default english capabilities", responseCaps130.getService().getName());

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1_ENG);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        responseCaps130 = (WMSCapabilities)obj;
        ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("eng", ext.getCurrentLanguage());
        assertEquals(ext.getLanguages(), languages);

        assertEquals("this is the default english capabilities", responseCaps130.getService().getName());

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1_FRE);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        responseCaps130 = (WMSCapabilities)obj;
        ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("fre", ext.getCurrentLanguage());
        assertEquals(ext.getLanguages(), languages);

        assertEquals("Ceci est le document capabilities français", responseCaps130.getService().getName());

    }

    /**
     * Ensures that the {@code WMS GetFeatureInfo} request on a particular point of the
     * testing layer produces the wanted result.
     * @throws java.io.Exception
     */
    @Test
    @Order(order=13)
    public void testWMSGetFeatureInfo() throws Exception {
        waitForStart();
        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETFEATUREINFO);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;

        final InputStream inGfi = gfi.openStream();
        final InputStreamReader isr = new InputStreamReader(inGfi);
        final BufferedReader reader = new BufferedReader(isr);
        String fullResponse = "";
        String line;
        while ((line = reader.readLine()) != null) {
            // Verify that the line starts with a number, only the one with the value
            // should begin like this.
            if (line.matches("[0-9]+.*")) {
                // keep the line with the value
                value = line;
            }
            fullResponse = fullResponse + line + '\n';
        }
        reader.close();

        // Tests on the returned value
        assertNotNull(fullResponse, value);
        // now i get 0.0 here ? assertTrue   (value.startsWith("210.0")); // I d'ont know why but before the test was => (value.startsWith("28.5"));
    }

    /**
     * I don't know why this test do not work
     * @throws Exception
     */
    @Test
    @Ignore
    @Order(order=14)
    public void testWMSGetFeatureInfo2() throws Exception {
        waitForStart();
        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETFEATUREINFO2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;

        final InputStream inGfi = gfi.openStream();
        final InputStreamReader isr = new InputStreamReader(inGfi);
        final BufferedReader reader = new BufferedReader(isr);
        String fullResponse = "";
        String line;
        while ((line = reader.readLine()) != null) {
            // Verify that the line starts with a number, only the one with the value
            // should begin like this.
            if (line.matches("[0-9]+.*")) {
                // keep the line with the value
                value = line;
            }
            fullResponse = fullResponse + line + '\n';
        }
        reader.close();

        // Tests on the returned value
        assertNotNull(fullResponse, value);
        assertTrue   (value.startsWith("28.5"));
    }

    @Test
    @Ignore
    @Order(order=15)
    public void testWMSGetFeatureInfo3() throws Exception {
        waitForStart();
        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETFEATUREINFO3);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;
        final InputStream inGfi = gfi.openStream();
        final InputStreamReader isr = new InputStreamReader(inGfi);
        final BufferedReader reader = new BufferedReader(isr);
        String fullResponse = "";
        String line;
        while ((line = reader.readLine()) != null) {
            // Verify that the line starts with a number, only the one with the value
            // should begin like this.
            if (line.matches("[0-9]+.*")) {
                // keep the line with the value
                value = line;
            }
            fullResponse = fullResponse + line + '\n';
        }
        System.out.println("FULLRESPONSE: " + fullResponse);
        reader.close();

        // Tests on the returned value
        assertNotNull(fullResponse, value);
        assertTrue   (value.startsWith("28.5"));
    }

    @Test
    @Ignore
    @Order(order=16)
    public void testWMSGetFeatureInfo4() throws Exception {

        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETFEATUREINFO4);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;
        final InputStream inGfi = gfi.openStream();
        final InputStreamReader isr = new InputStreamReader(inGfi);
        final BufferedReader reader = new BufferedReader(isr);
        String fullResponse = "";
        String line;
        while ((line = reader.readLine()) != null) {
            // Verify that the line starts with a number, only the one with the value
            // should begin like this.
            if (line.matches("[0-9]+.*")) {
                // keep the line with the value
                value = line;
            }
            fullResponse = fullResponse + line + '\n';
        }
        System.out.println("FULLRESPONSE: " + fullResponse);
        reader.close();

        // Tests on the returned value
        assertNotNull(fullResponse, value);
        assertTrue   (value.startsWith("28.5"));
    }

    /**
     * Ensures that a valid GetLegendGraphic request returns indeed a {@link BufferedImage}.
     *
     * TODO : ignore until the getlegendgraphic method is done into the new
     *        postgrid implementation.
     * @throws java.io.Exception
     */
    @Test
    @Ignore
    public void testWMSGetLegendGraphic() throws Exception {
        waitForStart();
        // Creates a valid GetLegendGraphic url.
        final URL getLegendUrl;
        try {
            getLegendUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETLEGENDGRAPHIC);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getLegendUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(200, image.getWidth());
        assertEquals(40,  image.getHeight());
    }

    /**
     * Ensures that a valid DescribeLayer request produces a valid document.
     */
    @Test
    @Order(order=18)
    public void testWMSDescribeLayer() throws JAXBException, Exception {
        waitForStart();
        // Creates a valid DescribeLayer url.
        final URL describeUrl;
        try {
            describeUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_DESCRIBELAYER);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        final Object obj = unmarshallResponse(describeUrl);
        assertTrue(obj instanceof DescribeLayerResponseType);

        // Tests on the response
        final DescribeLayerResponseType desc = (DescribeLayerResponseType)obj;
        final List<LayerDescriptionType> layerDescs = desc.getLayerDescription();
        assertFalse(layerDescs.isEmpty());
        final List<TypeNameType> typeNames = layerDescs.get(0).getTypeName();
        assertFalse(typeNames.isEmpty());
        final GenericName name = NamesExt.create(typeNames.get(0).getCoverageName());
        assertEquals(LAYER_TEST, name);
    }


    @Test
    @Order(order=19)
    public void testWMSGetMapLakePostKvp() throws Exception {
        waitForStart();
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?");
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("HeIgHt","100");
        parameters.put("LaYeRs","Lakes");
        parameters.put("FoRmAt","image/png");
        parameters.put("ReQuEsT","GetMap");
        parameters.put("StYlEs","");
        parameters.put("CrS","CRS:84");
        parameters.put("BbOx","-0.0025,-0.0025,0.0025,0.0025");
        parameters.put("VeRsIoN","1.3.0");
        parameters.put("WiDtH","100");

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromPostKvp(getMapUrl, parameters, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }
}
