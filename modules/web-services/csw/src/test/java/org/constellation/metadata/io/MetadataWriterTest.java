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

import java.util.Arrays;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.util.DefaultInternationalString;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MetadataWriterTest {

    @BeforeClass
    public static void setUpClass() throws Exception {


    }

    @AfterClass
    public static void tearDownClass() throws Exception {

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
    public void FindTitleTest() throws Exception {

        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        String expResult = "42292_5p_19900609195600";
        String result = MDWebCSWMetadataWriter.findTitle(record);

        assertEquals(expResult, result);

        record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setTitle(new SimpleLiteral("title1"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "title1";
        result = MDWebCSWMetadataWriter.findTitle(record);

        assertEquals(expResult, result);

        DefaultMetadata metadata = new DefaultMetadata();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        expResult = "titleMeta";
        result = MDWebCSWMetadataWriter.findTitle(metadata);

        assertEquals(expResult, result);
    }
}
