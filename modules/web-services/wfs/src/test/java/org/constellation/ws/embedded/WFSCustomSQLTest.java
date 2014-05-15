/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.ws.embedded;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;

import static org.constellation.provider.configuration.ProviderParameters.*;

import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import org.geotoolkit.xsd.xml.v2001.Schema;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Ensure extended datastores properly work.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WFSCustomSQLTest extends AbstractGrizzlyServer {

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
    @BeforeClass
    public static void initPool() throws JAXBException {
        ConfigurationEngine.setupTestEnvironement("WFSCustomSQLTest");

        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null),
                                                   new Source("omSrc", true, null, null),
                                                   new Source("shapeSrc", true, null, null),
                                                   new Source("postgisSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");
        config.getCustomParameters().put("transactionSecurized", "false");
        config.getCustomParameters().put("transactionnal", "true");

        ConfigurationEngine.storeConfiguration("WFS", "default", config);

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
