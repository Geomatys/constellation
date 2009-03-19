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

package org.constellation.generic.database;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericConfigurationXMLBindingTest {

    private Logger       logger = Logger.getLogger("org.constellation.generic.database");
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
        JAXBContext jbcontext = JAXBContext.newInstance(Automatic.class);
        unmarshaller    = jbcontext.createUnmarshaller();
        marshaller      = jbcontext.createMarshaller();
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
    public void marshalingTest() throws Exception {

        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");
        
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("staticVar01", "something");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));
        Single single = new Single(query);

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        MultiFixed multi = new MultiFixed(mquery);

        Queries queries = new Queries(null, single, multi, parameters);
        Automatic config = new Automatic("MDWEB", bdd, queries);

        StringWriter sw = new StringWriter();
        marshaller.marshal(config, sw);

        String result = sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\">"                                                 + '\n' +
        "    <bdd>"                                                                    + '\n' +
        "        <className>org.driver.test</className>"                               + '\n' +
        "        <connectURL>http://somehost/blablabla</connectURL>"                   + '\n' +
        "        <user>bobby</user>"                                                   + '\n' +
        "        <password>juanito</password>"                                         + '\n' +
        "    </bdd>"                                                                   + '\n' +
        "    <queries>"                                                                + '\n' +
        "        <parameters>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar01</key>"                                       + '\n' +
        "                <value>something</value>"                                     + '\n' +
        "            </entry>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar02</key>"                                       + '\n' +
        "                <value>blavl, bloub</value>"                                  + '\n' +
        "            </entry>"                                                         + '\n' +
        "        </parameters>"                                                        + '\n' +
        "        <single>"                                                             + '\n' +
	    "            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "        </single>"                                                            + '\n' +
        "        <multiFixed>"                                                         + '\n' +
        "            <query name=\"multiQuery1\">"                                     + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var02</var>"                                     + '\n' +
        "                        <sql>pp.name</sql>"                                   + '\n' +
        "                    </col>"                                                   + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var03</var>"                                     + '\n' +
        "                        <sql>tr.id</sql>"                                     + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp, transduction tr</from>"          + '\n' +
        "                <where>tr.parameter=pp.id</where>"                            + '\n' +
        "            </query>"                                                         + '\n' +
        "        </multiFixed>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        assertEquals(expResult, result);
    
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void unmarshalingTest() throws Exception {


        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\">"                                                 + '\n' +
        "    <bdd>"                                                                    + '\n' +
        "        <className>org.driver.test</className>"                               + '\n' +
        "        <connectURL>http://somehost/blablabla</connectURL>"                   + '\n' +
        "        <user>bobby</user>"                                                   + '\n' +
        "        <password>juanito</password>"                                         + '\n' +
        "    </bdd>"                                                                   + '\n' +
        "    <queries>"                                                                + '\n' +
        "        <parameters>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar01</key>"                                       + '\n' +
        "                <value>something</value>"                                     + '\n' +
        "            </entry>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar02</key>"                                       + '\n' +
        "                <value>blavl, bloub</value>"                                  + '\n' +
        "            </entry>"                                                         + '\n' +
        "        </parameters>"                                                        + '\n' +
        "        <single>"                                                             + '\n' +
	    "            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "        </single>"                                                            + '\n' +
        "        <multiFixed>"                                                         + '\n' +
        "            <query name=\"multiQuery1\">"                                     + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var02</var>"                                     + '\n' +
        "                        <sql>pp.name</sql>"                                   + '\n' +
        "                    </col>"                                                   + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var03</var>"                                     + '\n' +
        "                        <sql>tr.id</sql>"                                     + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp, transduction tr</from>"          + '\n' +
        "                <where>tr.parameter=pp.id</where>"                            + '\n' +
        "            </query>"                                                         + '\n' +
        "        </multiFixed>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        StringReader sr = new StringReader(xml);

        Automatic result = (Automatic) unmarshaller.unmarshal(sr);


        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");

        HashMap<String, String> parameters = new HashMap<String, String>();
        ArrayList<String> sp1 = new ArrayList<String>();
        parameters.put("staticVar01", "something");
        ArrayList<String> sp2 = new ArrayList<String>();
        sp2.add("value1");
        sp2.add("value2");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));
        Single single = new Single(query);

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        MultiFixed multi = new MultiFixed(mquery);

        Queries queries     = new Queries(null, single, multi, parameters);
        Automatic expResult = new Automatic("MDWEB", bdd, queries);

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
