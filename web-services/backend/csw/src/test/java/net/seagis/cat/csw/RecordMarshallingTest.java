/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.cat.csw;

// JUnit dependencies
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.seagis.dublincore.elements.SimpleLiteral;
import net.seagis.ows.v100.BoundingBoxType;
import net.seagis.ows.v100.WGS84BoundingBoxType;
import net.seagis.ws.rs.NamespacePrefixMapperImpl;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A Test suite verifying that the Record are correctly marshalled/unmarshalled
 * 
 * @author Guilhem Legal
 */
public class RecordMarshallingTest {
    
    private Logger       logger = Logger.getLogger("net.seagis.filter");
    private Unmarshaller recordUnmarshaller;
    private Marshaller   recordMarshaller;
   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.cat.csw");
        recordUnmarshaller    = jbcontext.createUnmarshaller();
        recordMarshaller      = jbcontext.createMarshaller();
        recordMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        recordMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void recordMarshalingTest() throws Exception {
        
        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));
        
        SimpleLiteral modified   = new SimpleLiteral("2007-11-15 21:26:49");
        SimpleLiteral Abstract   = new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.");
        SimpleLiteral references = new SimpleLiteral("http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml");
        SimpleLiteral spatial    = new SimpleLiteral("northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;");
        
        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        
        RecordType record = new RecordType(id, title, type, subject, null, modified, Abstract, bbox, null, null, null, spatial, references);
        
        StringWriter sw = new StringWriter();
        recordMarshaller.marshal(record, sw);
        
        String result = sw.toString();
        String expResult = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Record xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <dct:references>http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml</dct:references>" + '\n' +
        "    <dct:spatial>northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;</dct:spatial>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:Record>" + '\n';
    
        assertEquals(expResult, result);
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void recordUnmarshalingTest() throws Exception {
        String xml = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Record xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <dct:spatial>northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;</dct:spatial>" + '\n' +
        "    <dct:references>http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml</dct:references>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:Record>" + '\n';
        
        StringReader sr = new StringReader(xml);
        
        JAXBElement jb = (JAXBElement) recordUnmarshaller.unmarshal(sr);
        RecordType result = (RecordType) jb.getValue();
        
        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));
        
        SimpleLiteral modified   = new SimpleLiteral("2007-11-15 21:26:49");
        SimpleLiteral Abstract   = new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.");
        SimpleLiteral references = new SimpleLiteral("http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml");
        SimpleLiteral spatial    = new SimpleLiteral("northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;");
        
        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        
        RecordType expResult = new RecordType(id, title, type, subject, null, modified, Abstract, bbox, null, null, null, spatial, references);
        
        System.out.println("RESULT: " + result.toString());
        System.out.println("");
        System.out.println("EXPRESULT: " + expResult.toString());
        System.out.println("-----------------------------------------------------------");
        assertEquals(expResult, result);
    }

}
