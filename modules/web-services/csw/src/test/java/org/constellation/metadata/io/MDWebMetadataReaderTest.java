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

import java.io.StringReader;
import java.sql.Connection;
import java.util.List;
import javax.xml.bind.Unmarshaller;

// cstl dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.constellation.metadata.CSWworkerTest;
import org.constellation.util.Util;
import static org.constellation.metadata.CSWTestUtils.*;

// Geotoolkit dependencies
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.ebrim.xml.EBRIMClassesContext;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.xml.MarshallerPool;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataReaderTest {


    private static Automatic configuration;

    private static MDWebMetadataReader reader;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        List<Class> classes = EBRIMClassesContext.getAllClassesList();
        //classes.add(org.geotoolkit.sml.xml.v100.ObjectFactory.class);
        //classes.add(org.geotoolkit.naturesdi.NATSDI_DataIdentification.class);

        pool = new AnchoredMarshallerPool(classes.toArray(new Class[]{}));
        CSWworkerTest.fillPoolAnchor((AnchoredMarshallerPool) pool);

        final String url = "jdbc:derby:memory:MMRTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/Classe_Nature_SDI.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-5.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-6.sql"));
        
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML_v2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data_v2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data-2_v2.sql"));

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

    /*
     * TODO LOOK out for cache of CLasses in mdw meta reader
     *
     * SensorML timePeriod != iso timePeriod
     *
     */

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
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("2:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        result = reader.getMetadata("15:CSWCat", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta9.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOFRATest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("16:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta-fra1.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        pool.release(unmarshaller);
    }

     /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("17:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta10.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        result = reader.getMetadata("18:CSWCat", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        result = reader.getMetadata("19:CSWCat", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        

        result = reader.getMetadata("20:CSWCat", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);


        result = reader.getMetadata("21:CSWCat", AbstractMetadataReader.ISO_19115, null);

        expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

      /**
     * Tests the getMetadata method for ISO 19119 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISOGeoRectifiedTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("22:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

     /**
     * Tests the getMetadata method for ISO 19110 data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataISO19110Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("23:CSWCat", AbstractMetadataReader.ISO_19115, null);

        FeatureCatalogueImpl expResult = (FeatureCatalogueImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/featcatalog1.xml"));

        assertTrue(result instanceof FeatureCatalogueImpl);
        catalogueEquals(expResult, (FeatureCatalogueImpl)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for ISO 19115-2 data
     *
     * @throws java.lang.Exception
     */
    @Ignore
    public void getMetadataISO191152Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("24:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/imageMetadata.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for NATURE SDI data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataNatSDITest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object result = reader.getMetadata("26:CSWCat", AbstractMetadataReader.ISO_19115, null);

        DefaultMetadata expResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/nsdiMetadata.xml"));

        assertTrue(result instanceof DefaultMetadata);
        metadataEquals(expResult, (DefaultMetadata)result);

        pool.release(unmarshaller);
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataSystemSMLTest() throws Exception {

        
        Object absResult = reader.getMetadata("12:SMLC", AbstractMetadataReader.SENSORML, null);

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;


        SystemSMLEquals(expResult, result);


        absResult = reader.getMetadata("14:SMLC", AbstractMetadataReader.SENSORML, null);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        pool.release(unmarshaller);
        
        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;


        SystemSMLEquals(expResult, result);
        
    }

    /**
     * Tests the getMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        Object absResult = reader.getMetadata("13:SMLC", AbstractMetadataReader.SENSORML, null);

        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component.xml"));
        
        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;
        
        ComponentEquals(expResult, result);
    }
}
