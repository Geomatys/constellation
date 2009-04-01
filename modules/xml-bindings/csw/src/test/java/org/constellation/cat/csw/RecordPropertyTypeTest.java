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

package org.constellation.cat.csw;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.StringReader;
import java.util.logging.Logger;

//Junit dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.cat.csw.v202.RecordPropertyType;
import org.constellation.cat.csw.v202.TransactionType;
import org.constellation.cat.csw.v202.UpdateType;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RecordPropertyTypeTest {

    private Logger       logger = Logger.getLogger("org.constellation.filter");

    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

        JAXBContext jbcontext202 = JAXBContext.newInstance("org.constellation.cat.csw.v202");
        unmarshaller    = jbcontext202.createUnmarshaller();
        marshaller      = jbcontext202.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
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
    public void getValueStringTest() throws Exception {

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" verboseResponse=\"false\" version=\"2.0.2\" service=\"CSW\" >"             + '\n' +
        "    <csw:Update>"                                                                           + '\n' +
        "        <csw:RecordProperty>"                                                               + '\n' +
        "            <csw:Name>/csw:Record/dc:contributor</csw:Name>"                                + '\n' +
        "            <csw:Value>Jane</csw:Value>"                                                    + '\n' +
        "        </csw:RecordProperty>"                                                              + '\n' +
        "        <csw:Constraint version=\"1.1.0\">"                                                 + '\n' +
        "            <csw:CqlText>identifier='{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}'</csw:CqlText>" + '\n' +
        "        </csw:Constraint>"                                                                  + '\n' +
        "    </csw:Update>"                                                                          + '\n' +
        "</csw:Transaction>"+ '\n';

        TransactionType result = (TransactionType) unmarshaller.unmarshal(new StringReader(xml));

        assertTrue(result.getInsertOrUpdateOrDelete().size() == 1);
        assertTrue(result.getInsertOrUpdateOrDelete().get(0) instanceof UpdateType);

        UpdateType update = (UpdateType) result.getInsertOrUpdateOrDelete().get(0);

        assertTrue(update.getRecordProperty().size() == 1);

        RecordPropertyType property = update.getRecordProperty().get(0);

        assertEquals("Jane", property.getValue());

        xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" verboseResponse=\"false\" version=\"2.0.2\" service=\"CSW\" >"             + '\n' +
        "    <csw:Update>"                                                                           + '\n' +
        "        <csw:RecordProperty>"                                                               + '\n' +
        "            <csw:Name>/csw:Record/dc:contributor</csw:Name>"                                + '\n' +
        "            <csw:Value xsi:type=\"xs:string\">Jane</csw:Value>"                                                    + '\n' +
        "        </csw:RecordProperty>"                                                              + '\n' +
        "        <csw:Constraint version=\"1.1.0\">"                                                 + '\n' +
        "            <csw:CqlText>identifier='{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}'</csw:CqlText>" + '\n' +
        "        </csw:Constraint>"                                                                  + '\n' +
        "    </csw:Update>"                                                                          + '\n' +
        "</csw:Transaction>"+ '\n';

        result = (TransactionType) unmarshaller.unmarshal(new StringReader(xml));

        assertTrue(result.getInsertOrUpdateOrDelete().size() == 1);
        assertTrue(result.getInsertOrUpdateOrDelete().get(0) instanceof UpdateType);

        update = (UpdateType) result.getInsertOrUpdateOrDelete().get(0);

        assertTrue(update.getRecordProperty().size() == 1);

        property = update.getRecordProperty().get(0);

        assertEquals("Jane", property.getValue());
    }

    @Test
    public void getValueComplexTypeTest() throws Exception {

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" verboseResponse=\"false\" version=\"2.0.2\" service=\"CSW\" >" + '\n' +
        "    <csw:Update>"                                                                           + '\n' +
        "        <csw:RecordProperty>"                                                               + '\n' +
        "            <csw:Name>/csw:Record/dc:contributor</csw:Name>"                                + '\n' +
        "            <csw:Value>"                                                                    + '\n' +
        "                <gmd:EX_GeographicBoundingBox>"                                             + '\n' +
        "                    <gmd:extentTypeCode>"                                                   + '\n' +
        "                        <gco:Boolean>true</gco:Boolean>"                                    + '\n' +
        "                    </gmd:extentTypeCode>"                                                  + '\n' +
        "                    <gmd:westBoundLongitude>"                                               + '\n' +
        "                        <gco:Decimal>1.1667</gco:Decimal>"                                  + '\n' +
        "                    </gmd:westBoundLongitude>"                                              + '\n' +
        "                    <gmd:eastBoundLongitude>"                                               + '\n' +
        "                        <gco:Decimal>1.1667</gco:Decimal>"                                  + '\n' +
        "                    </gmd:eastBoundLongitude>"                                              + '\n' +
        "                    <gmd:southBoundLatitude>"                                               + '\n' +
        "                         <gco:Decimal>36.6</gco:Decimal>"                                   + '\n' +
        "                    </gmd:southBoundLatitude>"                                              + '\n' +
        "                    <gmd:northBoundLatitude>"                                               + '\n' +
        "                         <gco:Decimal>36.6</gco:Decimal>"                                   + '\n' +
        "                    </gmd:northBoundLatitude>"                                              + '\n' +
        "                </gmd:EX_GeographicBoundingBox>"                                            + '\n' +
        "            </csw:Value>"                                                                   + '\n' +
        "        </csw:RecordProperty>"                                                              + '\n' +
        "        <csw:Constraint version=\"1.1.0\">"                                                 + '\n' +
        "            <csw:CqlText>identifier='{8C71082D-5B3B-5F9D-FC40-F7807C8AB645}'</csw:CqlText>" + '\n' +
        "        </csw:Constraint>"                                                                  + '\n' +
        "    </csw:Update>"                                                                          + '\n' +
        "</csw:Transaction>"+ '\n';

        TransactionType result = (TransactionType) unmarshaller.unmarshal(new StringReader(xml));

        assertTrue(result.getInsertOrUpdateOrDelete().size() == 1);
        assertTrue(result.getInsertOrUpdateOrDelete().get(0) instanceof UpdateType);

        UpdateType update = (UpdateType) result.getInsertOrUpdateOrDelete().get(0);

        assertTrue(update.getRecordProperty().size() == 1);

        RecordPropertyType property = update.getRecordProperty().get(0);

        System.out.println(property.getValue());
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
            } else if("http://www.w3.org/2001/XMLSchema".equals(namespaceUri) )
            prefix = "xsd";
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
