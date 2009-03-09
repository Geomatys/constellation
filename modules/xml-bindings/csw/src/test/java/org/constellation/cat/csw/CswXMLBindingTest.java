/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.cat.csw;

// J2SE dependencies
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
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

// Constellation dependencies
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.AbstractRecordType;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetRecordByIdResponseType;
import org.constellation.cat.csw.v202.GetRecordsType;
import org.constellation.cat.csw.v202.ObjectFactory;
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.cat.csw.v202.QueryType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.cat.csw.v202.ResultType;
import org.constellation.cat.csw.v202.SummaryRecordType;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.constellation.ogc.FilterType;
import org.constellation.ogc.NotType;
import org.constellation.ogc.PropertyIsLikeType;
import org.constellation.ogc.PropertyNameType;
import org.constellation.ows.v100.BoundingBoxType;
import org.constellation.ows.v100.WGS84BoundingBoxType;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A Test suite verifying that the Record are correctly marshalled/unmarshalled
 * 
 * @author Guilhem Legal
 */
public class CswXMLBindingTest {
    
    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private Unmarshaller recordUnmarshaller202;
    private Marshaller   recordMarshaller202;
    
    private Unmarshaller recordUnmarshaller200;
    private Marshaller   recordMarshaller200;
   
     /**
     * A JAXB factory to csw object version 2.0.2
     */
    protected final ObjectFactory cswFactory202 = new ObjectFactory();;
    
    /**
     * A JAXB factory to csw object version 2.0.0 
     */
    protected final org.constellation.cat.csw.v200.ObjectFactory cswFactory200 = new org.constellation.cat.csw.v200.ObjectFactory();
    
    /**
     * a QName for csw:Record type
     */
    private final static QName _Record_QNAME = new QName("http://www.opengis.net/cat/csw/2.0.2", "Record");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext202 = JAXBContext.newInstance("org.constellation.cat.csw.v202");
        recordUnmarshaller202    = jbcontext202.createUnmarshaller();
        recordMarshaller202      = jbcontext202.createMarshaller();
        recordMarshaller202.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        recordMarshaller202.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
        
        JAXBContext jbcontext200 = JAXBContext.newInstance("org.constellation.cat.csw.v200:org.constellation.dublincore.v1.terms:org.constellation.dublincore.v2.terms");
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
    
        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        
        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);
        
        assertEquals(expResult, result);
        
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
        "    <dc:format>binary</dc:format>"                                         + '\n' +        
        "    <dc:date>2007-12-01</dc:date>"                                         + '\n' +  
        "    <dc:publisher>geomatys</dc:publisher>"                                 + '\n' +           
        "    <dc:creator>geomatys</dc:creator>"                                 + '\n' +               
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
        
        SimpleLiteral modified    = new SimpleLiteral("2007-11-15 21:26:49");
        SimpleLiteral date        = new SimpleLiteral("2007-12-01");
        SimpleLiteral Abstract    = new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.");
        SimpleLiteral references  = new SimpleLiteral("http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml");
        SimpleLiteral spatial     = new SimpleLiteral("northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;");
        SimpleLiteral format      = new SimpleLiteral("binary");
        SimpleLiteral distributor = new SimpleLiteral("geomatys");
        SimpleLiteral creator = new SimpleLiteral("geomatys");
        
        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        
        RecordType expResult = new RecordType(id, title, type, subject, format, modified, date, Abstract, bbox, creator, distributor, null, spatial, references);
        
        logger.finer("DATE " +expResult.getDate() + " - " + result.getDate());
        assertEquals(expResult.getDate(), result.getDate());

        logger.finer("ABSTRACT " +expResult.getAbstract() + " - " + result.getAbstract());
        assertEquals(expResult.getAbstract(), result.getAbstract());
        
        logger.finer("SPATIAL " +expResult.getSpatial() + " - " + result.getSpatial());
        assertEquals(expResult.getSpatial(), result.getSpatial());
        
        logger.finer("BBOXES " +expResult.getBoundingBox() + " - " + result.getBoundingBox());
        assertEquals(expResult.getBoundingBox().get(0).getValue(), result.getBoundingBox().get(0).getValue());
        
                
        logger.finer("RESULT: " + result.toString());
        logger.finer("");
        logger.finer("EXPRESULT: " + expResult.toString());
        logger.finer("-----------------------------------------------------------");
        assertEquals(expResult, result);
        
        
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
        org.constellation.cat.csw.v200.RecordType result2 = (org.constellation.cat.csw.v200.RecordType) jb.getValue();
        
        logger.finer("result:" + result2.toString());
        
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
        result2 = (org.constellation.cat.csw.v200.RecordType) jb.getValue();
        
        logger.finer("result:" + result2.toString());
    }

    /**
     * Test summary Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void summmaryRecordMarshalingTest() throws Exception {

        /*
         * Test marshalling csw summmary Record v2.0.2
         */

        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");

        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));

        List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
        formats.add(new SimpleLiteral("format 11-11"));
        formats.add(new SimpleLiteral("format 22-22"));

        SimpleLiteral modified         = new SimpleLiteral("2007-11-15 21:26:49");
        List<SimpleLiteral> Abstract   = new ArrayList<SimpleLiteral>();
        Abstract.add(new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm."));

        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));

        SummaryRecordType record = new SummaryRecordType(id, title, type,  bbox, subject, formats, modified, Abstract);

        StringWriter sw = new StringWriter();
        recordMarshaller202.marshal(record, sw);

        String result = sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:SummaryRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dc:format>format 11-11</dc:format>"                                   + '\n' +
        "    <dc:format>format 22-22</dc:format>"                                   + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:SummaryRecord>" + '\n';

        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);

        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);

        assertEquals(expResult, result);


        /*
         * Test marshalling csw summmary Record v2.0.2
         */

        List<SimpleLiteral> ids    = new ArrayList<SimpleLiteral>();
        ids.add(new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}"));
        ids.add(new SimpleLiteral("urn:ogc-x:df:F7807C8AB645"));
        List<SimpleLiteral> titles = new ArrayList<SimpleLiteral>();
        titles.add(new SimpleLiteral("(JASON-1)"));
        titles.add(new SimpleLiteral("(JASON-2)"));

        type       = new SimpleLiteral("clearinghouse");

        subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));

        formats = new ArrayList<SimpleLiteral>();
        formats.add(new SimpleLiteral("format 11-11"));
        formats.add(new SimpleLiteral("format 22-22"));

        List<SimpleLiteral> modifieds   = new ArrayList<SimpleLiteral>();
        modifieds.add(new SimpleLiteral("2007-11-15 21:26:49"));
        modifieds.add(new SimpleLiteral("2007-11-15 21:26:48"));
        Abstract   = new ArrayList<SimpleLiteral>();
        Abstract.add(new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm."));
        Abstract.add(new SimpleLiteral("Jason-2 blablablablabla."));

        bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        bbox.add(new WGS84BoundingBoxType(100, -6.04, -144, 5.9));

        record = new SummaryRecordType(ids, titles, type,  bbox, subject, formats, modifieds, Abstract);

        sw = new StringWriter();
        recordMarshaller202.marshal(record, sw);

        result = sw.toString();
        expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:SummaryRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:identifier>urn:ogc-x:df:F7807C8AB645</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:title>(JASON-2)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dc:format>format 11-11</dc:format>"                                   + '\n' +
        "    <dc:format>format 22-22</dc:format>"                                   + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:modified>2007-11-15 21:26:48</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <dct:abstract>Jason-2 blablablablabla.</dct:abstract>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>100.0 -6.04</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-144.0 5.9</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:SummaryRecord>" + '\n';

        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);

        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);

        assertEquals(expResult, result);

    }

    /**
     * Test summary Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void summmaryRecordUnmarshalingTest() throws Exception {

        /*
         * Test marshalling csw summmary Record v2.0.2
         */

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:SummaryRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dc:format>format 11-11</dc:format>"                                   + '\n' +
        "    <dc:format>format 22-22</dc:format>"                                   + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:SummaryRecord>" + '\n';

        StringReader sr = new StringReader(xml);
        JAXBElement<SummaryRecordType> jb = (JAXBElement) recordUnmarshaller202.unmarshal(sr);
        SummaryRecordType result = jb.getValue();

        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");

        List<SimpleLiteral> subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));

        List<SimpleLiteral> formats = new ArrayList<SimpleLiteral>();
        formats.add(new SimpleLiteral("format 11-11"));
        formats.add(new SimpleLiteral("format 22-22"));

        SimpleLiteral modified         = new SimpleLiteral("2007-11-15 21:26:49");
        List<SimpleLiteral> Abstract   = new ArrayList<SimpleLiteral>();
        Abstract.add(new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm."));

        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));

        SummaryRecordType expResult = new SummaryRecordType(id, title, type,  bbox, subject, formats, modified, Abstract);

        assertEquals(expResult, result);


        /*
         * Test marshalling csw summmary Record v2.0.2
         */

        xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:SummaryRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "    <dc:identifier>urn:ogc-x:df:F7807C8AB645</dc:identifier>" + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "    <dc:title>(JASON-2)</dc:title>"                                        + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "    <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "    <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "    <dc:format>format 11-11</dc:format>"                                   + '\n' +
        "    <dc:format>format 22-22</dc:format>"                                   + '\n' +
        "    <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "    <dct:modified>2007-11-15 21:26:48</dct:modified>"                      + '\n' +
        "    <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "    <dct:abstract>Jason-2 blablablablabla.</dct:abstract>" + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                                + '\n' +
        "        <ows:LowerCorner>100.0 -6.04</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-144.0 5.9</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                               + '\n' +
        "</csw:SummaryRecord>" + '\n';

        sr = new StringReader(xml);
        jb = (JAXBElement) recordUnmarshaller202.unmarshal(sr);
        result = jb.getValue();


        List<SimpleLiteral> ids    = new ArrayList<SimpleLiteral>();
        ids.add(new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}"));
        ids.add(new SimpleLiteral("urn:ogc-x:df:F7807C8AB645"));
        List<SimpleLiteral> titles = new ArrayList<SimpleLiteral>();
        titles.add(new SimpleLiteral("(JASON-1)"));
        titles.add(new SimpleLiteral("(JASON-2)"));

        type       = new SimpleLiteral("clearinghouse");

        subject = new ArrayList<SimpleLiteral>();
        subject.add(new SimpleLiteral("oceans elevation NASA/JPL/JASON-1"));
        subject.add(new SimpleLiteral("oceans elevation 2"));

        formats = new ArrayList<SimpleLiteral>();
        formats.add(new SimpleLiteral("format 11-11"));
        formats.add(new SimpleLiteral("format 22-22"));

        List<SimpleLiteral> modifieds   = new ArrayList<SimpleLiteral>();
        modifieds.add(new SimpleLiteral("2007-11-15 21:26:49"));
        modifieds.add(new SimpleLiteral("2007-11-15 21:26:48"));
        Abstract   = new ArrayList<SimpleLiteral>();
        Abstract.add(new SimpleLiteral("Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm."));
        Abstract.add(new SimpleLiteral("Jason-2 blablablablabla."));

        bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        bbox.add(new WGS84BoundingBoxType(100, -6.04, -144, 5.9));

        expResult = new SummaryRecordType(ids, titles, type,  bbox, subject, formats, modifieds, Abstract);

        assertEquals(expResult, result);

    }

    /**
     * Test brief Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void briefRecordMarshalingTest() throws Exception {

        /*
         * Test marshalling BRIEF csw Record v2.0.2
         */

        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");

        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));

        BriefRecordType record = new BriefRecordType(id, title, type, bbox);

        StringWriter sw = new StringWriter();
        recordMarshaller202.marshal(record, sw);

        String result = sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"               + '\n' +
        "<csw:BriefRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>"   + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                          + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                        + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "</csw:BriefRecord>" + '\n';

        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);

        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);

        assertEquals(expResult, result);

         /*
         * Test marshalling csw Record v2.0.2
         */

        List<SimpleLiteral> identifiers = new ArrayList<SimpleLiteral>();
        identifiers.add(new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}"));
        identifiers.add(new SimpleLiteral("urn:ogc:x-def:F7807C8AB645"));

        List<SimpleLiteral> titles = new ArrayList<SimpleLiteral>();
        titles.add(new SimpleLiteral("(JASON-1)"));
        titles.add(new SimpleLiteral("(JASON-2)"));

        type       = new SimpleLiteral("clearinghouse");

        bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        bbox.add(new WGS84BoundingBoxType(176, -16.4, -178, 6.1));

        record = new BriefRecordType(identifiers, titles, type, bbox);

        sw = new StringWriter();
        recordMarshaller202.marshal(record, sw);

        result = sw.toString();
        expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"               + '\n' +
        "<csw:BriefRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>"   + '\n' +
        "    <dc:identifier>urn:ogc:x-def:F7807C8AB645</dc:identifier>"               + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                          + '\n' +
        "    <dc:title>(JASON-2)</dc:title>"                                          + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                        + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>176.0 -16.4</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-178.0 6.1</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "</csw:BriefRecord>" + '\n';

        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);

        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);

        assertEquals(expResult, result);

    }

    /**
     * Test brief Record Unmarshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void briefRecordUnmarshalingTest() throws Exception {

        /*
         * Test marshalling BRIEF csw Record v2.0.2
         */

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"               + '\n' +
        "<csw:BriefRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>"   + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                          + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                        + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "</csw:BriefRecord>" + '\n';

        StringReader sr = new StringReader(xml);
        JAXBElement<BriefRecordType> jb = (JAXBElement) recordUnmarshaller202.unmarshal(sr);
        BriefRecordType result = jb.getValue();

        SimpleLiteral id         = new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}");
        SimpleLiteral title      = new SimpleLiteral("(JASON-1)");
        SimpleLiteral type       = new SimpleLiteral("clearinghouse");

        List<BoundingBoxType> bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));

        BriefRecordType expResult = new BriefRecordType(id, title, type, bbox);

        assertEquals(expResult, result);

         /*
         * Test marshalling csw Record v2.0.2
         */

        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"               + '\n' +
        "<csw:BriefRecord xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>"   + '\n' +
        "    <dc:identifier>urn:ogc:x-def:F7807C8AB645</dc:identifier>"               + '\n' +
        "    <dc:title>(JASON-1)</dc:title>"                                          + '\n' +
        "    <dc:title>(JASON-2)</dc:title>"                                          + '\n' +
        "    <dc:type>clearinghouse</dc:type>"                                        + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "    <ows:WGS84BoundingBox dimensions=\"2\">"                                 + '\n' +
        "        <ows:LowerCorner>176.0 -16.4</ows:LowerCorner>"          + '\n' +
        "        <ows:UpperCorner>-178.0 6.1</ows:UpperCorner>"          + '\n' +
        "    </ows:WGS84BoundingBox>"                                                 + '\n' +
        "</csw:BriefRecord>" + '\n';

        sr = new StringReader(xml);
        jb = (JAXBElement<BriefRecordType>) recordUnmarshaller202.unmarshal(sr);
        result = jb.getValue();

        List<SimpleLiteral> identifiers = new ArrayList<SimpleLiteral>();
        identifiers.add(new SimpleLiteral("{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}"));
        identifiers.add(new SimpleLiteral("urn:ogc:x-def:F7807C8AB645"));

        List<SimpleLiteral> titles = new ArrayList<SimpleLiteral>();
        titles.add(new SimpleLiteral("(JASON-1)"));
        titles.add(new SimpleLiteral("(JASON-2)"));

        type       = new SimpleLiteral("clearinghouse");

        bbox = new ArrayList<BoundingBoxType>();
        bbox.add(new WGS84BoundingBoxType(180, -66.0000000558794, -180, 65.9999999720603));
        bbox.add(new WGS84BoundingBoxType(176, -16.4, -178, 6.1));

        expResult = new BriefRecordType(identifiers, titles, type, bbox);

        assertEquals(expResult, result);

    }
    
    /**
     * Test getRecordById request Marshalling.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordByIdResponseMarshalingTest() throws Exception {
        
         /*
         * Test marshalling csw getRecordByIdResponse v2.0.2
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
        
        RecordType record           = new RecordType(id, title, type, subject, null, modified, null, Abstract, bbox, null, null, null, spatial, references);
        BriefRecordType briefRecord = new BriefRecordType(id, title, type, bbox);
        SummaryRecordType sumRecord = new SummaryRecordType(id, title, type, bbox, subject, null, modified, Abstract);
        
        List<AbstractRecordType> records = new ArrayList<AbstractRecordType>(); 
        records.add(record);
        records.add(briefRecord);
        records.add(sumRecord);
        GetRecordByIdResponse response = new GetRecordByIdResponseType(records, null);
        
        StringWriter sw = new StringWriter();
        recordMarshaller202.marshal(response, sw);
        
        String result = sw.toString();
        
        
        String expResult = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:GetRecordByIdResponse xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <csw:Record>"                                                              + '\n' +
        "        <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "        <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "        <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "        <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "        <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "        <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "        <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "        <dct:references>http://keel.esri.com/output/TOOLKIT_Browse_Metadata_P7540_T8020_D1098.xml</dct:references>" + '\n' +
        "        <dct:spatial>northlimit=65.9999999720603; eastlimit=180; southlimit=-66.0000000558794; westlimit=-180;</dct:spatial>" + '\n' +
        "        <ows:WGS84BoundingBox dimensions=\"2\">"                               + '\n' +
        "            <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"        + '\n' +
        "            <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"        + '\n' +
        "        </ows:WGS84BoundingBox>"                                               + '\n' +
        "    </csw:Record>"                                                             + '\n' +
        "    <csw:BriefRecord>"                                                         + '\n' +
        "        <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "        <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "        <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "        <ows:WGS84BoundingBox dimensions=\"2\">"                               + '\n' +
        "            <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"        + '\n' +
        "            <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"        + '\n' +
        "        </ows:WGS84BoundingBox>"                                               + '\n' +
        "    </csw:BriefRecord>"                                                        + '\n' +
        "    <csw:SummaryRecord>"                                                              + '\n' +
        "        <dc:identifier>{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}</dc:identifier>" + '\n' +
        "        <dc:title>(JASON-1)</dc:title>"                                        + '\n' +
        "        <dc:type>clearinghouse</dc:type>"                                      + '\n' +
        "        <dc:subject>oceans elevation NASA/JPL/JASON-1</dc:subject>"            + '\n' +
        "        <dc:subject>oceans elevation 2</dc:subject>"                           + '\n' +
        "        <dct:modified>2007-11-15 21:26:49</dct:modified>"                      + '\n' +
        "        <dct:abstract>Jason-1 is the first follow-on to the highly successful TOPEX/Poseidonmission that measured ocean surface topography to an accuracy of 4.2cm.</dct:abstract>" + '\n' +
        "        <ows:WGS84BoundingBox dimensions=\"2\">"                               + '\n' +
        "            <ows:LowerCorner>180.0 -66.0000000558794</ows:LowerCorner>"        + '\n' +
        "            <ows:UpperCorner>-180.0 65.9999999720603</ows:UpperCorner>"        + '\n' +
        "        </ows:WGS84BoundingBox>"                                               + '\n' +
        "    </csw:SummaryRecord>"                                                             + '\n' +
        "</csw:GetRecordByIdResponse>" + '\n';
        
        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        
        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);
        
        logger.finer("RESULT:" + '\n' + result);
        logger.finer("EXPRESULT:" + '\n' + expResult);
        assertEquals(expResult, result);
        
 
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordsMarshalingTest() throws Exception {
        
         /*
         * Test marshalling csw getRecordByIdResponse v2.0.2
         */
        
        /*
         * we build the first filter : < dublinCore:Title IS LIKE '*' >
         */ 
        List<QName> typeNames  = new ArrayList<QName>();
        PropertyNameType pname = new PropertyNameType("dc:Title");
        PropertyIsLikeType pil = new PropertyIsLikeType(pname, "something?", "*", "?", "\\");
        NotType n              = new NotType(pil);
        FilterType filter1     = new FilterType(n);
        
        /*
         * Second filter a special case for some unstandardized CSW : < title IS NOT LIKE 'something' >
         */
        typeNames          = new ArrayList<QName>();
        pname              = new PropertyNameType("title");
        pil                = new PropertyIsLikeType(pname, "something", null, null, null);
        n                  = new NotType(pil);
        FilterType filter2 = new FilterType(n);
        
        QueryConstraintType constraint = new QueryConstraintType(filter1, "1.1.0");
        typeNames.add(_Record_QNAME);
        QueryType query = new QueryType(typeNames, new ElementSetNameType(ElementSetType.FULL), null, constraint); 
        
        GetRecordsType getRecordsRequest = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 20, query, null);
         
        
        StringWriter sw = new StringWriter();
        recordMarshaller202.marshal(getRecordsRequest, sw);
        
        String result = sw.toString();
        
        
        String expResult = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:GetRecords maxRecords=\"20\" startPosition=\"1\" outputSchema=\"http://www.opengis.net/cat/csw/2.0.2\" outputFormat=\"application/xml\" resultType=\"results\" version=\"2.0.2\" service=\"CSW\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <csw:Query typeNames=\"csw:Record\">" + '\n' +
        "        <csw:ElementSetName>full</csw:ElementSetName>"                         + '\n' +
        "        <csw:Constraint version=\"1.1.0\">"                                    + '\n' +
        "            <ogc:Filter>"                                                      + '\n' +
        "                <ogc:Not>"                                                     + '\n' +
        "                    <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"?\" escapeChar=\"\\\">"    + '\n' +
        "                        <ogc:PropertyName>dc:Title</ogc:PropertyName>"         + '\n' +
        "                        <ogc:Literal>something?</ogc:Literal>"                 + '\n' +
        "                    </ogc:PropertyIsLike>"                                     + '\n' +
        "                </ogc:Not>"                                                    + '\n' +
        "            </ogc:Filter>"                                                     + '\n' +
        "        </csw:Constraint>"                                                     + '\n' +
        "    </csw:Query>"                                                              + '\n' +
        "</csw:GetRecords>" + '\n';
        logger.finer("RESULT:" + '\n' + result);
        
        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        
        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);
        
        logger.finer("RESULT:" + '\n' + result);
        logger.finer("EXPRESULT:" + '\n' + expResult);
        assertEquals(expResult, result);
        
 
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordsUnMarshalingTest() throws Exception {
        
         /*
         * Test unmarshalling csw getRecordByIdResponse v2.0.2
         */
        
        String xml = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:GetRecords maxRecords=\"20\" startPosition=\"1\" outputSchema=\"http://www.opengis.net/cat/csw/2.0.2\" outputFormat=\"application/xml\" resultType=\"results\" version=\"2.0.2\" service=\"CSW\"  xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">" + '\n' +
        "    <csw:Query typeNames=\"csw:Record\">" + '\n' +
        "        <csw:ElementSetName>full</csw:ElementSetName>"                         + '\n' +
        "        <csw:Constraint version=\"1.1.0\">"                                    + '\n' +
        "            <ogc:Filter>"                                                      + '\n' +
        "                <ogc:Not>"                                                     + '\n' +
        "                    <ogc:PropertyIsLike wildCard=\"*\" singleChar=\"?\" escapeChar=\"\\\">"    + '\n' +
        "                        <ogc:PropertyName>dc:Title</ogc:PropertyName>"         + '\n' +
        "                        <ogc:Literal>something?</ogc:Literal>"                 + '\n' +
        "                    </ogc:PropertyIsLike>"                                     + '\n' +
        "                </ogc:Not>"                                                    + '\n' +
        "            </ogc:Filter>"                                                     + '\n' +
        "        </csw:Constraint>"                                                     + '\n' +
        "    </csw:Query>"                                                              + '\n' +
        "</csw:GetRecords>" + '\n';
        
        StringReader sr = new StringReader(xml);
        
        Object result = recordUnmarshaller202.unmarshal(sr);
        
        /*
         * we build the first filter : < dublinCore:Title IS LIKE '*' >
         */ 
        List<QName> typeNames  = new ArrayList<QName>();
        PropertyNameType pname = new PropertyNameType("dc:Title");
        PropertyIsLikeType pil = new PropertyIsLikeType(pname, "something?", "*", "?", "\\");
        NotType n              = new NotType(pil);
        FilterType filter1     = new FilterType(n);
        
        /*
         * Second filter a special case for some unstandardized CSW : < title IS NOT LIKE 'something' >
         
        typeNames          = new ArrayList<QName>();
        pname              = new PropertyNameType("title");
        pil                = new PropertyIsLikeType(pname, "something", null, null, null);
        n                  = new NotType(pil);
        FilterType filter2 = new FilterType(n);*/
        
        QueryConstraintType constraint = new QueryConstraintType(filter1, "1.1.0");
        typeNames.add(_Record_QNAME);
        QueryType query = new QueryType(typeNames, new ElementSetNameType(ElementSetType.FULL), null, constraint); 
        
        GetRecordsType expResult = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 20, query, null);
         
        
        
        logger.info("RESULT:" + '\n' + result);
        logger.info("EXPRESULT:" + '\n' + expResult);
        GetRecordsType gres = (GetRecordsType)result;
        QueryType expQT = (QueryType) expResult.getAbstractQuery();
        QueryType resQT = (QueryType) gres.getAbstractQuery();

        assertEquals(expQT.getConstraint().getFilter().getLogicOps().getValue(), resQT.getConstraint().getFilter().getLogicOps().getValue());
        assertEquals(expQT.getConstraint().getFilter(), resQT.getConstraint().getFilter());
        assertEquals(expQT.getConstraint(), resQT.getConstraint());
        assertEquals(expResult.getAbstractQuery(), gres.getAbstractQuery());
        assertEquals(expResult, result);
        
 
    }
    
    
    class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

        /**
         * if set this namespace will be the root of the document with no prefix.
         */
        private String rootNamespace;

        public NamespacePrefixMapperImpl(String rootNamespace) {
            super();
            this.rootNamespace = rootNamespace;

        }

        /**
         * Returns a preferred prefix for the given namespace URI.
         */
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            String prefix = null;

            if (rootNamespace != null && rootNamespace.equals(namespaceUri)) {
                prefix = "";
            } else  if ("http://www.opengis.net/gml".equals(namespaceUri)) {
                prefix = "gml";
            } else if ("http://www.opengis.net/ogc".equals(namespaceUri)) {
                prefix = "ogc";
            } else if ("http://www.opengis.net/ows/1.1".equals(namespaceUri)) {
                prefix = "ows";
            } else if ("http://www.opengis.net/ows".equals(namespaceUri)) {
                prefix = "ows";
            } else if ("http://www.opengis.net/wms".equals(namespaceUri)) {
                prefix = "wms";
            } else if ("http://www.w3.org/1999/xlink".equals(namespaceUri)) {
                prefix = "xlink";
            } else if ("http://www.opengis.net/sld".equals(namespaceUri)) {
                prefix = "sld";
            } else if ("http://www.opengis.net/wcs".equals(namespaceUri)) {
                prefix = "wcs";
            } else if ("http://www.opengis.net/wcs/1.1.1".equals(namespaceUri)) {
                prefix = "wcs";
            } else if ("http://www.opengis.net/se".equals(namespaceUri)) {
                prefix = "se";
            } else if ("http://www.opengis.net/sos/1.0".equals(namespaceUri)) {
                prefix = "sos";
            } else if ("http://www.opengis.net/om/1.0".equals(namespaceUri)) {
                prefix = "om";
            } else if ("http://www.opengis.net/sensorML/1.0".equals(namespaceUri)) {
                prefix = "sml";
            } else if ("http://www.opengis.net/swe/1.0.1".equals(namespaceUri)) {
                prefix = "swe";
            } else if ("http://www.opengis.net/sa/1.0".equals(namespaceUri)) {
                prefix = "sa";
            } else if ("http://www.opengis.net/cat/csw/2.0.2".equals(namespaceUri)) {
                prefix = "csw";
            } else if ("http://purl.org/dc/elements/1.1/".equals(namespaceUri)) {
                prefix = "dc";
            } else if ("http://www.purl.org/dc/elements/1.1/".equals(namespaceUri)) {
                prefix = "dc2";
            } else if ("http://purl.org/dc/terms/".equals(namespaceUri)) {
                prefix = "dct";
            } else if ("http://www.purl.org/dc/terms/".equals(namespaceUri)) {
                prefix = "dct2";
            } else if ("http://www.isotc211.org/2005/gmd".equals(namespaceUri)) {
                prefix = "gmd";
            } else if ("http://www.isotc211.org/2005/gco".equals(namespaceUri)) {
                prefix = "gco";
            } else if ("http://www.isotc211.org/2005/srv".equals(namespaceUri)) {
                prefix = "srv";
            } else if ("http://www.isotc211.org/2005/gfc".equals(namespaceUri)) {
                prefix = "gfc";
            } else if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
                prefix = "xsi";
            } else if ("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0".equals(namespaceUri)) {
                prefix = "rim";
            } else if ("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5".equals(namespaceUri)) {
                prefix = "rim25";
            } else if ("http://www.opengis.net/cat/wrs/1.0".equals(namespaceUri)) {
                prefix = "wrs";
            } else if ("http://www.opengis.net/cat/wrs".equals(namespaceUri)) {
                prefix = "wrs09";
            } else if ("http://www.cnig.gouv.fr/2005/fra".equals(namespaceUri)) {
                prefix = "fra";
            }
            return prefix;
        }

        /**
         * Returns a list of namespace URIs that should be declared
         * at the root element.
         */
        @Override
        public String[] getPreDeclaredNamespaceUris() {
            return new String[]{};
        }
    }
}
