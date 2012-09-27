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


import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.Configurator;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.test.xml.DomComparator;

import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Ensure extended datastores properly work.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WFSCustomSQLTest extends AbstractTestRequest {

     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL =
               "request=DescribeFeatureType"
             + "&service=WFS"
             + "&version=1.1.0"
             + "&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1"
             + "&TypeName=CustomSQLQuery";


    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        initServer(new String[] {"org.constellation.wfs.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"});
        
        pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          ":org.geotoolkit.sampling.xml.v100" +
                         ":org.geotoolkit.internal.jaxb.geometry");

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {

                final ParameterValueGroup config = desc.createValue();

                if("postgis".equals(serviceName)){
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(PostgisNGDataStoreFactory.PARAMETERS_DESCRIPTOR,source);

                    srcconfig.parameter(HOST.getName().getCode()).setValue("flupke.geomatys.com");
                    srcconfig.parameter(PORT.getName().getCode()).setValue(5432);
                    srcconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                    srcconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    srcconfig.parameter(USER.getName().getCode()).setValue("test");
                    srcconfig.parameter(PASSWD.getName().getCode()).setValue("test");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");

                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                    //add a custom sql query layer
                    ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("CustomSQLQuery");
                    layer.parameter(LAYER_QUERY_LANGUAGE.getName().getCode()).setValue("CUSTOM-SQL");
                    layer.parameter(LAYER_QUERY_STATEMENT.getName().getCode()).setValue(
                            "SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");
                }

                //empty configuration for others
                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);



    }

    @AfterClass
    public static void finish() {
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testWFSDescribeFeatureGET() throws Exception {
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

        final DomComparator comparator = new DomComparator(WFSCustomSQLTest.class.getResource("/expected/customsqlquery.xsd"), getfeatsUrl);
        comparator.compare();

    }

}
