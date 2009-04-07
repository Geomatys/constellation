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

package org.constellation.skos;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class SkosXmlBindingTest {

    private Logger       logger = Logger.getLogger("org.constellation.skos");
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
        JAXBContext jbcontext  = JAXBContext.newInstance("org.constellation.skos");
        unmarshaller           = jbcontext.createUnmarshaller();
        marshaller             = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));

    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void RDFMarshalingTest() throws Exception {
        List<Concept> concepts = new ArrayList<Concept>();
        Concept c1 = new Concept("http://www.geomatys.com/test/bonjour", null, "bonjour", "salut", "Un terme de politesse pour saluer son interlocuteur.", null);
        Concept c2 = new Concept("http://www.geomatys.com/test/pluie", null, "pluie", Arrays.asList("averse", "précipitation"), "Un evenement meteorologique qui fais tomber de l'eau sur la terre.", null);
        Concept c3 = new Concept("http://www.geomatys.com/test/livre", null, "livre", Arrays.asList("bouquin", "ouvrage"), "Une reliure de papier avec des chose plus ou moins interesante ecrite dessus.", null);
        concepts.add(c1);
        concepts.add(c2);
        concepts.add(c3);
        RDF rdf = new RDF(concepts);

        StringWriter sw = new StringWriter();
        marshaller.marshal(rdf, sw);

        String result = sw.toString();
        //we remove the xmlmns
        result = removeXmlns(result);

        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                                                            + '\n' +
        "<rdf:RDF >"                                                                                                               + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/bonjour\">"                                                    + '\n' +
        "        <skos:prefLabel>bonjour</skos:prefLabel>"                                                                         + '\n' +
        "        <skos:altLabel>salut</skos:altLabel>"                                                                             + '\n' +
        "        <skos:definition>Un terme de politesse pour saluer son interlocuteur.</skos:definition>"                          + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/pluie\">"                                                      + '\n' +
        "        <skos:prefLabel>pluie</skos:prefLabel>"                                                                           + '\n' +
        "        <skos:altLabel>averse</skos:altLabel>"                                                                            + '\n' +
        "        <skos:altLabel>précipitation</skos:altLabel>"                                                                     + '\n' +
        "        <skos:definition>Un evenement meteorologique qui fais tomber de l'eau sur la terre.</skos:definition>"            + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/livre\">"                                                      + '\n' +
        "        <skos:prefLabel>livre</skos:prefLabel>"                                                                           + '\n' +
        "        <skos:altLabel>bouquin</skos:altLabel>"                                                                           + '\n' +
        "        <skos:altLabel>ouvrage</skos:altLabel>"                                                                           + '\n' +
        "        <skos:definition>Une reliure de papier avec des chose plus ou moins interesante ecrite dessus.</skos:definition>" + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "</rdf:RDF>"                                                                                                               + '\n';

        logger.finer("result" + result);
        logger.finer("expected" + expResult);
        assertEquals(expResult, result);

    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void RDFUnMarshalingTest() throws Exception {

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                                                            + '\n' +
        "<rdf:RDF xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"                                                                                                               + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/bonjour\">"                                                    + '\n' +
        "        <skos:prefLabel>bonjour</skos:prefLabel>"                                                                         + '\n' +
        "        <skos:altLabel>salut</skos:altLabel>"                                                                             + '\n' +
        "        <skos:definition>Un terme de politesse pour saluer son interlocuteur.</skos:definition>"                          + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/pluie\">"                                                      + '\n' +
        "        <skos:prefLabel>pluie</skos:prefLabel>"                                                                           + '\n' +
        "        <skos:altLabel>averse</skos:altLabel>"                                                                            + '\n' +
        "        <skos:altLabel>précipitation</skos:altLabel>"                                                                     + '\n' +
        "        <skos:definition>Un evenement meteorologique qui fais tomber de l'eau sur la terre.</skos:definition>"            + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "    <skos:Concept rdf:about=\"http://www.geomatys.com/test/livre\">"                                                      + '\n' +
        "        <skos:prefLabel>livre</skos:prefLabel>"                                                                           + '\n' +
        "        <skos:altLabel>bouquin</skos:altLabel>"                                                                           + '\n' +
        "        <skos:altLabel>ouvrage</skos:altLabel>"                                                                           + '\n' +
        "        <skos:definition>Une reliure de papier avec des chose plus ou moins interesante ecrite dessus.</skos:definition>" + '\n' +
        "    </skos:Concept>"                                                                                                      + '\n' +
        "</rdf:RDF>";

        StringReader sr = new StringReader(xml);

        Object unmarshalled = unmarshaller.unmarshal(sr);

        assertTrue(unmarshalled instanceof RDF);

        RDF result = (RDF) unmarshalled;

        List<Concept> concepts = new ArrayList<Concept>();
        Concept c1 = new Concept("http://www.geomatys.com/test/bonjour", null, "bonjour", "salut", "Un terme de politesse pour saluer son interlocuteur.", null);
        Concept c2 = new Concept("http://www.geomatys.com/test/pluie", null, "pluie", Arrays.asList("averse", "précipitation"), "Un evenement meteorologique qui fais tomber de l'eau sur la terre.", null);
        Concept c3 = new Concept("http://www.geomatys.com/test/livre", null, "livre", Arrays.asList("bouquin", "ouvrage"), "Une reliure de papier avec des chose plus ou moins interesante ecrite dessus.", null);
        concepts.add(c1);
        concepts.add(c2);
        concepts.add(c3);
        RDF expResult = new RDF(concepts);

        assertEquals(expResult.getConcept(), result.getConcept());
        assertEquals(expResult, result);
    }

    public String removeXmlns(String xml) {

        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns=\"[^\"]*\"", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");

        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");


        return s;
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
            else if( "http://www.w3.org/2004/02/skos/core#".equals(namespaceUri) )
                prefix = "skos";

            else if( "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri) )
                prefix = "rdf";
            
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
