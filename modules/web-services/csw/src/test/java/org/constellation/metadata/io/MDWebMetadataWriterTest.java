/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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


import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import java.io.StringReader;
import java.sql.Connection;
import javax.xml.bind.Unmarshaller;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.CSWworkerTest;
import org.constellation.util.Util;
import static org.constellation.metadata.CSWTestUtils.*;

import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.metadata.iso.DefaultMetadata;

import org.opengis.feature.catalog.FeatureCatalogue;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataWriterTest {


    private static Automatic configuration;

    private static MDWebMetadataReader reader;

    private static MDWebMetadataWriter writer;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        

        pool = CSWMarshallerPool.getInstance();
        CSWworkerTest.fillPoolAnchor((AnchoredMarshallerPool) pool);

        final String url = "jdbc:derby:memory:MMWTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/Classe_Nature_SDI.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/Classe_GEONETCAB.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/profiles/inputLevels.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML_v2.sql"));

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);

        reader = new MDWebMetadataReader(configuration);
        writer = new MDWebMetadataWriter(configuration);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
        if (ds != null) {
            ds.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        SensorML absExpResult = (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));

        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("2:CSWCat", AbstractMetadataReader.SENSORML,  null);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult =  (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));

        pool.release(unmarshaller);

        ComponentEquals(expResult, result);
    }
    
    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataSystemSMLTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        writer.storeMetadata(absExpResult);

        AbstractSensorML absResult = (AbstractSensorML) reader.getMetadata("3:CSWCat", AbstractMetadataReader.SENSORML, null);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        SystemSMLEquals(expResult, result);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        writer.storeMetadata(absExpResult);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        absResult = (AbstractSensorML) reader.getMetadata("4:CSWCat", AbstractMetadataReader.SENSORML, null);

        pool.release(unmarshaller);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;

        SystemSMLEquals(expResult, result);
        
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataGMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta10.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("5:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta10.xml"));

        metadataEquals(expResult,result);
        
        
        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("6:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_11));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("7:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_12));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("8:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_13));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("9:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_14));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("10:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_15));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("11:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_16));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("12:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_17));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("13:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_18));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_19));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("14:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(new StringReader(StaticMetadata.META_19));

        metadataEquals(expResult,result);

        pool.release(unmarshaller);

    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata19110Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        FeatureCatalogue absExpResult = (FeatureCatalogue) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/featcatalog1.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("15:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof FeatureCatalogueImpl);
        FeatureCatalogueImpl result = (FeatureCatalogueImpl) absResult;
        FeatureCatalogueImpl expResult =  (FeatureCatalogueImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/featcatalog1.xml"));

        pool.release(unmarshaller);
        catalogueEquals(expResult,result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta7.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("16:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta7.xml"));

        pool.release(unmarshaller);
        metadataEquals(expResult,result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataNsdiTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/nsdiMetadata.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("17:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/nsdiMetadata.xml"));

        pool.release(unmarshaller);
        metadataEquals(expResult,result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataGncTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/gncMetadata.xml"));
        writer.storeMetadata(absExpResult);
        Object absResult = reader.getMetadata("18:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/gncMetadata.xml"));

        metadataEquals(expResult,result);

        absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/gncMetadata2.xml"));
        writer.storeMetadata(absExpResult);
        absResult = reader.getMetadata("19:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        result = (DefaultMetadata) absResult;
        expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/gncMetadata2.xml"));

        metadataEquals(expResult,result);

        pool.release(unmarshaller);
        
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadata191152Test() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        DefaultMetadata absExpResult = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/imageMetadata.xml"));
        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("20:CSWCat", AbstractMetadataReader.ISO_19115,  null);
        assertTrue(absResult != null);
        assertTrue(absResult instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) absResult;
        DefaultMetadata expResult =  (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/imageMetadata.xml"));

        pool.release(unmarshaller);
        metadataEquals(expResult,result);
    }
}
