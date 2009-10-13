/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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


package org.constellation.metadata.io;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.AnchorPool;
import org.constellation.metadata.CSWClassesContext;
import org.constellation.metadata.CSWworkerTest;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataReaderTest {


    private static Automatic configuration;

    private MDWebMetadataReader reader;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pool = new AnchorPool(Arrays.asList(CSWClassesContext.getAllClasses()));

        final String url = "jdbc:derby:memory:MMRTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/metadata/sql/structure-mdweb.sql", con);
        Util.executeSQLScript("org/constellation/metadata/sql/ISO19115-data.sql", con);
        Util.executeSQLScript("org/constellation/metadata/sql/csw-data.sql", con);

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {

        reader = new MDWebMetadataReader(configuration);
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("2:CSWCat", MetadataReader.ISO_19115, null, null);

        DefaultMetaData expResult = (DefaultMetaData) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertTrue(result instanceof DefaultMetaData);
        CSWworkerTest.metadataEquals(expResult, (DefaultMetaData)result);
        pool.release(unmarshaller);
    }
}
