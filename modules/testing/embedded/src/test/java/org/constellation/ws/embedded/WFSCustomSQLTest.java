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

// JUnit dependencies
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xml.MarshallerPool;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.geotoolkit.test.xml.DomComparator;
import org.junit.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;

/**
 * Ensure extended datastores properly work.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class WFSCustomSQLTest extends AbstractTestRequest {

     private static final String WFS_DESCRIBE_FEATURE_TYPE_URL = 
             "http://localhost:9090/wfs/default"
             + "?request=DescribeFeatureType"
             + "&service=WFS"
             + "&version=1.1.0"
             + "&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1"
             + "&TypeName=CustomSQLQuery";

    
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        pool = new MarshallerPool("org.geotoolkit.wfs.xml.v110"   +
            		  ":org.geotoolkit.ogc.xml.v110"  +
            		  ":org.geotoolkit.gml.xml.v311"  +
                          ":org.geotoolkit.xsd.xml.v2001" +
                          ":org.geotoolkit.sampling.xml.v100" +
                         ":org.geotoolkit.internal.jaxb.geometry");
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testWFSDescribeFeatureGET() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL(WFS_DESCRIBE_FEATURE_TYPE_URL);
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
