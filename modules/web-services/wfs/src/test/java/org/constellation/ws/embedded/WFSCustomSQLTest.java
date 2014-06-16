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


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;

import org.constellation.test.utils.SpringTestRunner;

import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import org.geotoolkit.xsd.xml.v2001.Schema;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Ensure extended datastores properly work.
 *
 * @author Johann Sorel (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class WFSCustomSQLTest extends AbstractGrizzlyServer {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL =
               "request=DescribeFeatureType"
             + "&service=WFS"
             + "&version=1.1.0"
             + "&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1"
             + "&TypeName=CustomSQLQuery";


    public static boolean hasLocalDatabase() {
        return false; // TODO
    }

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
         try {
             ConfigurationEngine.setupTestEnvironement("WFSCustomSQLTest");

             
             final LayerContext config = new LayerContext();
             config.getCustomParameters().put("shiroAccessible", "false");
             config.getCustomParameters().put("transactionSecurized", "false");
             config.getCustomParameters().put("transactionnal", "true");
             
            serviceBusiness.create("WFS", "default", config, null);
            layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS");
            layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS");
            layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS");
            layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "default", "WFS");
            layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Bridges",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Streams",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Lakes",               "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Buildings",           "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Forests",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
            layerBusiness.add("Ponds",               "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "default", "WFS");
             
             initServer(new String[] {"org.constellation.wfs.ws.rs",
                 "org.constellation.configuration.ws.rs",
                 "org.constellation.ws.rs.provider"}, null);
             
             pool = new MarshallerPool(JAXBContext.newInstance("org.geotoolkit.wfs.xml.v110"   +
                     ":org.geotoolkit.ogc.xml.v110"  +
                     ":org.geotoolkit.gml.xml.v311"  +
                     ":org.geotoolkit.xsd.xml.v2001" +
                     ":org.geotoolkit.sampling.xml.v100" +
                     ":org.apache.sis.internal.jaxb.geometry"), null);
             
             final Configurator configurator = new AbstractConfigurator() {
                 @Override
                 public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                     
                     final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                     
                     final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                     
                     if (hasLocalDatabase()) {
                         final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                         getOrCreateValue(source, "id").setValue("postgisSrc");
                         getOrCreateValue(source, "load_all").setValue(true);
                         
                         final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                         final ParameterValueGroup pgconfig = createGroup(choice, " PostgresParameters");
                         getOrCreateValue(pgconfig,"host").setValue("flupke.geomatys.com");
                         getOrCreateValue(pgconfig,"port").setValue(5432);
                         getOrCreateValue(pgconfig,"database").setValue("cite-wfs");
                         getOrCreateValue(pgconfig,"schema").setValue("public");
                         getOrCreateValue(pgconfig,"user").setValue("test");
                         getOrCreateValue(pgconfig,"password").setValue("test");
                         getOrCreateValue(pgconfig,"namespace").setValue("no namespace");
                         
                         //add a custom sql query layer
                         final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                         getOrCreateValue(layer, "name").setValue("CustomSQLQuery");
                         getOrCreateValue(layer, "language").setValue("CUSTOM-SQL");
                         getOrCreateValue(layer, "statement").setValue("SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");
                         
                         lst.add(new AbstractMap.SimpleImmutableEntry<>("postgisSrc",source));
                     }
                     
                     return lst;
                     
                 }
                 
                 @Override
                 public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
             };
             
             DataProviders.getInstance().setConfigurator(configurator);
         } catch (Exception ex) {
             Logger.getLogger(WFSCustomSQLTest.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    @AfterClass
    public static void shutDown() {
        ConfigurationEngine.shutdownTestEnvironement("WFSCustomSQLTest");
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        finish();
    }

    @Test
    public void testWFSDescribeFeatureGET() throws Exception {
        waitForStart();
        assumeTrue(hasLocalDatabase());
        
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" +WFS_DESCRIBE_FEATURE_TYPE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);
        assertTrue(obj instanceof Schema);
        final Schema schema = (Schema) obj;
        final List elements = schema.getElements();
        assertEquals(1, elements.size());

        final XMLComparator comparator = new XMLComparator(WFSCustomSQLTest.class.getResource("/expected/customsqlquery.xsd"), getfeatsUrl);
        comparator.compare();

    }

}
