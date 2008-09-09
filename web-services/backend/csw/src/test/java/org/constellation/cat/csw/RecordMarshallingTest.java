/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package net.seagis.cat.csw;

// J2SE dependencies
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//seagis dependencies
import net.seagis.cat.csw.v202.RecordType;
import net.seagis.dublincore.v2.elements.SimpleLiteral;
import net.seagis.ows.v100.BoundingBoxType;
import net.seagis.ows.v100.WGS84BoundingBoxType;
import net.seagis.ws.rs.NamespacePrefixMapperImpl;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A Test suite verifying that the Record are correctly marshalled/unmarshalled
 * 
 * @author Guilhem Legal
 */
public class RecordMarshallingTest {
    
    private Logger       logger = Logger.getLogger("net.seagis.filter");
    private Unmarshaller recordUnmarshaller202;
    private Marshaller   recordMarshaller202;
    
    private Unmarshaller recordUnmarshaller200;
    private Marshaller   recordMarshaller200;
   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext202 = JAXBContext.newInstance("net.seagis.cat.csw.v202");
        recordUnmarshaller202    = jbcontext202.createUnmarshaller();
        recordMarshaller202      = jbcontext202.createMarshaller();
        recordMarshaller202.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        recordMarshaller202.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
        
        JAXBContext jbcontext200 = JAXBContext.newInstance("net.seagis.cat.csw.v200:net.seagis.dublincore.v1.terms:net.seagis.dublincore.v2.terms");
        recordUnmarshaller200    = jbcontext200.createUnmarshaller();
        recordMarshaller200      = jbcontext200.createMarshaller();
        recordMarshaller200.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        recordMarshaller200.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
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
        
        /*
         * Test marshalling csw Record v2.0.2
         */
        
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
        
        RecordType record = new RecordType(id, title, type, subject, null, modified, null, Abstract, bbox, null, null, null, spatial, references);
        
        StringWriter sw = new StringWriter();
        recordMarshaller202.marshal(record, sw);
        
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
        
        /*
         * Test marshalling csw Record v2.0.2
         */
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void recordUnmarshalingTest() throws Exception {
        
        /*
         * Test Unmarshalling csw Record v2.0.2
         */
        
        String xml = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Record xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dc:date>2007-12-01</dc:date>"                                         + '\n' +        
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
        
        JAXBElement jb = (JAXBElement) recordUnmarshaller202.unmarshal(sr);
        RecordType result = (RecordType) jb.getValue();
        
        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");
        
        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));
        
        SimpleLiteral modified   = new SimpleLiteral("2007-11-15 21:26:49");
        SimpleLiteral date       = new SimpleLiteral("2007-12-01");
        SimpleLiteral Abstract   = new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.");
        SimpleLiteral references = new SimpleLiteral("http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml");
        SimpleLiteral spatial    = new SimpleLiteral("northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;");
        
        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        
        RecordType expResult = new RecordType(id, title, type, subject, null, modified, date, Abstract, bbox, null, null, null, spatial, references);
        
        logger.info("DATE " +expResult.getDate() + " - " + result.getDate());
        assertEquals(expResult.getDate(), result.getDate());
        
        logger.finer("RESULT: " + result.toString());
        logger.finer("");
        logger.finer("EXPRESULT: " + expResult.toString());
        logger.finer("-----------------------------------------------------------");
        assertEquals(expResult, result);
        
        logger.info("ABSTRACT " +expResult.getAbstract() + " - " + result.getAbstract());
        assertEquals(expResult.getAbstract(), result.getAbstract());
        
        logger.info("SPATIAL " +expResult.getSpatial() + " - " + result.getSpatial());
        assertEquals(expResult.getSpatial(), result.getSpatial());
        
        /*
         * Test Unmarshalling csw Record v2.0.0 with http://purl... DC namespace
         */
        
        xml = 
        "<csw:Record xmlns:csw=\"http://www.opengis.net/cat/csw\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" + '\n' +
        "   <dc:identifier>ESIGNGRAVIMÉTRICOPENINSULAYBALEARES200703070000</dc:identifier>"                      + '\n' +
        "   <dc:title>Estudio Gravimétrico de la Península Ibérica y Baleares</dc:title>"                        + '\n' +      
        "   <dc:title>Mapa de Anomalías Gravimétricas</dc:title>"                                                + '\n' +
        "   <dc:creator>Instituto Geográfico Nacional</dc:creator>"                                              + '\n' +
        "   <dc:subject>http://www.fao.org/aos/concept#4668.Gravimetría</dc:subject>"                            + '\n' +
        "   <dc:subject>Anomalías Gravimétricas</dc:subject>"                                                    + '\n' +
        "   <dc:subject>Anomalías Aire Libre</dc:subject>"                                                       + '\n' +
        "   <dc:subject>Anomalías Bouguer</dc:subject>"                                                          + '\n' +
        "   <dc:subject>Información geocientífica</dc:subject>"                                                  + '\n' +
        "   <dc:description>El Estudio Gravimétrico de la Península Ibérica y Baleares, que representa las anomalías gravimétricas de esa zona, fue generado por el Instituto Geográfico Nacional en el año 1996. El estudio está constituido por dos mapas; Anomalías Gravimétricas Aire Libre de la Península Ibérica y Baleares, Anomalías Gravimétricas Bouguer de la Península Ibérica y Baleares más una memoria. Inicialmente para su generación se creó una base de datos gravimétrica homogénea a partir de observaciones de distinta procedencia, también se formó un modelo digital del terreno homogéneo a partir de otros modelos digitales del terreno procedentes de España, Portugal y Francia. Los mapas contienen isolíneas de anomalías gravimétricas en intervalos de 2mGal. Los datos se almacenan en formato DGN.</dc:description>" + '\n' +
        "   <dc:date>2007-03-07</dc:date>"                                                                       + '\n' +
        "   <dc:type>mapHardcopy</dc:type>"                                                                      + '\n' +
        "   <dc:type>mapDigital</dc:type>"                                                                       + '\n' +
        "   <dc:type>documentHardcopy</dc:type>"                                                                 + '\n' +
        "   <dc:format>DGN - Microstation format (Intergraph Corporation)</dc:format>"                           + '\n' +
        "   <dc:format>Papel</dc:format>"                                                                        + '\n' +
        "   <dc:identifier>www.cnig.es</dc:identifier>"                                                          + '\n' +
        "   <dc:source>El Banco de Datos Gravimétricos es una base de datos compuesta principalmente por las observaciones realizadas por el Instituto Geográfico Nacional desde 1960. Además se han añadido los datos del Instituto Portugués de Geografía y Catastro, del proyecto ECORS, de la Universidad de Cantabria y del Bureau Gravimétrico Internacional.</dc:source>" + '\n' +
        "   <dc:source>Para la creación del Modelo Digital del Terreno a escala 1:200.000 para toda la Península Ibérica, áreas marinas y terrestres adyacentes, en particular, se ha dispuesto de la siguiente información; Modelo Digital del Terreno a escala 1:200.000, Modelo Digital del Terreno obtenido del Defense Mapping Agency de los Estados Unidos, para completar la zona de Portugal; Modelo Digital del Terreno obtenido del Instituto Geográfico Nacional de Francia para la parte francesa del Pirineo y el Modelo Digital del Terreno generado a partir de las cartas náuticas del Instituto Hidrográfico de la Marina de España, que completa la parte marina hasta los 167 km. de la costa con un ancho de malla de 5 km.</dc:source>" + '\n' +
        "   <dc:language>es</dc:language>"                                                                       + '\n' +
        "   <dcterms:spatial>northlimit=43.83; southlimit=36.00; westlimit=-9.35; eastlimit=4.32;</dcterms:spatial>" + '\n' +
        "   <dcterms:spatial>ESPAÑA.ANDALUCÍA</dcterms:spatial>"                                                 + '\n' +
        "   <dcterms:spatial>ESPAÑA.ARAGÓN</dcterms:spatial>"                                                    + '\n' +
        "</csw:Record>";
        
        sr = new StringReader(xml);
        
        jb = (JAXBElement) recordUnmarshaller200.unmarshal(sr);
        net.seagis.cat.csw.v200.RecordType result2 = (net.seagis.cat.csw.v200.RecordType) jb.getValue();
        
        logger.info("result:" + result2.toString());
        
         /*
         * Test Unmarshalling csw Record v2.0.0 with http://www.purl... DC namespace
         */
        
        xml = 
        "<csw:Record xmlns:csw=\"http://www.opengis.net/cat/csw\" xmlns:dcterms=\"http://www.purl.org/dc/terms/\" xmlns:dc=\"http://www.purl.org/dc/elements/1.1/\">" + '\n' +
        "   <dc:identifier>ESIGNGRAVIMÉTRICOPENINSULAYBALEARES200703070000</dc:identifier>"                      + '\n' +
        "   <dc:title>Estudio Gravimétrico de la Península Ibérica y Baleares</dc:title>"                        + '\n' +      
        "   <dc:title>Mapa de Anomalías Gravimétricas</dc:title>"                                                + '\n' +
        "   <dc:creator>Instituto Geográfico Nacional</dc:creator>"                                              + '\n' +
        "   <dc:subject>http://www.fao.org/aos/concept#4668.Gravimetría</dc:subject>"                            + '\n' +
        "   <dc:subject>Anomalías Gravimétricas</dc:subject>"                                                    + '\n' +
        "   <dc:subject>Anomalías Aire Libre</dc:subject>"                                                       + '\n' +
        "   <dc:subject>Anomalías Bouguer</dc:subject>"                                                          + '\n' +
        "   <dc:subject>Información geocientífica</dc:subject>"                                                  + '\n' +
        "   <dc:description>El Estudio Gravimétrico de la Península Ibérica y Baleares, que representa las anomalías gravimétricas de esa zona, fue generado por el Instituto Geográfico Nacional en el año 1996. El estudio está constituido por dos mapas; Anomalías Gravimétricas Aire Libre de la Península Ibérica y Baleares, Anomalías Gravimétricas Bouguer de la Península Ibérica y Baleares más una memoria. Inicialmente para su generación se creó una base de datos gravimétrica homogénea a partir de observaciones de distinta procedencia, también se formó un modelo digital del terreno homogéneo a partir de otros modelos digitales del terreno procedentes de España, Portugal y Francia. Los mapas contienen isolíneas de anomalías gravimétricas en intervalos de 2mGal. Los datos se almacenan en formato DGN.</dc:description>" + '\n' +
        "   <dc:date>2007-03-07</dc:date>"                                                                       + '\n' +
        "   <dc:type>mapHardcopy</dc:type>"                                                                      + '\n' +
        "   <dc:type>mapDigital</dc:type>"                                                                       + '\n' +
        "   <dc:type>documentHardcopy</dc:type>"                                                                 + '\n' +
        "   <dc:format>DGN - Microstation format (Intergraph Corporation)</dc:format>"                           + '\n' +
        "   <dc:format>Papel</dc:format>"                                                                        + '\n' +
        "   <dc:identifier>www.cnig.es</dc:identifier>"                                                          + '\n' +
        "   <dc:source>El Banco de Datos Gravimétricos es una base de datos compuesta principalmente por las observaciones realizadas por el Instituto Geográfico Nacional desde 1960. Además se han añadido los datos del Instituto Portugués de Geografía y Catastro, del proyecto ECORS, de la Universidad de Cantabria y del Bureau Gravimétrico Internacional.</dc:source>" + '\n' +
        "   <dc:source>Para la creación del Modelo Digital del Terreno a escala 1:200.000 para toda la Península Ibérica, áreas marinas y terrestres adyacentes, en particular, se ha dispuesto de la siguiente información; Modelo Digital del Terreno a escala 1:200.000, Modelo Digital del Terreno obtenido del Defense Mapping Agency de los Estados Unidos, para completar la zona de Portugal; Modelo Digital del Terreno obtenido del Instituto Geográfico Nacional de Francia para la parte francesa del Pirineo y el Modelo Digital del Terreno generado a partir de las cartas náuticas del Instituto Hidrográfico de la Marina de España, que completa la parte marina hasta los 167 km. de la costa con un ancho de malla de 5 km.</dc:source>" + '\n' +
        "   <dc:language>es</dc:language>"                                                                       + '\n' +
        "   <dcterms:spatial>northlimit=43.83; southlimit=36.00; westlimit=-9.35; eastlimit=4.32;</dcterms:spatial>" + '\n' +
        "   <dcterms:spatial>ESPAÑA.ANDALUCÍA</dcterms:spatial>"                                                 + '\n' +
        "   <dcterms:spatial>ESPAÑA.ARAGÓN</dcterms:spatial>"                                                    + '\n' +
        "</csw:Record>";
        
        sr = new StringReader(xml);
        
        jb = (JAXBElement) recordUnmarshaller200.unmarshal(sr);
        result2 = (net.seagis.cat.csw.v200.RecordType) jb.getValue();
        
        logger.info("result:" + result2.toString());
    }

}
