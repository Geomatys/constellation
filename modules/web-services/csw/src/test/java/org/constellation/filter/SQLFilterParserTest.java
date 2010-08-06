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

package org.constellation.filter;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guilhem
 */
public class SQLFilterParserTest {
    
    private SQLFilterParser filterParser;
    private static final Logger LOGGER = Logger.getLogger("org.constellation.filter");
    private final static QName _ExtrinsicObject25_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "ExtrinsicObject");
    private final static QName _Association25_QNAME     = new QName("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", "Association");
    private static MarshallerPool pool;

    @BeforeClass
    public static void setUpClass() throws Exception {
        pool = new MarshallerPool("org.geotoolkit.ogc.xml.v110:org.geotoolkit.gml.xml.v311:org.geotoolkit.internal.jaxb.geometry");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        filterParser = new SQLFilterParser();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test simple comparison filter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void simpleComparisonFilterTest() throws Exception {

        Unmarshaller filterUnmarshaller = pool.acquireUnmarshaller();
        
        Map<String, String> prefixs = new HashMap<String, String>();
        prefixs.put("rim", "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
        prefixs.put("wrs", "http://www.opengis.net/cat/wrs");
        prefixs.put("wrs1", "http://www.opengis.net/cat/wrs/1.0");
        //filterParser.setPrefixs(prefixs);

        Map<String, QName> variables = new HashMap<String, QName>();
        variables.put("e1", _ExtrinsicObject25_QNAME);
        variables.put("a1", _Association25_QNAME);
        //filterParser.setVariables(variables);

        /**
         * Test 1: a simple Filter propertyIsLike
         */
        String XMLrequest =
                        "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
			"	<ogc:PropertyIsEqualTo> " +'\n' +
			"               <ogc:PropertyName>wrs:ExtrinsicObject/@mimeType</ogc:PropertyName>" +'\n' +
			"		<ogc:Literal>application/octet-stream</ogc:Literal>" +'\n' +
			"	</ogc:PropertyIsEqualTo>" +'\n' +
			" </ogc:Filter>";
        StringReader reader = new StringReader(XMLrequest);

        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        SQLQuery spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Web Registry Service v0.9:ExtrinsicObject:mimeType' AND v1.\"value\" ='application/octet-stream'  AND v1.\"form\"=\"identifier\" ");

        /**
         * Test 2: a simple Filter propertyIsNull
         */
        XMLrequest =    "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
			"	<ogc:PropertyIsNull> " +'\n' +
			"               <ogc:PropertyName>rim:ExtrinsicObject/@mimeType</ogc:PropertyName>" +'\n' +
			"	</ogc:PropertyIsNull>" +'\n' +
			" </ogc:Filter>";
        reader  = new StringReader(XMLrequest);

        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter  = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Ebrim v2.5:ExtrinsicObject:mimeType' AND v1.\"value\" IS NULL  AND v1.\"form\"=\"identifier\" ");

        /**
         * Test 3: a simple Filter propertyIsLike
         */
        XMLrequest =    "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
			"	<ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\"> "                                   +'\n' +
			"               <ogc:PropertyName>wrs1:ExtrinsicObject/@mimeType</ogc:PropertyName>"                                +'\n' +
                        "		<ogc:Literal>*application*</ogc:Literal>"                                                          +'\n' +
			"	</ogc:PropertyIsLike>" +'\n' +
			" </ogc:Filter>";
        reader  = new StringReader(XMLrequest);

        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter  = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Web Registry Service v1.0:ExtrinsicObject:mimeType' AND v1.\"value\" LIKE'%application%'  AND v1.\"form\"=\"identifier\" ");

        pool.release(filterUnmarshaller);
    }

    /**
     * Test simple comparison filter. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void multipleComparisonFilterTest() throws Exception {
       
        Unmarshaller filterUnmarshaller = pool.acquireUnmarshaller();
        
        Map<String, String> prefixs = new HashMap<String, String>();
        prefixs.put("rim", "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
        prefixs.put("rim3", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
        //filterParser.setPrefixs(prefixs);
        
        Map<String, QName> variables = new HashMap<String, QName>();
        variables.put("e1", _ExtrinsicObject25_QNAME);
        variables.put("a1", _Association25_QNAME);
        //filterParser.setVariables(variables);

        /**
         * Test 1: a multiple Filter propertyIsEqualTo
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
			"	<ogc:And>" +'\n' +
			 "		<ogc:PropertyIsEqualTo> " +'\n' +
			"			<ogc:PropertyName>rim:ExtrinsicObject/@mimeType</ogc:PropertyName>" +'\n' +
			"		 	<ogc:Literal>application/octet-stream</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@home</ogc:PropertyName>" +'\n' +
		     	"			<ogc:Literal>http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@minorVersion</ogc:PropertyName>" +'\n' +
			"			<ogc:Literal>0</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@majorVersion</ogc:PropertyName>" +'\n' +
			"			<ogc:Literal>1</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"	</ogc:And>" +'\n' +
			" </ogc:Filter>";
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);
        
        SQLQuery spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 , \"TextValues\" v2 , \"TextValues\" v3 , \"TextValues\" v4 WHERE v1.\"path\" = 'Ebrim v2.5:ExtrinsicObject:mimeType' AND v1.\"value\" ='application/octet-stream'  AND v1.\"form\"=\"identifier\"  AND v2.\"path\" = 'Ebrim v2.5:ExtrinsicObject:home' AND v2.\"value\" ='http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi'  AND v2.\"form\"=\"identifier\"  AND v3.\"path\" = 'Ebrim v2.5:ExtrinsicObject:minorVersion' AND v3.\"value\" ='0'  AND v3.\"form\"=\"identifier\"  AND v4.\"path\" = 'Ebrim v2.5:ExtrinsicObject:majorVersion' AND v4.\"value\" ='1'  AND v4.\"form\"=\"identifier\" ");
        
        
        /**
         * Test 2: a multiple Filter PropertyIsEqualTo
         */
        XMLrequest ="<ogc:Filter  xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
                           "    <ogc:Or>" +'\n' +
			"	<ogc:And>" +'\n' +
			 "		<ogc:PropertyIsEqualTo> " +'\n' +
			"			<ogc:PropertyName>rim:ExtrinsicObject/@mimeType</ogc:PropertyName>" +'\n' +
			"		 	<ogc:Literal>application/octet-stream</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@home</ogc:PropertyName>" +'\n' +
		     	"			<ogc:Literal>http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@minorVersion</ogc:PropertyName>" +'\n' +
			"			<ogc:Literal>0</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"		<ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$e1/@majorVersion</ogc:PropertyName>" +'\n' +
			"			<ogc:Literal>1</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
			"	</ogc:And>" +'\n' +
                         "      <ogc:PropertyIsEqualTo>" +'\n' +
			"			<ogc:PropertyName>$a1/@status</ogc:PropertyName>" +'\n' +
			"			<ogc:Literal>Approved</ogc:Literal>" +'\n' +
			"		</ogc:PropertyIsEqualTo>" +'\n' +
                         "      </ogc:Or>" +'\n' +
			" </ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Ebrim v2.5:Association:status' AND v1.\"value\" ='Approved'  AND v1.\"form\"=\"identifier\" ) UNION (SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 , \"TextValues\" v2 , \"TextValues\" v3 , \"TextValues\" v4 WHERE v1.\"path\" = 'Ebrim v2.5:ExtrinsicObject:mimeType' AND v1.\"value\" ='application/octet-stream'  AND v1.\"form\"=\"identifier\"  AND v2.\"path\" = 'Ebrim v2.5:ExtrinsicObject:home' AND v2.\"value\" ='http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi'  AND v2.\"form\"=\"identifier\"  AND v3.\"path\" = 'Ebrim v2.5:ExtrinsicObject:minorVersion' AND v3.\"value\" ='0'  AND v3.\"form\"=\"identifier\"  AND v4.\"path\" = 'Ebrim v2.5:ExtrinsicObject:majorVersion' AND v4.\"value\" ='1'  AND v4.\"form\"=\"identifier\" ) ");
        
        /**
         * Test 3: a not Filter on PropertyIsEqualTo
         */
        XMLrequest ="<ogc:Filter  xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
                    "    <ogc:Not>" +'\n' +
		    "		<ogc:PropertyIsEqualTo> " +'\n' +
		    "			<ogc:PropertyName>rim:ExtrinsicObject/@mimeType</ogc:PropertyName>" +'\n' +
		    "		 	<ogc:Literal>application/octet-stream</ogc:Literal>" +'\n' +
		    "		</ogc:PropertyIsEqualTo>" +'\n' +
		    "    </ogc:Not>" +'\n' +
	            "</ogc:Filter>";

        reader = new StringReader(XMLrequest);

        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Ebrim v2.5:ExtrinsicObject:mimeType' AND v1.\"value\" !='application/octet-stream'  AND v1.\"form\"=\"identifier\" ");


        /**
         * Test 3: a not Filter on PropertyIsGreaterThan
         */
        XMLrequest ="<ogc:Filter  xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
                    "    <ogc:Not>" +'\n' +
		    "		<ogc:PropertyIsGreaterThan> " +'\n' +
		    "			<ogc:PropertyName>rim3:date</ogc:PropertyName>" +'\n' +
		    "		 	<ogc:Literal>2007-06-02T00:00:00+00:00</ogc:Literal>" +'\n' +
		    "		</ogc:PropertyIsGreaterThan>" +'\n' +
		    "    </ogc:Not>" +'\n' +
	            "</ogc:Filter>";

        reader = new StringReader(XMLrequest);

        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter  = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Ebrim v3.0:date' AND v1.\"value\" <='2007-06-02 00:00:00'  AND v1.\"form\"=\"identifier\" ");

        /**
         * Test 3: a not Filter on PropertyIsLessThanOrEqualTo
         */
        XMLrequest ="<ogc:Filter  xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5\"> " +'\n' +
                    "    <ogc:Not>" +'\n' +
		    "		<ogc:PropertyIsLessThanOrEqualTo> " +'\n' +
		    "			<ogc:PropertyName>urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0:date</ogc:PropertyName>" +'\n' +
		    "		 	<ogc:Literal>2007-06-02T00:00:00+00:00</ogc:Literal>" +'\n' +
		    "		</ogc:PropertyIsLessThanOrEqualTo>" +'\n' +
		    "    </ogc:Not>" +'\n' +
	            "</ogc:Filter>";

        reader = new StringReader(XMLrequest);

        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter  = (FilterType) element.getValue();

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().isEmpty());
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SQLQuery) filterParser.getQuery(new QueryConstraintType(filter, "1.1.0"), variables, prefixs);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "SELECT distinct \"identifier\", \"catalog\" FROM \"Forms\"  , \"TextValues\" v1 WHERE v1.\"path\" = 'Ebrim v3.0:date' AND v1.\"value\" >'2007-06-02 00:00:00'  AND v1.\"form\"=\"identifier\" ");

        pool.release(filterUnmarshaller);
    }

}
